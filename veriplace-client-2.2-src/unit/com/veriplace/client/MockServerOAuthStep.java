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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.veriplace.oauth.consumer.Client;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.Request;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;

import java.io.IOException;
import java.net.URL;

/**
 * Parameters for an expected request and simulated response from a {@link MockServerOAuthClient}.
 */
public class MockServerOAuthStep extends Client {

   protected String stepName;
   protected String expectedBaseServerUrl = "https://veriplace.com";
   protected String expectedRelativeUrl = "";
   protected RequestMethod expectedMethod;
   protected ParameterSet expectedParameters = new ParameterSet();
   protected String responseBody = null;
   protected int responseCode = 200;
   protected String responseReasonPhrase = "";
   protected ParameterSet responseParameters = new ParameterSet();
   protected ParameterSet responseHeaders = new ParameterSet(true);
   
   public MockServerOAuthStep(MockServerOAuthClient parent, String name) {
      this.stepName = name;
      parent.add(this);
   }
   
   public String getName() {
      return stepName;
   }
   
   @Override
   public Response getResponse(Request request, boolean storeHeaders)
         throws IOException {
      assertEquals(stepName, expectedMethod, request.getRequestMethod());

      String expectedUrl = expectedBaseServerUrl + expectedRelativeUrl;
      URL url = request.getBaseRequestUrl();
      String urlString = url.toExternalForm();
      String urlQuery = url.getQuery();
      if (urlQuery != null) {
         urlString = urlString.substring(0, urlString.length() - (urlQuery.length() + 1));
      }
      assertEquals(stepName, expectedUrl, urlString);

      ParameterSet combinedParameters = new ParameterSet();
      combinedParameters.putAll(request.getRequestParameters());
      combinedParameters.putAll(request.getProtocolParameters());
      combinedParameters.putAll(request.getAdditionalParameters());
      if (urlQuery != null) {
         setParameters(combinedParameters, urlQuery);
      }
      
      for (String name: expectedParameters.getKeys()) {
         String value = expectedParameters.getFirst(name);
         if (combinedParameters.contains(name)) {
            assertEquals(name, value, combinedParameters.getFirst(name));
         }
         else {
            fail(this.stepName + ": Missing expected request parameter: " + name);
         }
      }
      
      byte[] data = (responseBody == null) ? new byte[0] : responseBody.getBytes();
      String contentType = "text/xml";
      
      return new Response(responseCode, responseReasonPhrase, responseParameters, data, contentType,
            (storeHeaders ? responseHeaders : null),null);
   }
   
   public MockServerOAuthStep setExpectedMethod(RequestMethod method) {
      this.expectedMethod = method;
      return this;
   }
   
   public MockServerOAuthStep setExpectedServerBaseUrl(String url) {
      this.expectedBaseServerUrl = url;
      return this;
   }
   
   public MockServerOAuthStep setExpectedRelativeUrl(String url) {
      this.expectedRelativeUrl = url;
      return this;
   }
   
   public MockServerOAuthStep setExpectedParameter(String name, String value) {
      expectedParameters.put(name, value);
      return this;
   }
   
   public MockServerOAuthStep setExpectedParameter(Parameter parameter, String value) {
      expectedParameters.put(parameter, value);
      return this;
   }
   
   public MockServerOAuthStep setExpectedToken(Token token) {
      expectedParameters.put(Parameter.Token, token.getToken());
      expectedParameters.put(Parameter.TokenSecret, token.getTokenSecret());
      return this;
   }
   
   public MockServerOAuthStep setResponseHeader(String name, String value) {
      responseHeaders.put(name, value);
      return this;
   }
   
   public MockServerOAuthStep setResponseParameter(String name, String value) {
      responseParameters.put(name, value);
      return this;
   }

   public MockServerOAuthStep setResponseParameter(Parameter parameter, String value) {
      responseParameters.put(parameter, value);
      return this;
   }
  
   public MockServerOAuthStep setResponseCode(int code) {
      this.responseCode = code;
      return this;
   }
   
   public MockServerOAuthStep setResponseReasonPhrase(String phrase) {
      this.responseReasonPhrase = phrase;
      return this;
   }
   
   public MockServerOAuthStep setResponseBody(String body) {
      this.responseBody = body;
      return this;
   }
}
