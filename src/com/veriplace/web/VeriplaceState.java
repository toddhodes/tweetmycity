/* Copyright 2008-2009 WaveMarket, Inc.
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
import com.veriplace.client.Location;
import com.veriplace.client.User;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.client.util.UserDiscoveryParameters;
import com.veriplace.client.util.SetLocationParameters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents the current state of a request in progress, which includes:
 * <ul>
 * <li>The current {@link javax.servlet.http.HttpServletRequest} and {@link javax.servlet.http.HttpServletResponse}
 * <li>Any requirements that have been defined for this request: Veriplace user discovery,
 * the current user location, and/or permission to change the location (although this last
 * is not yet supported)
 * <li>Named attributes which may be passed on to a subsequent redirect/callback cycle
 * <li>The next action to take (redirect, display a local page, etc.) if fulfilling the
 *     requirements requires diverting the request
 * </ul>
 * <p>
 * An application that would like to perform some operation, but may need to divert the
 * flow to satisfy a user/location discovery requirement, should first obtain a
 * VeriplaceState from its {@link com.veriplace.web.VeriplaceContext}, and call
 * {@link #setRequiresLocation(boolean)} or {@link #setRequiresUser(boolean)} to specify what
 * it is looking for.  Then, call {@link #isComplete()} to see if the operation can proceed,
 * and if it returns false, pass control to the {@link com.veriplace.web.StatusHandler};
 * or call {@link #completeAll()} to do both of those steps at once.
 * <p>
 * Alternatively, you can use the classes in {@link com.veriplace.web.servlet} or
 * {@link com.veriplace.web.spring} to hide the details of this process even further,
 * so that your servlet handler will only be called after the requirements are completed.
 */
public class VeriplaceState {

   private static final Log logger = LogFactory.getLog(VeriplaceState.class);
   
   private VeriplaceContext context;
   private Client client;
   private HttpServletRequest request;
   private HttpServletResponse response;
   private StatusHandler statusHandler;
   private UserRequirement userRequirement;
   private LocationRequirement locationRequirement;
   private SetLocationRequirement setLocationRequirement;
   private String locationMode;
   private HashSet<String> persistentAttributeNames;
   private boolean isCallback;
   private boolean isCallbackError;
   private Token accessToken;
   private RequestStatus requestStatus;
   private String redirectUrl;
   
   private static final String DUMMY_PARAM = "veriplace_temp";
   private static final String STATE_ATTRIBUTE = "veriplace_state";
   private static final String PERSIST_NAMES_ATTRIBUTE = "veriplace_keep";
   private static final String UTF8 = "utf-8";

   VeriplaceState(VeriplaceContext context, Client client, HttpServletRequest request, HttpServletResponse response) {
      this.context = context;
      this.client = client;
      this.request = request;
      this.response = response;
      this.statusHandler = context.getStatusHandler();
      requestStatus = RequestStatus.Starting;
      persistentAttributeNames = new HashSet<String>();

      accessToken = null;
      isCallback = isCallbackError = false;
      String accessTokenParam = request.getParameter(Parameter.Token.getKey());
      if (accessTokenParam == null || accessTokenParam.equals("")) {
         logger.debug("Not a callback");
         captureParameters();
      }
      else {
         isCallback = true;
         accessToken = client.getAccessToken(request);
         // If the token parameter was present, but the client returns a null token, it means
         // that we did return from a callback but that the current operation has been cancelled.
         if (accessToken == null) {
            logger.debug("Got a callback with a null token");
            isCallbackError = true;
         }
         else {
            passCapturedParameters();
         }
      }
   }

   public VeriplaceContext getContext() {
      return context;
   }
   
   public Client getClient() {
      return client;
   }
   
   public void setClient(Client client) {
      this.client = client;
   }
   
   public StatusHandler getStatusHandler() {
      return statusHandler;
   }
   
   public void setStatusHandler(StatusHandler statusHandler) {
      this.statusHandler = statusHandler;
   }
   
   public static VeriplaceState getFromRequest(ServletRequest request) {
      return (VeriplaceState) request.getAttribute(STATE_ATTRIBUTE);
   }
   
   public void attachToRequest(ServletRequest request) {
      request.setAttribute(STATE_ATTRIBUTE, this);
   }
   
   /**
    * See {@link #setRequiresUser(boolean)}.
    */
   public boolean getRequiresUser() {
      return (userRequirement != null);
   }
   
   /**
    * Specifies whether the application will require a valid user identity in order to process this request.
    */
   public void setRequiresUser(boolean requiresUser) {
      if (requiresUser) {
         if (userRequirement == null) {
            userRequirement = new UserRequirement();
            requestStatus = RequestStatus.Starting;
         }
      }
      else {
         if ((locationRequirement == null) && (setLocationRequirement == null)) {
            userRequirement = null;
         }
         else {
            throw new IllegalStateException("Can't have location discovery without user discovery");
         }
      }
   }

