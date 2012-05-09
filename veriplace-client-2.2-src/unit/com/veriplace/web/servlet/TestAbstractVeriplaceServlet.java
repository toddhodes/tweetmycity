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
package com.veriplace.web.servlet;

import static org.junit.Assert.*;

import com.veriplace.client.Client;
import com.veriplace.client.LocationMode;
import com.veriplace.client.TestBase;
import com.veriplace.client.VeriplaceException;
import com.veriplace.web.Redirector;
import com.veriplace.web.Veriplace;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.views.StatusViewException;
import com.veriplace.web.views.StatusViewRenderer;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class TestAbstractVeriplaceServlet extends TestBase {

   private static final String PROPERTIES_FILE_NAME = "etc/servlet-test.properties";
   
   private MockServletConfig config;
   private MockServletContext context;
   private TestRedirector redirector;
   private TestStatusViewRenderer status;
   
   @Before
   public void setUp() throws Exception {
      super.setUp();
      context = new MockServletContext();
      context.addInitParameter("veriplace.properties-file", PROPERTIES_FILE_NAME);
      config = new MockServletConfig(context, "servlet");
      redirector = new TestRedirector();
      status = new TestStatusViewRenderer();
   }

   @Test
   public void testInitNoAnnotation() throws Exception {
      TestServlet servlet = new TestServletNoAnnotation();
      servlet.init(config);
      
      validateInstance(servlet.getVeriplace(), true);
   }
   
   @Test
   public void testInitPlainAnnotation() throws Exception {
      TestServlet servlet = new TestServletPlainAnnotation();
      servlet.init(config);
      
      validateInstance(servlet.getVeriplace(), true);
   }
   
   @Test
   public void testInitNoStatusViews() throws Exception {
      TestServlet servlet = new TestServletNoStatusViews();
      servlet.init(config);
      
      assertNotNull(servlet.getVeriplace());
      validateInstance(servlet.getVeriplace(), false);
   }
   
   @Test
   public void testRequireUserRedirect() throws Exception {
      // - Servlet with user discovery requirement
      // - Request with no token & no user ID
      // -> Redirects to user authorization
      
      TestServlet servlet = new TestServletRequiresUser();
      setup(servlet);
      
      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      String shouldRedirectTo = createUserAuthRedirectUrl(REQUEST_TOKEN, USER_REQUEST_URI);

      servlet.test(request, response);
      
      assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
      assertFalse(servlet.isCompleted());
      assertEquals(shouldRedirectTo, response.getRedirectedUrl());
   }
   
   @Test
   public void testRequireUserSuccess() throws Exception {
      // - Servlet with user discovery requirement
      // - Request with valid user discovery access token
      // -> Gets the user, executes the servlet

      TestServlet servlet = new TestServletRequiresUser();
      setup(servlet);
      
      prepareValidCallbackToken(ACCESS_TOKEN);
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 200, USER_DOCUMENT);

      servlet.test(request, response);
      
      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      assertTrue(servlet.isCompleted());
      assertNotNull(servlet.getLastState().getUser());
      assertEquals(USER_ID, servlet.getLastState().getUser().getId());
   }

   @Test
   public void testRequireLocationRedirect() throws Exception {
      // - Servlet with location requirement
      // - Request with valid user discovery access token
      // -> Gets the user, then redirects to location authorization

      TestServlet servlet = new TestServletRequiresLocation();
      setup(servlet);
      
      prepareValidCallbackToken(ACCESS_TOKEN);
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 200, USER_DOCUMENT);

      prepareLocationAccessTokenRequestFailure();
      
      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      String shouldRedirectTo = createUserAuthRedirectUrl(REQUEST_TOKEN, LOCATION_REQUEST_URI);

      servlet.test(request, response);
      
      assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
      assertFalse(servlet.isCompleted());
      assertEquals(shouldRedirectTo, response.getRedirectedUrl());
   }
   
   @Test
   public void testRequireLocationSuccess() throws Exception {
      // - Servlet with location requirement
      // - Callback request with user ID and valid location permission token
      // -> Starts location request in background, goes to wait page
      //
      // - Wait page refreshes; new request with async request ID
      // -> Gets result of location request, executes servlet

      TestServlet servlet = new TestServletRequiresLocation();
      setup(servlet);
      
      prepareValidCallbackToken(ACCESS_TOKEN);
      prepareCallbackUser(USER);
      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_DOCUMENT);

      servlet.test(request, response);
      
      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      assertFalse(servlet.isCompleted());
      assertTrue(status.isWaiting());
      
      newRequest();
      prepareCallbackUser(USER);
      ++currentAsyncRequestId;
      addAsyncRequestCallbackParameters();
      
      servlet.test(request, response);
      
      if (status.getException() != null) {
         throw status.getException();
      }
      
      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      assertTrue(servlet.isCompleted());
      assertNotNull(servlet.getLastState().getLocation());
   }

   @Test
   public void testRequireLocationWithModeSuccess() throws Exception {
      TestServlet servlet = new TestServletRequiresLocationWithMode();
      setup(servlet);
      
      prepareValidCallbackToken(ACCESS_TOKEN);
      prepareCallbackUser(USER);
      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_DOCUMENT);

      servlet.test(request, response);
      
      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      assertFalse(servlet.isCompleted());
      assertTrue(status.isWaiting());
      
      newRequest();
      prepareCallbackUser(USER);
      ++currentAsyncRequestId;
      addAsyncRequestCallbackParameters();
      
      servlet.test(request, response);
      
      if (status.getException() != null) {
         throw status.getException();
      }
      
      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      assertTrue(servlet.isCompleted());
      assertNotNull(servlet.getLastState().getLocation());
   }

   @Test
   public void testRequireLocationSynchronousSuccess() throws Exception {
      // - Servlet with location requirement
      // - Callback request with user ID and valid location permission token
      // - We do *not* have a "please wait" page defined
      // -> Performs location request synchronously, executes servlet

      TestServlet servlet = new TestServletRequiresLocation();
      setup(servlet);
      status.setCanWait(false);
      
      prepareValidCallbackToken(ACCESS_TOKEN);
      prepareCallbackUser(USER);
      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_DOCUMENT);

      servlet.test(request, response);
      
      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      assertTrue(servlet.isCompleted());
      assertFalse(status.isWaiting());
      assertNotNull(servlet.getLastState().getLocation());
   }

   protected void validateInstance(Veriplace veriplace, boolean shouldHaveStatusViewRenderer) {
      assertNotNull(veriplace);
      assertEquals(CONSUMER_KEY, veriplace.getClient().getConsumer().getConsumerKey());
      assertEquals(CONSUMER_SECRET, veriplace.getClient().getConsumer().getConsumerSecret());
      if (shouldHaveStatusViewRenderer) {
         assertNotNull(veriplace.getStatusViewRenderer());
         assertEquals(ServletStatusViewRenderer.class, veriplace.getStatusViewRenderer().getClass());
      }
      else {
         assertNull(veriplace.getStatusViewRenderer());
      }
   }

   protected void setup(TestServlet servlet) throws Exception {
      servlet.init(config);
      servlet.getVeriplace().getClient().getConsumer().setClient(mockServer);
      servlet.getVeriplace().setRedirector(redirector);
      servlet.getVeriplace().setStatusViewRenderer(status);
   }

   protected void newRequest() {
      super.newRequest();
      if (status != null) {
         status.reset();
      }
   }
   
   class TestServlet extends AbstractVeriplaceServlet {

      private VeriplaceState lastState;
      private boolean completed;
      
      public Veriplace getVeriplace() {
         return veriplace;
      }
      
      public void test(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
         doGet(request, response);
      }
      
      @Override
      protected void doRequestInternal(HttpServletRequest request,
            HttpServletResponse response, VeriplaceState veriplaceState)
            throws IOException, ServletException, VeriplaceException {
         
         lastState = veriplaceState;
         completed = true;
      }
 
      @Override
      protected Client getVeriplaceClient(HttpServletRequest request, Veriplace defaultInstance) {
         return client;
      }
      
      public VeriplaceState getLastState() {
         return lastState;
      }
      
      public boolean isCompleted() {
         return completed;
      }
   }
   
   class TestServletNoAnnotation extends TestServlet {
      
   }
 
   @UsesVeriplace
   class TestServletPlainAnnotation extends TestServlet {
      
   }

   @UsesVeriplace(useStatusViews = false)
   class TestServletNoStatusViews extends TestServlet {
      
   }

   @UsesVeriplace(requireUser = true)
   class TestServletRequiresUser extends TestServlet {

   }

   @UsesVeriplace(requireLocation = true)
   class TestServletRequiresLocation extends TestServlet {

   }

   @UsesVeriplace(requireLocation = true, mode = LocationMode.AREA)
   class TestServletRequiresLocationWithMode extends TestServlet {

   }

   static class TestRedirector implements Redirector {

      // Use this instead of DefaultRedirector just because the MockHttpServletResponse class
      // doesn't automatically update its status when you call sendRedirect.
      public void sendRedirect(HttpServletRequest request,
            HttpServletResponse response, String url) throws IOException {
         response.sendRedirect(url);
         response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
      }
   }
   
   static class TestStatusViewRenderer implements StatusViewRenderer {

      private Exception exception;
      private boolean waiting;
      private boolean canWait = true;
      private VeriplaceState state;
      
      public boolean renderErrorView(HttpServletRequest request,
            HttpServletResponse response, VeriplaceState state,
            Exception exception) throws StatusViewException {
         this.state = state;
         this.exception = exception;
         return true;
      }

      public boolean canRenderWaitingView() {
         return canWait;
      }
      
      public boolean renderWaitingView(HttpServletRequest request,
            HttpServletResponse response, VeriplaceState state,
            String callbackUrl) throws StatusViewException {
         this.state = state;
         this.waiting = true;
         return true;
      }
      
      public Exception getException() {
         return exception;
      }
      
      public boolean isError() {
         return (exception != null);
      }
      
      public boolean isWaiting() {
         return waiting;
      }
      
      public void setCanWait(boolean canWait) {
         this.canWait = canWait;
      }
      
      public VeriplaceState getState() {
         return state;
      }
      
      public void reset() {
         state = null;
         exception = null;
         waiting = false;
         canWait = true;
      }
   }
}
