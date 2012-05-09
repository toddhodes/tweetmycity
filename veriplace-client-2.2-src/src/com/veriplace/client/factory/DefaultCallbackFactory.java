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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Standard implementation of {@link com.veriplace.client.factory.CallbackFactory}.
 * This is used by {@link com.veriplace.client.Client} and {@link com.veriplace.web.VeriplaceState}
 * if you don't specify a different implementation.
 * <p>
 * When constructing a callback URL, DefaultCallbackFactory behaves as follows:
 * <ul>
 * <li> Use the automatically detected hostname from HttpServletRequest.getServerName(), unless a
 * different value was specified in the constructor.  You may need to specify the value if your
 * server's internal host name does not match the external domain name. </li>
 * <li> Use the automatically detected port from HttpServletRequest.getServerName(), unless a
 * different value was specified in the constructor.  You may need to specify the value if requests
 * are being forwarded to your application from a different port.  The port is omitted from the URL
 * if it is the standard value of 80 (for HTTP) or 443 (for HTTPS).</li>
 * <li> Use the automatically detected URL path components from HttpServletRequest.getContextPath(),
 * getServletPath(), and getPathInfo(), unless a different path was specified in the constructor.
 * You may need to specify the path if requests are being forwarded to your application from within
 * another application whose URL can't be automatically detected. </li>
 * <li> If no parameter names were specified in the constructor, copy <i>all</i> HTTP request parameters
 * (query string and/or POST values) to the callback URL. </li>
 * <li> If parameter names were specified in the constructor, then include or exclude parameters based
 * on what was specified. </li>
 * </ul>
 * @since 2.0
 */
public class DefaultCallbackFactory implements CallbackFactory {

   private final String overrideServerName;
   private final Integer overrideServerPort;
   private final String overridePath;
   private final Set<String> includeParameters;
   private final Set<String> excludeParameters;
   
   /**
    * Constructs a DefaultCallbackFactory with no overridden properties.
    */
   public DefaultCallbackFactory() {
      this(null, null, null); 
   }
   
   /**
    * Constructs a DefaultCallbackFactory, overriding the server name.
    * @param overrideServerName  the hostname to use in callback URLs, or null to
    *   use the default value
    */
   public DefaultCallbackFactory(String overrideServerName) {
      this(overrideServerName, null, null);
   }
   
   /**
    * Constructs a DefaultCallbackFactory, overriding the server name and port.
    * @param overrideServerName  the hostname to use in callback URLs (e.g.
    *   "myhost.com"), or null to use the default value
    * @param overrideServerPort  the port to use in callback URLs, or null to use
    *   the default value
    */
   public DefaultCallbackFactory(String overrideServerName,
                                 Integer overrideServerPort) {
      this(overrideServerName, overrideServerPort, null);
   }
   
   /**
    * Constructs a DefaultCallbackFactory, overriding the server name, port, and
    * URL path.
    * @param overrideServerName  the hostname to use in callback URLs, or null to
    *   use the default value
    * @param overrideServerPort  the port to use in callback URLs, or null to use
    *   the default value
    * @param overridePath  the path to use in callback URLs (e.g. "/foo/bar"), or
    *   null to use the default value
    */
   public DefaultCallbackFactory(String overrideServerName,
                                 Integer overrideServerPort,
                                 String overridePath) {
      this(overrideServerName, overrideServerPort, overridePath, false, null);
   }

   /**
    * Constructs a DefaultCallbackFactory, overriding the server name, port, and
    * URL path, and specifying the parameter names to be captured or not captured.
    * @param overrideServerName  the hostname to use in callback URLs, or null to
    *   use the default value
    * @param overrideServerPort  the port to use in callback URLs, or null to use
    *   the default value
    * @param overridePath  the path to use in callback URLs, or null to use
    *   the default value
    * @param captureSpecifiedParameters  true if the specified parameters are the
    *   only ones that should be captured; false if they are the only ones that
    *   should <i>not</i> be captured
    * @param specifiedParameters  a list of parameter names
    * @deprecated
    */
   public DefaultCallbackFactory(String overrideServerName,
                                 Integer overrideServerPort,
                                 String overridePath,
                                 boolean captureSpecifiedParameters,
                                 String[] specifiedParameters) {
      this(overrideServerName, overrideServerPort, overridePath,
            (captureSpecifiedParameters ? specifiedParameters : null),
            (captureSpecifiedParameters ? null : specifiedParameters));
   }
   
