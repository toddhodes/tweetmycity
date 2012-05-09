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

import com.veriplace.client.GetLocationNotPermittedException;
import com.veriplace.client.Location;
import com.veriplace.client.TestBase;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryNotPermittedException;
import com.veriplace.client.VeriplaceOAuthException;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.web.views.MockViewRenderer;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for {@link com.veriplace.web.Veriplace}.  These tests verify that the API
 * sends the correct parameters to the server (although we do not check OAuth signatures
 * here), correctly decodes both success and failure responses, throws exceptions as
 * appropriate, and updates the {@link com.veriplace.web.VeriplaceState}.
 */
public class TestVeriplace extends TestBase {

   private Veriplace veriplace;
   private MockViewRenderer renderer;
   
   @Before
   public void setUp() throws Exception {
      super.setUp();
      veriplace = new Veriplace(client);
      renderer = new MockViewRenderer();
      veriplace.setStatusViewRenderer(renderer);
      currentAsyncRequestId = 0;
   }

   @Test
   public void testOpenState() {
      VeriplaceState state = veriplace.open(request, response);
      assertSame(veriplace, state.getVeriplace());
      assertSame(request, state.getRequest());
      assertNull(state.getUser());
      assertNull(state.getLocation());
      assertEquals(false, state.isCallback());  // no oauth_token parameter
   }
   
   @Test
   public void testOpenStateGetsAccessToken() {
      prepareValidCallbackToken(ACCESS_TOKEN);
      
      VeriplaceState state = veriplace.open(request, response);
      
      assertEquals(true, state.isCallback());
      assertNotNull(state.getAccessToken());
      assertNull(state.getLastErrorException());
      assertEquals(ACCESS_TOKEN_VALUE, state.getAccessToken().getToken());
      assertEquals(ACCESS_TOKEN_SECRET, state.getAccessToken().getTokenSecret());
   }
   
   @Test
   public void testOpenStateGetsInvalidAccessToken() {
      tokenStore.add(REQUEST_TOKEN);
      mockServer.shouldRefuseAccessToken(REQUEST_TOKEN, VERIFIER);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      VeriplaceState state = veriplace.open(request, response);
      
      assertEquals(true, state.isCallback());
      assertNotNull(state.getLastErrorException());
      assertTrue(state.getLastErrorException() instanceof VeriplaceOAuthException);
      assertNull(state.getAccessToken());
   }
   
   @Test
   public void testOpenStateCapturesParameters() {
      request.setServerName("thing.com");
      request.setServletPath("/part");
      request.addParameter("foo", "bar");
      request.addParameter("foo", "baz");
      request.addParameter("action", "inaction");
      request.addParameter("oauth_thingy", "ignoreme"); // should not capture any oauth_ parameters
      String urlShouldBe = "http://thing.com/part?veriplace_temp=part&foo=bar&foo=baz&action=inaction";
      
      VeriplaceState state = veriplace.open(request, response);

      assertEquals(urlShouldBe, veriplace.getCallbackUrl(state));
   }
   
   @Test
   public void testGetFromRequest() {
      assertNull(VeriplaceState.getFromRequest(request));

      VeriplaceState state = veriplace.open(request, response);

      assertSame(state, VeriplaceState.getFromRequest(request));
   }
   
   @Test
   public void testUserRequirementStep1() throws Exception {
      // Starting from scratch with no credentials:  user discovery requirement causes
      // a redirect to the user authorization URL
      request.addParameter("foo", "bar");
      VeriplaceState state = veriplace.open(request, response);
      
      mockServer.shouldGrantRequestToken(REQUEST_TOKEN)
            .setExpectedParameter("oauth_callback", veriplace.getCallbackUrl(state));
      String shouldRedirectTo = createUserAuthRedirectUrl(REQUEST_TOKEN, USER_REQUEST_URI);
      
      try {
         state.requireUser();
         fail("Expected RedirectedToVeriplaceException");
      }
      catch (RedirectedToVeriplaceException e) {
         assertEquals(shouldRedirectTo, e.getRedirectedToUrl());
         assertEquals(shouldRedirectTo, response.getRedirectedUrl());
      }
   }

   @Test
   public void testUserRequirementStep2Success() throws Exception {
      // Returning via callback from successful user authorization: user discovery requirement
      // does not cause a redirect, but immediately queries the user ID from the server
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldGrantAccessToken(REQUEST_TOKEN, VERIFIER, ACCESS_TOKEN);
      
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 200, USER_DOCUMENT);
      
      VeriplaceState state = veriplace.open(request, response);
      
