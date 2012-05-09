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
import com.veriplace.client.util.GetLocationRequestManager;

import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal implementation of completing a Veriplace location request.
 */
class LocationRequirement implements Requirement {

   private static final Log logger = LogFactory.getLog(LocationRequirement.class);

   private UserRequirement userRequirement;
   private Location location;
   private Long requestId;

   public static final String LOCATION_ATTRIBUTE = "veriplace_location";
   public static final String REQUEST_ID_ATTRIBUTE = "veriplace_request_id";

	/**
	 * While most web browsers will happily wait minutes for a page to return
	 * content, the same is not true of the gateways between mobile browsers
	 * and the rest of the web. Verizon gateways, in particular, appear to
	 * give up after around 40 seconds, so we set our timeout a bit lower.
	 */
	 public static final long WAIT_TIMEOUT = 30000L;

   public LocationRequirement(UserRequirement userRequirement) {
      this.userRequirement = userRequirement;
   }

   public LocationRequirement() {
      this(new UserRequirement());
   }
   
   /**
    * If the location requirement for this request has been completed, returns the
    * {@link com.veriplace.client.Location} that is attached to the request; otherwise
    * returns null.  Don't use this method before the request has been processed by
    * {@link com.veriplace.web.VeriplaceState#completeAll()}.
    */
   public static Location getLocation(HttpServletRequest request) {
      return (Location) request.getAttribute(LOCATION_ATTRIBUTE);
   }

   protected static void setLocation(VeriplaceState state, Location location) {
      state.setAttribute(LOCATION_ATTRIBUTE, location);
   }
   
   public void reset(VeriplaceState state) {
      state.setAttribute(LOCATION_ATTRIBUTE, null);
      state.setAttribute(REQUEST_ID_ATTRIBUTE, null);
   }
   
   public boolean complete(VeriplaceState state) {
      // User discovery always has to happen first.
      if (! userRequirement.complete(state)) {
         return false;
      }
      
      state.addPersistentAttributeName(REQUEST_ID_ATTRIBUTE);
      
      if (getLocation(state.getRequest()) != null) {
         return true;
      }
      
      VeriplaceContext context = state.getContext();
      User user = UserRequirement.getUser(state.getRequest());

      Long requestId = state.getRequestParamLong(REQUEST_ID_ATTRIBUTE);
      if (requestId == null) {
         
         // First step is to get an access token, which may require another redirect/callback cycle.
         Token token = state.getAccessToken();
         if (token == null) {
            token = context.getGetLocationTokenStore().get(user);
            state.setAccessToken(token);
            if (token == null) {
               if (state.isCallbackError()) {
                  // A callback with a null token means that we did request a token, but that the request
                  // was denied or cancelled by the user.
                  state.setRequestStatus(RequestStatus.LocationError);
                  return false;
               }
               logger.info("No get location access token; sending get location redirect");
               String callback = state.getCallbackUrl();
               String url = state.getClient().getGetLocationAPI().getRedirectURL(callback, user);
               // Force a redirect; when we get a callback, it'll give us a token.
               state.setRequestStatus(RequestStatus.RequiresRedirect, url);
               return false;
            }
         }
         
         // Now that we have a token, we can issue a location request; this happens on a background thread.
         // We'll put up the "please wait" page and wait for the auto-refresh to call us back.
         GetLocationRequestManager lrm = context.getGetLocationRequestManager();
         requestId = lrm.submitRequest(lrm.newRequest(state.getClient(),
               user, token, state.getLocationMode()));

         logger.info("Sent location request; showing wait page");
         state.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
         String callback = state.getCallbackUrl();//state.getClient().getUserDiscoveryAPI().getRedirectURL(state.getCallbackUrl(), null);
         state.setRequestStatus(RequestStatus.Waiting, callback);

         return false;
      }
      
      // We've already gone through the steps above and issued a request.  Now we've been called abck
      // via the auto-refresh on the wait page.  We should block until the request is finished.
      logger.debug("Have request ID; blocking for completion");
      if (!context.getGetLocationRequestManager().waitForCompletion(requestId,WAIT_TIMEOUT)) {
					logger.info("Timed out waiting for location; showing wait page again");
					String callback = state.getCallbackUrl();
					state.setRequestStatus(RequestStatus.Waiting, callback);
					return false;
			}
      location = context.getGetLocationRequestManager().getResult(requestId);
      if (location == null) {
         // A null result means that the location request couldn't be completed because of a problem at
         // an earlier stage, e.g. the user token has expired; this implies that we may still be able to
         // get a location but must start the process over.
         logger.debug("Could not get location; invalid token");
         context.getGetLocationTokenStore().remove(user);
         userRequirement.reset(state);
         // Don't set any error condition or redirect, but still return false; this causes the VeriplaceState
         // to start over with the user requirement.
         return false;
      }
      setLocation(state, location);
      if (location.getCode() == 0) {
         logger.info("Obtained location");
         return true;
      }
      else {
         // A non-null location object with a non-zero error code means that the location request failed
         // in a non-negotiable way, e.g. the application is no longer authorized to get locations.  We
         // still store the location object in case anyone is interested in the error code, but we'll
         // abort the current request and redirect to an error page.
         logger.info("Error in location request, code = " + location.getCode());
         state.setRequestStatus(RequestStatus.LocationError);
         return false;
      }
   }

   public String[] getAttributeNames() {
      return new String[] {
         REQUEST_ID_ATTRIBUTE
      };
   }
}
