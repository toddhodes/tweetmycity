/* Copyright 2010 WaveMarket, Inc.
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
package com.veriplace.demo.web;

import com.veriplace.client.ConfigurationException;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.VeriplaceException;
import com.veriplace.web.RespondedException;
import com.veriplace.web.VeriplaceState;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Controller for the landing page.  Just redirects to the appropriate page
 * (location, login, or error) after checking whether the user is already known
 * to us.
 */
public class StartPage extends BaseServlet {

   private static final Log logger = LogFactory.getLog(StartPage.class);
   
   private String startView;
   private String loginPageUrl;
   private String locatePageUrl;
   private String errorView;

   /**
    * Overridden to get configuration parameters from <tt>web.xml</tt>.
    * <ul>
    * <li> <tt>startView</tt>: name of the JSP to show at first </li>
    * <li> <tt>loginPageUrl</tt>: URL of the servlet to redirect to if we
    *   need the user to log in </li>
    * <li> <tt>locatePageUrl</tt>: URL of the servlet to redirect to once we
    *   have identified the user </li>
    * <li> <tt>errorView</tt>: name of the JSP to show in case of an
    *   unexpected error </li>
    */
   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      startView = config.getInitParameter("startView");
      loginPageUrl = config.getInitParameter("loginPageUrl");
      locatePageUrl = config.getInitParameter("locatePageUrl");
      errorView = config.getInitParameter("errorView");
   }

   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {

      // The "I Accept" form has a hidden field called "start".
      if (request.getParameter("start") == null) {
         showPage(request, response, startView);
      }
      else {
         submitForm(request, response);
      }     
   }
     
   protected void submitForm(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
   
      // If user didn't accept terms of service, redisplay page with an error.
      if (request.getParameter("acceptTerms") == null) {
         request.setAttribute("mustAcceptTermsError", true);
         showPage(request, response, startView);
         return;
      }
      
      VeriplaceState vs = application.getVeriplace().open(request, response);
      
      final User user;
      try {
         // Ask Veriplace to identify the current user if possible.  This will
         // succeed if the user is accessing the application through a mobile
         // browser on a Veriplace-enabled phone, or if the user is on a desktop
         // browser and has previously logged into Veriplace with the number and
         // password of a Veriplace-enabled phone.
         vs.setUserInteractionAllowed(false);
         vs.requireUser();
         
         // If requireUser() returns without an exception, then we have a valid
         // user identifier.  This doesn't tell us anything about the user; it
         // just distinguishes them from other users and allows us to request
         // locations through Veriplace.
         user = vs.getUser();
      }
      catch (RespondedException e) {
         // The Veriplace client has triggered a browser redirect, so we should
         // stop handling this request and wait for the second request that will
         // come when Veriplace redirects back to us.  This is not an interactive
         // request, so the user doesn't see anything - the Veriplace site
         // immediately redirects back to the application, providing an access
         // token if successful.  This is an OAuth transaction that allows the
         // user's login state to be completely independent of the application.
         return;
      }
      catch (UserDiscoveryException e) {
         // There's no currently logged-in Veriplace user; redirect to the page
         // that will allow the user to log in.
         response.sendRedirect(loginPageUrl);
         return;
      }
      catch (VeriplaceException e) {
         // Catch-all for any other unexpected problem reported by Veriplace.
         logger.warn("Unexpected error in user discovery: " + e);
         logger.debug(e, e);
         showPage(request, response, errorView);
         return;
      }
      
      // Redirect to the controller that checks for usage limits and permission
      // and then locates the user.  Store the current user identifier in the
      // HTTP session.
      putInSession(request, VERIPLACE_USER_SESSION_KEY, user);
      response.sendRedirect(locatePageUrl);
   }
}
