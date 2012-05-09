/* Copyright 2008-2010 WaveMarket, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.veriplace.web;

import com.veriplace.client.Client;
import com.veriplace.client.ConfigurationException;
import com.veriplace.client.GetLocationNotPermittedException;
import com.veriplace.client.RequestDeniedException;
import com.veriplace.client.SetLocationParameters;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.UserDiscoveryNotPermittedException;
import com.veriplace.client.UserDiscoveryParameters;
import com.veriplace.client.factory.CallbackFactory;
import com.veriplace.client.factory.ClientFactory;
import com.veriplace.client.factory.DefaultClientFactory;
import com.veriplace.client.store.MemoryUserTokenStore;
import com.veriplace.client.store.UserTokenStore;
import com.veriplace.client.util.GetLocationRequestManager;
import com.veriplace.client.util.SetLocationRequestManager;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.web.views.RespondedWithStatusViewException;
import com.veriplace.web.views.StatusViewRenderer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Veriplace client for web applications.  The web client hides many of the details of
 * Veriplace's OAuth-based authorization process, providing a persistent state across
 * transactions that may involve redirects and callbacks.
 * <p>
 * An instance of {@link Veriplace} manages the following resources:
 * <ul>
 * <li> A {@link Client} which handles communication with the Veriplace server.  This is the
 * lower-level client API, which you can still access if desired. </li>
 * <li> Asynchronous request managers which allow time-consuming requests to run on
 * background threads. </li>
 * <li> Token caches to preserve user access tokens transparently across callbacks. </li>
 * <li> A {@link Redirector} allowing Veriplace to perform browser redirects transparently. </li>
 * <li> An optional {@link com.veriplace.web.views.StatusViewRenderer} allowing Veriplace to
 * display error or waiting pages automatically. </li>
 * </ul>
 * <p>
 * All of this class's methods operate on a {@link VeriplaceState} object, which holds all
 * the information that is associated with the current HTTP request.  When a request completes
 * successfully, it updates the VeriplaceState's properties.
 * <p>
 * If a request cannot be completed, it throws a subclass of {@link com.veriplace.client.VeriplaceException}.
 * The web client defines several new exception classes for non-error conditions that can interrupt
 * the handling of a request:  {@link RespondedException}, which usually means that the user
 * has been redirected to the Veriplace site, and {@link WaitingException}, which means that
 * the application is waiting for an asynchronous request and may wish to display an
 * intermediate status page.
 * <p>
 * The standard Veriplace frameworks for servlets
 * ({@link com.veriplace.web.servlet.AbstractVeriplaceServlet}) and Spring servlets
 * ({@link com.veriplace.web.spring.VeriplaceInterceptor}), and the
 * <a href="../../../../../../tags/index.html" target="_top">JSP tag library</a>, take
 * care of creating and configuring the Veriplace instance and calling the appropriate
 * methods to acquire user or location data; applications using those frameworks will usually
 * just interact with the {@link com.veriplace.web.VeriplaceState} class to get the data.
 */
public class Veriplace {

   private static final Log logger = LogFactory.getLog(Veriplace.class);
   
   private static final String DUMMY_PARAM = "veriplace_temp";
   private static final String UTF8 = "utf-8";

   private Client client;
   private ClientFactory clientFactory;
   private CallbackFactory callbackFactory;
   private Redirector redirector;
   private StatusViewRenderer statusViewRenderer;
   private GetLocationRequestManager getLocationRequestManager;
   private SetLocationRequestManager setLocationRequestManager;
   private UserTokenStore locationTokenStore;
   private UserTokenStore setLocationTokenStore;

   protected Veriplace() {
      locationTokenStore = new MemoryUserTokenStore();
      setLocationTokenStore = new MemoryUserTokenStore();
      getLocationRequestManager = new GetLocationRequestManager();
      setLocationRequestManager = new SetLocationRequestManager();
      redirector = new DefaultRedirector();
   }
   