      state.requireUser();
      assertNotNull(state.getUser());
      assertEquals(USER_ID, state.getUser().getId());
   }

   @Test
   public void testUserRequirementStep2BadAccessToken() throws Exception {
      // Returning via callback from user authorization, with a request token, but there's
      // no valid access token.
      
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldRefuseAccessToken(REQUEST_TOKEN, VERIFIER);

      veriplace.setStatusViewRenderer(null);  // so it won't try to display its own wait page

      VeriplaceState state = veriplace.open(request, response);
      assertTrue(state.isCallback());
      assertNotNull(state.getLastErrorException());
      
      try {
         state.requireUser();
         fail("Expected UserDiscoveryNotPermittedException");
      }
      catch (UserDiscoveryNotPermittedException e) {
         assertNotNull(e.getCause());
         assertEquals(VeriplaceOAuthException.class, e.getCause().getClass());
         assertEquals(401, ((VeriplaceOAuthException) e.getCause()).getCode());
      }
   }

   @Test
   public void testRequireUserImmediateSuccess() throws Exception {
      // Calling requireUserImmediate succeeds (and doesn't redirect) if you got a callback
      // with a valid user discovery token.
      
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldGrantAccessToken(REQUEST_TOKEN, VERIFIER, ACCESS_TOKEN);
      
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 200, USER_DOCUMENT);
      
      VeriplaceState state = veriplace.open(request, response);
      assertTrue(state.isCallback());
      assertNull(state.getLastErrorException());
      
      state.requireUserImmediate();
      
      assertNotNull(state.getUser());
      assertEquals(USER_ID, state.getUser().getId());
   }
   
   @Test
   public void testRequireUserImmediateNoCallback() throws Exception {
      // Calling requireUserImmediate fails (and doesn't redirect) if you don't have a 
      // callback token.
      
      VeriplaceState state = veriplace.open(request, response);
      assertFalse(state.isCallback());
      
      try {
         state.requireUserImmediate();
         fail("Expected UserDiscoveryNotPermittedException");
      }
      catch (UserDiscoveryNotPermittedException e) {
         assertNull(e.getCause());
      }
   }
   
   @Test
   public void testRequireUserImmediateNoToken() throws Exception {
      // Calling requireUserImmediate fails (and doesn't redirect) if you have a
      // callback request token but are denied an access token.
      
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldRefuseAccessToken(REQUEST_TOKEN, VERIFIER);
      
      VeriplaceState state = veriplace.open(request, response);
      assertTrue(state.isCallback());
      assertNotNull(state.getLastErrorException());
      
      try {
         state.requireUserImmediate();
      }
      catch (UserDiscoveryNotPermittedException e) {
         assertNotNull(e.getCause());
         assertEquals(401, e.getCause().getCode());
      }
   }
   
   @Test
   public void testRequireUserImmediateRequestDenied() throws Exception {
      // Calling requireUserImmediate fails (and doesn't redirect) if you have a
      // valid access token but the server denies the user discovery request. 
      
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldGrantAccessToken(REQUEST_TOKEN, VERIFIER, ACCESS_TOKEN);

      prepareUserDiscoveryRequest(ACCESS_TOKEN, 401, null);

      VeriplaceState state = veriplace.open(request, response);
      assertTrue(state.isCallback());
      assertNull(state.getLastErrorException());
      
      try {
         state.requireUserImmediate();
      }
      catch (UserDiscoveryNotPermittedException e) {
         assertNotNull(e.getCause());
         assertEquals(401, e.getCause().getCode());
      }
   }
   
   @Test
   public void testGetLocationPermissionRequirementImmediateSuccess() throws Exception {
      // Starting with a user ID but no location access token:  permission requirement
      // tries a direct permission query (GetLocationAPI.getLocationAccessToken) first
      request.addParameter("foo", "bar");
      User user = new User(USER_ID);
      VeriplaceState state = veriplace.open(request, response);
      state.setUser(user);
      
      prepareLocationAccessTokenRequestSuccess(ACCESS_TOKEN);

      state.requireGetLocationPermission();
      assertTrue(state.hasGetLocationPermission());
      assertEquals(ACCESS_TOKEN_VALUE, state.getGetLocationPermissionToken().getToken());
   }
   
   @Test
   public void testGetLocationPermissionRequirementStep1() throws Exception {
      // Starting with a user ID but no location access token:  permission requirement
      // tries a direct permission query (GetLocationAPI.getLocationAccessToken) and
      // if that fails, then it performs a user authorization redirect
      request.addParameter("foo", "bar");
      User user = new User(USER_ID);
      VeriplaceState state = veriplace.open(request, response);
      state.setUser(user);
      
      prepareLocationAccessTokenRequestFailure();
      
      String shouldRedirectTo = createUserAuthRedirectUrl(REQUEST_TOKEN, LOCATION_REQUEST_URI);

      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      
      mockServer.shouldGrantUserAuthorizationRedirect(REQUEST_TOKEN, VERIFIER,
            LOCATION_REQUEST_URI, client.getServerUri(), true);
      
      try {
         state.requireGetLocationPermission();
         fail("Expected RedirectedToVeriplaceException");
      }
      catch (RedirectedToVeriplaceException e) {
         assertEquals(shouldRedirectTo, e.getRedirectedToUrl());
      }
   }
   
   @Test
   public void testGetLocationPermissionRequirementStep2() throws Exception {
      // Returning via callback from successful location authorization:
      // permission requirement grabs the access token
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldGrantAccessToken(REQUEST_TOKEN, VERIFIER, ACCESS_TOKEN);

      VeriplaceState state = veriplace.open(request, response);
      state.setUser(USER);

      state.requireGetLocationPermission();
      assert(state.hasGetLocationPermission());
      assertEquals(ACCESS_TOKEN_VALUE, state.getGetLocationPermissionToken().getToken());
      assertNotNull(veriplace.getGetLocationTokenStore().get(USER));
      assertEquals(ACCESS_TOKEN_VALUE, veriplace.getGetLocationTokenStore().get(USER).getToken());
   }

   @Test
   public void testGetLocationPermissionRequirementStep2BadAccessToken() throws Exception {
      // Returning via callback from authorization, with a request token, but there's
      // no valid access token.
      
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldRefuseAccessToken(REQUEST_TOKEN, VERIFIER);

      veriplace.setStatusViewRenderer(null);  // so it won't try to display its own wait page

      User user = new User(USER_ID);
      VeriplaceState state = veriplace.open(request, response);
      assertTrue(state.isCallback());
      assertNotNull(state.getLastErrorException());
      state.setUser(user);
      
      try {
         state.requireGetLocationPermission();
         fail("Expected GetLocationNotPermittedException");
      }
      catch (GetLocationNotPermittedException e) {
         assertNotNull(e.getCause());
         assertEquals(VeriplaceOAuthException.class, e.getCause().getClass());
         assertEquals(401, ((VeriplaceOAuthException) e.getCause()).getCode());
      }
   }

   @Test
   public void testLocationRequirement() throws Exception {
      // Starting with a user ID and an access token:  location requirement
      // starts an asynchronous location request and displays the "please wait" page
      
      veriplace.setStatusViewRenderer(null);  // so it won't try to display its own wait page
      
      VeriplaceState state = veriplace.open(request, response);
      User user = new User(USER_ID);
      state.setUser(user);
      state.setGetLocationPermissionToken(ACCESS_TOKEN);
      veriplace.getGetLocationTokenStore().put(state.getUser(), state.getGetLocationPermissionToken());
      
      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_DOCUMENT);
      
      try {
         state.requireLocation();
         fail("Expected WaitingException");
      }
      catch (WaitingException e) {
         validateAsyncRequestCallbackParameters(e);
      }
      
      // simulate a refresh on the waiting page; the asynchronous task should complete successfully
      request = new MockHttpServletRequest();
      addAsyncRequestCallbackParameters();
      
      state = veriplace.open(request, response);
      
      state.requireLocation();
      Location location = state.getLocation();
      assertNotNull(location);
      assertEquals(LOCATION_ID, location.getId());
      
      // and the access token is now cached
      Token token = veriplace.getGetLocationTokenStore().get(user);
      assertNotNull(token);
      assertEquals(ACCESS_TOKEN.getToken(), token.getToken());
   }
   
   @Test
   public void testLocationRequirementSynchronous() throws Exception {
      // Starting with a user ID and an access token, when asynchronous requests are
      // disabled:  location requirement performs the location request immediately.
      
      VeriplaceState state = veriplace.open(request, response);
      User user = new User(USER_ID);
      state.setUser(user);
      state.setGetLocationPermissionToken(ACCESS_TOKEN);
      veriplace.getGetLocationTokenStore().put(state.getUser(), state.getGetLocationPermissionToken());
      state.setAsynchronousRequestAllowed(false);
      
      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_DOCUMENT);
      
      state.requireLocation();
      
      Location location = state.getLocation();
      assertNotNull(location);
      assertEquals(LOCATION_ID, location.getId());
      
      // and the access token is now cached
      Token token = veriplace.getGetLocationTokenStore().get(user);
      assertNotNull(token);
      assertEquals(ACCESS_TOKEN.getToken(), token.getToken());
   }
   
   @Test
   public void testLocationRequirementUsesCachedToken() throws Exception {
      // If the Veriplace instance has previously cached a permission token for the same
      // user, requireLocation skips the initial permission request step and goes
      // directly to the location query.
      
      request.addParameter("foo", "bar");
      
      veriplace.setStatusViewRenderer(null);  // so it won't try to display its own wait page
      
      VeriplaceState state = veriplace.open(request, response);
      state.setUser(USER);
      veriplace.getGetLocationTokenStore().put(USER, ACCESS_TOKEN);

      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_DOCUMENT);
      
      try {
         state.requireLocation();
         fail("Expected WaitingException");
      }
      catch (WaitingException e) {
         validateAsyncRequestCallbackParameters(e);
      }

      // and the access token is still cached
      Token token = veriplace.getGetLocationTokenStore().get(USER);
      assertNotNull(token);
      assertEquals(ACCESS_TOKEN.getToken(), token.getToken());
   }

   @Test
   public void testLocationRequirementRestartsIfCachedTokenFails() throws Exception {
      // If the Veriplace instance has previously cached a permission token for the same
      // user, requireLocation skips directly to the location query and tries to use this
      // token; however, if it gets a permission failure error (401), it throws away the
      // cached token and tries an explicit permission request.  It should only repeat
      // this flow once!
      
      request.addParameter("foo", "bar");
      Token betterAccessToken = new Token("access!", "yes!");
      
      veriplace.setStatusViewRenderer(null);  // so it won't try to display its own wait page
      
      VeriplaceState state = veriplace.open(request, response);
      state.setUser(USER);
      veriplace.getGetLocationTokenStore().put(USER, ACCESS_TOKEN);

      // here's the initial location request with the cached token
      prepareLocationRequest(ACCESS_TOKEN, null, 401, null);

      try {
         state.requireLocation();
         fail("Expected WaitingException");
      }
      catch (WaitingException e) {
         validateAsyncRequestCallbackParameters(e);
      }

      // now simulate a refresh on the waiting page; the asynchronous task will have failed,
      // and it should start over from the location permission step
      request = new MockHttpServletRequest();
      addAsyncRequestCallbackParameters();

      state = veriplace.open(request, response);

      prepareLocationAccessTokenRequestSuccess(betterAccessToken);

      prepareLocationRequest(betterAccessToken, null, 200, LOCATION_DOCUMENT);
      
      try {
         state.requireLocation();
         fail("Expected WaitingException");
      }
      catch (WaitingException e) {
         validateAsyncRequestCallbackParameters(e);
      }

      // this time when we refresh the waiting page, the request has succeeded with the new token
      request = new MockHttpServletRequest();
      addAsyncRequestCallbackParameters();

      state = veriplace.open(request, response);

      state.requireLocation();
      Location location = state.getLocation();
      assertNotNull(location);
      assertEquals(LOCATION_ID, location.getId());
      
      // and the new token has been stored in the cache
      Token token = veriplace.getGetLocationTokenStore().get(USER);
      assertNotNull(token);
      assertEquals(betterAccessToken.getToken(), token.getToken());
   }

   @Test
   public void testLocationRequirementRestartsOnlyOnce() throws Exception {
      // Same as the previous test, but this time when it repeats the permission flow,
      // the server returns another 401 error.  The client should *not* repeat the flow
      // again in this case; it means the server has for some reason chosen to grant us
      // a token that can't actually be used, and we don't want to get into an infinite
      // loop of attempting to use it.
      
      request.addParameter("foo", "bar");
      Token betterAccessToken = new Token("access!", "yes!");
      
      veriplace.setStatusViewRenderer(null);  // so it won't try to display its own wait page
      
      VeriplaceState state = veriplace.open(request, response);
      state.setUser(USER);
      veriplace.getGetLocationTokenStore().put(USER, ACCESS_TOKEN);

      // here's the initial location request with the cached token
      prepareLocationRequest(ACCESS_TOKEN, null, 401, null);

      try {
         state.requireLocation();
         fail("Expected WaitingException");
      }
      catch (WaitingException e) {
         validateAsyncRequestCallbackParameters(e);
      }

      // now simulate a refresh on the waiting page; the asynchronous task will have failed,
      // and it should start over from the location permission step
      request = new MockHttpServletRequest();
      addAsyncRequestCallbackParameters();

      state = veriplace.open(request, response);
      
      prepareLocationAccessTokenRequestSuccess(betterAccessToken);

      prepareLocationRequest(betterAccessToken, null, 401, null);
      
      try {
         state.requireLocation();
         fail("Expected WaitingException");
      }
      catch (WaitingException e) {
         validateAsyncRequestCallbackParameters(e);
      }

      // this time when we refresh the waiting page, the request has failed again and does
      // not try to continue
      request = new MockHttpServletRequest();
      addAsyncRequestCallbackParameters();
      state = veriplace.open(request, response);

      try {
         state.requireLocation();
         fail("Expected GetLocationNotPermittedException");
      }
      catch (GetLocationNotPermittedException e) {
      }
      
      // and we no longer have a cached token for this user
      assertNull(veriplace.getGetLocationTokenStore().get(USER));
   }
}
