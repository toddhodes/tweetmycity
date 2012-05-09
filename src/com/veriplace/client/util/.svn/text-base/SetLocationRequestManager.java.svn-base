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
import com.veriplace.client.util.AbstractRequestManager.AbstractRequest;
import com.veriplace.client.util.GetLocationRequestManager.LocationRequest;

import java.util.concurrent.Callable;

/**
 * Abstraction around making a set-location request and retrieving the result.
 */
public class SetLocationRequestManager
      extends AbstractRequestManager<Location> {

   private static final Log logger = LogFactory.getLog(SetLocationRequestManager.class);

   /**
    * Create a new location request to be submitted with {@link com.veriplace.client.util.AbstractRequestManager#submitRequest}.
    */
   public AbstractRequest<Location> newRequest(Client client, User user, Token accessToken,
         SetLocationParameters params) {
      
      return new SetLocationRequest(client, user, accessToken, params);
   }
   
   /**
    * Inner class representing a set-location request.
    */
   protected static class SetLocationRequest extends AbstractRequest<Location> {

      private final SetLocationParameters parameters;

      public SetLocationRequest(Client client, User user, Token accessToken,
            SetLocationParameters parameters) {
         super(client, user, accessToken);
         this.parameters = parameters;
      }

      public Location call() {
         logger.debug("Calling setLocationAPI");
         logger.debug("  accessToken: " + accessToken.getToken());
         logger.debug("  user: " + user.getId());
         Location location;
         if (parameters.getGeocodingQueryString() != null) {
            logger.debug("  query: " + parameters.getGeocodingQueryString());
            location = client.getSetLocationAPI().setLocation(accessToken, user,
                  parameters.getGeocodingQueryString());
         }
         else {
            logger.debug("  longitude/latitude: " + parameters.getLongitude()
                  + "/" + parameters.getLatitude());
            location = client.getSetLocationAPI().setLocation(accessToken, user,
                  String.valueOf(parameters.getLongitude()),
                  String.valueOf(parameters.getLatitude()),
                  (parameters.getAccuracy() == null ? null : String.valueOf(parameters.getAccuracy())));
         }
         
         storeResult(location);

         if (location == null) {
            logger.debug("Failed to set location");
         } else {
            logger.debug("Set location succeeded");
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