   /**
    * Creates a Veriplace instance using an existing {@link com.veriplace.client.Client}.
    */
   public Veriplace(Client client) {
      this();
      setClient(client);
   }

   /**
    * Creates a Veriplace instance using a specific {@link ClientFactory}.
    * @throws ConfigurationException  if a configuration property is invalid
    */
   public Veriplace(ClientFactory clientFactory)
      throws ConfigurationException {
      this(clientFactory.getClient());
      this.clientFactory = clientFactory;
   }

   /**
    * Creates a Veriplace instance and initializes its {@link com.veriplace.client.Client}
    * using a Properties object, with the keys defined by {@link com.veriplace.client.factory.DefaultClientFactory}.
    * @throws ConfigurationException  if a configuration property is invalid
    */
   public Veriplace(Properties properties)
      throws ConfigurationException {
      this(new DefaultClientFactory(properties));
   }

   /**
    * Creates a Veriplace instance and initializes its {@link com.veriplace.client.Client}
    * using properties file, with the keys defined by {@link com.veriplace.client.factory.ClientFactory}.
    * @throws ConfigurationException  if a configuration property is invalid
    */
   public Veriplace(String propertiesFileName)
      throws IOException, ConfigurationException {
      this(loadProperties(propertiesFileName));
   }

   private static Properties loadProperties(String propertiesFileName)
      throws IOException {

      Properties properties = new Properties();
      FileInputStream fis = new FileInputStream(propertiesFileName);
      properties.load(fis);
      return properties;
   }

   /**
    * Creates a Veriplace instance with the same properties as another instance, but using
    * a different Veriplace client.  You may wish to do this if your application needs to
    * change its configuration on the fly for certain requests, without interfering with
    * requests that are being handled on other threads. 
    */
   public Veriplace(Veriplace fromInstance, Client newClient) {
      locationTokenStore = fromInstance.locationTokenStore;
      setLocationTokenStore = fromInstance.setLocationTokenStore;
      getLocationRequestManager = fromInstance.getLocationRequestManager;
      setLocationRequestManager = fromInstance.setLocationRequestManager;
      redirector = fromInstance.redirector;
      statusViewRenderer = fromInstance.statusViewRenderer;
      setClient(newClient);
   }
   
   private void setClient(Client client) {
      if (client == null) {
         throw new IllegalArgumentException();
      }
      this.client = client;
      this.callbackFactory = client.getCallbackFactory();
   }
   
   /**
    * Returns the {@link com.veriplace.client.Client} that handles lower-level
    * communications with the Veriplace server.  This object provides several capabilities
    * that don't have corresponding methods in the web client API, simply because they
    * don't require any transaction state management and are not needed by most
    * applications, such as the methods in {@link com.veriplace.client.PermissionAPI}.
    */
   public Client getClient() {
      return client;
   }

   /**
    * Returns the currently configured {@link ClientFactory}, if any.
    */
   public ClientFactory getClientFactory() {
      return clientFactory;
   }

   public Properties getProperties() {
      if (clientFactory == null ||
          !(clientFactory instanceof DefaultClientFactory)) {
         return new Properties();
      }

      return ((DefaultClientFactory)clientFactory).getProperties();
   }
   
   /**
    * See {@link #setRedirector(Redirector)}.
    */
   public Redirector getRedirector() {
      return redirector;
   }
   
   /**
    * Designates a {@link com.veriplace.web.Redirector} object which will take care of sending
    * redirect responses.  If you don't specify otherwise, it uses {@link com.veriplace.web.DefaultRedirector}.
    */
   public void setRedirector(Redirector redirector) {
      this.redirector = redirector;
   }
   
   /**
    * See {@link #setStatusViewRenderer(StatusViewRenderer)}.
    */
   public StatusViewRenderer getStatusViewRenderer() {
      return statusViewRenderer;
   }
   