   /**
    * See {@link #setUserInteractionAllowed(boolean)}.
    */
   public boolean isUserInteractionAllowed() {
      return (userRequirement == null) ? false : userRequirement.isInteractionAllowed();
   }
   
   /**
    * Specifies whether Veriplace can redirect to a login page or otherwise interact with the user when
    * we need to determine the current user.  This is true by default; if false, then any request for
    * the current user and/or location will fail unless the user is already logged in.
    */
   public void setUserInteractionAllowed(boolean userInteractionAllowed) {
      if (userRequirement == null) {
         throw new IllegalStateException("Can't set userInteractionAllowed, user discovery is not enabled");
      }
      userRequirement.setInteractionAllowed(userInteractionAllowed);
   }
   
   /**
    * See {@link #setRequiresLocation(boolean)}.
    */
   public boolean getRequiresLocation() {
      return (locationRequirement != null);
   }
   
   /**
    * Specifies whether the application will require a valid user location in order to process this request.
    */
   public void setRequiresLocation(boolean requiresLocation) {
      if (requiresLocation) {
         if (locationRequirement == null) {
            setRequiresUser(true);
            locationRequirement = new LocationRequirement(userRequirement);
            requestStatus = RequestStatus.Starting;
         }
      }
      else {
         locationRequirement = null;
      }
   }
   
   /**
    * See {@link #setLocationMode(String)}.
    */
   public String getLocationMode() {
      return locationMode;
   }
   
   /**
    * Specifies the method or degree of accuracy for obtaining location. Allowable values are defined
    * in {@link com.veriplace.client.LocationMode}. If the value is null, Veriplace will use the
    * default mode for the current application.
    */
   public void setLocationMode(String locationMode) {
      this.locationMode = locationMode;
   }
   
   /**
    * See {@link #setRequiresSetLocationPermission(boolean)}.
    */
   public boolean getRequiresSetLocationPermission() {
      return (setLocationRequirement != null);
   }
   
   /**
    * Specifies whether the application will require permission to update the user's location
    * in order to process this request.  Note that this is not yet supported by the Veriplace
    * platform; see {@link com.veriplace.client.SetLocationAPI}.
    */
   public void setRequiresSetLocationPermission(boolean requiresSetLocationPermission) {
      if (requiresSetLocationPermission) {
         if (setLocationRequirement == null) {
            setRequiresUser(true);
            setLocationRequirement = new SetLocationRequirement(userRequirement);
            requestStatus = RequestStatus.Starting;
         }
      }
      else {
         setLocationRequirement = null;
      }
   }
   
   /**
    * Returns true if any user/location requirements have been specified.
    */
   public boolean hasRequirements() {
      return getRequiresUser() || getRequiresLocation() || getRequiresSetLocationPermission();
   }
   
   /**
    * Specifies that the value of the given request attribute (or request parameter, if there is no
    * such attribute) should be preserved if there is a redirect/callback.  You should not normally
    * have to do this, since Veriplace automatically preserves any parameters that were present in
    * the request that originally triggered the callback.
    */
   public void addPersistentAttributeName(String name) {
      persistentAttributeNames.add(name);
   }
   
   /**
    * Returns true if the current request is a callback from Veriplace.
    */
   public boolean isCallback() {
      return isCallback;
   }
   
   /**
    * Returns true if the current request is a callback from Veriplace, and if the accompanying access token
    * was not valid.
    */
   public boolean isCallbackError() {
      return isCallbackError;
   }
   
   public Token getAccessToken() {
      return accessToken;
   }
   
   public void setAccessToken(Token token) {
      logger.debug("new accessToken = " + ((token == null) ? "null" : token.getToken()));
      accessToken = token;
   }
   
   /**
    * Attempts to complete all the current requirements using the current HTTP request.
    * If successful, returns true; otherwise returns false, and updates the values of
    * {@link #getRequestStatus()} and {@link #getRedirectUrl()} to indicate what needs
    * to happen next.  You should not have to use this method directly unless you need
    * some unusually fine-grained control over redirects or error conditions.
    */
   public boolean isComplete() {
      while (requestStatus == RequestStatus.Starting) {
         boolean ok = true;
         for (Requirement r : getRequirements()) {
            if (! r.complete(this)) {
               ok = false;
               break;
            }
         }
         if (ok) {
            requestStatus = RequestStatus.Completed;
            return true;
         }
      }
      return false;
   }
   
