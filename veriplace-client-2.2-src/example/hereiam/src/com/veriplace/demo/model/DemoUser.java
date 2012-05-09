/* Copyright 2010 WaveMarket, Inc.
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
package com.veriplace.demo.model;

import com.veriplace.client.User;
import com.veriplace.oauth.consumer.Token;

import java.util.Date;

/**
 * Stores information about a user who has made at least one location request.
 * The application never has any personally identifying information about the
 * user, only an opaque Veriplace {@link User} ID, and an access token
 * representing the permission we acquired to locate the user.
 */
public class DemoUser {

   private User veriplaceUser;
   private Token accessToken;
   private Date firstLocateTime;
   private int locateCount;

   /**
    * Returns the Veriplace user identifier, given to us by a
    * {@link com.veriplace.client.UserDiscoveryAPI} call.
    */
   public User getVeriplaceUser() {
      return veriplaceUser;
   }
   
   public void setVeriplaceUser(User user) {
      veriplaceUser = user;
   }

   /**
    * Returns the Veriplace access token that we previously acquired for
    * this user.
    */
   public Token getAccessToken() {
      return accessToken;
   }

   public void setAccessToken(Token accessToken) {
      this.accessToken = accessToken;
   }

   /**
    * Returns the date/time that the user made the first location request.
    */
   public Date getFirstLocateTime() {
      return firstLocateTime;
   }

   public void setFirstLocateTime(Date firstLocateTime) {
      this.firstLocateTime = firstLocateTime;
   }

   /**
    * Returns the number of locations the user has obtained.
    */
   public int getLocateCount() {
      return locateCount;
   }

   public void setLocateCount(int locateCount) {
      this.locateCount = locateCount;
   }
}
