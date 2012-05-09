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
package com.veriplace.client.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryParameters;
import com.veriplace.client.TestData;

import org.junit.Test;
import org.w3c.dom.Document;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link com.veriplace.client.factory.UserFactory}.
 */
public class TestUserFactory 
   implements TestData {

   protected UserFactory factory = new UserFactory();
   protected DocumentFactory documentFactory = new DocumentFactory();
   
   @Test
   public void testGetUser() throws Exception {

      Document document = documentFactory.getDocument(USER_DOCUMENT.getBytes());

      User user = factory.getUser(document);
      assertNotNull(user);

      assertEquals((long)user.getId(),999);
   }

   @Test
   public void testGetUsers() throws Exception {

      Document document = documentFactory.getDocument(USERS_DOCUMENT.getBytes());

      List<User> users = factory.getUsers(document);
      assertNotNull(users);
      assertEquals(users.size(),2);

      long[] ids = { 999L, 997L };

      int i = 0;
      for (User user: users) {
         assertEquals((long)user.getId(),ids[i++]);
      }
   }

   @Test
   public void testGetUsersByPII() throws Exception {

      Document document = documentFactory.getDocument(USERS_BY_PII_DOCUMENT.getBytes());

      Map<UserDiscoveryParameters,User> users = factory.getUsersByPII(document);
      assertNotNull(users);
      assertEquals(users.size(),2);

      UserDiscoveryParameters key1 = UserDiscoveryParameters.byPhone("1115551212");
      UserDiscoveryParameters key2 = UserDiscoveryParameters.byPhone("1115551213");

      assertTrue(users.containsKey(key1));
      assertTrue(users.containsKey(key2));
      assertEquals((long)users.get(key1).getId(),999L);
      assertEquals((long)users.get(key2).getId(),997L);
   }
}

/*
** Local Variables:
**   c-basic-offset: 3
**   tab-width: 3
**   indent-tabs-mode: nil
** End:
**
** ex: set softtabstop=3 tabstop=3 expandtab cindent shiftwidth=3
*/
