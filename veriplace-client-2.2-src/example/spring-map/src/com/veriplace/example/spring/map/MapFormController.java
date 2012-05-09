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
 * Simple controller that just displays the initial search page.  This is defined separately from
 * {@link com.veriplace.example.spring.map.MapGetLocationController} so that the
 * {@link com.veriplace.example.spring.LocationDiscoveryInterceptor} will not be triggered for this request.
 */
public class MapFormController extends AbstractController {

   private static final String formPage = "index";
   
   public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
         throws Exception {

      return new ModelAndView(formPage);
   }
}
