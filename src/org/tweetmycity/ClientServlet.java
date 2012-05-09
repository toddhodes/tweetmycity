/* Copyright 2008 WaveMarket, Inc.
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

import com.veriplace.client.Client;
import com.veriplace.client.User;
import com.veriplace.client.factory.ClientFactory;
import com.veriplace.client.store.TokenStore;
import com.veriplace.client.store.FileTokenStore;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Abstract servlet base class for obtaining Veriplace {@link Client} reference.
 */
public abstract class ClientServlet
   extends HttpServlet {

   protected Client client;

   /**
    * Initialize this servlet, pulling configuration parameters from the
    * servlet config.
    */
   @Override
   public void init(ServletConfig config)
      throws ServletException {

      /* Construct a Veriplace Client instance from a Properties file
       * using ClientFactory.
       *
       * The specified Properties file must contain:
       *
       * consumer.key    - the application's OAuth consumer_key
       * consumer.secret - the application's OAuth consumer_secret
       * veriplace.url   - the URL of the Veriplace server
       *
       * In addition, the Properties file may contain:
       *
       * applicationToken.value 
       * applicationToken.secret
       *
       * If present, these properties specify the application-specified
       * access token that allows your application to perform user discovery 
       * by phone number, email, etc.
       */
      Properties properties = new Properties();

      /* Look up the properties file path (as defined in web.xml).
       */
      String propertyFilePath = 
         config.getServletContext().getInitParameter("veriplace.properties-file");

      try {
         properties.load(new FileInputStream(propertyFilePath));
      } catch (IOException e) {
         throw new ServletException(e);
      }

      /* The OAuth protocol requires that the request token secret 
       * obtained before redirecting for user authorization be provided
       * subsequently to obtain an access token. The TokenStore interface
       * provides a mechanism for storing this state between requests.
       *
       * This example uses a FileTokenStore, which stores request token 
       * information on the file system, by default in "/tmp". Since this
       * location will not work on all architectures, developers are expected
       * to either reconfigure the FileTokenStore, use the MemoryTokenStore
       * implementation, or provide their own implementation.
       */
      TokenStore requestTokenStore = new FileTokenStore();

      try {
         this.client = new ClientFactory().getClient(properties,
                                                     requestTokenStore);
      } catch (NoSuchAlgorithmException e) {
         throw new ServletException(e);
      } catch (MalformedURLException e) {
         throw new ServletException(e);
      }
   }



   /**
    * Pull the current user from request parameters
    */
   protected User getUser(HttpServletRequest request) {
      try {
         long userId = Long.parseLong(request.getParameter("user"));
         return new User(userId);
      } catch (NullPointerException e) {
      } catch (NumberFormatException e) {
      }
      return null;
   }

   /**
    * Construct User.
    */
   protected User userFromString(String vpUserId) {
      try {
         long userId = Long.parseLong(vpUserId);
         return new User(userId);
      } catch (NullPointerException e) {
         e.printStackTrace();
      } catch (NumberFormatException e) {
         e.printStackTrace();
      }
      return null;
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

