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

import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Revision;

import org.junit.Test;

/**
 * Unit tests for {@link com.veriplace.client.UserDiscoveryAPI}.  These tests verify that the
 * API sends the correct parameters to the server (although we do not check OAuth signatures
 * here) and correctly decodes both success and failure responses.
 */
public class TestUserDiscoveryAPI extends TestBase {

   @Test
   public void testGetRedirectURL() throws Exception {
      String callback = createCallback();
      String expectedUrl = createUserAuthRedirectUrl(REQUEST_TOKEN, USER_REQUEST_URI);

      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      String url = client.getUserDiscoveryAPI().getRedirectURL(callback, USER);
      assertEquals(expectedUrl, url);
   }
   
   @Test
   public void testGetUserSuccess() throws Exception {
      
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 200, USER_DOCUMENT);
      
      User user = client.getUserDiscoveryAPI().getUser(ACCESS_TOKEN);
      
      assertNotNull(user);
      assertEquals(USER_ID, user.getId());
   }
   
   @Test
   public void testGetUserBadParameter() throws Exception {
      // For HTTP error 400, throw BadParameterException

      prepareUserDiscoveryRequest(ACCESS_TOKEN, 400, null);
      
      try {
         client.getUserDiscoveryAPI().getUser(ACCESS_TOKEN);
         fail("Expected BadParameterException");
      }
      catch (BadParameterException e) {
      }
   }
   
   @Test
   public void testGetUserNotPermitted() throws Exception {
      // For HTTP error 401, throw UserDiscoveryNotPermittedException
      
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 401, null);
      
      try {
         client.getUserDiscoveryAPI().getUser(ACCESS_TOKEN);
         fail("Expected UserDiscoveryNotPermittedException");
      }
      catch (UserDiscoveryNotPermittedException e) {
         assertNotNull(e.getCause());
         assertEquals(VeriplaceOAuthException.class, e.getCause().getClass());
         assertEquals(401, ((VeriplaceOAuthException) e.getCause()).getCode());
      }
   }
   
   @Test
   public void testGetUserNotFound() throws Exception {
      // For HTTP error 404, throw UserNotFoundException
      
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 404, null);
      
      try {
         client.getUserDiscoveryAPI().getUser(ACCESS_TOKEN);
         fail("Expected UserNotFoundException");
      }
      catch (UserNotFoundException e) {
      }
   }
   
   @Test
   public void testGetUserOtherError() throws Exception {
      // For HTTP errors other than 400, 401, and 404, throw VeriplaceOAuthException
      
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 500, null);
      
      try {
         client.getUserDiscoveryAPI().getUser(ACCESS_TOKEN);
         fail("Expected VeriplaceOAuthException");
      }
      catch (VeriplaceOAuthException e) {
         assertEquals(500, e.getCode());
      }
   }
   
   public void testGetUserByEmail() throws Exception {
      // Successful query for user by email address:
      // client sends correct parameters, correctly decodes response
      
      createClientWithAppToken(APP_TOKEN);
      String email = "foo@bar.com";
      
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 200, USER_DOCUMENT)
            .setExpectedToken(APP_TOKEN)
            .setExpectedParameter("email", email);
      
      User user = client.getUserDiscoveryAPI().getUserByEmail(email);
      
      assertNotNull(user);
      assertEquals(USER_ID, user.getId());
   }
   
   
   public void testGetUserByMobileNumber() throws Exception {
      // Successful query for user by mobile number:
      // client sends correct parameters, correctly decodes response
      
      createClientWithAppToken(APP_TOKEN);
      String number = "1115551212";

      prepareUserDiscoveryRequest(ACCESS_TOKEN, 200, USER_DOCUMENT)
            .setExpectedToken(APP_TOKEN)
            .setExpectedParameter("mobile", number);
      
      User user = client.getUserDiscoveryAPI().getUserByMobileNumber(number);
      
      assertNotNull(user);
      assertEquals(USER_ID, user.getId());
   }
   
   public void testGetUserByOpenId() throws Exception {
      // Successful query for user by OpenID:
      // client sends correct parameters, correctly decodes response
      
      createClientWithAppToken(APP_TOKEN);
      String openid = "abcdef";

      prepareUserDiscoveryRequest(ACCESS_TOKEN, 200, USER_DOCUMENT)
            .setExpectedToken(APP_TOKEN)
            .setExpectedParameter("openid", openid);
      
      User user = client.getUserDiscoveryAPI().getUserByOpenId(openid);
      
      assertNotNull(user);
      assertEquals(USER_ID, user.getId());
   }

   public void testGetUserByEmailNoAppToken() throws Exception {
      // Query for user by email, but app doesn't have an app token:
      // server sends 401 error, client throws the appropriate exception
      
      // (Note, there aren't separate versions of this test for
      // mobile number/OpenID, because they all go through the same
      // code path)
      
      String email = "foo@bar.com";
      
      prepareUserDiscoveryRequest(ACCESS_TOKEN, 401, null)
            .setExpectedParameter("email", email);
      
      try {
         client.getUserDiscoveryAPI().getUserByEmail(email);
         fail("Expected UserDiscoveryNotPermittedException");
      }
      catch (UserDiscoveryNotPermittedException e) {
      }
   }
   
   public void testGetUserByEmailNotFound() throws Exception {
      // Query for user by email; app has valid app token, but user is not
      // found; server sends 404 error, client throws the appropriate exception
      
      // (Note, there aren't separate versions of this test for
      // mobile number/OpenID, because they all go through the same
      // code path)
      
      createClientWithAppToken(APP_TOKEN);
      String email = "foo@bar.com";

      prepareUserDiscoveryRequest(ACCESS_TOKEN, 404, null)
            .setExpectedParameter("email", email);
      
      try {
         client.getUserDiscoveryAPI().getUserByEmail(email);
         fail("Expected DiscoveryException");
      }
      catch (UserNotFoundException e) {
      }
   }
   
   protected void createClientWithAppToken(Token appToken) throws Exception {
      ClientConfiguration config = new ClientConfiguration(CONSUMER_KEY, CONSUMER_SECRET);
      config.setProtocol(Revision.Core1_0);
      config.setApplicationToken(appToken);
      client = new Client(config);
   }
}