   /**
    * Constructs a DefaultCallbackFactory, overriding the server name, port, and
    * URL path, and specifying the parameter names to be captured or not captured.
    * @param overrideServerName  the hostname to use in callback URLs, or null to
    *   use the default value
    * @param overrideServerPort  the port to use in callback URLs, or null to use
    *   the default value
    * @param overridePath  the path to use in callback URLs, or null to use
    *   the default value
    * @param includeParameters  specific parameter names to be captured, or null to
    *   include all by default
    * @param excludeParameters  specific parameter names to be excluded, or null for
    *   no specific exclusions
    */
   public DefaultCallbackFactory(String overrideServerName,
         Integer overrideServerPort,
         String overridePath,
         String[] includeParameters,
         String[] excludeParameters) {
      
      this.overrideServerName = overrideServerName;
      this.overrideServerPort = overrideServerPort;
      if (overridePath != null) {
         if (overridePath.equals("")) {
            overridePath = null;
         }
         else {
            if (! overridePath.startsWith("/")) {
               overridePath = "/" + overridePath;
            }
         }
      }
      this.overridePath = overridePath;
      if (includeParameters == null) {
         this.includeParameters = null;
      }
      else {
         this.includeParameters = new HashSet<String>();
         for (String name: includeParameters) {
            this.includeParameters.add(name);
         }
      }
      if (excludeParameters == null) {
         this.excludeParameters = null;
      }
      else {
         this.excludeParameters = new HashSet<String>();
         for (String name: excludeParameters) {
            this.excludeParameters.add(name);
         }
      }
   }

   public String getOverrideServerName() {
      return overrideServerName;
   }

   public Integer getOverrideServerPort() {
      return overrideServerPort;
   }

   public String getOverridePath() {
      return overridePath;
   }

   public Set<String> getIncludeParameters() {
      return includeParameters;
   }
   
   public Set<String> getExcludeParameters() {
      return excludeParameters;
   }

   public String createCallbackUrl(HttpServletRequest request, boolean includePath) {
      String host = (overrideServerName != null) ? overrideServerName : request.getServerName();
      int port = (overrideServerPort != null) ? overrideServerPort : request.getServerPort();
      StringBuilder callback = new StringBuilder();
      callback.append(request.getScheme());
      callback.append("://");
      callback.append(host);
      if (port != 80 && port != 443) {
         callback.append(":");
         callback.append(port);
      }
      if (includePath) {
         if (overridePath != null) {
            callback.append(overridePath);
         }
         else {
            callback.append(request.getContextPath());
            callback.append(request.getServletPath());
            String subpath = request.getPathInfo();
            if ((subpath != null) && !subpath.equals("")) {
               if (! subpath.startsWith("/")) {
                  callback.append('/');
               }
               callback.append(subpath);
            }
         }
      }
      return callback.toString();
   }

   public Map<String, String[]> captureParameters(HttpServletRequest request) {
      Map<String, String[]> ret = new HashMap<String, String[]>();
      Enumeration<?> names = request.getParameterNames();
      while (names.hasMoreElements()) {
         String name = (String) names.nextElement();
         if (name.startsWith("oauth_") || name.startsWith("veriplace_")) {
            // don't automatically copy parameters generated by Veriplace
            continue;
         }
         if ((includeParameters != null) && (! includeParameters.contains(name))) {
            continue;
         }
         if ((excludeParameters != null) && excludeParameters.contains(name)) {
            continue;
         }
         String[] values = request.getParameterValues(name);
         if (values != null) {
            ret.put(name, values);
         }
      }
      return ret;
   }
}
