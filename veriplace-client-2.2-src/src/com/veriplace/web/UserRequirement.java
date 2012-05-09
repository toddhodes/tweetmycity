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
package com.veriplace.web;

import com.veriplace.client.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for requirements that identify a user.
 */
// package-private
abstract class UserRequirement extends Requirement {

   private static final Log logger = LogFactory.getLog(UserRequirement.class);
   
   protected UserRequirement(Veriplace veriplace) {
      super(veriplace);
   }
   
   protected boolean alreadyHaveUser(VeriplaceState state) {
      User user = state.getUser();
      if (user == null) {
         Long userId = state.getRequestParamLong(VeriplaceState.USER_ID_CALLBACK_PARAM);
         if (userId != null) {
            logger.debug("Already have user ID");
            user = new User(userId);
            state.setUser(user);
         }
      }
      if (user != null) {
         state.setCallbackParameter(VeriplaceState.USER_ID_CALLBACK_PARAM, user.getId());
         return true;
      }
      return false;
   }
   
   protected void storeUser(VeriplaceState state, User user) {
      state.setUser(user);
      state.setCallbackParameter(VeriplaceState.USER_ID_CALLBACK_PARAM, user.getId());
   }
}
