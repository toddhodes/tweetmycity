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
package com.veriplace.client;

/**
 * Encapsulates the various kinds of parameters that can be passed to
 * {@link SetLocationAPI} methods.
 */
public class SetLocationParameters {

   private String address;
   private Double longitude;
   private Double latitude;
   private Double uncertainty;
   
   public SetLocationParameters() {
   }
   
   public Double getUncertainty() {
      return uncertainty;
   }
   
   public Double getAccuracy() {
      return uncertainty;
   }
   
   public void setUncertainty(Double uncertainty) {
      this.uncertainty = uncertainty;
   }

   public void setAccuracy(Double uncertainty) {
      this.uncertainty = uncertainty;
   }
   
   public String getAddress() {
      return address;
   }
   
   public void setAddress(String address) {
      this.address = address;
   }
   
   public Double getLatitude() {
      return latitude;
   }
   
   public void setLatitude(Double latitude) {
      this.latitude = latitude;
   }
   
   public Double getLongitude() {
      return longitude;
   }
   
   public void setLongitude(Double longitude) {
      this.longitude = longitude;
   }
   
   public boolean isSpecified() {
      return (address != null) || (longitude != null) || (latitude != null) || (uncertainty != null);
   }
   
   public boolean isValid() {
      if (address != null) {
         return (longitude == null) && (latitude == null) && (uncertainty == null);
      }
      else {
         return (longitude != null) && (latitude != null);
      }
   }
}
