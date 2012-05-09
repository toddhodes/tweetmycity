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
import com.veriplace.web.views.RespondedWithStatusViewException;
import com.veriplace.web.views.StatusViewRenderer;

/**
 * Indicates that the application should stop handling the current HTTP request because
 * Veriplace has already sent a response to the end user:  either a redirect to another site
 * ({@link RedirectedToVeriplaceException}), or a status page provided by a {@link StatusViewRenderer}
 * ({@link RespondedWithStatusViewException}).  No further action needs to be taken.  In
 * the case of a redirect, or a "please wait" status page, the application should receive a
 * callback request later to continue the current flow.
 * @since 2.0
 */
public class RespondedException extends VeriplaceException {

   protected RespondedException() {
      this(null);
   }
   
   protected RespondedException(Throwable cause) {
      super(cause);
   }
}
