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
package com.veriplace.example.servlet.map;

import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.UserDiscoveryParameters;
import com.veriplace.client.VeriplaceException;
import com.veriplace.web.Veriplace;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.servlet.AbstractVeriplaceServlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides a form for discovering the currently logged-in Veriplace user by phone number,
 * E-mail, or OpenID.  This method of user discovery avoids having to redirect to a Veriplace
 * page; it is a single synchronous request that either immediately succeeds or fails.  For
 * this to work, your application must have an application token in its properties file.
 * <p>
 * This simple servlet derives from {@link com.veriplace.web.servlet.AbstractVeriplaceServlet}, so it
 * has access to a {@link com.veriplace.web.VeriplaceState} object which it uses to make a
 * user discovery request.  The servlet does not specify a
 * {@link com.veriplace.web.servlet.UsesVeriplace} attribute, because no request preprocessing
 * is needed until the user submits the search form.  If the search is successful, it simply
 * forwards the request over to the regular "get location" servlet.
 * <p>
 * Requires the following <tt>init-param</tt>s in web.xml:
 * <ul>
 * <li> <tt>veriplace.views</tt>: See {@link com.veriplace.web.servlet.AbstractVeriplaceServlet}. </li>
 * <li> <tt>resultPath</tt>: Relative URL of the regular Get Location servlet. </li>
 * </ul> 
 */
public class MapFindUserServlet extends AbstractVeriplaceServlet {
   
   private String resultPath;
      
   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);

      resultPath = config.getInitParameter("resultPath");
   }

   @Override
   protected void doRequestInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    Veriplace veriplace, 
                                    VeriplaceState veriplaceState)
      throws ServletException, 
             IOException, 
             VeriplaceException {

      String findCommand = request.getParameter("find");
      if ((findCommand == null) || findCommand.equals("")) {
         // Pass the request to the base class, which will just forward to the view defined in our
         // "veriplace.defaultview" parameter in web.xml.
         super.doRequestInternal(request, response, veriplace, veriplaceState);
         return;
      }
      
      UserDiscoveryParameters fp = new UserDiscoveryParameters();
      fp.setPhone(request.getParameter("phone"));
      fp.setEmail(request.getParameter("email"));
      fp.setOpenId(request.getParameter("openid"));
      
      // Attempt to find the user.  Note, the "false" parameter here means that if there is already
      // a user ID attached to the request, it will skip the search.  This is desirable because
      // otherwise if the location request triggered a callback and we started again from the top
      // of this handler, we would unnecessarily repeat the same search we just did.
      try {
         veriplaceState.requireUser(fp);
      } catch (UserDiscoveryException e) {
         request.setAttribute("error", "User search failed (not found, or insufficient permission).");
         super.doRequestInternal(request, response, veriplace, veriplaceState);
         return;
      }
      
      // Forward the request over to the "get location" servlet.  The user ID we've just obtained
      // is now in the VeriplaceState that's attached to the request, so the other servlet will
      // not try to do its own user discovery.      
      request.getRequestDispatcher(resultPath).forward(request, response);
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
