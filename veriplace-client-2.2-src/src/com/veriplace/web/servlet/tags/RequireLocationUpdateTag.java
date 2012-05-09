/* Copyright 2008-2010 WaveMarket, Inc.
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
package com.veriplace.web.servlet.tags;

import com.veriplace.client.Location;
import com.veriplace.client.SetLocationParameters;

public class RequireLocationUpdateTag extends AbstractUserSupportingRequireTag {

   protected Double longitude = null;
   protected Double latitude = null;
   protected Double uncertainty = null;
   protected String address = null;
   
   @Override
   protected boolean handleTagInternal() throws Exception {
      super.handleTagInternal();

      SetLocationParameters parameters = new SetLocationParameters();
      if (longitude != null &&
          latitude != null &&
          uncertainty != null) {
         parameters.setLongitude(longitude);
         parameters.setLatitude(latitude);
         parameters.setUncertainty(uncertainty);
      } else if (address != null) {
         parameters.setAddress(address);
      }
      veriplaceState.setUserLocation(parameters);
      return true;
   }
   
   protected Object getResultObject() {
      return veriplaceState.getLocation();
   }

   public void setLongitude(Double longitude) {
      this.longitude = longitude;
   }
   
   public void setLatitude(Double latitude) {
      this.latitude = latitude;
   }

   public void setUncertainty(Double uncertainty) {
      this.uncertainty = uncertainty;
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public static class ExtraInfo extends AbstractRequireTag.ExtraInfo {
      
      protected Class getObjectClass() {
         return Location.class;
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
