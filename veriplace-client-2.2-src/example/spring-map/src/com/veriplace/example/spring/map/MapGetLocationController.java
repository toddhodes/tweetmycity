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
package com.veriplace.example.spring.map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
/**
 * This is a simple implementation of the Map example using the Spring web MVC framework.
 * Nearly all of the logic is contained in the configuration file, WEB-INF/appContext/springMap-servlet.xml,
 * which defines the mapping between request parameters and servlet methods, and also defines an
 * interceptor ({@link com.veriplace.web.spring.LocationDiscoveryInterceptor}) that gets called
 * along the way to locateUser.
 */
public class MapGetLocationController extends AbstractController {

   private String resultViewName;
   private String googleApiKey;
   
   public void setResultViewName(String name) {
      resultViewName = name;
   }
   
   public void setGoogleApiKey(String key) {
      googleApiKey = key;
   }
   
   /**
    * This method is called when the user has submitted the search form.  If we are here, we are
    * guaranteed to have a valid location attached to the request, because the
    * {@link com.veriplace.web.spring.LocationDiscoveryInterceptor} has already processed the
    * request, putting us through however many redirect/callback cycles may be required to get the
    * user and location data; if any of those steps failed, we would have been redirected to another
    * page rather than getting to this method.  Therefore, all we have to do at this point is display
    * the result page with the location.  We don't have to worry about transferring any LocationState
    * properties to the result model, because we're derived from a helper class that already does that.
    */
   @Override
   public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
         throws Exception {

      ModelAndView mv = new ModelAndView(resultViewName);
      mv.addObject("googleApiKey", googleApiKey);
      return mv;
   }
}
