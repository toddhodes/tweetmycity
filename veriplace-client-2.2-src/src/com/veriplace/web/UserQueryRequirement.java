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

import com.veriplace.client.Client;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.UserDiscoveryParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal implementation of a Veriplace user identity query.
 */
// package-private
class UserQueryRequirement extends UserRequirement {

   private static final Log logger = LogFactory.getLog(UserQueryRequirement.class);

   protected UserDiscoveryParameters parameters;
   
   public UserQueryRequirement(Veriplace veriplace, UserDiscoveryParameters parameters) {
      super(veriplace);
      this.parameters = parameters;
   }
   
   public void complete(VeriplaceState state)
         throws ShouldRedirectException,
                UserDiscoveryException,
                UnexpectedException {

      if (alreadyHaveUser(state)) {
         return;
      }

      Client client = veriplace.getClient();
      User user = client.getUserDiscoveryAPI().getUserByParameters(parameters);
      if (user == null) {
         throw new IllegalStateException("Unexpected null result from UserDiscoveryAPI method");
      }
      logger.info("User search succeeded");
      storeUser(state, user);
   }
}
