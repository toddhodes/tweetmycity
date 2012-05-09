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

import com.veriplace.oauth.consumer.Token;

/**
 * Interface for tracking {@link Token}s between OAuth requests.
 */
public interface TokenStore {
   
   /**
    * Retrieve a token.
    * @return a token where {@link Token#getToken} equals the given value.
    */
   public Token get(String token);

   /**
    * Save a token.
    */
   public void add(Token token);

   /**
    * Remove a token from this store.
    */
   public void remove(Token token);
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


