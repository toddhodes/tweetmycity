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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default implementation of {@link com.veriplace.web.Redirector}.  Sends a regular HTTP
 * redirect response, using code 302 for HTTP 1.0 or 303 for HTTP 1.1.
 * <p>
 * The choice of status code is based on the HTTP 1.0 specification, which states:
 * <blockquote>
 * If the 302 status code is received in response to a request using the POST method, 
 * the user agent must not automatically redirect the request unless it can be confirmed 
 * by the user, since this might change the conditions under which the request was issued.
 * <p>
 * Note: When automatically redirecting a POST request after receiving a 302 status code, 
 * some existing user agents will erroneously change it into a GET request.
 * </blockquote>
 * <p>
 * In practice, most HTTP 1.0 User Agents handle 302 redirects after a POST as a GET. 
 * However, the HTTP 1.1 specification added a 303 redirect specification for this scenario 
 * and some HTTP 1.1 User Agents do not handle 302 redirects "conventionally". 
 * <p>
 * Therefore, applications should send a different redirect code for 1.1 User Agents.
 * @since 2.0
 */
public class DefaultRedirector implements Redirector {

   private static final Log logger = LogFactory.getLog(DefaultRedirector.class);

   public void sendRedirect(HttpServletRequest request,
         HttpServletResponse response, String url) throws IOException {
      
      logger.debug("Redirecting to " + url);

      if ("HTTP/1.1".equalsIgnoreCase(request.getProtocol())) {
            response.setStatus(303);
            response.setHeader("Location", response.encodeRedirectURL(url));
      } else {
            response.sendRedirect(response.encodeRedirectURL(url));
      }
   }
}
