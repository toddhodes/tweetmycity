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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.veriplace.oauth.message.RequestMethod;

import org.junit.Test;

/**
 * Unit tests for {@link com.veriplace.client.PermissionAPI}.  These tests verify that the
 * API sends the correct parameters to the server (although we do not check OAuth signatures
 * here) and correctly decodes both success and failure responses.
 */
public class TestPermissionAPI extends TestBase {

   private static final String PERMISSION_URI = "/api/1.0/permission";

   @Test
   public void testGetRedirectUrl() throws Exception {
      try {
         String callback = createCallback();
         client.getPermissionAPI().getRedirectURL(callback, USER);
         fail("Expected UnsupportedOperationException");
      }
      catch (UnsupportedOperationException e) {
      }
   }
   
   @Test
   public void testVerifySuccess() throws Exception {
      MockServerOAuthStep step = mockServer.addStep();
      step.setExpectedMethod(RequestMethod.GET);
      step.setExpectedRelativeUrl(PERMISSION_URI);
      step.setExpectedToken(ACCESS_TOKEN);
      step.setResponseCode(200);
      
      boolean success = client.getPermissionAPI().verify(ACCESS_TOKEN);
      assertTrue(success);
   }
   
   @Test
   public void testVerifyFailure() throws Exception {
      MockServerOAuthStep step = mockServer.addStep();
      step.setExpectedMethod(RequestMethod.GET);
      step.setExpectedRelativeUrl(PERMISSION_URI);
      step.setExpectedToken(ACCESS_TOKEN);
      step.setResponseCode(401);
      
      boolean success = client.getPermissionAPI().verify(ACCESS_TOKEN);
      assertFalse(success);
   }
}
