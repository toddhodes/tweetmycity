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
 * Encapsulates the various ways in which you can attempt to find a user.
 */
public class UserDiscoveryParameters {

   private String phone;
   private String email;
   private String openId;

   public UserDiscoveryParameters() {
   }
   
   public String getEmail() {
      return email;
   }
   
   public void setEmail(String email) {
      this.email = email;
   }
   
   public String getOpenId() {
      return openId;
   }
   
   public void setOpenId(String openId) {
      this.openId = openId;
   }
   
   public String getPhone() {
      return phone;
   }

   public void setPhone(String phone) {
      this.phone = phone;
   }
   
   public boolean isSpecified() {
      return (email != null) || (phone != null) || (openId != null);
   }
}
