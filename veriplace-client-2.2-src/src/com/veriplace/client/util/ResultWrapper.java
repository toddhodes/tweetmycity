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
package com.veriplace.client.util;

import com.veriplace.client.VeriplaceException;
import com.veriplace.oauth.consumer.Token;

/**
 * Stores a result object if the request succeeded, or an exception if it failed.
 * @param <T> type of the result object
 */
public class ResultWrapper<T> {

   private final T result;
   private final VeriplaceException exception;
   private final Token token;
   
   public ResultWrapper(T result, Token token) {
      this(result, token, null);
   }
   
   public ResultWrapper(VeriplaceException exception) {
      this(null, null, exception);
   }
   
   protected ResultWrapper(T result, Token token, VeriplaceException exception) {
      if ((result == null) && (exception == null)) {
         throw new IllegalStateException();
      }
      this.result = result;
      this.exception = exception;
      this.token = token;
   }
   
   public T getResult() {
      return result;
   }
   
   public VeriplaceException getException() {
      return exception;
   }
   
   public Token getToken() {
      return token;
   }
}
