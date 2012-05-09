/* Copyright 2008 WaveMarket, Inc.
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
package com.veriplace.example.client;

import com.veriplace.client.Location;
import com.veriplace.client.User;
import com.veriplace.client.VeriplaceException;
import com.veriplace.oauth.consumer.Token;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Sample servlet that makes a GetLocation request using the Veriplace Client library.
 */
public class GetLocation
   extends ClientServlet {

   @Override
   protected void doGet(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      User user = getUser(request);

      if (user == null) {
         doDiscoverUser(request,response);
      } else if (client.isCallback(request)) {
         doCallback(request,response,user);
      } else {
         doForm(request,response,user);
      }
   }

   /**
    * Pull the current user from request parameters
    */
   protected User getUser(HttpServletRequest request) {
      try {
         long userId = Long.parseLong(request.getParameter("user"));
         return new User(userId);
      } catch (NullPointerException e) {
      } catch (NumberFormatException e) {
      }
      return null;
   }

   /**
    * Delegate to {@link UserDiscovery} servlet, e.g. if we don't know the user.
    */
   protected void doDiscoverUser(HttpServletRequest request,
                                 HttpServletResponse response)
      throws ServletException,
             IOException {

      String callback = client.getCallbackFactory().createCallbackUrl(request,false) + request.getRequestURI();

      try {
         String redirectUrl = client.getUserDiscoveryAPI().getRedirectURL(callback,null);
         response.sendRedirect(redirectUrl);
      } catch (Exception e) {
         throw new ServletException(e);
      }
   }

   /**
    * Show a simple html page and form.
    */
   protected void doForm(HttpServletRequest request,
                         HttpServletResponse response,
                         User user)
      throws ServletException,
             IOException {

      StringBuilder buf = new StringBuilder();
      buf.append("<html>");
      buf.append(" <body>");
      buf.append("  <h2>Veriplace Client Example</h2>");
      buf.append("  <form method='post'>");
      buf.append("   <input type='hidden' name='user' value='" + user.getId() + "'/>");
      buf.append("   <input type='submit' value='Locate User'/>");
      buf.append("  </form>");
      buf.append(" </body>");
      buf.append("</html>");

      response.setContentType("text/html");
      response.getOutputStream().write(buf.toString().getBytes());
   }

   protected void doCallback(HttpServletRequest request,
                             HttpServletResponse response,
                             User user)
      throws ServletException,
             IOException {

      // retrieve the Access Token, if any
      try {
         Token accessToken = client.getAccessToken(request);

         // get user
         Location location = client.getGetLocationAPI().getLocation(accessToken, user);

         // show user
         StringBuilder buf = new StringBuilder();
         buf.append("<html>");
         buf.append(" <body>");
         buf.append("  <h2>Veriplace Client Example</h2>");
         buf.append("  <p>User: " + user.getId() + "</p>");
         if (location != null) {
            buf.append("  <p>Longitude: " + location.getLongitude() + "</p>");
            buf.append("  <p>Latitude: " + location.getLatitude() + "</p>");
         }
         buf.append("</p>");
         buf.append(" </body>");
         buf.append("</html>");
         
         response.setContentType("text/html");
         response.getOutputStream().write(buf.toString().getBytes());
      }
      catch (VeriplaceException e) {
         // either access was not granted by the user 
         // or the page has been reloaded and the original request token 
         // is no longer valid
         doForm(request,response,user);
      }
   }

   /**
    * On a post, locate user
    */
   @Override
   protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
      throws ServletException,
             IOException {

      User user = getUser(request);

      if (user == null) {
         doDiscoverUser(request,response);
         return;
      } 

      try {
         // construct callback url
         String callback = 
            client.getCallbackFactory().createCallbackUrl(request,false) +
            request.getRequestURI() + 
            "?user=" + 
            user.getId();
         // construct the redirect URL for user authorization
         String redirectUrl = client.getGetLocationAPI().getRedirectURL(callback,user);
         // redirect the User Agent
         response.sendRedirect(redirectUrl);
      } catch (Exception e) {
         throw new ServletException(e);
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