   /**
    * Designates a {@link com.veriplace.web.views.StatusViewRenderer} object which will be used to
    * display special status pages (errors, or a "please wait" page) if appropriate.  This should normally
    * be an instance of {@link com.veriplace.web.servlet.ServletStatusViewRenderer}, except for Spring
    * applications which should use {@link com.veriplace.web.spring.SpringStatusViewRenderer}.  If you do
    * not have a ViewRenderer, you can still make a request but you will have to catch errors (or
    * {@link WaitingException}) and handle them appropriately. 
    */
   public void setStatusViewRenderer(StatusViewRenderer statusViewRenderer) {
      this.statusViewRenderer = statusViewRenderer;
   }
   
   /**
    * See {@link #setGetLocationRequestManager(GetLocationRequestManager)}.
    */
   public GetLocationRequestManager getGetLocationRequestManager() {
      return getLocationRequestManager;
   }
   
   /**
    * Designates a {@link com.veriplace.client.util.GetLocationRequestManager} object to provide background
    * processing of location requests. By default, it will use an implementation based on
    * {@link com.veriplace.client.util.MemoryRequestStore}.
    */
   public void setGetLocationRequestManager(GetLocationRequestManager lrm) {
      getLocationRequestManager = lrm;
   }
   
   /**
    * See {@link #setSetLocationRequestManager(SetLocationRequestManager)}.
    */
   public SetLocationRequestManager getSetLocationRequestManager() {
      return setLocationRequestManager;
   }
   
   /**
    * Designates a {@link com.veriplace.client.util.SetLocationRequestManager} object to provide background
    * processing of set-location requests. By default, it will use an implementation based on
    * {@link com.veriplace.client.util.MemoryRequestStore}.
    */
   public void setSetLocationRequestManager(SetLocationRequestManager lrm) {
      setLocationRequestManager = lrm;
   }

   /**
    * See {@link #setGetLocationTokenStore(UserTokenStore)}.
    */
   public UserTokenStore getGetLocationTokenStore() {
      return locationTokenStore;
   }
   
   /**
    * See {@link #setSetLocationTokenStore(UserTokenStore)}.
    */
   public UserTokenStore getSetLocationTokenStore() {
      return setLocationTokenStore;
   }
   
   /**
    * Designates a {@link com.veriplace.client.store.UserTokenStore} object to provide cacheing of
    * Veriplace access tokens. By default, it will use a {@link com.veriplace.client.store.MemoryUserTokenStore}.
    */
   public void setGetLocationTokenStore(UserTokenStore store) {
      locationTokenStore = store;
   }

   /**
    * Designates a {@link com.veriplace.client.store.UserTokenStore} object to provide cacheing of
    * Veriplace access tokens for set-location requests. By default, it will use a
    * {@link com.veriplace.client.store.MemoryUserTokenStore}.
    */
   public void setSetLocationTokenStore(UserTokenStore store) {
      setLocationTokenStore = store;
   }
   
   /**
    * Creates a new {@link com.veriplace.web.VeriplaceState} object for the current HTTP
    * request.  This object receives the results of user and location queries, and maintains
    * other information (access tokens and callback state) that allow operations to be carried
    * out across multiple requests and callbacks.
    */
   public VeriplaceState open(HttpServletRequest request, HttpServletResponse response) {
      VeriplaceState state;
      state = VeriplaceState.getFromRequest(request);
      if (state != null) {
         logger.debug("Found veriplace state in request");
         return state;
      }

      logger.debug("Did not find veriplace state in request; creating new state");
      
      state = new VeriplaceState(this, request, response);
      state.attachToRequest(request);
      
      state.addCallbackParameters(callbackFactory.captureParameters(state.getRequest()));

      state.setRequestId(state.getRequestParamLong(VeriplaceState.REQUEST_ID_CALLBACK_PARAM));
      
      if (statusViewRenderer != null) {
         state.setAsynchronousRequestAllowed(statusViewRenderer.canRenderWaitingView());
      }
      
      String accessTokenParam = request.getParameter(Parameter.Token.getKey());
      if (accessTokenParam == null || accessTokenParam.equals("")) {
         logger.debug("Not a callback");
      }
      else {
         state.setCallback(true);
         try {
            Token accessToken = client.getAccessToken(request);
            state.setAccessToken(accessToken);
         }
         catch (UnexpectedException e) {
            // If the token parameter was present, but the client returns a null token, it means
            // that we did return from a callback but that the current operation has been cancelled.
            logger.debug("Got a callback with a null token");
            state.setLastErrorException(e);
         }
      }
      
      state.attachToRequest(request);
      return state;
   }
   
