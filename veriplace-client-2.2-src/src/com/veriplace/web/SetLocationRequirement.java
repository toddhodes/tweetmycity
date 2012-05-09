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

import com.veriplace.client.Location;
import com.veriplace.client.SetLocationException;
import com.veriplace.client.SetLocationNotPermittedException;
import com.veriplace.client.SetLocationParameters;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.VeriplaceException;
import com.veriplace.client.util.ResultWrapper;
import com.veriplace.client.util.SetLocationRequestManager;
import com.veriplace.oauth.consumer.Token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal implementation of updating a user's location, using a background request.
 */
// package-private
class SetLocationRequirement extends Requirement {

   private static final Log logger = LogFactory.getLog(SetLocationRequirement.class);

   private SetLocationParameters parameters;
   
   public SetLocationRequirement(Veriplace veriplace, SetLocationParameters parameters) {
      super(veriplace);
      this.parameters = parameters;
   }
   
   public void complete(VeriplaceState state)
         throws SetLocationException,
                WaitingException,
                ShouldRestartException,
                UnexpectedException {

      if (state.getUser() == null) {
         throw new IllegalStateException();
      }
      if (state.isAsynchronousRequestAllowed()) {
         completeAsynchronous(state);
      }
      else {
         completeSynchronous(state);
      }
   }

   protected void completeSynchronous(VeriplaceState state)
         throws SetLocationException,
                ShouldRestartException,
                UnexpectedException {

      User user = state.getUser();
      Token token = state.getSetLocationPermissionToken();
      Location updatedLocation;
      try {
         updatedLocation = veriplace.getClient().getSetLocationAPI().setLocation(token, user, parameters);
      }
      catch (SetLocationNotPermittedException e) {
         // If the access token was invalid, and *if* we acquired it from the token cache,
         // then we'll clear it from the cache and try starting over to get a new token.
         // However, we should only do this once; if it wasn't a cached token, just fail.
         if (veriplace.getSetLocationTokenStore().get(user) != null) {
            veriplace.getSetLocationTokenStore().remove(user);
            state.setSetLocationPermissionToken(null);
            throw new ShouldRestartException();
         }
         throw e;
      }
      veriplace.getSetLocationTokenStore().put(user, token);
      logger.debug("new location: " + updatedLocation);
      state.setLocation(updatedLocation);
   }
   
   protected void completeAsynchronous(VeriplaceState state)
         throws SetLocationException,
                WaitingException,
                ShouldRestartException,
                UnexpectedException {
      
      User user = state.getUser();
      
      Long requestId = popRequestId(state);
      if (requestId == null) {
         if (! state.hasSetLocationPermission()) {
            throw new IllegalStateException();
         }

         // Perform location update on a background thread.
         // We'll put up the "please wait" page and wait for the auto-refresh to call us back.
         SetLocationRequestManager lrm = veriplace.getSetLocationRequestManager();
         Token token = state.getSetLocationPermissionToken();
         requestId = lrm.submitRequest(lrm.newRequest(veriplace.getClient(), 
                                                      user, token, parameters));
         pushRequestId(state, requestId);

         logger.info("Sent set-location request; showing wait page");
         String callback = veriplace.getCallbackUrl(state);
         /*
         callbackUrl = veriplace.getClient().getSetLocationAPI().getRedirectURL(callbackUrl,
               state.getUser());
         */
         throw new WaitingException(callback);
      }
      
      // If the request ID is present, then we've already issued a request and now we've been
      // called back from the wait page.  We should block until the request is finished.
      logger.debug("Have request ID; blocking for completion");
      veriplace.getSetLocationRequestManager().waitForCompletion(requestId);

      // The request is finished; get the result.
      Location updatedLocation;
      try {
         ResultWrapper<Location> result = 
            veriplace.getSetLocationRequestManager().getResultAndToken(requestId);
         // If the request returned an error, this will throw the corresponding exception.
         // Otherwise, it succeeded, so we can now cache the access token for future use.
         veriplace.getSetLocationTokenStore().put(user, result.getToken());
         updatedLocation = result.getResult();
      }
      catch (SetLocationNotPermittedException e) {
         // If the access token was invalid, and *if* we acquired it from the token cache,
         // then we'll clear it from the cache and try starting over to get a new token.
         // However, we should only do this once; if it wasn't a cached token, just fail.
         if (veriplace.getSetLocationTokenStore().get(user) != null) {
            veriplace.getSetLocationTokenStore().remove(user);
            state.setSetLocationPermissionToken(null);
            throw new ShouldRestartException();
         }
         throw e;
      }
      catch (SetLocationException e) {
         throw e;
      }
      catch (UnexpectedException e) {
         throw e;
      }
      catch (VeriplaceException e) {
         throw new UnexpectedException(e);
      }
      logger.debug("new location: " + updatedLocation);
      state.setLocation(updatedLocation);
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
