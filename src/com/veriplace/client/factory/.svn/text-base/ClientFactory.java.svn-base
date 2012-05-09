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
package com.veriplace.client.factory;

import com.veriplace.client.Client;
import com.veriplace.client.store.TokenStore;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Revision;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;

import java.security.NoSuchAlgorithmException;

import java.util.Properties;

/**
 * Factory for constructing {@link Client} instances from {@link Properties}
 */
public class ClientFactory {

   /**
    * The property key for the <i>oauth_consumer_key</i>: "consumer.key"
    */
   public static final String CONSUMER_KEY_PROPERTY = "consumer.key";

   /**
    * The property key for the <i>oauth_consumer_secret</i>: "consumer.secret"
    */
   public static final String CONSUMER_SECRET_PROPERTY = "consumer.secret";

   /**
    * The property key for using OAuth Core 1.0 Rev A.
    */
   public static final String REV_A_PROPERTY = "veriplace.rev_a";

   /**
    * The property key for the Veriplace <i>application-specific Access Token value</i>: "applicationToken.value"
    */
   public static final String APPLICATION_TOKEN_VALUE_PROPERTY = "applicationToken.value";

   /**
    * The property key for the Veriplace <i>application-specific Access Token secret</i>: "applicationToken.secret"
    */
   public static final String APPLICATION_TOKEN_SECRET_PROPERTY = "applicationToken.secret";

   /**
    * The property key for specifying the hostname for callback URLs, if it can't be detected automatically
    * due to your network configuration: "veriplace.callback.host"
    */
   public static final String CALLBACK_SERVER_NAME_PROPERTY = "veriplace.callback.host";
   
   /**
    * The property key for specifying the port for callback URLs, if it can't be detected automatically
    * due to your network configuration: "veriplace.callback.port"
    */
   public static final String CALLBACK_SERVER_PORT_PROPERTY = "veriplace.callback.port";
   
   /**
    * The property key for the Veriplace <i>url</i>: "veriplace.url"
    * <p>
    * The base URL will usually be something like <pre>http://veriplace.com</pre> 
    * or <pre>http://demo.veriplace.com</pre>
    */
   public static final String VERIPLACE_URL_PROPERTY = "veriplace.url";

   /**
    * The property key for whether Veriplace <i>requires https</i>: "veriplace.https"
    */
   public static final String VERIPLACE_HTTPS_PROPERTY = "veriplace.https";

   private static final Log logger = LogFactory.getLog(ClientFactory.class);

   /**
    * Create a new Client from a {@link Properties} object.
    * <p>
    * The following properties are required:
    * <ul>
    * <li>{@link #CONSUMER_KEY_PROPERTY}</li>
    * <li>{@link #CONSUMER_SECRET_PROPERTY}</li>
    * <li>{@link #VERIPLACE_URL_PROPERTY}</li>
    * </ul>
    * <p>
    * The following properties are optional:
    * <ul>
    * <li>{@link #APPLICATION_TOKEN_VALUE_PROPERTY}</li>
    * <li>{@link #APPLICATION_TOKEN_SECRET_PROPERTY}</li>
    * <li>{@link #CALLBACK_SERVER_NAME_PROPERTY}</li>
    * <li>{@link #CALLBACK_SERVER_PORT_PROPERTY}</li>
    * <li>{@link #VERIPLACE_HTTPS_PROPERTY}</li>
    * </ul>
    */
   public Client getClient(Properties properties) 
      throws NoSuchAlgorithmException,
             MalformedURLException,
             IllegalArgumentException {
      return getClient(properties,null);
   }

   public Client getClient(Properties properties,
                           TokenStore requestTokenStore) 
      throws NoSuchAlgorithmException,
             MalformedURLException,
             IllegalArgumentException {

      String consumerKey =
         properties.getProperty(CONSUMER_KEY_PROPERTY);

      if (consumerKey == null ||
          consumerKey.trim().length() == 0) {
         String msg = "Null or empty value found for required property: " + CONSUMER_KEY_PROPERTY;
         logger.warn(msg);
         throw new IllegalArgumentException(msg);
      }

      String consumerSecret = 
         properties.getProperty(CONSUMER_SECRET_PROPERTY);

      if (consumerSecret == null ||
          consumerSecret.trim().length() == 0) {
         String msg = "Null or empty value found for required property: " + CONSUMER_SECRET_PROPERTY;
         logger.warn(msg);
         throw new IllegalArgumentException(msg);
      }

      String revA = 
         properties.getProperty(REV_A_PROPERTY);

      // use Rev A by default
      Revision revision = Revision.Core1_0RevA;

      if (revA != null &&
          !Boolean.valueOf(revA)) {
         revision = Revision.Core1_0;
      }

      Token applicationToken = 
         new Token(properties.getProperty(APPLICATION_TOKEN_VALUE_PROPERTY),
                   properties.getProperty(APPLICATION_TOKEN_SECRET_PROPERTY));

      String baseUrl = 
         properties.getProperty(VERIPLACE_URL_PROPERTY);

      if (baseUrl == null ||
          baseUrl.trim().length() == 0) {
         String msg = "Null or empty value found for required property: " + VERIPLACE_URL_PROPERTY;
         logger.warn(msg);
         throw new IllegalArgumentException(msg);
      }

      String useHttpsStr =
         properties.getProperty(VERIPLACE_HTTPS_PROPERTY);

      Boolean useHttps = null;
      if (useHttpsStr != null && useHttpsStr.trim().length() != 0) {
         useHttps = Boolean.valueOf(useHttpsStr);
      }

      String serverName =
         properties.getProperty(CALLBACK_SERVER_NAME_PROPERTY);
      if (serverName != null) {
         serverName = serverName.trim();
         if (serverName.length() == 0) {
            serverName = null;
         }
      }
      String serverPortStr =
         properties.getProperty(CALLBACK_SERVER_PORT_PROPERTY);
      Integer serverPort = null;
      if (serverPortStr != null && serverPortStr.trim().length() != 0) {
         try {
            serverPort = Integer.parseInt(serverPortStr);
         }
         catch (NumberFormatException e) {
            logger.warn("Invalid value found for optional property: " + CALLBACK_SERVER_PORT_PROPERTY);
            serverPort = null;
         }
      }
      return new Client(consumerKey,
                        consumerSecret,
                        revision,
                        applicationToken,
                        baseUrl,
                        useHttps,
                        serverName,
                        serverPort,
                        requestTokenStore);
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
