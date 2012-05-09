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
package org.tweetmycity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;


/** store user data to the filesystem
 */
public class UserStore {

   private static final Log logger = LogFactory.getLog(UserStore.class);

   /**
    * Default directory
    */
   public static final String DEFAULT_DIRECTORY = "/opt/wm/data/tweetmycity";

   /**
    * Default file base
    */
   public static final String DEFAULT_FILE_BASE = "tmcuser";

   /**
    * Directory to store users.
    */
   protected final String directory;

   /**
    * File base to store users.
    */
   protected final String fileBase;

   /**
    * Create a new file token store using default values.
    */
   public UserStore() {
      this(DEFAULT_FILE_BASE);
   }

   /**
    * Create a new file token store using custom fileBase.
    * @param fileBase the file base for storing tokens
    */
   public UserStore(String fileBase) {
      this(fileBase,DEFAULT_DIRECTORY);
   }

   /**
    * Create a new file token store using custom fileBase and directory.
    * @param fileBase the file base for storing tokens
    * @param directory the directory for storing tokens
    */
   public UserStore(String fileBase, 
                    String directory) {
      this.fileBase = fileBase;
      this.directory = directory;
   }
   
   /**
    * Get a user by userId.
    */
   public synchronized TmcUser get(long userId) {

      try {
         File file = getFile(userId + ".ttok");
         FileInputStream fis = new FileInputStream(file);
         logger.debug("Reading info from file: " + file.getPath());
         
         StringBuilder tTok = new StringBuilder();
         int c;
         while ((c = fis.read()) != -1) {
            if (c == '\n') {
               break;
            }
            tTok.append((char)c);
         }
         
         file = getFile(userId + ".ttoksec");
         fis = new FileInputStream(file);
         logger.debug("Reading info from file: " + file.getPath());

         StringBuilder tTokSec = new StringBuilder();
         while ((c = fis.read()) != -1) {
            if (c == '\n') {
               break;
            }
            tTokSec.append((char)c);
         }

         file = getFile(userId + ".dev");

         fis = new FileInputStream(file);
         StringBuilder dev = new StringBuilder();
         if (file.exists()) {
            logger.debug("Reading info from file: " + file.getPath());
            while ((c = fis.read()) != -1) {
               if (c == '\n') {
                  break;
               }
               dev.append((char)c);
            }
         }  else {
            dev.append("phone");
         }

         file = getFile(userId + ".lastloc");
         StringBuilder lastloc = new StringBuilder();         
         if (file.exists()) {
            fis = new FileInputStream(file);
            logger.debug("Reading info from file: " + file.getPath());

            while ((c = fis.read()) != -1) {
               if (c == '\n') {
                  break;
               }
               lastloc.append((char)c);
            }
         } else {
            lastloc.append("unknown");
         }


         logger.debug("twitterTok was: " + tTok.toString());
         logger.debug("twitterTokSec was: " + tTokSec.toString());
         logger.debug("deviceDesc: " + dev.toString());
         logger.debug("lastloc: " + lastloc.toString());

         return new TmcUser(userId,
                            tTok.toString(),
                            tTokSec.toString(),
                            dev.toString(),
                            lastloc.toString());
      } catch (FileNotFoundException e) {
         logger.debug(e);

         return null;
      } catch (IOException e) {
         logger.warn(e);

         return null;
      }
   }

   /**
    * Add a user to this store.
    */
   public synchronized void add(TmcUser tmcUser) {

      try {
         File file = getFile(tmcUser.getUserId() + ".ttok");
         FileOutputStream fos = new FileOutputStream(file);
         logger.debug("Writing ttok to file: " + file.getPath());
         fos.write(tmcUser.getTwitterToken().getBytes());
         fos.close();

         file = getFile(tmcUser.getUserId() + ".ttoksec");
         fos = new FileOutputStream(file);
         logger.debug("Writing ttoksec to file: " + file.getPath());
         fos.write(tmcUser.getTwitterTokenSecret().getBytes());
         fos.close();

         file = getFile(tmcUser.getUserId() + ".dev");
         fos = new FileOutputStream(file);
         logger.debug("Writing device description to file: " + file.getPath());
         fos.write(tmcUser.getDeviceDescription().getBytes());
         fos.close();

         file = getFile(tmcUser.getUserId() + ".lastloc");
         fos = new FileOutputStream(file);
         logger.debug("Writing lastloc to file: " + file.getPath());
         fos.write(tmcUser.getLastCityState().getBytes());
         fos.close();

      } catch (IOException e) {
         logger.warn(e);
      }
   }


   public void addUser(long vpId, String twitterTok, String twitterTokSec, String dev) {
      TmcUser u = new TmcUser(vpId, twitterTok, twitterTokSec, dev, null); 
      add(u);
   }

   public void addUser(long vpId, String twitterTok, String twitterTokSec) {
      addUser(vpId, twitterTok, twitterTokSec, null); 
   }


   /**
    * Update user information in this store.
    */
   public synchronized void update(TmcUser tmcUser) {
      add(tmcUser);
   }



   /**
    * Remove a user from this store.
    */
   public synchronized void remove(TmcUser tmcUser) {

      File file = getFile(tmcUser.getUserId() + ".ttok");
      if (file.exists()) {
         file.delete();
      }

      file = getFile(tmcUser.getUserId() + ".ttoksec");
      if (file.exists()) {
         file.delete();
      }

      file = getFile(tmcUser.getUserId() + ".dev");
      if (file.exists()) {
         file.delete();
      }

      file = getFile(tmcUser.getUserId() + ".lastloc");
      if (file.exists()) {
         file.delete();
      }
   }


   /**
    * Get all users.
    */
   public synchronized List<TmcUser> getUsers() {
      List<TmcUser> ret = new ArrayList<TmcUser>();

      // dir listing
      File dir = new File(directory);
      dir.mkdirs();
      File userdir = new File(directory);
      //logger.debug("usersdir '" + userdir + "'");

      for (String file : userdir.list()) {
         String[] split = file.split("\\.");
         //logger.debug("file '" + file + "'");

         //tmcuser.8219567698096403872.tid
         if (split.length == 3 && split[2].equals("ttok")) {
            try {
               long vpId = Long.parseLong(split[1]);
               TmcUser u = get(vpId);
               ret.add(u);               
            } catch (NumberFormatException nfe) {
               logger.error("error parsing file '" + file + "'");
            }
         }
      }

      return ret;
   }


   /**
    * Get the File for this tmc.
    */
   protected File getFile(String tmc) {
      File dir = new File(directory);
      dir.mkdirs();

      return new File(directory + 
                      File.separatorChar +
                      fileBase +
                      "." +
                      tmc);
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


