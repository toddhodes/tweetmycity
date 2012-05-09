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

import com.veriplace.client.UnexpectedException;

/**
 * Thrown if a {@link StatusViewRenderer} encounters an error and cannot output
 * the requested status view.
 * @since 2.0
 */
public class StatusViewException extends UnexpectedException {

   public StatusViewException(String message) {
      this(message, null);
   }

   public StatusViewException(Throwable cause) {
      this(null, cause);
   }
   
   public StatusViewException(String message, Throwable cause) {
      super(message, cause);
   }
}
