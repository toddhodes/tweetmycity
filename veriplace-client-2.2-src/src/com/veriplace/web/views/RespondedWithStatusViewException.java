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
package com.veriplace.web.views;

import com.veriplace.web.RespondedException;

/**
 * Indicates that the application should stop handling the current HTTP request because
 * Veriplace has determined that a transaction cannot be completed at present (either
 * because of an error, or because it is in a waiting state), and has automatically
 * served up a response page from a {@link StatusViewRenderer}.  No further action needs
 * to be taken.  In the case of a "please wait" status page, the application should receive
 * a callback request later to continue the current flow.
 * <p>
 * You can determine what condition produced the response page by calling
 * {@link Exception#getCause()}.
 * @since 2.0
 */
public class RespondedWithStatusViewException extends RespondedException {

   public RespondedWithStatusViewException(Exception handledException) {
      super(handledException);
   }
}
