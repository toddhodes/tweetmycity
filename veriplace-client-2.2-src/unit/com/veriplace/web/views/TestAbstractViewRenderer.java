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
package com.veriplace.web.views;

import static org.junit.Assert.*;

import com.veriplace.client.Client;
import com.veriplace.client.GetLocationException;
import com.veriplace.client.GetLocationNotPermittedException;
import com.veriplace.client.MalformedResponseException;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.UserDiscoveryNotPermittedException;
import com.veriplace.client.UserNotFoundException;
import com.veriplace.web.Veriplace;
import com.veriplace.web.VeriplaceState;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link com.veriplace.web.views.AbstractStatusViewRenderer}.
 */
public class TestAbstractViewRenderer {

   private MockViewRenderer renderer;
   private Veriplace veriplace;
   private Client client;
   private MockHttpServletRequest request;
   private MockHttpServletResponse response;
   
   @Before
   public void setUp() throws Exception {
      client = new Client("key", "secret");
      veriplace = new Veriplace(client);
      reset();
   }
   
   @Test
   public void testConstructor() {
      assertNull(renderer.getWaitingViewName());
      assertNull(renderer.getDefaultErrorViewName());
   }

   @Test
   public void testErrorViewFallback() {
      renderer.setDefaultErrorViewName("default");
      renderer.setErrorViewName(GetLocationException.class, "get");
      renderer.setErrorViewName(UserDiscoveryException.class, "user/general");
      renderer.setErrorViewName(UserDiscoveryNotPermittedException.class, "user/specific");
      renderer.setErrorViewName(UnexpectedException.class, "weird");

      assertEquals("get", renderer.getErrorViewName(GetLocationException.class));
      assertEquals("get", renderer.getErrorViewName(GetLocationNotPermittedException.class));
      assertEquals("user/general", renderer.getErrorViewName(UserDiscoveryException.class));
      assertEquals("user/general", renderer.getErrorViewName(UserNotFoundException.class));
      assertEquals("user/specific", renderer.getErrorViewName(UserDiscoveryNotPermittedException.class));
      assertEquals("weird", renderer.getErrorViewName(UnexpectedException.class));
      assertEquals("weird", renderer.getErrorViewName(MalformedResponseException.class));
   }
   
   @Test
   public void testViewMap() {
      Map<String, String> map = new HashMap<String, String>();
      map.put("waiting", "zzz");
      map.put("error", "foo");
      map.put("error.UserDiscovery", "bar");
      renderer.setViewMap(map);
      assertEquals("zzz", renderer.getWaitingViewName());
      assertEquals("foo", renderer.getDefaultErrorViewName());
      assertEquals("bar", renderer.getErrorViewName(UserDiscoveryException.class));
   }

   @Test
   public void testCanRenderWaitingView() throws Exception {
      assertNull(renderer.getWaitingViewName());
      assertFalse(renderer.canRenderWaitingView());
      
      renderer.setWaitingViewName("foo");
      assertTrue(renderer.canRenderWaitingView());
   }
   
   @Test
   public void testRenderWaitingView() throws Exception {
      renderer.setWaitingViewName("wait");
      VeriplaceState state = createState();

      // render waiting view with callback URL attribute
      String url = "pleasewait";
      boolean handled = renderer.renderWaitingView(request, response, state, url);
      assertEquals(true, handled);
      assertEquals("wait", renderer.getRenderedView());
      assertSame(state, request.getAttribute("veriplace"));
      assertEquals(url, request.getAttribute("veriplace_callback"));
      reset();
   }
   
   @Test
   public void testRenderErrorView() throws Exception {
      renderer.setDefaultErrorViewName("error");
      VeriplaceState state = createState();
      
      // render error view
      Exception e = new GetLocationException();
      boolean handled = renderer.renderErrorView(request, response, state, e);
      assertEquals(true, handled);
      assertEquals("error", renderer.getRenderedView());
      assertSame(state, request.getAttribute("veriplace"));
      reset();
   }
   
   protected VeriplaceState createState() {
      return veriplace.open(request, response);
   }

   protected void reset() {
      renderer = new MockViewRenderer();
      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
   }
}
