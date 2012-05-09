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
package com.veriplace.web.servlet;

import com.veriplace.client.Client;
import com.veriplace.client.ConfigurationException;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.VeriplaceException;
import com.veriplace.web.RespondedException;
import com.veriplace.web.Veriplace;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.views.StatusViewException;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for servlets that use Veriplace APIs.
 * <p>
 * Deriving from this class allows you to access Veriplace user/location data as follows:
 * <ul>
 * <li> When the servlet starts up, it initializes a {@link com.veriplace.web.Veriplace} instance
 * or retrieves an existing one, using {@link VeriplaceServletHelper#getSharedVeriplaceInstance(javax.servlet.ServletContext)}
 * (see {@link VeriplaceServletHelper} for configuration parameters). <li>
 * <li> For each servlet request, if your class has a {@link com.veriplace.web.servlet.UsesVeriplace} annotation
 * specifying a need for user or location data, the corresponding Veriplace requests are done before your servlet
 * handler executes. This may include redirects and callbacks, which happen transparently. </li>
 * <li> The base class provides your request handler with a {@link com.veriplace.web.VeriplaceState} object
 * which you can use to get the user/location properties, or to perform other operations. </li>
 * </ul>
 * <p>
 * You may attach the following <tt>init-param</tt>s to your servlet in <tt>web.xml</tt>:
 * <ul>
 * <li> <tt>veriplace.views</tt>: The name for a set of parameters in <tt>web.xml</tt> specifying how
 * Veriplace should display status or error pages for this servlet.  If the name is "myParams", the
 * corresponding <tt>context-param</tt>s will have names starting with "veriplace.views.myParams.".  See
 * {@link VeriplaceServletHelper#getViewRendererFromViewParams(javax.servlet.ServletContext, String)}. </li>
 * <li> <tt>veriplace.defaultview</tt>: Optional name of a view that this class's default implementation
 * of {@link #doRequestInternal(HttpServletRequest, HttpServletResponse, VeriplaceState)} will display.
 * This is an easy way to define a servlet that does some simple preprocessing of a request and then
 * forwards to a JSP. The view name prefix and suffix specified in your view parameters are also applied
 * to this view name, so by default there is an implied ".jsp" extension.</li>
 * </ul> 
 */
public abstract class AbstractVeriplaceServlet
   extends HttpServlet {

   private static final Log logger = LogFactory.getLog(AbstractVeriplaceServlet.class);
   private static final String VIEW_PARAMS_INIT_PARAM = "veriplace.views";
   private static final String DEFAULT_VIEW_INIT_PARAM = "veriplace.defaultview";
   
   protected Veriplace veriplace;
   protected String defaultViewName;
   private String propertiesFilePath;
   private Properties properties = null;
   
   /**
    * Initialize the client.
    */
   @Override
   public void init(ServletConfig config)
      throws ServletException {

      super.init(config);
      try {
         logger.info("Initializing veriplace instance");
         veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(config.getServletContext());
      } catch (Throwable t) {
         logger.warn(t);
         logger.debug(t,t);
         throw new ServletException(t);
      }

      boolean useStatusViews = true;
      UsesVeriplace anno = this.getClass().getAnnotation(UsesVeriplace.class);
      if (anno != null) {
         useStatusViews = anno.useStatusViews();
      }
      
      if (useStatusViews) {
         ServletStatusViewRenderer viewRenderer;
         String viewParamsName = config.getInitParameter(VIEW_PARAMS_INIT_PARAM);
         if (viewParamsName != null) {
            viewRenderer = VeriplaceServletHelper.getViewRendererFromViewParams(config.getServletContext(), viewParamsName);
         }
         else {
            viewRenderer = new ServletStatusViewRenderer();
         }
         veriplace.setStatusViewRenderer(viewRenderer);
      }
      else {
         veriplace.setStatusViewRenderer(null);
      }
      defaultViewName = config.getInitParameter(DEFAULT_VIEW_INIT_PARAM);
      propertiesFilePath = config.getInitParameter(VeriplaceServletHelper.PROPERTIES_FILE_CONTEXT_PARAM);
   }
   
   @Override
   protected void doGet(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      doGetOrPost(request, response);
   }
   
   @Override
   protected void doPost(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      doGetOrPost(request, response);
   }
   
   protected void doGetOrPost(HttpServletRequest request,
                              HttpServletResponse response)
      throws ServletException,
             IOException {

      VeriplaceState veriplaceState = null;
      Exception exception = null;
      try {
         veriplaceState = createState(request, response);
         applyRequirements(request, response, veriplaceState);
         doRequestInternal(request, response, veriplaceState);
      }
      catch (RespondedException e) {
         // not an error; flow has been interrupted by a redirect to the
         // Veriplace site or to a status page
      }
      catch (VeriplaceException e) {
         exception = e;
      }
      // catch-all for errors that were not handled elsewhere in the flow
      if (exception != null) {
         if (veriplaceState.getVeriplace().getStatusViewRenderer() != null) {
            try {
               veriplaceState.getVeriplace().getStatusViewRenderer().renderErrorView(request, response, veriplaceState, exception);
            }
            catch (StatusViewException e) {
               throw new ServletException(e);
            }
         }
         else {
            throw new ServletException(exception);
         }
      }
   }

   /**
    * Override this method to implement your request handler.
    * <p>
    * This method will be called for both GET and POST requests.  If you need to distinguish
    * between GET and POST, check the properties of the HttpServletRequest; however, be aware
    * that an operation that starts out as a POST may come back as a GET, if you went through
    * a redirect/callback process for location discovery.
    * <p>
    * The base class implementation of this method simply forwards the request to the page
    * specified in <tt>init-param</tt>s as <tt>veriplace.defaultview</tt>.
    */
   protected void doRequestInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    VeriplaceState veriplaceState)
      throws IOException,
             ServletException,
             VeriplaceException {
      
      doRequestInternal(request, response, veriplaceState.getVeriplace(), veriplaceState);
   }
   
   /**
    * Override this method to implement your request handler.  This version provides the
    * current {@link Veriplace} instance as a convenience.
    * <p>
    * This method will be called for both GET and POST requests.  If you need to distinguish
    * between GET and POST, check the properties of the HttpServletRequest; however, be aware
    * that an operation that starts out as a POST may come back as a GET, if you went through
    * a redirect/callback process for location discovery.
    * <p>
    * The base class implementation of this method simply forwards the request to the page
    * specified in <tt>init-param</tt>s as <tt>veriplace.defaultview</tt>.
    */
   protected void doRequestInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Veriplace veriplace,
                                    VeriplaceState veriplaceState)
      throws IOException,
             ServletException,
             VeriplaceException {
      
      if (defaultViewName != null) {
         ServletStatusViewRenderer viewRenderer = (ServletStatusViewRenderer)
               veriplaceState.getVeriplace().getStatusViewRenderer();
         if (viewRenderer != null) {
            logger.debug("Rendering view: " + defaultViewName);
            viewRenderer.renderViewInternal(request, response, veriplaceState, defaultViewName);
         }
      }
   }
   
   /**
    * Override this method if you might need to use a different Veriplace {@link Client} for some
    * requests.  For instance, you might use the same servlet to handling requests for more than
    * one Veriplace application, each with its own consumer key and token.  A Client is a fairly
    * lightweight object, so it's acceptable to create a new one for each request if you want to.
    */
   protected Client getVeriplaceClient(HttpServletRequest request, Veriplace defaultInstance)
         throws ConfigurationException,
                UnexpectedException {
      
      return null;
   }
   
   /**
    * Override this method if you need to change any properties of the {@link VeriplaceState},
    * such as {@link VeriplaceState#setLocationMode(String)}, based on properties of the current request.
    */
   protected void setupVeriplaceState(HttpServletRequest request, VeriplaceState state)
         throws VeriplaceException {
   }

   /**
    * Returns the contents of the properties file specified in the "veriplace.properties-file"
    * servlet context parameter in web.xml.  This allows you to access non-Veriplace-related
    * properties that you may wish to have in the same file for convenience.
    * @throws ConfigurationException  if the properties file was not specified or can't be read
    */
   protected Properties getProperties()
         throws ConfigurationException {
      return VeriplaceServletHelper.getSharedVeriplaceProperties(getServletConfig().getServletContext());
   }
   
   private VeriplaceState createState(HttpServletRequest request,
                                      HttpServletResponse response)
         throws ConfigurationException,
                VeriplaceException {
      
      Client client = getVeriplaceClient(request, veriplace);
      Veriplace instance = veriplace;
      if (client != null) {
         instance = new Veriplace(veriplace, client);
      }
      VeriplaceState state = instance.open(request, response);
      
      setupVeriplaceState(request, state);
      return state;
   }
   
   private void applyRequirements(HttpServletRequest request,
                                  HttpServletResponse response,
                                  VeriplaceState state)
         throws VeriplaceException,
                ServletException {
      
      Veriplace instance = state.getVeriplace();
      UsesVeriplace anno = this.getClass().getAnnotation(UsesVeriplace.class);
      if (anno != null) {
         if (! anno.allowUserInteraction()) {
            state.setUserInteractionAllowed(false);
         }
         if (anno.requireUser()) {
            state.requireUser();
         }
         if (anno.requireGetLocationPermission()) {
            state.requireGetLocationPermission();
         }
         if (anno.requireLocation()) {
            String mode = anno.mode();
            if (! mode.equals("")) {
               state.setLocationMode(mode);
            }
            state.requireLocation();
         }
         if (anno.requireSetLocationPermission()) {
            state.requireSetLocationPermission();
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
