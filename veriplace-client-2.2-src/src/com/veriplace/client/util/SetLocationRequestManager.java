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
package com.veriplace.client.util;

import com.veriplace.client.Client;
import com.veriplace.client.Location;
import com.veriplace.client.SetLocationParameters;
import com.veriplace.client.User;
import com.veriplace.client.VeriplaceException;
import com.veriplace.oauth.consumer.Token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

      protected Location call()
            throws VeriplaceException {
         logger.debug("Calling setLocationAPI");
         logger.debug("  accessToken: " + accessToken.getToken());
         logger.debug("  user: " + user.getId());
         if (parameters.getAddress() != null) {
            logger.debug("  query: " + parameters.getAddress());
         }
         else {
            logger.debug("  longitude/latitude: " + parameters.getLongitude()
                  + "/" + parameters.getLatitude());
         }
         Location location = client.getSetLocationAPI().setLocation(accessToken, user, parameters);
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
