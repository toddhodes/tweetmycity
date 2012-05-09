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

import java.util.Date;

/**
 * Representation of the location of a Veriplace user.
 * <p>
 * Location should always contain coordinate data, represented
 * as a {@link #getLongitude longitude} and a {@link #getLatitude latitude} values,
 * measured in degrees. Coordinate data will also contain an {@link #getUncertainty uncertainty radius}
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
   private final Double uncertainty;
   private final String street;
   private final String city;
   private final String neighborhood;
   private final String state;
   private final String postal;
   private final String countryCode;

   public Location(Long id,
                   Date creationDate,
                   Date expirationDate,
                   Double longitude,
                   Double latitude,
                   Double uncertainty,
                   String street,
                   String neighborhood,
                   String city,
                   String state,
                   String postal,
                   String countryCode) {

      this.id = id;

      this.creationDate = creationDate;
      this.expirationDate = expirationDate;
      this.longitude = longitude;
      this.latitude = latitude;
      this.uncertainty = uncertainty;
      this.street = street;
      this.city = city;
      this.neighborhood = neighborhood;
      this.state = state;
      this.postal = postal;
      this.countryCode = countryCode;
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
    * @since 2.0
    */
   public Double getUncertainty() {
      return uncertainty;
   }

   /**
    * @deprecated  Obsolete name for {@link #getUncertainty()}.
    */
   @Deprecated
   public Double getAccuracy() {
      return uncertainty;
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
    * Reconstruct this location as a single address line.
    */
   public String getAddressLine() {
      StringBuilder address = new StringBuilder();
      if (street != null) {
         address.append(street);
      } 
      if (city != null) {
         if (address.length() > 0) {
            address.append(", ");
         }
         address.append(city);
      }
      if (state != null) {
         if (address.length() > 0) {
            address.append(", ");
         }
         address.append(state);
      }
      if (postal != null) {
         if (address.length() > 0) {
            address.append(" ");
         }
         address.append(postal);
      }
      return address.toString();
   }

   public String toString() {
      return "(" + longitude + ", " + latitude + ", " + uncertainty + ")";
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
