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
package com.veriplace.web.spring;

import com.veriplace.web.VeriplaceState;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An Interceptor that does not allow requests to proceed until the current Veriplace user has
 * been identified.
 */
public class UserDiscoveryInterceptor extends VeriplaceInterceptor {

   private String userAttributeName;
   private boolean interactionAllowed;

   public UserDiscoveryInterceptor() {
      interactionAllowed = true;
   }
   
   /**
    * See {@link #setUserAttributeName(String)}.
    */
   public String getUserAttributeName() {
      return userAttributeName;
   }

   /**
    * Specifies that when user discovery has been successfully completed on a request, the
    * {@link com.veriplace.client.User} object should be stored in the request as an attribute
    * with this name.
    */
   public void setUserAttributeName(String userAttributeName) {
      this.userAttributeName = userAttributeName;
   }

   /**
    * See {@link #setInteractionAllowed(boolean)}.
    */
   public boolean isInteractionAllowed() {
      return interactionAllowed;
   }
   
   /**
    * Specifies whether Veriplace can redirect to a login page or otherwise interact with the user when
    * we need to determine the current user.  This is true by default; if false, then user discovery
    * will fail unless the user is already logged in.
    */
   public void setInteractionAllowed(boolean interactionAllowed) {
      this.interactionAllowed = interactionAllowed;
   }
   
   protected boolean handleInternal(VeriplaceState state, HttpServletRequest request, HttpServletResponse response)
         throws Exception {

      state.setRequiresUser(true);
      state.setUserInteractionAllowed(interactionAllowed);
      if (state.completeAll()) {
         if ((userAttributeName != null) && !userAttributeName.equals("")) {
            request.setAttribute(userAttributeName, state.getUser());
         }
         return true;
      }
      return false;
   }
}
