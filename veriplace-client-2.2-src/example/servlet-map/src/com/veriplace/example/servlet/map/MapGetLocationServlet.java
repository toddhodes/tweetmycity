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

import com.veriplace.client.ConfigurationException;
import com.veriplace.client.LocationMode;
import com.veriplace.client.VeriplaceException;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.servlet.AbstractVeriplaceServlet;
import com.veriplace.web.servlet.UsesVeriplace;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Displays the location of the current Veriplace user.
 * <p>
 * By deriving from {@link com.veriplace.web.servlet.AbstractVeriplaceServlet} and specifying
 * the annotation {@link com.veriplace.web.servlet.UsesVeriplace} with <tt>requireLocation = true</tt>,
 * this servlet triggers a location discovery request (including user discovery, if we do not
 * already know the current user) before its request handler executes.  This may require
 * redirecting to an external Veriplace page, and may fail or be cancelled at any point, in which
 * case we will be redirected to an error page.
 * <p>
 * In this simple implementation, we display the results by forwarding to a JSP (which is the default
 * behavior of AbstractVeriplaceServlet).  This entire servlet could be replaced by that JSP, using
 * the Veriplace custom tag library, except that we have defined one additional property (a Google Maps
 * API key) which we need to add to the request attributes.
 * <p>
 * Note that other servlets in this example, such as {@link MapFindUserServlet}, can forward a
 * request to this servlet to get and display the location, after performing the user discovery
 * step in some other way.
 * <p>
 * Requires the following <tt>init-param</tt>s in web.xml:
 * <ul>
 * <li> <tt>veriplace.views</tt>: See {@link com.veriplace.web.servlet.AbstractVeriplaceServlet}. </li>
 * <li> <tt>veriplace.defaultview</tt>: Name of the JSP we will display. </li>
 * </ul> 
 */
@UsesVeriplace(
      requireLocation = true,
      mode = LocationMode.ZOOM
)
public class MapGetLocationServlet extends AbstractVeriplaceServlet {
      
   private String googleApiKey;
   
   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      
      try {
         googleApiKey = getProperties().getProperty("googleApiKey");
      }
      catch (ConfigurationException e) {
         throw new ServletException(e);
      }
   }

   @Override
   protected void doRequestInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    VeriplaceState veriplaceState)
      throws ServletException, 
             IOException, 
             VeriplaceException {

      request.setAttribute("googleApiKey", googleApiKey);

      // Pass the request to the base class, which will just forward to the view defined in our
      // "veriplace.defaultview" parameter in web.xml.
      super.doRequestInternal(request, response, veriplaceState);
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
