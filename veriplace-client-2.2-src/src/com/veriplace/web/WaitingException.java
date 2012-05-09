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

import com.veriplace.client.VeriplaceException;

/**
 * Indicates that Veriplace has started an asynchronous request which may take some
 * time.  To avoid leaving the user hanging on a partially rendered page, the application
 * may want to display a "please wait" page, which should have an automatic refresh to the
 * callback URL that is contained in the exception, and then stop handling the current HTTP
 * request.  (This is highly recommended in order to support users who access your application
 * through a mobile phone web gateway, since such gateways often have a short timeout and will
 * abandon a request if it appears to be stalled.)  If this is not a concern, you can simply
 * repeat the previous method call, which will either return successfully if the request has
 * completed or throw another WaitingException if you still need to wait.
 * <p>
 * This behavior can be handled automatically by a {@link com.veriplace.web.views.StatusViewRenderer},
 * which is the default.
 * @since 2.0
 */
public class WaitingException extends VeriplaceException {

   private final String callbackUrl;
   
   public WaitingException(String callbackUrl) {
      super();
      this.callbackUrl = callbackUrl;
   }
   
   public String getCallbackUrl() {
      return callbackUrl;
   }
}
