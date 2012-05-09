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
 * Thrown to indicate that the Veriplace server returned a response that
 * the client could not understand.  This is defined as a subclass of
 * {@link UnexpectedException} because it should theoretically never happen
 * and does not imply any problem with the parameters sent by the client.
 * @since 2.0
 */
public class MalformedResponseException extends UnexpectedException {

   public MalformedResponseException(String message) {
      this(message, null);
   }

   public MalformedResponseException(Throwable cause) {
      this(null, cause);
   }

   public MalformedResponseException(String message, Throwable cause) {
      super(message, cause);
   }
}
