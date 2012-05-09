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
package com.veriplace.client.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.veriplace.client.User;
import com.veriplace.oauth.consumer.Token;

import org.junit.Before;
import org.junit.Test;

/**
 * Base class for {@link com.veriplace.client.store.UserTokenStore} unit tests.
 */
public abstract class TestUserTokenStore {

   protected UserTokenStore store;
   protected User user1;
   protected User user2;
   protected Token token1;
   protected Token token2;
   
   @Before
   public void setUp() throws Exception {
      store = createUserTokenStore();

      token1 = new Token("foo", "bar");
      token2 = new Token("goo", "baz");
      user1 = new User(1L);
      user2 = new User(2L);
   }
   
   protected abstract UserTokenStore createUserTokenStore() throws Exception;
   
   @Test
   public void testGetNotFound() throws Exception {
      Token token = store.get(user1);
      assertNull(token);
   }

   @Test
   public void testPutGet() throws Exception {
      store.put(user1, token1);
      store.put(user2, token2);
      
      Token token = store.get(user1);
      assertNotNull(token);
      assertEquals(token1.getToken(), token.getToken());
      assertEquals(token1.getTokenSecret(), token.getTokenSecret());
      
      token = store.get(user2);
      assertNotNull(token);
      assertEquals(token2.getToken(), token.getToken());
      assertEquals(token2.getTokenSecret(), token.getTokenSecret());
   }
   
   @Test
   public void testRemove() throws Exception {
      testPutGet();
      
      store.remove(user1);
      
      Token token = store.get(user1);
      assertNull(token);
      
      token = store.get(user2);
      assertNotNull(token);      
      assertEquals(token2.getToken(), token.getToken());
      assertEquals(token2.getTokenSecret(), token.getTokenSecret());
   }

   @Test
   public void testReplace() throws Exception {
      testPutGet();
      
      Token token3 = new Token("boo", "far");
      store.put(user1, token3);
      
      Token token = store.get(user1);
      assertNotNull(token);
      assertEquals(token3.getToken(), token.getToken());
      assertEquals(token3.getTokenSecret(), token.getTokenSecret());
      
      token = store.get(user2);
      assertNotNull(token);      
      assertEquals(token2.getToken(), token.getToken());
      assertEquals(token2.getTokenSecret(), token.getTokenSecret());
   }
}