   /**
    * Deprecated.  Use {@link VeriplaceState#requireUser()}.
    * @deprecated
    */
   public void requireUser(VeriplaceState state)
         throws RespondedException, UserDiscoveryException, UnexpectedException, ServletException {
      requireUserInternal(state);
   }
   
   // package-private
   void requireUserInternal(VeriplaceState state)
      throws RespondedException,
             UserDiscoveryException,
             UnexpectedException,
             ServletException {
      try {
         completeRequirements(state, false, new Requirement[] { new UserDiscoveryRequirement(this, false) });
      }
      catch (WaitingException e) {
         // There's no opportunity for a "please wait" page in this transaction
         throw new IllegalStateException("Unexpected WaitingException in requireUser");
      }
      catch (UserDiscoveryException e) {
         throw e;
      }
      catch (RequestDeniedException e) {
         // Shouldn't get this unless it's a UserDiscoveryException
         throw new UnexpectedException(e);
      }
   }

   /**
    * Deprecated.  Use {@link VeriplaceState#requireUser(UserDiscoveryParameters)}.
    * @deprecated
    */
   public void requireUser(VeriplaceState state, UserDiscoveryParameters parameters)
         throws RespondedException, UserDiscoveryException, UnexpectedException, ServletException {
      requireUserInternal(state, parameters);
   }
   
   // package-private
   void requireUserInternal(VeriplaceState state, UserDiscoveryParameters parameters)
      throws RespondedException,
             UserDiscoveryException,
             UnexpectedException,
             ServletException {
      try {
         completeRequirementsImmediate(state, new Requirement[] { new UserQueryRequirement(this, parameters) });
      }
      catch (UserDiscoveryException e) {
         throw e;
      }
      catch (RequestDeniedException e) {
         // Shouldn't get this unless it's a UserDiscoveryException
         throw new UnexpectedException(e);
      }
   }

   /**
    * Deprecated.  Use {@link VeriplaceState#requireUserImmediate()}.
    * @deprecated
    */
   public void requireUserImmediate(VeriplaceState state)
      throws UserDiscoveryNotPermittedException, UnexpectedException, ServletException {
      requireUserImmediateInternal(state);
   }
   
   // package-private
   void requireUserImmediateInternal(VeriplaceState state)
      throws UserDiscoveryNotPermittedException,
          UnexpectedException,
             ServletException {
      try {
         completeRequirementsImmediate(state, new Requirement[] { new UserDiscoveryRequirement(this, true) });
      }
      catch (UserDiscoveryNotPermittedException e) {
         throw e;
      }
      catch (RequestDeniedException e) {
         // Shouldn't get this unless it's a UserDiscoveryNotPermittedException
         throw new UnexpectedException(e);
      }
   }

   /**
    * Deprecated.  Use {@link VeriplaceState#requireGetLocationPermission()}.
    * @deprecated
    */
   public void requireGetLocationPermission(VeriplaceState state)
         throws RespondedException, RequestDeniedException, UnexpectedException, ServletException {
      requireGetLocationPermissionInternal(state);
   }
   
