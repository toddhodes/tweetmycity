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
import com.veriplace.client.User;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.client.util.UserDiscoveryParameters;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Internal implementation of completing a Veriplace user discovery request.
 */
class UserRequirement implements Requirement {

   private static final Log logger = LogFactory.getLog(UserRequirement.class);
   
   protected static final String USER_ATTRIBUTE = "veriplace_user";
   protected static final String USER_ID_ATTRIBUTE = "veriplace_userid";
   
   private boolean interactionAllowed;
   
   public UserRequirement() {
      interactionAllowed = true;
   }
   
   /**
    * If the user discovery requirement for this request has been completed, returns the
    * {@link com.veriplace.client.User} that is attached to the request; otherwise
    * returns null.  Don't use this method before the request has been processed by
    * {@link com.veriplace.web.VeriplaceState#completeAll()}.
    */
   public static User getUser(HttpServletRequest request) {
      return (User) request.getAttribute(USER_ATTRIBUTE);
   }
   
   protected static void setUser(VeriplaceState state, User user) {
      state.getRequest().setAttribute(USER_ATTRIBUTE, user);
      if (user == null) {
         state.getRequest().removeAttribute(USER_ID_ATTRIBUTE);
      }
      else {
         state.getRequest().setAttribute(USER_ID_ATTRIBUTE, user.getId());
      }
   }
   
   public boolean isInteractionAllowed() {
      return interactionAllowed;
   }
   
   public void setInteractionAllowed(boolean interactionAllowed) {
      this.interactionAllowed = interactionAllowed;
   }
   
   public void reset(VeriplaceState state) {
      if (getUser(state.getRequest()) != null) {
         state.getContext().getGetLocationTokenStore().remove(getUser(state.getRequest()));
         setUser(state, null);
      }
   }
   
   public boolean complete(VeriplaceState state) {
      VeriplaceContext context = state.getContext();
      User user = getUser(state.getRequest());
      state.addPersistentAttributeName(USER_ID_ATTRIBUTE);

      Token token = state.getAccessToken();
      if (user != null) {
         context.getGetLocationTokenStore().put(user, token);
         return true;
      }

      // Did we already get the user in a previous request?  If so, the last callback should've
      // included a user ID.
      if (user == null) {
         Long userId = state.getRequestParamLong(USER_ID_ATTRIBUTE);
         if (userId != null) {
            logger.debug("Already have user ID");
            setUser(state, new User(userId));
            return true;
         }
      }

      // The first step is to request a user discovery token; this requires a redirect and callback.
      if (token == null) {
         if (state.isCallbackError()) {
            // A callback with a null token means that we did request a token, but that the request
            // was denied or cancelled by the user.
            state.setRequestStatus(RequestStatus.UserDiscoveryError);
            return false;
         }
         logger.info("No user or callback; sending user discovery redirect");
         String callback = state.getCallbackUrl();
         boolean immediate = !interactionAllowed;
         String url = state.getClient().getUserDiscoveryAPI().getRedirectURL(callback, null,
               immediate);
         if (url == null) {
            logger.error("User rediscovery redirect failed; invalid consumer key or secret?");
            state.setRequestStatus(RequestStatus.UserDiscoveryError);
         }
         else {
            state.setRequestStatus(RequestStatus.RequiresRedirect, url);
         }
         return false;
      }
      
      // We've returned via callback and now have a user discovery token.  Now obtaining the user
      // just requires a single call with no further redirect.
      logger.info("Invoking user discovery API");
      user = state.getClient().getUserDiscoveryAPI().getUser(token);
      token = null;
      state.setAccessToken(null);
      if (user == null) {
         logger.info("User discovery failed");
         state.setRequestStatus(RequestStatus.UserDiscoveryError);
         return false;
      }
      else {
         logger.info("Discovered user");
         setUser(state, user);
         return true;
      }
   }
   
   public String[] getAttributeNames() {
      return new String[] {
            USER_ID_ATTRIBUTE
      };
   }
   
   public boolean findUser(VeriplaceState state, UserDiscoveryParameters params, boolean forceNewSearch) {
      if (! forceNewSearch) {
         // If we already found and stored a user ID in the same request cycle, just use that.
         Long userId = state.getRequestParamLong(USER_ID_ATTRIBUTE);
         if (userId != null) {
            logger.debug("Already have user ID");
            setUser(state, new User(userId));
            return true;
         }
      }
      
      Client client = state.getClient();
      User user = null;
      if (params.getPhone() != null) {
         logger.debug("Searching for user by phone: " + params.getPhone());
         user = client.getUserDiscoveryAPI().getUserByMobileNumber(params.getPhone());
      }
      else if (params.getEmail() != null) {
         logger.debug("Searching for user by email: " + params.getEmail());
         user = client.getUserDiscoveryAPI().getUserByEmail(params.getEmail());
      }
      else if (params.getOpenId() != null) {
         logger.debug("Searching for user by OpenID: " + params.getOpenId());
         user = client.getUserDiscoveryAPI().getUserByOpenId(params.getOpenId());
      }
      if (user == null) {
         logger.info("User search failed");
         return false;
      }
      setUser(state, user);
      logger.info("User search succeeded");
      return true;
   }
}
