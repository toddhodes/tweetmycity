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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.veriplace.oauth.message.RequestMethod;

import org.junit.Test;

/**
 * Unit tests for {@link com.veriplace.client.SetLocationAPI}.  These tests verify that the
 * API sends the correct parameters to the server (although we do not check OAuth signatures
 * here) and correctly decodes both success and failure responses.
 */
public class TestSetLocationAPI extends TestBase {

   private static final String SET_LOCATION_REQUEST_URI = "/api/1.0/users/" + USER_ID + "/location";

   @Test
   public void testGetRedirectURL() throws Exception {
      String callback = createCallback();
      String expectedUrl = createUserAuthRedirectUrl(REQUEST_TOKEN, SET_LOCATION_REQUEST_URI);

      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      String url = client.getSetLocationAPI().getRedirectURL(callback, USER);
      assertEquals(expectedUrl, url);
   }
   
   @Test
   public void testSetLocationLongLatSuccess() throws Exception {
      String latitude = String.valueOf(LATITUDE);
      String longitude = String.valueOf(LONGITUDE);
      String accuracy = String.valueOf(ACCURACY);
      
      MockServerOAuthStep step = mockServer.addStep();
      step.setExpectedMethod(RequestMethod.POST);
      step.setExpectedRelativeUrl(SET_LOCATION_REQUEST_URI);
      step.setExpectedToken(ACCESS_TOKEN);
      step.setExpectedParameter("longitude", longitude);
      step.setExpectedParameter("latitude", latitude);
      step.setExpectedParameter("accuracy", accuracy);
      step.setResponseCode(200);
      step.setResponseBody(LOCATION_UPDATE_DOCUMENT);
      
      Location location = client.getSetLocationAPI().setLocation(ACCESS_TOKEN, USER,
            longitude, latitude, accuracy);
      
      assertNotNull(location);
      // we'll check the other location properties in TestLocationFactory
   }

   @Test
   public void testSetLocationGeocodingSuccess() throws Exception {
      String address = STREET;
      
      MockServerOAuthStep step = mockServer.addStep();
      step.setExpectedMethod(RequestMethod.POST);
      step.setExpectedRelativeUrl(SET_LOCATION_REQUEST_URI);
      step.setExpectedToken(ACCESS_TOKEN);
      step.setExpectedParameter("location", address);
      step.setResponseCode(200);
      step.setResponseBody(LOCATION_UPDATE_DOCUMENT);
      
      Location location = client.getSetLocationAPI().setLocation(ACCESS_TOKEN, USER, address);
      
      assertNotNull(location);
      // we'll check the other location properties in TestLocationFactory
   }

   @Test
   public void testSetLocationBadParameter() throws Exception {
      // For HTTP error 400, throw BadParameterException
      
      String address = STREET;
      
      MockServerOAuthStep step = mockServer.addStep();
      step.setExpectedMethod(RequestMethod.POST);
      step.setExpectedRelativeUrl(SET_LOCATION_REQUEST_URI);
      step.setExpectedToken(ACCESS_TOKEN);
      step.setExpectedParameter("location", address);
      step.setResponseCode(400);

      try {
         client.getSetLocationAPI().setLocation(ACCESS_TOKEN, USER, address);
         fail("Expected BadParameterException");
      }
      catch (BadParameterException e) {
      }
   }

   @Test
   public void testSetLocationNotPermitted() throws Exception {
      // For HTTP error 401, throw SetLocationNotPermittedException
      
      String address = STREET;
      
      MockServerOAuthStep step = mockServer.addStep();
      step.setExpectedMethod(RequestMethod.POST);
      step.setExpectedRelativeUrl(SET_LOCATION_REQUEST_URI);
      step.setExpectedToken(ACCESS_TOKEN);
      step.setExpectedParameter("location", address);
      step.setResponseCode(401);

      try {
         client.getSetLocationAPI().setLocation(ACCESS_TOKEN, USER, address);
         fail("Expected SetLocationNotPermittedException");
      }
      catch (SetLocationNotPermittedException e) {
         assertNotNull(e.getCause());
         assertEquals(VeriplaceOAuthException.class, e.getCause().getClass());
         assertEquals(401, ((VeriplaceOAuthException) e.getCause()).getCode());
      }
   }

   @Test
   public void testSetLocationOtherError() throws Exception {
      // For HTTP errors other than 400 and 401, throw VeriplaceOAuthException
      
      String address = STREET;
      
      MockServerOAuthStep step = mockServer.addStep();
      step.setExpectedMethod(RequestMethod.POST);
      step.setExpectedRelativeUrl(SET_LOCATION_REQUEST_URI);
      step.setExpectedToken(ACCESS_TOKEN);
      step.setExpectedParameter("location", address);
      step.setResponseCode(500);

      try {
         client.getSetLocationAPI().setLocation(ACCESS_TOKEN, USER, address);
         fail("Expected VeriplaceOAuthException");
      }
      catch (VeriplaceOAuthException e) {
         assertEquals(500, e.getCode());
      }
   }
}
