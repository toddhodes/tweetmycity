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

import java.io.File;

/**
 * Unit tests for {@link com.veriplace.client.store.FileTokenStore}.
 */
public class TestFileTokenStore extends TestTokenStore {

   private static final String TEMP_FILE_PATH = "/tmp/TestFileTokenStore";
   
   protected TokenStore createTokenStore() throws Exception {
      File dir = new File(TEMP_FILE_PATH);
      dir.mkdirs();
      for (File drek: dir.listFiles()) {
         drek.delete();
      }
      return new FileTokenStore("token", TEMP_FILE_PATH);
   }
}