   /**
    * Attempts to complete all the current requirements using the current HTTP request,
    * and if a redirect is required or an error occurs, sends an appropriate response
    * to the request via the current {@link com.veriplace.web.StatusHandler}.
    * @return  true if the requirements are complete (in which case you can call
    * {@link #getLocation()}, etc., to get the results); false if the request was
    * routed to a redirect, error page, or wait page, in which case you should stop
    * processing the request.
    */
   public boolean completeAll()
         throws IOException, ServletException {
      while (true) {
         boolean done = isComplete();
         if (statusHandler != null) {
            boolean redirected = statusHandler.handleRequestStatus(requestStatus, redirectUrl, this, request, response);
            return !redirected;
         }
         if (done) {
            return true;
         }
      }
   }
   
   /**
    * Returns the current {@link javax.servlet.http.HttpServletRequest}.
    */
   public HttpServletRequest getRequest() {
      return request;
   }
   
   /**
    * Returns the current {@link com.veriplace.client.User}, or null if we have not obtained
    * the user identity for this request.
    */
   public User getUser() {
      return UserRequirement.getUser(request);
   }
   
   /**
    * Returns the current {@link com.veriplace.client.Location}, or null if we have not obtained
    * a user location for this request.
    */
   public Location getLocation() {
      return LocationRequirement.getLocation(request);
   }
   
   /**
    * Shortcut for getting the value of a named request attribute, or of a request parameter if
    * there is no such attribute.
    */
   public Object getAttribute(String name) {
      Object value = request.getAttribute(name);
      if (value == null) {
         value = request.getParameter(name);
      }
      return value;
   }
   
   /**
    * Shortcut for setting a named attribute on the current request.
    */
   public void setAttribute(String name, Object value) {
      request.setAttribute(name, value);
   }
   
   /**
    * Returns a {@link com.veriplace.web.RequestStatus} representing the progress of the current
    * request. This is always {@link com.veriplace.web.RequestStatus#Starting} to begin with, and
    * {@link com.veriplace.web.RequestStatus#Completed} if all the requirements have been fulfilled.
    * If it is anything else, then the requirements have not yet been met and we need to perform
    * a redirect or display a special page; see {@link com.veriplace.web.StatusHandler}.
    */
   public RequestStatus getRequestStatus() {
      return requestStatus;
   }
   
   void setRequestStatus(RequestStatus requestStatus) {
      logger.debug("Request status is " + requestStatus);
      this.requestStatus = requestStatus;
   }
   
   void setRequestStatus(RequestStatus requestStatus, String url) {
      logger.debug("Request status is " + requestStatus + " (URL: " + url + ")");
      if ((requestStatus == RequestStatus.RequiresRedirect) && (url == null)) {
         throw new IllegalArgumentException();
      }
      this.requestStatus = requestStatus;
      this.redirectUrl = url;
   }
   
   /**
    * Returns the URL of the external Veriplace page we are redirecting to, if any; or, if we are
    * going to display a "please wait" page with an automatic refresh, the callback URL to our
    * application.
    */
   public String getRedirectUrl() {
      return redirectUrl;
   }
   
   /**
    * Attempts to discover the current user based on one of the methods available in
    * {@link com.veriplace.client.util.UserDiscoveryParameters}.  Unlike the regular user discovery process, this
    * cannot trigger a redirect or callback; it will either succeed or fail immediately.
    * @param params  the search criteria
    * @param forceNewSearch  true if the search should be performed even if we already know the
    * current user
    * @return  true if the request succeeded (call {@link #getUser()} to get the result); false if
    * it failed.
    */
   public boolean findUser(UserDiscoveryParameters params, boolean forceNewSearch)
         throws ServletException, IOException {
      if (params == null) {
         throw new NullPointerException();
      }
      setRequiresUser(true);
      return userRequirement.findUser(this, params, forceNewSearch);
   }
   
   /**
    * For future use, not currently supported.
    * Attempts to set a new location for the current user, assuming we already have a valid user identity
    * and permission to set the location.  This wrapper method passes the access token and user object from
    * the LocationState to {@link com.veriplace.client.SetLocationAPI}.
    * @return  true if the request was completed (call {@link #getLocation()} to get the updated location;
    * it will be null if the request was refused), or false if the request was redirected and will return
    * via callback. 
    */
   public boolean setUserLocation(SetLocationParameters params)
         throws ServletException, IOException {
      if (params == null) {
         throw new NullPointerException();
      }
      if (setLocationRequirement == null) {
         throw new IllegalStateException("Attempted to set user location without asking permission");
      }
      setLocationRequirement.startSetLocationRequest(this, params);
      return this.completeAll();
   }

