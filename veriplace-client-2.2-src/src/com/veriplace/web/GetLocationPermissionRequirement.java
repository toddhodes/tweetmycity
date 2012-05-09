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

import com.veriplace.client.GetLocationException;
import com.veriplace.client.GetLocationNotPermittedException;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.VeriplaceOAuthException;
import com.veriplace.oauth.consumer.Token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal implementation of obtaining permission to locate a user.
 */
// package-private
class GetLocationPermissionRequirement extends Requirement {

   private static final Log logger = LogFactory.getLog(GetLocationPermissionRequirement.class);

   private final boolean canUseCache;
   
   public GetLocationPermissionRequirement(Veriplace veriplace, boolean canUseCache) {
      super(veriplace);
      this.canUseCache = canUseCache;
   }
   
   public void complete(VeriplaceState state) 
         throws ShouldRedirectException,
                ShouldRestartException,
                WaitingException,
                GetLocationException,
                UserDiscoveryException,
                UnexpectedException {
      
      if (state.getUser() == null) {
         throw new IllegalStateException();
      }

      if (state.hasGetLocationPermission()) {
         return;
      }
      
      if (state.getRequestId() != null) {
         // A request is already in progress.  This requirement still got called just because
         // it's always part of the flow, but we shouldn't try to get a new token.
         return;
      }
      
      User user = state.getUser();
      Token newAccessToken;
      try {
         newAccessToken = popAccessToken(state);
      }
      catch (VeriplaceOAuthException e) {
         throw new GetLocationNotPermittedException(e);
      }
      if (newAccessToken != null) { 
         state.setGetLocationPermissionToken(newAccessToken);
         veriplace.getGetLocationTokenStore().put(user, newAccessToken);
         return;
      }

      if (canUseCache) {
         Token cachedToken = veriplace.getGetLocationTokenStore().get(user);
         if (cachedToken != null) {
            state.setGetLocationPermissionToken(cachedToken);
            return;
         }
      }

      try {
         Token token = veriplace.getClient().getGetLocationAPI().getLocationAccessToken(user);
         // If that didn't throw an exception, then the token is non-null and valid.
      
         state.setGetLocationPermissionToken(token);
         // Don't store the token in the cache yet - wait till we've successfully used it.
      }
      catch (GetLocationNotPermittedException e) {
         // We don't currently have permission; redirect to the opt-in flow, if allowed.
         if (state.isUserInteractionAllowed()) {
            logger.info("No get location access token; sending get location redirect");
            String callback = veriplace.getCallbackUrl(state);
            String url = veriplace.getClient().getGetLocationAPI().getRedirectURL(callback, user);
            // Force a redirect; when we get a callback, it'll give us a token.
            throw new ShouldRedirectException(url);
         }
         else {
            // User interaction isn't allowed, so there's nothing we can do.
            throw e;
         }
      }
   }
}
