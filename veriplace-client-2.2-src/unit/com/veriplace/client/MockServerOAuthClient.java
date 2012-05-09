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

import static org.junit.Assert.fail;

import com.veriplace.oauth.consumer.Client;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.oauth.message.Request;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Mock implementation of an OAuth HTTP client (not the Veriplace client).
 * Can be configured with any number of steps, each of which represents a request
 * that we expect the Veriplace client to make, and the response that the server
 * should send.
 */
public class MockServerOAuthClient extends Client {

   protected boolean completed = false;
   protected Queue<MockServerOAuthStep> steps = new LinkedBlockingQueue<MockServerOAuthStep>();
   protected String expectedConsumerKey;

   public MockServerOAuthClient(String expectedConsumerKey) {
      this.expectedConsumerKey = expectedConsumerKey;
   }
   
   void add(MockServerOAuthStep step) {
      steps.add(step);
   }

   public MockServerOAuthStep addStep(String name) {
      return new MockServerOAuthStep(this, name);
   }
   
   public MockServerOAuthStep addStep() {
      if (! steps.isEmpty()) {
         fail("Need to provide a name for each server step if there's more than one step");
      }
      return new MockServerOAuthStep(this, "MockServerOAuthClient");
   }
   
   @Override
   public Response getResponse(Request request, boolean storeHeaders)
         throws IOException {
      if (steps.isEmpty()) {
         fail("Did not expect any more HTTP requests at this point");
      }
      MockServerOAuthStep next = steps.remove();
      return next.getResponse(request, storeHeaders);
   }
   
   public MockServerOAuthStep shouldGrantRequestToken(Token requestToken) {
      return addStep("request token request")
            .setExpectedRelativeUrl("/api/requestToken")
            .setExpectedMethod(RequestMethod.POST)
            .setExpectedParameter(Parameter.ConsumerKey, expectedConsumerKey)
            .setResponseParameter(Parameter.Token, requestToken.getToken())
            .setResponseParameter(Parameter.TokenSecret, requestToken.getTokenSecret());
   }
   
   public MockServerOAuthStep shouldGrantAccessToken(Token requestToken, String verifier,
         Token accessToken) {
      return addStep("access token request")
            .setExpectedRelativeUrl("/api/accessToken")
            .setExpectedMethod(RequestMethod.POST)
            .setExpectedParameter(Parameter.ConsumerKey, expectedConsumerKey)
            .setExpectedParameter(Parameter.Token, requestToken.getToken())
            .setExpectedParameter(Parameter.Verifier, verifier)
            .setResponseParameter(Parameter.Token, accessToken.getToken())
            .setResponseParameter(Parameter.TokenSecret, accessToken.getTokenSecret());
   }
   
   public MockServerOAuthStep shouldRefuseAccessToken(Token requestToken, String verifier) {
      return addStep("access token request")
            .setExpectedRelativeUrl("/api/accessToken")
            .setExpectedMethod(RequestMethod.POST)
            .setExpectedParameter(Parameter.ConsumerKey, expectedConsumerKey)
            .setExpectedParameter(Parameter.Token, requestToken.getToken())
            .setExpectedParameter(Parameter.Verifier, verifier)
            .setResponseCode(401)
            .setResponseReasonPhrase("No you can't");
   }
   
   public MockServerOAuthStep shouldGrantUserAuthorizationRedirect(Token requestToken,
         String verifier, String requestUri, String baseUrl, boolean immediate) {
      MockServerOAuthStep step = addStep("user auth redirect")
            .setExpectedServerBaseUrl("http://veriplace.com") // no SSL for user auth request
            .setExpectedRelativeUrl("/api/userAuthorization")
            .setExpectedMethod(RequestMethod.GET)
            .setExpectedParameter(Parameter.Token, requestToken.getToken());
      if (immediate) {
         step.setExpectedParameter("immediate", "true");
      }
      step.setExpectedParameter("uri", baseUrl + requestUri)
            .setResponseCode(302)
            .setResponseHeader("Location", baseUrl + requestUri
                  + "?oauth_token=" + requestToken.getToken() + "&oauth_verifier=" + verifier);
      return step;
   }
}
