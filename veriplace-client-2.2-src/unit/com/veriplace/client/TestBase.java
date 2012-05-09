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

import static org.junit.Assert.assertTrue;

import com.veriplace.client.store.MemoryTokenStore;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.web.WaitingException;

import java.net.URLEncoder;

import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Base class for all but the simplest unit tests.  Provides a Veriplace client which
 * will communicate with a {@link MockServerOAuthClient}.  Has methods for simulating
 * and validating various kinds of requests and responses.  
 */
public class TestBase implements TestData {

   protected Client client;
   protected MockServerOAuthClient mockServer;
   protected MemoryTokenStore tokenStore;
   protected MockHttpServletRequest request;
   protected MockHttpServletResponse response;
   protected long currentAsyncRequestId;
   
   @Before
   public void setUp() throws Exception {
      mockServer = new MockServerOAuthClient(CONSUMER_KEY);
      tokenStore = new MemoryTokenStore();
      client = createClient();
      client.getConsumer().setClient(mockServer);
      currentAsyncRequestId = 0;
      newRequest();
   }
      
   protected Client createClient() throws Exception {
      ClientConfiguration config = new ClientConfiguration(CONSUMER_KEY, CONSUMER_SECRET);
      config.setTokenStore(tokenStore);
      return new Client(config);
   }
   
   protected void newRequest() {
      request = new MockHttpServletRequest();
      response = new MockHttpServletResponse();
   }
   
   protected String createCallback() {
      return "http://foo.com/bar";
   }
   
   protected String createUserAuthRedirectUrl(Token requestToken, String uri)
         throws Exception {
      return client.getServerUri() + "/api/userAuthorization?oauth_token=" + requestToken.getToken()
            + "&uri=" + URLEncoder.encode(client.getServerDirectUri() + uri, "UTF8");
   }
   
   /**
    * Sets request parameters to simulate a callback that contains a request token and
    * verifier; tells the mock server that the client should request an access token,
    * which the server will grant.
    */
   protected void prepareValidCallbackToken(Token accessToken) {
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldGrantAccessToken(REQUEST_TOKEN, VERIFIER, accessToken);
   }
   
   /**
    * Sets request parameters to simulate a callback that contains a request token and
    * verifier; tells the mock server that the client should request an access token,
    * which the server will refuse.
    */
   protected void prepareInvalidCallbackToken() {
      tokenStore.add(REQUEST_TOKEN);
      request.addParameter("oauth_token", REQUEST_TOKEN_VALUE);
      request.addParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldRefuseAccessToken(REQUEST_TOKEN, VERIFIER);
   }

   /**
    * Sets request parameters to simulate a callback that contains a user ID, as if
    * returning from a location redirect after the user has been identified.
    */
   protected void prepareCallbackUser(User user) {
      request.addParameter("veriplace_user_id", String.valueOf(user.getId()));
   }

   /**
    * Sets up pre- & postconditions for the following events:
    * <ul>
    * <li> The client submits a user discovery request with an access token. </li>
    * <li> The server responds with the specified status code and (if not null) document content. </li>
    * </ul>
    */
   protected MockServerOAuthStep prepareUserDiscoveryRequest(Token accessToken,
         int responseCode, String responseBody) {
      return mockServer.addStep("user discovery request")
            .setExpectedMethod(RequestMethod.GET)
            .setExpectedRelativeUrl(USER_REQUEST_URI)
            .setExpectedToken(accessToken)
            .setResponseCode(responseCode)
            .setResponseBody(responseBody);      
   }
   
   /**
    * Sets up pre- and postconditions for the series of requests carried out by
    * GetLocationAPI.getLocationAccessToken(); the access token is granted.
    */
   protected void prepareLocationAccessTokenRequestSuccess(Token accessToken) {
      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      mockServer.shouldGrantUserAuthorizationRedirect(REQUEST_TOKEN, VERIFIER,
            client.getServerDirectUri() + LOCATION_REQUEST_URI, "", true);
      mockServer.shouldGrantAccessToken(REQUEST_TOKEN, VERIFIER, accessToken);
   }

   /**
    * Sets up pre- and postconditions for the series of requests carried out by
    * GetLocationAPI.getLocationAccessToken(); the access token is refused.
    */
   protected void prepareLocationAccessTokenRequestFailure() {
      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      mockServer.shouldGrantUserAuthorizationRedirect(REQUEST_TOKEN, VERIFIER,
            client.getServerDirectUri() + LOCATION_REQUEST_URI, "", true);
      mockServer.shouldRefuseAccessToken(REQUEST_TOKEN, VERIFIER);
   }

   /**
    * Sets up pre- & postconditions for the following events:
    * <ul>
    * <li> The client submits a location request with an access token. </li>
    * <li> The server responds with the specified status code and (if not null) document content. </li>
    * </ul>
    */
   protected MockServerOAuthStep prepareLocationRequest(Token accessToken,
         String locationMode, int responseCode, String responseBody) {
      MockServerOAuthStep step = mockServer.addStep("location request")
            .setExpectedMethod(RequestMethod.POST)
            .setExpectedRelativeUrl(LOCATION_REQUEST_URI)
            .setExpectedToken(accessToken);
      if (locationMode != null) {
         step.setExpectedParameter("mode", locationMode);
      }
      return step.setResponseCode(responseCode)
            .setResponseBody(responseBody);
   }
   
   protected void addAsyncRequestCallbackParameters() {
      request.setParameter("veriplace_request_id", String.valueOf(currentAsyncRequestId));
      request.addParameter("veriplace_user_id", String.valueOf(USER_ID));
   }
   
   protected void validateAsyncRequestCallbackParameters(WaitingException e) {
      ++currentAsyncRequestId;
      assertTrue(e.getCallbackUrl().contains("veriplace_request_id=" + currentAsyncRequestId));
      assertTrue(e.getCallbackUrl().contains("veriplace_user_id=" + USER_ID));
   }
}
