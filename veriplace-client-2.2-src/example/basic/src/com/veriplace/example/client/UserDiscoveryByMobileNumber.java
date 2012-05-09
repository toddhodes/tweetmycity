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

import com.veriplace.client.User;
import com.veriplace.client.VeriplaceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

/**
 * Sample servlet that makes a UserDiscovery request using the Veriplace Client library.
 * <p>
 * This example supports the user discovery query using a mobile number.
 */
public class UserDiscoveryByMobileNumber 
   extends ClientServlet {

   @Override
   protected void doGet(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      /* There is no callback for this flow.
       *
       * OAuth is not used to obtain an access token; instead
       * the preconfigured application token is used.
       */
      doForm(request,response);
   }

   /**
    * Show a simple html page and form.
    */
   protected void doForm(HttpServletRequest request,
                         HttpServletResponse response)
      throws ServletException,
             IOException {

      StringBuilder buf = new StringBuilder();
      buf.append("<html>");
      buf.append(" <body>");
      buf.append("  <h2>Veriplace Client Example</h2>");
      buf.append("  <form method='post'>");
      buf.append("   <input type='text' name='mobileNumber'/>");
      buf.append("   <input type='submit' value='Discover User For Specified Mobile Number'/>");
      buf.append("  </form>");
      buf.append(" </body>");
      buf.append("</html>");

      response.setContentType("text/html");
      response.getOutputStream().write(buf.toString().getBytes());
   }

   /**
    * On a post, perform user discovery 
    */
   @Override
   protected void doPost(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      String mobileNumber = request.getParameter("mobileNumber");

      if (mobileNumber == null) {
         doForm(request,response);
         return;
      }

      try {
         // get user
         User user = client.getUserDiscoveryAPI().getUserByMobileNumber(mobileNumber);
   
         // show user
         StringBuilder buf = new StringBuilder();
         buf.append("<html>");
         buf.append(" <body>");
         buf.append("  <h2>Veriplace Client Example</h2>");
         if (user != null) {
            buf.append("  <p>User: " + user.getId() + "</p>");
            buf.append("  <p><a href='location?user=" + user.getId() + "'>Get Location</a></p>");
         }
         buf.append(" </body>");
         buf.append("</html>");
         
         response.setContentType("text/html");
         response.getOutputStream().write(buf.toString().getBytes());
      }
      catch (VeriplaceException e) {
         doForm(request, response);
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

