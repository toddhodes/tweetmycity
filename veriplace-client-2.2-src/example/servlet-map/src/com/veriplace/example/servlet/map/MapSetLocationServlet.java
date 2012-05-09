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

import com.veriplace.client.SetLocationException;
import com.veriplace.client.SetLocationParameters;
import com.veriplace.client.VeriplaceException;
import com.veriplace.web.Veriplace;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.servlet.AbstractVeriplaceServlet;
import com.veriplace.web.servlet.UsesVeriplace;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides a form for setting a new location for the current Veriplace user, either by
 * longitude/latitude or with an address string (e.g. a street address or city/state).
 * If the request is successful, the form is redisplayed with the details of the new
 * location.
 * <p>
 * By deriving from {@link com.veriplace.web.servlet.AbstractVeriplaceServlet} and specifying
 * the annotation {@link com.veriplace.web.servlet.UsesVeriplace} with
 * <tt>requireSetLocationPermission = true</tt>, this servlet acquires a token from Veriplace
 * which can be used to set the location.  If the application does not have permission to get
 * this token, the request is redirected to an error page and the servlet handler does not
 * execute.
 * <p>
 * <p>
 * Requires the following <tt>init-param</tt>s in web.xml:
 * <ul>
 * <li> <tt>veriplace.views</tt>: See {@link com.veriplace.web.servlet.AbstractVeriplaceServlet}. </li>
 * <li> <tt>veriplace.defaultview</tt>: Name of the JSP containing our form. </li>
 * </ul> 
 */
@UsesVeriplace (
      requireSetLocationPermission = true
)
public class MapSetLocationServlet extends AbstractVeriplaceServlet {
      
   @Override
   protected void doRequestInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    Veriplace veriplace, 
                                    VeriplaceState veriplaceState)
         throws ServletException, 
                IOException, 
                VeriplaceException {


      String setCommand = request.getParameter("set");
      if ((setCommand != null) && !setCommand.equals("")) {
         SetLocationParameters params = new SetLocationParameters();
         String place = request.getParameter("address");
         if ((place != null) && !place.equals("")) {
            params.setAddress(place);
         }
         else {
            try {
               params.setLongitude(Double.parseDouble(request.getParameter("longitude")));
               params.setLatitude(Double.parseDouble(request.getParameter("latitude")));
               String accuStr = request.getParameter("accuracy");
               if ((accuStr != null) && !accuStr.equals("")) {
                  params.setUncertainty(Double.parseDouble(accuStr));
               }
            } catch (NumberFormatException e) {
            } catch (NullPointerException e) {
            }
         }
         if (params.isValid()) {
            try {
               veriplaceState.setUserLocation(params);
            }
            catch (SetLocationException e) {
            }
         }
         request.setAttribute("showResult", true);
      }

      // Pass the request to the base class, which will just forward to the view defined in our
      // "veriplace.defaultview" parameter in web.xml.
      super.doRequestInternal(request, response, veriplace, veriplaceState);
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
