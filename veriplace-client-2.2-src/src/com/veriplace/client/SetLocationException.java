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
package com.veriplace.client;

/**
 * Base class for exceptions in {@link SetLocationAPI} methods.
 * @since 2.0
 */
public class SetLocationException extends RequestDeniedException {

   public SetLocationException() {
      this((Throwable) null);
   }
   
   public SetLocationException(String message) {
      this(message, null);
   }

   public SetLocationException(Throwable cause) {
      this("Set location request failed", cause);
   }

   public SetLocationException(String message, Throwable cause) {
      super(message, cause);
   }
}
