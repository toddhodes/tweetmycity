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

import com.veriplace.oauth.consumer.Token;

import org.junit.Before;
import org.junit.Test;

/**
 * Base class for {@link com.veriplace.client.store.TokenStore} unit tests.
 */
public abstract class TestTokenStore {

   protected TokenStore store;
   protected Token token1;
   protected Token token2;
   
   @Before
   public void setUp() throws Exception {
      store = createTokenStore();

      token1 = new Token("foo", "bar");
      token2 = new Token("goo", "baz");
   }
   
   protected abstract TokenStore createTokenStore() throws Exception;
   
   @Test
   public void testGetNotFound() throws Exception {
      Token token = store.get("foo");
      assertNull(token);
   }

   @Test
   public void testPutGet() throws Exception {
      store.add(token1);
      store.add(token2);
      
      Token token = store.get(token1.getToken());
      assertNotNull(token);
      assertEquals(token1.getToken(), token.getToken());
      assertEquals(token1.getTokenSecret(), token.getTokenSecret());
      
      token = store.get(token2.getToken());
      assertNotNull(token);
      assertEquals(token2.getToken(), token.getToken());
      assertEquals(token2.getTokenSecret(), token.getTokenSecret());
   }
   
   @Test
   public void testRemove() throws Exception {
      testPutGet();
      
      store.remove(token1);
      
      Token token = store.get(token1.getToken());
      assertNull(token);
      
      token = store.get(token2.getToken());
      assertNotNull(token);      
      assertEquals(token2.getToken(), token.getToken());
      assertEquals(token2.getTokenSecret(), token.getTokenSecret());
   }
}
