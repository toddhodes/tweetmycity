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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.veriplace.client.Client;
import com.veriplace.client.Location;
import com.veriplace.client.User;
import com.veriplace.oauth.consumer.Token;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Abstraction around making a location request and retrieving the result.
 */
public class GetLocationRequestManager
      extends AbstractRequestManager<Location> {

   private static final Log logger = LogFactory.getLog(GetLocationRequestManager.class);

   /**
    * Create a new location request to be submitted with {@link com.veriplace.client.util.AbstractRequestManager#submitRequest}.
    */
   public AbstractRequest<Location> newRequest(Client client, User user, Token accessToken,
         String mode) {
      
      return new LocationRequest(client, user, accessToken, mode);
   }
   
   /**
    * Inner class representing a location request.
    */
   protected static class LocationRequest extends AbstractRequest<Location> {

      private final String mode;

      public LocationRequest(Client client, User user, Token accessToken, String mode) {
         super(client, user, accessToken);
         this.mode = mode;
      }

      public Location call() {
         logger.debug("Calling getLocationAPI");
         logger.debug("  accessToken: " + accessToken.getToken());
         logger.debug("  user: " + user.getId());
         logger.debug("  mode: " + mode);
         Location location = client.getGetLocationAPI().getLocation(accessToken, user, mode);

         storeResult(location);

         if (location == null) {
            logger.debug("Did not obtain location (or error)");
         } else {
            logger.debug("Obtained location (or error)");
         }
         return location;
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
