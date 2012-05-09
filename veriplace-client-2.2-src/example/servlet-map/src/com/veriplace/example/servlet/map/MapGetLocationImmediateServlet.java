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

import com.veriplace.web.VeriplaceState;
import com.veriplace.web.servlet.AbstractVeriplaceServlet;
import com.veriplace.web.servlet.UsesVeriplace;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Displays the current user's location, if the user is already logged into Veriplace; otherwise
 * goes to the error page.
 * <p>
 * This simple servlet exists only to provide an additional preprocessing step to
 * {@link MapGetLocationServlet}.  By specifying a {@link com.veriplace.web.servlet.UsesVeriplace}
 * annotation with <tt>requireUser = true</tt> and <tt>allowUserInteraction = false</tt>, it causes
 * Veriplace to go through the user discovery process -- which may include an invisible redirect --
 * but does not allow it to solicit any user input, unlike the default behavior.  Then, if this
 * succeeds, the servlet handler simply forwards the request over to the regular "get location"
 * handler.
 * <p>
 * Requires the following <tt>init-param</tt>s in web.xml:
 * <ul>
 * <li> <tt>veriplace.views</tt>: See {@link com.veriplace.web.servlet.AbstractVeriplaceServlet}. </li>
 * <li> <tt>resultPath</tt>: Relative URL of the regular Get Location servlet. </li>
 * </ul> 
 */
@UsesVeriplace(
      requireUser = true,
      allowUserInteraction = false
)
public class MapGetLocationImmediateServlet extends AbstractVeriplaceServlet {

   private String resultPath;
   
   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
   
      resultPath = config.getInitParameter("resultPath");
   }

   @Override
   protected void doRequestInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    VeriplaceState veriplaceState)
      throws ServletException, 
             IOException {

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