   /**
    * Constructs a URL for returning to the current request from an external page, and
    * passing along any necessary attributes to recreate the current state.
    */
   String getCallbackUrl() {
      StringBuilder buf = new StringBuilder();
      buf.append(client.prepareCallback(request));
      buf.append(request.getContextPath());
      buf.append(request.getServletPath());
      String path = request.getPathInfo();
      if ((path != null) && !path.equals("")) {
         if (! path.startsWith("/")) {
            buf.append('/');
         }
         buf.append(path);
      }
      buf.append('?');

      // The following extra parameter is a workaround for some mobile browsers that can
      // mangle the first query string parameter during redirects or refreshes.
      addParameter(buf, DUMMY_PARAM, path);
      buf.append('&');
      
      addParameters(buf, getCallbackParameters());
      return buf.toString();
   }

   /**
    * Creates HTML hidden form fields for the current user identity.  Using JSP expression
    * syntax, if the attribute name "veriplace" refers to the LocationState, you can simply
    * write <tt>${veriplace.userFields}</tt> to include these fields in a form. 
    */         
   public String getUserFields() {
      if (userRequirement == null) {
         return null;
      }
      StringBuilder buf = new StringBuilder();
      for (String name : userRequirement.getAttributeNames()) {
         addHiddenField(buf, name, getAttribute(name));
      }
      return buf.toString();
   }
   
   /**
    * Creates HTML hidden form fields for all currently defined persistent attributes.
    * Using JSP expression syntax, if the attribute name "veriplace" refers to the LocationState,
    * you can simply write <tt>${veriplace.allFields}</tt> to include these fields in a form. 
    */         
   public String getAllFields() {
      StringBuilder buf = new StringBuilder();
      for (Map.Entry<String, Object> kv : getCallbackParameters().entrySet()) {
         addHiddenField(buf, kv.getKey(), kv.getValue());
      }
      return buf.toString();
   }
   
   protected Requirement[] getRequirements() {
      LinkedList<Requirement> rr = new LinkedList<Requirement>();
      if (userRequirement != null) {
         rr.add(userRequirement);
      }
      if (locationRequirement != null) {
         rr.add(locationRequirement);
      }
      if (setLocationRequirement != null) {
         rr.add(setLocationRequirement);
      }
      Requirement[] a = new Requirement[rr.size()];
      rr.toArray(a);
      return a;
   }
   
   protected Map<String, Object> getCallbackParameters() {
      for (Requirement r : getRequirements()) {
         for (String name : r.getAttributeNames()) {
            persistentAttributeNames.add(name);
         }
      }
      HashMap<String, Object> map = new HashMap<String, Object>();
      for (String name : persistentAttributeNames) {
         Object value = getAttribute(name);
         logger.debug("storing parameter for callback: " + name + "=" + value);
         if (value != null) {
            map.put(name, value);
         }
      }
      return map;
   }

   
   protected void addParameter(StringBuilder buf, String name, String value) {
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
   
   protected void addParameters(StringBuilder buf, Map<String, Object> params) {
      boolean first = true;
      for (String name : params.keySet()) {
         if (! first) {
            buf.append('&');
         }
         Object value = params.get(name);
         String vs = (value == null) ? "" : value.toString();
         addParameter(buf, name, vs);
         first = false;
      }
   }

   protected void addHiddenField(StringBuilder buf, String name, Object value) {
      String v = (value == null) ? "" : StringEscapeUtils.escapeHtml(value.toString());
      buf.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + v + "\"/>");
   }
   
   /**
    * Marks all of the current request parameters so they will be persisted across any
    * number of redirect/callback cycles.
    */
   protected void captureParameters() {
      StringBuilder buf = new StringBuilder();
      Enumeration names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String name = (String) names.nextElement();
         addPersistentAttributeName(name);
         if (buf.length() > 0) {
            buf.append(',');
         }
         buf.append(name);
      }
      if (buf.length() > 0) {
         setAttribute(PERSIST_NAMES_ATTRIBUTE, buf.toString());
				 logger.debug("persisting attribute: " + buf.toString());
         addPersistentAttributeName(PERSIST_NAMES_ATTRIBUTE);
      }
   }
   
   protected void passCapturedParameters() {
      String names = request.getParameter(PERSIST_NAMES_ATTRIBUTE);
      if ((names != null) && !names.equals("")) {
         for (String name : names.split(",")) {
            addPersistentAttributeName(name);
         }
         addPersistentAttributeName(PERSIST_NAMES_ATTRIBUTE);
      }
   }
   
   /**
    * Shortcut for getting a named string parameter from the current HTTP request.
    */
   protected String getRequestParamString(String name) {
      return request.getParameter(name);
   }
   
   /**
    * Helper method for getting a named numeric parameter from the current HTTP request.
    */
   protected Long getRequestParamLong(String name) {
      String s = request.getParameter(name);
      if (s != null) {
         try {
            long n = Long.parseLong(s);
            return new Long(n);
         }
         catch (NumberFormatException e) {
         }
      }
      return null;      
   }
}
