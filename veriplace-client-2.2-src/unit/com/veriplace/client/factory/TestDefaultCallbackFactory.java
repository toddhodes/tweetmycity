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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for {@link com.veriplace.client.factory.DefaultCallbackFactory}.
 */
public class TestDefaultCallbackFactory {

   @Test
   public void testNoOverrides() {
      // Constructs a default callback URL based on server name & port
      // (as reported by HttpServletRequest)
      String serverName = "globex.com";
      int serverPort = 8080;
      
      CallbackFactory cf = new DefaultCallbackFactory();
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setServerName(serverName);
      request.setServerPort(serverPort);
      request.setScheme("http");
      request.setContextPath("/resources");
      request.setServletPath("/teams");
      request.setPathInfo("/broncos");
      String path = "/resources/teams/broncos";
      String urlShouldBe;
      
      // test that the URL is constructed properly from scheme, host, and port
      urlShouldBe = "http://globex.com:8080";
      assertEquals(urlShouldBe, cf.createCallbackUrl(request, false));
      assertEquals(urlShouldBe + path, cf.createCallbackUrl(request, true));
      
      request.setServerPort(80);  // 80 is standard, can omit from URL
      urlShouldBe = "http://globex.com";
      assertEquals(urlShouldBe, cf.createCallbackUrl(request, false));
      assertEquals(urlShouldBe + path, cf.createCallbackUrl(request, true));

      request.setScheme("https");
      request.setServerPort(8443);
      urlShouldBe = "https://globex.com:8443";
      assertEquals(urlShouldBe, cf.createCallbackUrl(request, false));
      assertEquals(urlShouldBe + path, cf.createCallbackUrl(request, true));
      
      request.setServerPort(443);  // 443 is standard, can omit from URL
      urlShouldBe = "https://globex.com";
      assertEquals(urlShouldBe, cf.createCallbackUrl(request, false));
      assertEquals(urlShouldBe + path, cf.createCallbackUrl(request, true));
   }

   @Test
   public void testOverrideServerAndPort() {
      // Can override auto-detected server/port with specific values from constructor
      String desiredServerName = "globex.com";
      int desiredServerPort = 8080;
      
      String autoServerName = "scorpio.gov";
      int autoServerPort = 777;
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setServerName(autoServerName);
      request.setServerPort(autoServerPort);

      CallbackFactory cf = new DefaultCallbackFactory(desiredServerName);

      assertEquals("http://globex.com:777", cf.createCallbackUrl(request, false));

      cf = new DefaultCallbackFactory(null, desiredServerPort);

      assertEquals("http://scorpio.gov:8080", cf.createCallbackUrl(request, false));

      cf = new DefaultCallbackFactory(desiredServerName, desiredServerPort);

      assertEquals("http://globex.com:8080", cf.createCallbackUrl(request, false));
      
   }

   @Test
   public void testOverridePath() {
      // Can override auto-detected URL subpath with specific value from constructor
      String desiredPath = "/managers/simpson";
      
      String autoServerName = "scorpio.gov";
      int autoServerPort = 777;
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setServerName(autoServerName);
      request.setServerPort(autoServerPort);
      request.setContextPath("/resources");
      request.setServletPath("/teams");
      request.setPathInfo("/broncos");

      CallbackFactory cf = new DefaultCallbackFactory(null, null, desiredPath);

      assertEquals("http://scorpio.gov:777/managers/simpson", cf.createCallbackUrl(request, true));
   }
   
   @Test
   public void testAutoCaptureParams() {
      // Captures all GET/POST parameters if not specified otherwise
      CallbackFactory cf = new DefaultCallbackFactory();
      
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.addParameter("foo", new String[] { "bar", "baz" });
      request.addParameter("oauth_token", "12345");
      request.addParameter("action", "reaction");
      
      Map<String, String[]> params = cf.captureParameters(request);
      assertEquals(2, params.size());
      assertArrayEquals(new String[] { "bar", "baz" }, params.get("foo"));
      assertArrayEquals(new String[] { "reaction" }, params.get("action"));
      // OAuth parameters should be excluded
   }
   
   @Test
   public void testCaptureOnlySpecifiedParams() {
      // Captures only the specified parameter names
      CallbackFactory cf = new DefaultCallbackFactory(null, null, null,
            new String[] { "action", "inaction" }, null);
      
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.addParameter("foo", new String[] { "bar", "baz" });
      request.addParameter("oauth_token", "12345");
      request.addParameter("action", "reaction");
      
      Map<String, String[]> params = cf.captureParameters(request);
      assertEquals(1, params.size());
      assertArrayEquals(new String[] { "reaction" }, params.get("action"));
      // it's OK that "inaction" was not in the request
   }
   
   @Test
   public void testCaptureAllButSpecifiedParams() {
      // Captures all parameters except for the specified parameter names
      CallbackFactory cf = new DefaultCallbackFactory(null, null, null,
            null, new String[] { "action", "inaction" });
      
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.addParameter("foo", new String[] { "bar", "baz" });
      request.addParameter("oauth_token", "12345");
      request.addParameter("action", "reaction");
      
      Map<String, String[]> params = cf.captureParameters(request);
      assertEquals(1, params.size());
      assertArrayEquals(new String[] { "bar", "baz" }, params.get("foo"));
   }
}
