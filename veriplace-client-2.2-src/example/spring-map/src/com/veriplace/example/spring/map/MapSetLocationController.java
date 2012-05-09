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

import com.veriplace.client.SetLocationParameters;
import com.veriplace.web.VeriplaceState;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * This is a simple implementation of the Map example using the Spring web MVC framework.
 * Nearly all of the logic is contained in the configuration file, WEB-INF/appContext/springMap-servlet.xml,
 * which defines the mapping between request parameters and servlet methods, and also defines an
 * interceptor ({@link com.veriplace.web.spring.LocationDiscoveryInterceptor}) that gets called
 * along the way to locateUser.
 */
public class MapSetLocationController extends SimpleFormController {

   @Override
   protected boolean isFormSubmission(HttpServletRequest request) {
      return (request.getParameter("set") != null);
   }

   @Override
   protected ModelAndView onSubmit(HttpServletRequest request, 
                                   HttpServletResponse response,
                                   Object command, 
                                   BindException errors) throws Exception {

      SetLocationParameters params = (SetLocationParameters) command;
      VeriplaceState state = VeriplaceState.getFromRequest(request);

      if (params.isValid()) {
         state.setUserLocation(params);
      }

      // Call the superclass implementation, which returns the form view that's defined in our
      // configuration.
      return super.onSubmit(command, errors);
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
