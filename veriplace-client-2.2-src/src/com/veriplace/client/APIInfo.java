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

import com.veriplace.oauth.message.RequestMethod;

/**
 * Information needed to make a request for a Veriplace API.
 */
class APIInfo {

   private final String uri;
   private final RequestMethod requestMethod;

   public APIInfo(String uri,
                  RequestMethod requestMethod) {
      this.uri = uri;
      this.requestMethod = requestMethod;
   }

   /**
    * Get the URI for the resource controlled by this API.
    * <p>
    * This URI is used in the OAuth user authorization redirect to identify
    * what resource needs to be authorized.
    */
   public String getURI() {
      return uri;
   }

   /**
    * Get the request method for this URI, e.g. GET or POST
    */
   public RequestMethod getRequestMethod() {
      return requestMethod;
   }

   /**
    * Convenient factory method
    */
   public static APIInfo get(String uri) {
      return new APIInfo(uri,RequestMethod.GET);
   }

   /**
    * Convenient factory method
    */
   public static APIInfo post(String uri) {
      return new APIInfo(uri,RequestMethod.POST);
   }
}

/*
** Local Variables:
**   c-basic-offset: 3
**   tab-width: 3
**   indent-tabs-mode: nil
** End:
**
** ex: set softtabstop=3 tabstop=3 expandtab cindent shiftwidth=3
*/


