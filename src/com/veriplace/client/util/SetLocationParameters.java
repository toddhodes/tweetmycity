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
package com.veriplace.client.util;

/**
 * Encapsulates the various ways in which you can attempt to update a user's location.
 */
public class SetLocationParameters {

   private String geocodingQueryString;
   private Double longitude;
   private Double latitude;
   private Double accuracy;
   
   public SetLocationParameters() {
   }
   
   public Double getAccuracy() {
      return accuracy;
   }
   
   public void setAccuracy(Double accuracy) {
      this.accuracy = accuracy;
   }
   
   public String getGeocodingQueryString() {
      return geocodingQueryString;
   }
   
   public void setGeocodingQueryString(String geocodingQueryString) {
      this.geocodingQueryString = geocodingQueryString;
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
      return (geocodingQueryString != null) || (longitude != null) || (latitude != null) || (accuracy != null);
   }
   
   public boolean isValid() {
      if (geocodingQueryString != null) {
         return (longitude == null) && (latitude == null) && (accuracy == null);
      }
      else {
         return (longitude != null) && (latitude != null);
      }
   }
}
