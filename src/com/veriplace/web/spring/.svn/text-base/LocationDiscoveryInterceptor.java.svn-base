/* Copyright 2008-2009 WaveMarket, Inc.
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
package com.veriplace.web.spring;

import com.veriplace.web.VeriplaceState;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An Interceptor that does not allow requests to proceed until the current user's location
 * is available from Veriplace. 
 */
public class LocationDiscoveryInterceptor extends VeriplaceInterceptor {

   private String locationAttributeName;
   private String locationMode;
   
   /**
    * See {@link #setLocationAttributeName(String)}.
    */
   public String getLocationAttributeName() {
      return locationAttributeName;
   }

   /**
    * Specifies that when location discovery has been successfully completed on a request, the
    * {@link com.veriplace.client.Location} object should be stored in the request as an attribute
    * with this name.
    */
   public void setLocationAttributeName(String locationAttributeName) {
      this.locationAttributeName = locationAttributeName;
   }

   /**
    * See {@link #setLocationMode(String)}.
    */
   public String getLocationMode() {
      return locationMode;
   }

   /**
    * Specifies the method or degree of accuracy for obtaining location. The names of allowable modes are
    * defined in {@link com.veriplace.client.LocationMode}.
    */
   public void setLocationMode(String locationMode) {
      this.locationMode = locationMode;
   }

   protected boolean handleInternal(VeriplaceState state, HttpServletRequest request, HttpServletResponse response)
         throws Exception {

      state.setRequiresLocation(true);
      state.setLocationMode(locationMode);
      if (state.completeAll()) {
         if ((locationAttributeName != null) && !locationAttributeName.equals("")) {
            request.setAttribute(locationAttributeName, state.getLocation());
         }
         return true;
      }
      return false;
   }
}
