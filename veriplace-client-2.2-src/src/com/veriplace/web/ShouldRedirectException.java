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
 * Used internally by {@link VeriplaceState} to detect when a redirect is required.
 * After sending the redirect, the client will then throw a {@link RedirectedToVeriplaceException}
 * to the application. 
 */
// package-private
class ShouldRedirectException extends Exception {

   private final String redirectToUrl;
   
   public ShouldRedirectException(String redirectToUrl) {
      super();
      this.redirectToUrl = redirectToUrl; 
   }
   
   public String getRedirectToUrl() {
      return redirectToUrl;
   }
}
