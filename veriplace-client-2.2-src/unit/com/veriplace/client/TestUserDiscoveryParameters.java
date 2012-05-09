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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for {@link com.veriplace.client.UserDiscoveryParameters}.
 */
public class TestUserDiscoveryParameters extends TestBase {

   private static final String PHONE = "1115551212";
   private static final String PHONE2 = "1115551213";
   private static final String EMAIL = "foo@bar.com";
   private static final String EMAIL2 = "foo@baz.com";
   private static final String OPENID = "abc123";
   private static final String OPENID2 = "abc124";
   
   @Test
   public void testByPhone() {
      UserDiscoveryParameters udp = UserDiscoveryParameters.byPhone(PHONE);
      assertNotNull(udp);
      assertEquals(PHONE, udp.getPhone());
      assertNull(udp.getEmail());
      assertNull(udp.getOpenId());
   }
   
   @Test
   public void testByEmail() {
      UserDiscoveryParameters udp = UserDiscoveryParameters.byEmail(EMAIL);
      assertNotNull(udp);
      assertNull(udp.getPhone());
      assertEquals(EMAIL, udp.getEmail());
      assertNull(udp.getOpenId());
   }
   
   @Test
   public void testByOpenId() {
      UserDiscoveryParameters udp = UserDiscoveryParameters.byOpenId(OPENID);
      assertNotNull(udp);
      assertNull(udp.getPhone());
      assertNull(udp.getEmail());
      assertEquals(OPENID, udp.getOpenId());
   }

   @Test
   public void testSetters() {
      UserDiscoveryParameters udp = new UserDiscoveryParameters();
      assertFalse(udp.isSpecified());
      udp.setPhone(PHONE);
      assertEquals(PHONE, udp.getPhone());
      assertTrue(udp.isSpecified());
      udp.setPhone(PHONE2);
      assertEquals(PHONE2, udp.getPhone());

      try {
         udp.setEmail(EMAIL);
         fail("Expected IllegalArgumentException");
      }
      catch (IllegalArgumentException e) {
      }
      try {
         udp.setOpenId(OPENID);
         fail("Expected IllegalArgumentException");
      }
      catch (IllegalArgumentException e) {
      }

      udp.setPhone(null);
      assertNull(udp.getPhone());
      udp.setEmail(EMAIL);
      assertEquals(EMAIL, udp.getEmail());
      assertTrue(udp.isSpecified());
      udp.setEmail(EMAIL2);
      assertEquals(EMAIL2, udp.getEmail());

      try {
         udp.setPhone(PHONE);
         fail("Expected IllegalArgumentException");
      }
      catch (IllegalArgumentException e) {
      }
      try {
         udp.setOpenId(OPENID);
         fail("Expected IllegalArgumentException");
      }
      catch (IllegalArgumentException e) {
      }
      
      udp.setEmail(null);
      assertNull(udp.getEmail());
      udp.setOpenId(OPENID);
      assertEquals(OPENID, udp.getOpenId());
      udp.setOpenId(OPENID2);
      assertEquals(OPENID2, udp.getOpenId());

      try {
         udp.setEmail(EMAIL);
         fail("Expected IllegalArgumentException");
      }
      catch (IllegalArgumentException e) {
      }
      try {
         udp.setPhone(PHONE);
         fail("Expected IllegalArgumentException");
      }
      catch (IllegalArgumentException e) {
      }
   }
   
   @Test
   public void testEquals() {
      UserDiscoveryParameters udp1 = new UserDiscoveryParameters();
      UserDiscoveryParameters udp2 = new UserDiscoveryParameters();
      assertTrue(udp1.equals(udp2));
      
      udp1.setPhone(PHONE);
      assertFalse(udp1.equals(udp2));
      udp2.setPhone(PHONE);
      assertTrue(udp1.equals(udp2));
      
      udp1.setPhone(null);
      udp1.setEmail(EMAIL);
      assertFalse(udp1.equals(udp2));
      udp2.setPhone(null);
      udp2.setEmail(EMAIL);
      assertTrue(udp1.equals(udp2));

      udp1.setEmail(null);
      udp1.setOpenId(OPENID);
      assertFalse(udp1.equals(udp2));
      udp2.setEmail(null);
      udp2.setOpenId(OPENID);
      assertTrue(udp1.equals(udp2));
   }
}
