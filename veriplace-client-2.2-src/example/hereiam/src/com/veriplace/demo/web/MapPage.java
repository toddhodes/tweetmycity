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
import com.veriplace.client.Location;
import com.veriplace.demo.model.DemoUser;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Controller that displays the most recent location for the currently logged-in
 * user.  This is separate from {@link LocatePage} so that if the user refreshes
 * the browser, we'll just redisplay the same location rather than requesting a new
 * one.
 * <p>
 * The Veriplace {@link Location} object always contains longitude and latitude
 * coordinates and an uncertainty radius, and may also have a street address, city,
 * state, and postal code. In this simple implementation, our output page just shows
 * the coordinates and/or address (using <a href="http://java.sun.com/products/jsp/jstl/">JSTL</a>
 * tags to get the properties of the Location object). A fancier version could use
 * JavaScript controls to display an interactive map centered on those coordinates.
 */
public class MapPage extends BaseServlet {

   private String startPageUrl;
   private String mapView;

   /**
    * Overridden to get configuration parameters from <tt>web.xml</tt>.
    * <ul>
    * <li> <tt>mapView</tt>: name of the JSP to show with the results </li>
    * <li> <tt>startPageUrl</tt>: relative URL to redirect to if we need to
    *   start over, e.g. if the user's session expired </li>
    */
   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      startPageUrl = config.getInitParameter("startPageUrl");
      mapView = config.getInitParameter("mapView");
   }
   
   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {

      DemoUser demoUser = (DemoUser) getFromSession(request,
            DEMO_USER_SESSION_KEY);
      Location location = (Location) getFromSession(request,
            LOCATION_SESSION_KEY);
      if ((demoUser == null) || (location == null)) {
         // The session is gone - maybe the user closed the browser and then
         // tried to reload this URL.  Just go back to the start page.
         response.sendRedirect(startPageUrl);
         return;
      }
      
      // Success; display the result page.
      int remainingLocates = application.getRemainingLocates(demoUser);
      int remainingHours = application.getRemainingHours(demoUser);
      int remainingDays = remainingHours / 24;
      request.setAttribute("location", location);
      request.setAttribute("remainingLocates", remainingLocates);
      request.setAttribute("remainingDays", remainingDays);
      request.setAttribute("remainingHours", remainingHours);
      showPage(request, response, mapView);
   }
}
