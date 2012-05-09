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
 * An Interceptor that does not allow requests to proceed until Veriplace has given
 * permission to update the current user's location.  This capability is not supported
 * in the current Veriplace platform; see {@link com.veriplace.client.SetLocationAPI}. 
 */
public class SetLocationPermissionInterceptor extends VeriplaceInterceptor {

   protected boolean handleInternal(VeriplaceState state, HttpServletRequest request, HttpServletResponse response)
         throws Exception {

      state.setRequiresSetLocationPermission(true);
      return state.completeAll();
   }
}
