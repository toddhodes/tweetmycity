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

/**
 * Indicates that the application should stop handling the current HTTP request because
 * Veriplace has redirected the end user, for user discovery or authorization.  When you
 * catch this exception, the redirect response has already been sent and no further
 * action needs to be taken until the application receives the next callback.
 * @since 2.0
 */
public class RedirectedToVeriplaceException extends RespondedException {

   private final String redirectedToUrl;
   
   public RedirectedToVeriplaceException(String redirectedToUrl) {
      super();
      this.redirectedToUrl = redirectedToUrl;
   }
   
   public String getRedirectedToUrl() {
      return redirectedToUrl;
   }
}
