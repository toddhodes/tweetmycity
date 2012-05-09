/* Copyright 2008-2009 WaveMarket, Inc.
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

import com.veriplace.oauth.consumer.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * An in-memory implemention of {@link TokenStore}
 */
public class MemoryTokenStore
   implements TokenStore {
   
   private Map<String,Token> tokens = new HashMap<String,Token>();

   /**
    * Get a token by value.
    */
   public synchronized Token get(String token) {
      return tokens.get(token);
   }

   /**
    * Add a token to this store.
    */
   public synchronized void add(Token token) {
      tokens.put(token.getToken(),token);
   }

   /**
    * Remove a token from this store.
    */
   public synchronized void remove(Token token) {
      tokens.remove(token.getToken());
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


