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
package com.veriplace.web.servlet.tags;

import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryParameters;

public class RequireUserTag extends AbstractRequireTag {

   protected String phone = null;
   protected String email = null;
   protected String openId = null;
   
   @Override
   protected boolean handleTagInternal() throws Exception {
      super.handleTagInternal();

      UserDiscoveryParameters params = null;
      if (phone != null) {
         params = new UserDiscoveryParameters();
         params.setPhone(phone);
      } else if (email != null) {
         params = new UserDiscoveryParameters();
         params.setEmail(email);
      } else if (openId != null) {
         params = new UserDiscoveryParameters();
         params.setOpenId(openId);
      }

      if (params != null) {
         veriplaceState.requireUser(params);
      } else {
         veriplaceState.requireUser();
      }
      return true;
   }

   public void setPhone(String phone) {
      this.phone = phone;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public void setOpenId(String openId) {
      this.openId = openId;
   }
   
   protected Object getResultObject() {
      return veriplaceState.getUser();
   }

   public static class ExtraInfo extends AbstractRequireTag.ExtraInfo {
      
      protected Class getObjectClass() {
         return User.class;
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