   // package-private
   void requireGetLocationPermissionInternal(VeriplaceState state)
      throws RespondedException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      completeRequirementsNoWaiting(state, new Requirement[] {
         new UserDiscoveryRequirement(this, false),
         new GetLocationPermissionRequirement(this, false)
      });
   }

   /**
    * Deprecated.  Use {@link VeriplaceState#requireGetLocationPermissionImmediate()}.
    * @deprecated
    */
   public void requireGetLocationPermissionImmediate(VeriplaceState state)
         throws GetLocationNotPermittedException, UserDiscoveryException, UnexpectedException {
      requireGetLocationPermissionImmediateInternal(state);
   }
   
   // package-private
   void requireGetLocationPermissionImmediateInternal(VeriplaceState state)
      throws GetLocationNotPermittedException,
             UserDiscoveryException,
             UnexpectedException {
      if (state.getUser() == null) {
         throw new UserDiscoveryException();
      }
      if (! state.hasGetLocationPermission()) {
         Token token = client.getGetLocationAPI().getLocationAccessToken(state.getUser());
         state.setGetLocationPermissionToken(token);
      }
   }

   /**
    * Deprecated.  Use {@link VeriplaceState#requireLocation()}.
    * @deprecated
    */
   public void requireLocation(VeriplaceState state)
         throws RespondedException, WaitingException, RequestDeniedException,
                UnexpectedException, ServletException {
      requireLocationInternal(state);
   }
   
   // package-private
   void requireLocationInternal(VeriplaceState state)
      throws RespondedException,
             WaitingException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      completeRequirements(state, false, new Requirement[] {
         new UserDiscoveryRequirement(this, false),
         new GetLocationPermissionRequirement(this, true),
         new GetLocationRequirement(this)
      });
   }

   /**
    * Deprecated.  Use {@link VeriplaceState#requireLocation(long)}.
    * @deprecated
    */
   public void requireLocation(VeriplaceState state, long locationId)
         throws RespondedException, RequestDeniedException, UnexpectedException, ServletException {
      requireLocationInternal(state, locationId);
   }
   
   // package-private
   void requireLocationInternal(VeriplaceState state, long locationId)
      throws RespondedException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      completeRequirementsNoWaiting(state, new Requirement[] {
         new UserDiscoveryRequirement(this, false),
         new GetLocationPermissionRequirement(this, false),
         new GetLocationByIdRequirement(this, locationId)
      });
   }

   /**
    * Deprecated.  Use {@link VeriplaceState#requireSetLocationPermission()}.
    * @deprecated
    */
   public void requireSetLocationPermission(VeriplaceState state)
         throws RespondedException, RequestDeniedException, UnexpectedException, ServletException {
      requireSetLocationPermissionInternal(state);
   }
   
   // package-private
   void requireSetLocationPermissionInternal(VeriplaceState state)
      throws RespondedException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      completeRequirementsNoWaiting(state, new Requirement[] {
         new UserDiscoveryRequirement(this, false),
         new SetLocationPermissionRequirement(this, false)
      });
   }

   /**
    * Deprecated.  Use {@link VeriplaceState#setUserLocation(SetLocationParameters)}.
    * @deprecated
    */
   public void setUserLocation(VeriplaceState state, SetLocationParameters parameters)
         throws RespondedException, WaitingException, RequestDeniedException,
                UnexpectedException, ServletException {
      setUserLocationInternal(state, parameters);
   }
   
   // package-private
   void setUserLocationInternal(VeriplaceState state, SetLocationParameters parameters)
      throws RespondedException,
             WaitingException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      completeRequirements(state, false, new Requirement[] {
         new UserDiscoveryRequirement(this, false),
         new SetLocationPermissionRequirement(this, true),
         new SetLocationRequirement(this, parameters)
      });
   }
   
   /**
    * Constructs a URL for returning to the current request from an external page, and
    * passing along any necessary attributes to recreate the current state.  You will
    * normally not need to call this method yourself.
    */
   public String getCallbackUrl(VeriplaceState state) {
      StringBuilder buf = new StringBuilder();
      buf.append(callbackFactory.createCallbackUrl(state.getRequest(), true));
      buf.append('?');

      // The following extra parameter is a workaround for some mobile browsers that can
      // mangle the first query string parameter during redirects or refreshes.  The dummy
      // parameter value should be the same as the last path component.
      String dummyValue = buf.substring(buf.lastIndexOf("/") + 1, buf.length() - 1);
      addParameter(buf, DUMMY_PARAM, dummyValue);

      for (Map.Entry<String, String[]> entry: state.getCallbackParameters().entrySet()) {
         String name = entry.getKey();
         for (String value: entry.getValue()) {
            buf.append('&');
            addParameter(buf, name, value);
         }
      }
      return buf.toString();
   }
   
   private void completeRequirements(VeriplaceState state,
                                     boolean immediate,
                                     Requirement[] requirements)
      throws RespondedException,
             WaitingException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      try {
         boolean done = false;
         while (! done) {
            try {
               for (Requirement r : requirements) {
                  r.complete(state);
               }
               done = true;
            }
            catch (ShouldRestartException e) {
               continue;
            }
         }
      }
      catch (ShouldRedirectException e) {
         if (immediate) {
            throw new UnexpectedException(e);
         }
         try {
            redirector.sendRedirect(state.getRequest(), state.getResponse(), e.getRedirectToUrl());
         }
         catch (IOException x) {
            throw new UnexpectedException(x);
         }
         throw new RedirectedToVeriplaceException(e.getRedirectToUrl());
      }
      catch (WaitingException e) {
         if (! immediate) {
            tryStatusView(state, e, e.getCallbackUrl());
         }
         throw e;
      }
      catch (RequestDeniedException e) {
         state.setLastErrorException(e);
         if (! immediate) {
            tryStatusView(state, e, null);
         }
         throw e;
      }
      catch (UnexpectedException e) {
         state.setLastErrorException(e);
         if (! immediate) {
            tryStatusView(state, e, null);
         }
         throw e;
      }
   }

   protected void tryStatusView(VeriplaceState state, Exception e, String callbackUrl)
      throws RespondedWithStatusViewException,
             UnexpectedException,
             ServletException {
      if (statusViewRenderer != null) {
         boolean handled;
         if (e instanceof WaitingException) {
            handled = statusViewRenderer.renderWaitingView(state.getRequest(), state.getResponse(),
                                                           state, ((WaitingException) e).getCallbackUrl());
         }
         else {
            handled = statusViewRenderer.renderErrorView(state.getRequest(), state.getResponse(),
                                                         state, e);
         }
         if (handled) {
            throw new RespondedWithStatusViewException(e);
         }
      }
   }
   
   private void completeRequirementsNoWaiting(VeriplaceState state,
                                              Requirement[] requirements)
      throws RespondedException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      try {
         completeRequirements(state, false, requirements);
      }
      catch (WaitingException e) {
         throw new UnexpectedException(e);
      }
   }
   
   private void completeRequirementsImmediate(VeriplaceState state,
                                              Requirement[] requirements)
      throws RequestDeniedException,
             UnexpectedException,
             ServletException {
      try {
         completeRequirements(state, true, requirements);
      }
      catch (WaitingException e) {
         throw new UnexpectedException(e);
      }
      catch (RespondedException e) {
         throw new UnexpectedException(e);
      }
   }

   private void addParameter(StringBuilder buf, String name, String value) {
      buf.append(name);
      buf.append('=');
      if ((value != null) && !value.equals("")) {
         try {
            buf.append(URLEncoder.encode(value, UTF8));
         }
         catch (UnsupportedEncodingException e) {
            buf.append(value);
         }
      }
   }
}

/*
** Local Variables:
**   c-basic-offset: 3
**   tab-width: 3
**   indent-tabs-mode: nil
** End:
**
** ex: set softtabstop=3 tabstop=3 expandtab cindent shiftwidth=3
*/
