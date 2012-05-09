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

import com.veriplace.client.SetLocationException;
import com.veriplace.client.SetLocationNotPermittedException;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.VeriplaceOAuthException;
import com.veriplace.oauth.consumer.Token;

/**
 * Internal implementation of obtaining permission from Veriplace to update a user's location.
 */
// package-private
class SetLocationPermissionRequirement extends Requirement {

   private final boolean canUseCache;
   
   public SetLocationPermissionRequirement(Veriplace veriplace, boolean canUseCache) {
      super(veriplace);
      this.canUseCache = canUseCache;
   }
   
   public void complete(VeriplaceState state)
         throws ShouldRedirectException,
                SetLocationException,
                UserDiscoveryException,
                UnexpectedException {
      
      if (state.getUser() == null) {
         throw new IllegalStateException();
      }
      
      if (state.hasSetLocationPermission()) {
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
         throw new SetLocationNotPermittedException(e);
      }
      if (newAccessToken != null) {
         state.setSetLocationPermissionToken(newAccessToken);
         return;
      }
      
      if (canUseCache) {
         Token cachedToken = veriplace.getSetLocationTokenStore().get(user);
         if (cachedToken != null) {
            state.setSetLocationPermissionToken(cachedToken);
            return;
         }
      }
      
      try {
         Token token = veriplace.getClient().getSetLocationAPI().getSetLocationAccessToken(user);
         // If that didn't throw an exception, then the token is non-null and valid.
         
         state.setSetLocationPermissionToken(token);
         // Don't store the token in the cache yet - wait till we've successfully used it.
      }
      catch (SetLocationException e) {
         // We don't currently have permission; redirect to the opt-in flow, if allowed.
         if (state.isUserInteractionAllowed()) {
            String callback = veriplace.getCallbackUrl(state);
            String url = veriplace.getClient().getSetLocationAPI().getRedirectURL(callback, user);
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

/*
** Local Variables:
**   c-basic-offset: 3
**   tab-width: 3
**   indent-tabs-mode: nil
** End:
**
** ex: set softtabstop=3 tabstop=3 expandtab cindent shiftwidth=3
*/
