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
package com.veriplace.web.servlet;

import com.veriplace.client.Client;
import com.veriplace.client.Location;
import com.veriplace.client.User;
import com.veriplace.web.VeriplaceContext;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.RequestStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Properties;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for servlets that use Veriplace APIs.
 * <p>
 * Deriving from this class allows you to access Veriplace user/location data as follows:
 * <ul>
 * <li> When the servlet starts up, it initializes a {@link com.veriplace.web.VeriplaceContext}
 * or retrieves an existing one, using {@link VeriplaceServletHelper#getSharedVeriplaceContext(javax.servlet.ServletContext)}
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
 * {@link VeriplaceServletHelper#getStatusHandlerFromViewParams(javax.servlet.ServletContext, String)}. </li>
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
   
   protected VeriplaceContext veriplaceContext;
   protected String propertiesFilePath;
   protected Properties properties;
   protected ServletStatusHandler statusHandler;
   protected String defaultViewName;
   
   /**
    * Initialize the client.
    */
   @Override
   public void init(ServletConfig config)
      throws ServletException {

      try {
         veriplaceContext = VeriplaceServletHelper.getSharedVeriplaceContext(config.getServletContext());
         properties = veriplaceContext.getProperties();
      }
      catch (Throwable t) {
         logger.warn(t);
         logger.debug(t,t);
         throw new ServletException(t);
      }
      statusHandler = (ServletStatusHandler) veriplaceContext.getStatusHandler();
      String viewParamsName = config.getInitParameter(VIEW_PARAMS_INIT_PARAM);
      if (viewParamsName != null) {
         statusHandler = VeriplaceServletHelper.getStatusHandlerFromViewParams(config.getServletContext(), viewParamsName); 
      }
      defaultViewName = config.getInitParameter(DEFAULT_VIEW_INIT_PARAM);
   }
   
   @Override
   protected void doGet(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      VeriplaceState veriplaceState = null;
      try {
         veriplaceState = createState(request, response);
         if (veriplaceState.hasRequirements() && (! veriplaceState.completeAll())) {
            return;
         }
         doRequestInternal(request, response, veriplaceState);
      } catch (ServletException e) {
         statusHandler.renderView("error", veriplaceState, null, request, response);
      }

   }
   
   @Override
   protected void doPost(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      VeriplaceState veriplaceState = null;
      try {
         veriplaceState = createState(request, response);
         if (veriplaceState.hasRequirements() && (! veriplaceState.completeAll())) {
            return;
         }
         doRequestInternal(request, response, veriplaceState);
      } catch (ServletException e) {
         statusHandler.renderView("error", veriplaceState, null, request, response);
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
   protected void doRequestInternal(HttpServletRequest request, HttpServletResponse response,
         VeriplaceState veriplaceState) throws ServletException, IOException {
      if (defaultViewName != null) {
         statusHandler.renderView(defaultViewName, veriplaceState, null, request, response);
      }
   }
   
   /**
    * Override this method if you might need to use a different Veriplace {@link Client} for some
    * requests -- for instance, if the same servlet is handling requests for more than one Veriplace
    * application, each with its own consumer key and token. A Client is a fairly lightweight object,
    * so it's acceptable to create a new one for each request if you want to.
    */
   protected Client getVeriplaceClient(HttpServletRequest request)
         throws ServletException, IOException {
      return null;
   }
   
   /**
    * Override this method if you need to change any properties of the {@link VeriplaceState}
    * based on parameters from the current request.
    */
   protected void setupVeriplaceState(HttpServletRequest request, VeriplaceState state)
         throws ServletException, IOException {
   }
   
   private VeriplaceState createState(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      VeriplaceState state = veriplaceContext.useRequest(request, response, getVeriplaceClient(request));
      state.setStatusHandler(statusHandler);
      UsesVeriplace anno = this.getClass().getAnnotation(UsesVeriplace.class);
      if (anno != null) {
         if (anno.requireUser()) {
            state.setRequiresUser(true);
         }
         if (anno.requireLocation()) {
            state.setRequiresLocation(true);
            String mode = anno.mode();
            if (! mode.equals("")) {
               state.setLocationMode(mode);
            }
         }
         if (anno.requireSetLocationPermission()) {
            state.setRequiresSetLocationPermission(true);
         }
         if (! anno.allowUserInteraction()) {
            state.setUserInteractionAllowed(false);
         }
      }
      setupVeriplaceState(request, state);
      return state;
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
