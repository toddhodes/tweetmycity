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
import com.veriplace.client.Location;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.oauth.consumer.Token;

/**
 * Internal implementation of requesting a location by ID.  This is an immediate lookup
 * and could easily be done with the low-level API, but it's provided here for consistency.
 */
// package-private
class GetLocationByIdRequirement extends Requirement {

   private final long locationId;
   
   public GetLocationByIdRequirement(Veriplace veriplace, long locationId) {
      super(veriplace);
      this.locationId = locationId;
   }
   
   public void complete(VeriplaceState state) 
         throws GetLocationException,
                ShouldRestartException,
                UserDiscoveryException,
                UnexpectedException {
      
      if ((state.getUser() == null) || (! state.hasGetLocationPermission())) {
         throw new IllegalStateException();
      }
      if ((state.getLocation() != null) && (state.getLocation().getId() == locationId)) {
         return;
      }
      User user = state.getUser();
      Token token = state.getGetLocationPermissionToken();

      try {
         Location location = veriplace.getClient().getGetLocationAPI().getLocationById(token, user, locationId);
         if (location == null) {
            // If there was no exception, the result should not be null
            throw new IllegalStateException("Unexpected null result from getLocationById");
         }
         state.setLocation(location);
      }
      catch (GetLocationNotPermittedException e) {
         // If the access token was invalid, and *if* we acquired it from the token cache,
         // then we'll clear it from the cache and try starting over to get a new token.
         // However, we should only do this once; if it wasn't a cached token, just fail.
         if (veriplace.getGetLocationTokenStore().get(user) != null) {
            veriplace.getGetLocationTokenStore().remove(user);
            state.setGetLocationPermissionToken(null);
            throw new ShouldRestartException();
         }
         throw e;
      }
   }
}
