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
import com.veriplace.client.PositionFailureException;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.VeriplaceException;
import com.veriplace.client.util.GetLocationRequestManager;
import com.veriplace.client.util.ResultWrapper;
import com.veriplace.oauth.consumer.Token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal implementation of completing a Veriplace location request.
 */
// package-private
class GetLocationRequirement extends Requirement {

   private static final Log logger = LogFactory.getLog(GetLocationRequirement.class);

   /**
    * While most web browsers will happily wait minutes for a page to return
    * content, the same is not true of the gateways between mobile browsers
    * and the rest of the web. Verizon gateways, in particular, appear to
    * give up after around 40 seconds, so we set our timeout a bit lower.
    */
   public static final long WAIT_TIMEOUT = 30000L;

   public GetLocationRequirement(Veriplace veriplace) {
      super(veriplace);
   }
   
   public void complete(VeriplaceState state) 
      throws ShouldRedirectException,
             ShouldRestartException,
             WaitingException,
             GetLocationException,
             UnexpectedException {
      
      if (state.getUser() == null) {
         throw new IllegalStateException();
      }
      if (state.getLocation() != null) {
         return;
      }
      if (state.isAsynchronousRequestAllowed()) {
         completeAsynchronous(state);
      }
      else {
         completeSynchronous(state);
      }
   }

   protected void completeSynchronous(VeriplaceState state)
      throws ShouldRestartException,
             GetLocationException,
             UnexpectedException {
      
      User user = state.getUser();
      Token token = state.getGetLocationPermissionToken();
      Location location;
      try {
         location = veriplace.getClient().getGetLocationAPI().getLocation(token,
               user, state.getLocationMode());
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
      if (location == null) {
         // If there was no exception, the result should not be null
         throw new IllegalStateException("Unexpected null result from GetLocationRequestManager");
      }
      veriplace.getGetLocationTokenStore().put(user, token);
      logger.info("Obtained location");
      state.setLocation(location);
   }
   
   protected void completeAsynchronous(VeriplaceState state) 
      throws ShouldRedirectException,
             ShouldRestartException,
             WaitingException,
             GetLocationException,
             UnexpectedException {
      
      User user = state.getUser();

      Long requestId = popRequestId(state);
      if (requestId == null) {
         if (! state.hasGetLocationPermission()) {
            throw new IllegalStateException();
         }
         // Issue a location request on a background thread.
         // We'll put up the "please wait" page and wait for the auto-refresh to call us back.
         GetLocationRequestManager lrm = veriplace.getGetLocationRequestManager();
         Token token = state.getGetLocationPermissionToken();
         requestId = lrm.submitRequest(lrm.newRequest(veriplace.getClient(),
                                                      user, token, state.getLocationMode()));
         pushRequestId(state, requestId);
         
         logger.info("Sent location request; showing wait page");
         String callback = veriplace.getCallbackUrl(state);
         throw new WaitingException(callback);
      }
      
      // If the request ID is present, then we've already issued a request and now we've been
      // called back from the wait page.  We should block until the request is finished, or
      // until it's time to refresh the wait page.
      logger.debug("Have request ID; blocking for completion");
      if (! veriplace.getGetLocationRequestManager().waitForCompletion(requestId, WAIT_TIMEOUT)) {
         logger.info("Timed out waiting for location; showing wait page again");
         pushRequestId(state, requestId);
         String callback = veriplace.getCallbackUrl(state);
         throw new WaitingException(callback);
      }
      
      // The request is finished; get the result.
      Location location;
      try {
         ResultWrapper<Location> result =
            veriplace.getGetLocationRequestManager().getResultAndToken(requestId);
         // If the request returned an error, this will throw the corresponding exception.
         // Otherwise, it succeeded, so we can now cache the access token for future use.
         veriplace.getGetLocationTokenStore().put(user, result.getToken());
         location = result.getResult();
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
      catch (PositionFailureException e) {
         // In some cases, the position failure will contain a cached "last known" location
         // This will be saved in the state's lastErrorException one level up.
         throw e;
      }
      catch (GetLocationException e) {
         throw e;
      }
      catch (UnexpectedException e) {
         throw e;
      }
      catch (VeriplaceException e) {
         throw new UnexpectedException(e);
      }
      if (location == null) {
         // If there was no exception, the result should not be null
         throw new IllegalStateException("Unexpected null result from GetLocationRequestManager");
      }
      logger.info("Obtained location");
      state.setLocation(location);
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
