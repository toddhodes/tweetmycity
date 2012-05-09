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
package com.veriplace.web;

import com.veriplace.client.Client;
import com.veriplace.client.Location;
import com.veriplace.client.User;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.client.util.SetLocationParameters;
import com.veriplace.client.util.SetLocationRequestManager;

import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal implementation of obtaining permission from Veriplace to update a user's location,
 * and then (if you have called {@link com.veriplace.web.VeriplaceState#setUserLocation(SetLocationParameters)})
 * updating it.
 */
class SetLocationRequirement implements Requirement {

   private static final Log logger = LogFactory.getLog(SetLocationRequirement.class);

   private UserRequirement userRequirement;
   private Location location;
   private Long requestId;
   private Token accessToken;
   
   private static final String REQUEST_ID_ATTRIBUTE = "veriplace_set_reqid";
   private static final String NEW_LOCATION_ATTRIBUTE = "veriplace_new_location";
   
   public SetLocationRequirement(UserRequirement userRequirement) {
      this.userRequirement = userRequirement;
   }

   public SetLocationRequirement() {
      this(new UserRequirement());
   }
   
   public Token getAccessToken() {
      return accessToken;
   }
   
   public void startSetLocationRequest(VeriplaceState state, SetLocationParameters params) {
      state.setRequestStatus(RequestStatus.Starting);
      if (requestId != null) {
         return;
      }
      if (accessToken == null) {
         throw new IllegalStateException("Attempted to set location before permission was granted");
      }
      VeriplaceContext context = state.getContext();
      SetLocationRequestManager lrm = context.getSetLocationRequestManager();
      long id = lrm.submitRequest(lrm.newRequest(state.getClient(), state.getUser(),
            accessToken, params));
      requestId = id;

      logger.info("Sent set-location request; showing wait page");
      state.setAttribute(REQUEST_ID_ATTRIBUTE, id);
      String callback = state.getClient().getSetLocationAPI().getRedirectURL(state.getCallbackUrl(),
            state.getUser());
      state.setRequestStatus(RequestStatus.Waiting, callback);
   }
   
   public void reset(VeriplaceState state) {
      state.setAttribute(REQUEST_ID_ATTRIBUTE, null);
   }
   
   public boolean complete(VeriplaceState state) {
      // User discovery always has to happen first.
      if (! userRequirement.complete(state)) {
         return false;
      }
      
      if (state.isCallbackError()) {
         state.setRequestStatus(RequestStatus.SetLocationError);
         return false;
      }
      
      VeriplaceContext context = state.getContext();
      User user = UserRequirement.getUser(state.getRequest());
      accessToken = state.getAccessToken();
      requestId = state.getRequestParamLong(REQUEST_ID_ATTRIBUTE);

      if (requestId == null) {
         
         if (accessToken == null) {
            accessToken = state.getContext().getSetLocationTokenStore().get(state.getUser());
            if (accessToken == null) {
               logger.info("Need an access token to set location; sending redirect");
               String callback = state.getCallbackUrl();
               String url = state.getClient().getSetLocationAPI().getRedirectURL(callback, user);
               state.setRequestStatus(RequestStatus.RequiresRedirect, url);
               return false;
            }
            else {
               logger.debug("Got token " + accessToken.getToken() + " for user " + state.getUser().getId());
            }
         }
         else {
            logger.debug("Storing token " + accessToken.getToken() + " for user " + state.getUser().getId());
            state.getContext().getSetLocationTokenStore().put(state.getUser(), accessToken);
         }
         logger.info("Obtained permission to set location");

      }
      else {
         // We get to this point if we have actually tried to set the location, and are returning from
         // a callback from the "please wait" page.  This will only happen if you actually tried to
         // change the location, rather than just asking for permission to change it.
         SetLocationRequestManager lrm = context.getSetLocationRequestManager();
         lrm.waitForCompletion(requestId);
         Location result = lrm.getResult(requestId);
         logger.debug("new location: " + result);
         LocationRequirement.setLocation(state, result);
      }
      
      return true;
   }

   public String[] getAttributeNames() {
      return new String[] {
         REQUEST_ID_ATTRIBUTE
      };
   }
}
