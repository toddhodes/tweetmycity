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

public abstract class AbstractUserSupportingRequireTag extends AbstractRequireTag {

   protected User user = null;
   protected Long userId = null;

   @Override
   protected boolean handleTagInternal() throws Exception {
      super.handleTagInternal();

      if (user != null) {
         veriplaceState.setUser(user);  
      }
      else if (userId != null) {
         veriplaceState.setUser(userId);
      }

      return true;
   }

   public void setUser(User user) {
      this.user = user;
   }
   
   public void setUserId(Long userId) {
      this.userId = userId;
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
