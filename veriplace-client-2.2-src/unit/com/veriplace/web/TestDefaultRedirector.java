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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link com.veriplace.client.web.DefaultRedirector}.
 */
public class TestDefaultRedirector {

   private DefaultRedirector redirector;
   private MockHttpServletRequest request;
   private MockHttpServletResponse response;

   @Before
   public void setUp() {
      redirector = new DefaultRedirector();
      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
   }
   
   @Test
   public void testSendRedirectHttp10() throws Exception {
      // send regular redirect (302) by default
      String url = "http://elsewhere.com";
      redirector.sendRedirect(request, response, url);
      assertEquals(url, response.getRedirectedUrl());
      assertFalse(response.getStatus() == 303);  // MockHttpServletResponse won't set status code for plain redirect
   }
   
   @Test
   public void testSendRedirectHttp11() throws Exception {
      // send 303 redirect for HTTP 1.1 user agents
      String url = "http://elsewhere.com";
      request.setProtocol("HTTP/1.1");
      redirector.sendRedirect(request, response, url);
      assertEquals(url, response.getHeader("Location"));
      assertEquals(303, response.getStatus());
   }
}
