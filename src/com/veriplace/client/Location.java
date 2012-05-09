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
package com.veriplace.client;

import java.util.Date;

/**
 * Representation of the location of a Veriplace user.
 * <p>
 * In the event that a location fix was not sucessful, an {@link #getCode error code} 
 * and {@link #getMessage error message} will be returned.
 * <p>
 * Otherwise, location should always contain coordinate data, represented
 * as a {@link #getLongitude longitude} and a {@link #getLatitude latitude} values,
 * measured in degrees. Coordinate data will also contain an {@link #getAccuracy uncertainty radius}
 * measured in meters.
 * <p>
 * In most cases, location will also contain geographic information, conveying the
 * street, neighborhood, city, state, postal, and country values of the nearest geographic
 * location to the given coordinate data.
 * <p>
 * In all cases, location will return its {@link #getCreationDate creation} and 
 * {@link #getExpirationDate expiration} dates. The former can be used to determine
 * if the location represents a recent or a cached position; the latter should be
 * used to delete expired data as per the <u>Developer Terms of the Service</u>.
 */
public class Location {

   private final Long id;

   private final Date creationDate;
   private final Date expirationDate;
   private final Double longitude;
   private final Double latitude;
   private final Double accuracy;
   private final String street;
   private final String city;
   private final String neighborhood;
   private final String state;
   private final String postal;
   private final String countryCode;
   private final int code;
   private final String message;

   public Location(Long id,
                   Date creationDate,
                   Date expirationDate,
                   Double longitude,
                   Double latitude,
                   Double accuracy,
                   String street,
                   String neighborhood,
                   String city,
                   String state,
                   String postal,
                   String countryCode,
                   int code,
                   String message) {

      this.id = id;

      this.creationDate = creationDate;
      this.expirationDate = expirationDate;
      this.longitude = longitude;
      this.latitude = latitude;
      this.accuracy = accuracy;
      this.street = street;
      this.city = city;
      this.neighborhood = neighborhood;
      this.state = state;
      this.postal = postal;
      this.countryCode = countryCode;

      this.code = code;
      this.message = message;
   }

   public Location(Long id,
                   Date creationDate,
                   Date expirationDate,
                   int code,
                   String message) {
      this(id,
           creationDate,
           expirationDate,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           null,
           code,
           message);
   }

   /**
    * Get this location's unique identifier, which may be used to retrieve its
    * data without relying on external storage.
    * <p>
    * Note that location updates will not generally have ids.
    */
   public Long getId() {
      return id;
   }

   /**
    * Get this location's creation date, which indicates whether the location
    * represents a current or cached position.
    */
   public Date getCreationDate() {
      return creationDate;
   }

   /**
    * Get this location's expiration date, which indicates when this location
    * must be deleted.
    */
   public Date getExpirationDate() {
      return expirationDate;
   }

   /**
    * Get this location's longitude, in degrees, or null if none.
    */
   public Double getLongitude() {
      return longitude;
   }
   
   /**
    * Get this location's latitude, in degrees, or null if none.
    */
   public Double getLatitude() {
      return latitude;
   }

   /**
    * Get this location's uncertainty radius, in meters, or null if none.
    */
   public Double getAccuracy() {
      return accuracy;
   }

   /**
    * Get this location's street address, which may represent a house number, 
    * a street, or an intersection.
    */
   public String getStreet() {
      return street;
   }

   /** 
    * Get this location's neighborhood, if null if none.
    * Note that neighbhorhood data is only available for certain metro regions.
    */
   public String getNeighborhood() {
      return neighborhood;
   }

   /** 
    * Get this location's city, or null if none.
    */
   public String getCity() {
      return city;
   }

   /**
    * Get this location's state, or null if none.
    */
   public String getState() {
      return state;
   }

   /**
    * Get this location's postal code, or null if none.
    */
   public String getPostal() {
      return postal;
   }

   /**
    * Get this location's country code, or null if none.
    */
   public String getCountryCode() {
      return countryCode;
   }

   /**
    * Get this location's error code, which should be one of:
    * <ul>
    * <li><b>0</b> - Ok</li>
    * <li><b>100</b> - Position Failure</li>
    * <li><b>200</b> - Restricted</li>
    */
   public int getCode() {
      return code;
   }

   /**
    * Get this location's error message, if any.
    */
   public String getMessage() {
      return message;
   }

   public String toString() {
      if (message == null) {
         return "(" + longitude + ", " + latitude + ", " + accuracy + ")";
      } else {
         return message;
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



