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
package com.veriplace.demo.persist;

import com.veriplace.client.User;
import com.veriplace.demo.model.DemoUser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintains the list of users known to this application. This simple
 * implementation just stores the data in memory.
 */
public class DemoUserManager {

   private Map<User, DemoUser> userMap = new HashMap<User, DemoUser>();
   private long lastId = 0;
   
   /**
    * Finds a user by the Veriplace user identifier.
    */
   public DemoUser getByVeriplaceUser(User user) {
      return userMap.get(user);
   }

   /**
    * Adds or updates user information.
    */
   public synchronized DemoUser save(DemoUser demoUser) {
      userMap.put(demoUser.getVeriplaceUser(), demoUser);
      return demoUser;
   }

   /**
    * Adds a Veriplace user to our local user list if necessary, or returns the
    * existing user object.
    */
   public DemoUser getOrCreateDemoUser(User user) {
      DemoUser demoUser = getByVeriplaceUser(user);
      if (demoUser == null) {
         demoUser = new DemoUser();
         demoUser.setVeriplaceUser(user);
         demoUser.setLocateCount(0);
         save(demoUser);
      }
      return demoUser;
   }
}
