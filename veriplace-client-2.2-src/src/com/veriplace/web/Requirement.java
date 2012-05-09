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

import com.veriplace.client.RequestDeniedException;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.VeriplaceException;
import com.veriplace.oauth.consumer.Token;

/**
 * A logical requirement that may require multiple request cycles to complete.  It is created
 * by a {@link Veriplace} instance and operates on a {@link VeriplaceState}.
 */
// package-private
abstract class Requirement {

   protected final Veriplace veriplace;
   
   protected Requirement(Veriplace veriplace) {
      this.veriplace = veriplace;
   }
   
   /**
    * Attempts to complete the requirement, and throws an exception if not successful.
    * Whether successful or not, it will update the HttpServletRequest's attributes to
    * reflect the state of the requirement.
    * @throws ShouldRedirectException  if the flow must be interrupted to redirect the end user
    * @throws ShouldRestartException  if the requirement flow should be restarted from the top
    * @throws WaitingException  if the flow should be diverted to a "please wait" page
    * @throws RequestDeniedException  if a Veriplace request was denied for a well-defined reason
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public abstract void complete(VeriplaceState state)
         throws ShouldRedirectException,
                ShouldRestartException,
                WaitingException,
                RequestDeniedException,
                UnexpectedException;
   
   /**
    * Claims the access token that was returned in the last callback, and clears it from the state.
    * @return  an access token (will not be null)
    * @throws VeriplaceException  if the access token was invalid or missing
    */
   protected Token popAccessToken(VeriplaceState state)
         throws UnexpectedException {
      if (state.isCallback()) {
         if (state.getAccessToken() == null) {
            // If the access token is null, it means that we received a callback with a request token
            // but that we weren't able to get the corresponding access token (it had expired, etc.).
            // The error from the access token query has been saved in the state.
            VeriplaceException e = state.getLastErrorException();
            if (e instanceof UnexpectedException) {
               throw (UnexpectedException) e;
            }
            else {
               throw new UnexpectedException(e);
            }
         }
         else {
            // We've received a new access token and no other requirement has gobbled it up yet,
            // so we can assume it's for this one.
            Token newToken = state.getAccessToken();
            state.setAccessToken(null);
            state.setCallback(false);
            return newToken;
         }
      }
      return null;
   }
   
   /**
    * Retrieve the ID of a pending background request, and clear the stored ID.
    */
   protected Long popRequestId(VeriplaceState state) {
      // Is the ID present in the state object?
      Long requestId = state.getRequestId();
      if (requestId != null) {
         // Clear the stored value.
         state.setRequestId(null);
      }
      return requestId;
   }
   
   /**
    * Store the ID of a pending background request so it will be passed along in the next callback.
    */
   protected void pushRequestId(VeriplaceState state, Long requestId) {
      state.setRequestId(requestId);
      state.setCallbackParameter(VeriplaceState.REQUEST_ID_CALLBACK_PARAM, requestId);
   }
}
