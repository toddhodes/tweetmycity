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
package com.veriplace.client.factory;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for an object that {@link com.veriplace.client.Client} and {@link com.veriplace.web.VeriplaceState}
 * use to generate callback URLs for OAuth transactions.  The standard implementation of this
 * is {@link com.veriplace.client.factory.DefaultCallbackFactory}, which provides automatic
 * detection of URL properties and parameters, and allows you to override most of these if
 * needed.  You can also create your own implementation of CallbackFactory if you require
 * different behavior.
 * @since 2.0
 */
public interface CallbackFactory {

   /**
    * Given an HTTP request, returns a callback URL that should access the same
    * resource as that request.
    * @param request  the current HTTP request
    * @param includePath  true if the full URL path should be included (not including the
    *   query string); false if it should just be the server and port
    */
   public String createCallbackUrl(HttpServletRequest request, boolean includePath);
   
   /**
    * Given an HTTP request, returns a map containing the names and values of all HTTP
    * GET or POST parameters that should be included in a callback.
    * @param request  the current HTTP request
    * @return  a map of parameter names and values; each value is an array, since a parameter
    *   name may have multiple values
    */
   public Map<String, String[]> captureParameters(HttpServletRequest request);
}
