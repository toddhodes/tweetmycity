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

import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.UserDiscoveryNotPermittedException;
import com.veriplace.client.VeriplaceOAuthException;
import com.veriplace.oauth.consumer.Token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal implementation of completing a Veriplace user discovery request.
 */
// package-private
class UserDiscoveryRequirement extends UserRequirement {

   private static final Log logger = LogFactory.getLog(UserDiscoveryRequirement.class);
   
   private final boolean immediate;
   
   public UserDiscoveryRequirement(Veriplace veriplace, boolean immediate) {
      super(veriplace);
      this.immediate = immediate;
   }
   
   public void complete(VeriplaceState state)
         throws ShouldRedirectException,
                UserDiscoveryException,
                UnexpectedException {

      if (alreadyHaveUser(state)) {
         return;
      }

      Token newAccessToken;
      try {
         newAccessToken = popAccessToken(state);
      }
      catch (VeriplaceOAuthException e) {
         throw new UserDiscoveryNotPermittedException(e);
      }
      if (newAccessToken == null) {
         if (immediate) {
            throw new UserDiscoveryNotPermittedException();
         }
         logger.info("No user or callback; sending user discovery redirect");
         String callback = veriplace.getCallbackUrl(state);
         String url = veriplace.getClient().getUserDiscoveryAPI().getRedirectURL(callback, null,
               (! state.isUserInteractionAllowed()));
         if (url == null) {
            logger.error("User rediscovery redirect failed; invalid consumer key or secret?");
            throw new UserDiscoveryException();
         }
         else {
            throw new ShouldRedirectException(url);
         }
      }
      else {
         // We've returned via callback and now have a user discovery token.  Now obtaining the user
         // just requires a single call with no further redirect.
         logger.info("Invoking user discovery API");
         User user = veriplace.getClient().getUserDiscoveryAPI().getUser(newAccessToken);
         if (user == null) {
            throw new IllegalStateException("Unexpected null result from getUser");
         }
         logger.info("Discovered user");

         storeUser(state, user);
      }
   }
}
