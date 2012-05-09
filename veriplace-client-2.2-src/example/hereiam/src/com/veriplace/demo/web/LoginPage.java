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
import com.veriplace.client.UserDiscoveryNotPermittedException;
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
 * Controller for performing an interactive user discovery request, allowing the
 * user to log into Veriplace or register their phone for the first time.  That
 * could be done in the same step as {@link StartPage}, but this application is
 * designed with an extra step to let the user know that we're about to redirect
 * to the Veriplace site.
 */
public class LoginPage extends BaseServlet {

   private static final Log logger = LogFactory.getLog(LoginPage.class);
   
   private String startView;
   private String locatePageUrl;
   private String unsupportedPhoneView;
   private String errorView;

   /**
    * Overridden to get configuration parameters from <tt>web.xml</tt>.
    * <ul>
    * <li> <tt>startView</tt>: name of the JSP to show at first </li>
    * <li> <tt>locatePageUrl</tt>: URL of the servlet to redirect to once
    *   we have identified the user </li>
    * <li> <tt>unsupportedPhoneView</tt>: name of the JSP to show if Veriplace
    *   is unable to identify the user (the user cancels login, or does not have
    *   a compatible phone) </li>
    * <li> <tt>errorView</tt>: name of the JSP to show in case of an
    *   unexpected error </li>
    */
   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      startView = config.getInitParameter("startView");
      locatePageUrl = config.getInitParameter("locatePageUrl");
      unsupportedPhoneView = config.getInitParameter("unsupportedPhoneView");
      errorView = config.getInitParameter("errorView");
   }

   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {

      if (request.getParameter("start") == null) {
         showPage(request, response, startView);
      }
      else {
         submitForm(request, response);
      }     
   }
     
   protected void submitForm(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      
      // Do user discovery interactively
      VeriplaceState vs = application.getVeriplace().open(request, response);
      try {
         vs.requireUser();
      }
      catch (UserDiscoveryNotPermittedException e) {
         showPage(request, response, unsupportedPhoneView);
         return;
      }
      catch (RespondedException e) {
         // Redirected by client
         return;
      }
      catch (VeriplaceException e) {
         logger.warn("Unexpected error in user discovery: " + e);
         logger.debug(e, e);
         showPage(request, response, errorView);
      }
      User user = vs.getUser();
      
      // Redirect to the controller that checks for usage limits and permission
      // and then locates the user.  We use a redirect here, rather than
      // forwarding the request internally, to avoid problems if the user
      // refreshes the browser.  Store the current user identifier in the HTTP
      // session.
      putInSession(request, VERIPLACE_USER_SESSION_KEY, user);
      response.sendRedirect(locatePageUrl);
   }
}
