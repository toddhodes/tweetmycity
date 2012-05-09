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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A file based implemention of {@link TokenStore}
 * <p>
 * This implementation maps the {@link FileTokenStore#get get}, 
 * {@link FileTokenStore#add add}, and {@link FileTokenStore#remove remove}
 * operations to file operations, where each token is stored in a file
 * with a name based on the value of {@link Token#getToken}.
 */
public class FileTokenStore
   implements TokenStore {

   private static final Log logger = LogFactory.getLog(FileTokenStore.class);

   /**
    * Default directory to store tokens. (/tmp)
    */
   public static final String DEFAULT_DIRECTORY = System.getProperty("java.io.tmpdir","/tmp");

   /**
    * Default file base to store token data. (token)
    */
   public static final String DEFAULT_FILE_BASE = "token";

   /**
    * Directory to store tokens.
    */
   protected final String directory;

   /**
    * File base to store tokens.
    */
   protected final String fileBase;

   /**
    * Create a new file token store using default values.
    */
   public FileTokenStore() {
      this(DEFAULT_FILE_BASE);
   }

   /**
    * Create a new file token store using custom fileBase.
    * @param fileBase the file base for storing tokens
    */
   public FileTokenStore(String fileBase) {
      this(fileBase,DEFAULT_DIRECTORY);
   }

   /**
    * Create a new file token store using custom fileBase and directory.
    * @param fileBase the file base for storing tokens
    * @param directory the directory for storing tokens
    */
   public FileTokenStore(String fileBase, 
                         String directory) {
      this.fileBase = fileBase;
      this.directory = directory;
   }
   
   /**
    * Get a token by value.
    */
   public synchronized Token get(String token) {

      try {
         File file = getFile(token);
         FileInputStream fis = new FileInputStream(file);

         logger.debug("Reading token from file: " + file.getPath());
         
         StringBuilder secret = new StringBuilder();
         
         int c;
         while ((c = fis.read()) != -1) {
            if (c == '\n') {
               break;
            }
            secret.append((char)c);
         }

         logger.debug("Token secret was: " + secret);

         return new Token(token,secret.toString());
      } catch (FileNotFoundException e) {
         logger.debug(e);

         return null;
      } catch (IOException e) {
         logger.warn(e);

         return null;
      }
   }

   /**
    * Add a token to this store.
    */
   public synchronized void add(Token token) {

      try {
         File file = getFile(token.getToken());

         FileOutputStream fos = new FileOutputStream(file);

         logger.debug("Writing token to file: " + file.getPath());

         fos.write(token.getTokenSecret().getBytes());
         fos.close();

      } catch (IOException e) {
         logger.warn(e);
      }
   }

   /**
    * Remove a token from this store.
    */
   public synchronized void remove(Token token) {

      File file = getFile(token.getToken());
      if (file.exists()) {
         file.delete();
      }
   }

   /**
    * Get the File for this token.
    */
   protected File getFile(String token) {
      return new File(directory + 
                      File.separatorChar +
                      fileBase +
                      "." +
                      token);
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


