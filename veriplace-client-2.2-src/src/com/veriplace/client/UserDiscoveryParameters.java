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
 * Encapsulates the various ways in which you can attempt to find a user
 * with {@link UserDiscoveryAPI#getUserByParameters(UserDiscoveryParameters)}.
 * You may specify one of the following: mobile number, email address, or
 * OpenID identifier.
 */
public class UserDiscoveryParameters {

   private String phone;
   private String email;
   private String openId;

   public UserDiscoveryParameters() {
   }
   
   /**
    * Convenience method for creating a new UserDiscoveryParameters object
    * with a mobile number.
    */
   public static UserDiscoveryParameters byPhone(String phone) {
      UserDiscoveryParameters udp = new UserDiscoveryParameters();
      udp.setPhone(phone);
      return udp;
   }
   
   /**
    * Convenience method for creating a new UserDiscoveryParameters object
    * with an email address.
    */
   public static UserDiscoveryParameters byEmail(String email) {
      UserDiscoveryParameters udp = new UserDiscoveryParameters();
      udp.setEmail(email);
      return udp;
   }
   
   /**
    * Convenience method for creating a new UserDiscoveryParameters object
    * with an OpenID identifier.
    */
   public static UserDiscoveryParameters byOpenId(String openId) {
      UserDiscoveryParameters udp = new UserDiscoveryParameters();
      udp.setOpenId(openId);
      return udp;
   }
   
   public String getEmail() {
      return email;
   }
   
   /**
    * Specifies searching by email address.
    * @throws IllegalArgumentException  if you have already specified a
    *   different search property
    */
   public void setEmail(String email) {
      if ((email != null) && email.equals("")) {
         email = null;
      }
      if ((email != null) && ((this.phone != null) || (this.openId != null))) {
         throw new IllegalArgumentException();
      }
      this.email = email;
   }
   
   public String getOpenId() {
      return openId;
   }
   
   /**
    * Specifies searching by OpenID identifier.
    * @throws IllegalArgumentException  if you have already specified a
    *   different search property
    */
   public void setOpenId(String openId) {
      if ((openId != null) && openId.equals("")) {
         openId = null;
      }
      if ((openId != null) && ((this.phone != null) || (this.email != null))) {
         throw new IllegalArgumentException();
      }
      this.openId = openId;
   }
   
   public String getPhone() {
      return phone;
   }

   /**
    * Specifies searching by mobile number.
    * @throws IllegalArgumentException  if you have already specified a
    *   different search property
    */
   public void setPhone(String phone) {
      if ((phone != null) && phone.equals("")) {
         phone = null;
      }
      if ((phone != null) && ((this.email != null) || (this.openId != null))) {
         throw new IllegalArgumentException();
      }
      this.phone = phone;
   }
   
   public boolean isSpecified() {
      return (email != null) || (phone != null) || (openId != null);
   }
   
   @Override
   public boolean equals(Object other) {
      if (! (other instanceof UserDiscoveryParameters)) {
         return false;
      }
      UserDiscoveryParameters udp = (UserDiscoveryParameters) other;
      return ((phone != null) && (udp.phone != null) && phone.equals(udp.phone))
            || ((email != null) && (udp.email != null) && email.equals(udp.email))
            || ((openId != null) && (udp.openId != null) && openId.equals(udp.openId))
            || (! isSpecified() && !udp.isSpecified());
   }
   
   @Override
   public int hashCode() {
      if (phone != null) {
         return phone.hashCode();
      }
      if (email != null) {
         return email.hashCode();
      }
      if (openId != null) {
         return openId.hashCode();
      }
      return 0;
   }

   @Override
   public String toString() {
      if (phone != null) {
	 return phone;
      }
      if (email != null) {
	 return email;
      }
      if (openId != null) {
	 return openId;
      }
      return "";
   }
}
