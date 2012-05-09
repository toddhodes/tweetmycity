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

import com.veriplace.client.Client;
import com.veriplace.client.ClientConfiguration;
import com.veriplace.client.ConfigurationException;
import com.veriplace.client.DefaultLocationFilter;
import com.veriplace.client.store.TokenStore;
import com.veriplace.client.store.MemoryTokenStore;
import com.veriplace.client.store.FileTokenStore;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Revision;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory for constructing {@link Client} instances from {@link Properties}.
 * <p>
 * The minimal properties file must contain:
 * <pre>
 * veriplace.application.consumer.key=&lt;your consumer key&gt;
 * veriplace.application.consumer.secret=&lt;your consumer secret&gt;
 * </pre>
 * <p>
 * It is also common to include:
 * <pre>
 * veriplace.application.token.value=&lt;your application token&gt;
 * veriplace.application.token.secret=&lt;your application token secret&gt;
 * </pre>
 * <p>
 * Other configuration properties are documented below and additional properties
 * specific to your application can be added as needed. If you are unsure how to
 * specify any of the above properties, consult the
 * <a href="http://developer.veriplace.com/devportal">Veriplace Developer Portal</a>.
 */
public class DefaultClientFactory
   implements ClientFactory {

   /**
    * The property key for the OAuth consumer key: "veriplace.application.consumer.key".
    * @since 2.1
    */
   public static final String CONSUMER_KEY = "veriplace.application.consumer.key";
	
	/**
	 * @deprecated  Obsolete; use {@link #CONSUMER_KEY}.
	 */
   @Deprecated
	public static final String CONSUMER_KEY_PROPERTY = "consumer.key";
	
   /**
    * The property key for the OAuth consumer secret: "veriplace.application.consumer.secret".
    * @since 2.1
    */
   public static final String CONSUMER_SECRET = "veriplace.application.consumer.secret";

	/**
	 * @deprecated  Obsolete; use {@link #CONSUMER_SECRET}.
	 */
   @Deprecated
   public static final String CONSUMER_SECRET_PROPERTY = "consumer.secret";

	/**
	 * The property key for the protocol version to use: "veriplace.server.protocol".  The value should be
	 * the name of one of the constants in {@link com.veriplace.oauth.message.Revision}.  If not
	 * specified, defaults to {@link ClientConfiguration#DEFAULT_PROTOCOL}.
	 * @since 2.1
	 */
	public static final String PROTOCOL = "veriplace.server.protocol";
	
   /**
    * @deprecated  Obsolete; specify protocol name with {@link #PROTOCOL}.
    */
   @Deprecated
   public static final String REV_A_PROPERTY = "veriplace.rev_a";

   /**
    * The property key for the application-specific Access Token value, if any:
    * "veriplace.application.token.value".
    * @since 2.1
    */
   public static final String APPLICATION_TOKEN_VALUE = "veriplace.application.token.value";

	/**
	 * @deprecated  Obsolete; use {@link #APPLICATION_TOKEN_VALUE}.
	 */
   @Deprecated
   public static final String APPLICATION_TOKEN_VALUE_PROPERTY = "applicationToken.value";

   /**
    * The property key for the application-specific Access Token secret<:
    * "veriplace.application.token.secret".
    * @since 2.1
    */
   public static final String APPLICATION_TOKEN_SECRET = "veriplace.application.token.secret";

	/**
	 * @deprecated  Obsolete; use {@link #APPLICATION_TOKEN_SECRET}.
	 */
   @Deprecated
   public static final String APPLICATION_TOKEN_SECRET_PROPERTY = "applicationToken.secret";

   /**
    * The property key for specifying the hostname for callback URLs, if it can't be detected
    * automatically due to your network configuration: "veriplace.application.callback.host".
    * @since 2.1
    */
   public static final String CALLBACK_HOST = "veriplace.application.callback.host";

	/**
	 * @deprecated  Obsolete; use {@link #CALLBACK_HOST}.
	 */
   @Deprecated
   public static final String CALLBACK_SERVER_NAME_PROPERTY = "veriplace.callback.host";
   
   /**
    * The property key for specifying the port for callback URLs, if it can't be detected
    * automatically due to your network configuration: "veriplace.application.callback.port".
    * @since 2.1
    */
   public static final String CALLBACK_PORT = "veriplace.application.callback.port";

   /**
    * @deprecated  Obsolete; use {@link #CALLBACK_PORT}.
    */
   @Deprecated
   public static final String CALLBACK_PORT_PROPERTY = "veriplace.callback.port";
   
   /**
    * The property key for specifying an exact subpath for callback URLs, if it can't be detected
    * automatically due to your network configuration: "veriplace.callback.path".
    * @since 2.1
    */
   public static final String CALLBACK_PATH = "veriplace.application.callback.path";

	/**
	 * @deprecated  Obsolete; use {@link #CALLBACK_PATH}.
	 */
   @Deprecated
   public static final String CALLBACK_PATH_PROPERTY = "veriplace.callback.path";
   
   /**
    * The property key for specifying the names of request parameters to be copied to the callback
    * URL, if you don't want all parameters to be copied:
    * "veriplace.application.callback.include-params". Value is a comma-delimited list.
    * @since 2.1
    */
   public static final String CALLBACK_INCLUDE_PARAMETERS =
   		"veriplace.application.callback.include-params";

	/**
	 * @deprecated  Obsolete; use {@link #CALLBACK_INCLUDE_PARAMETERS}.
	 */
   @Deprecated
   public static final String CALLBACK_INCLUDE_PARAMETERS_PROPERTY =
   		"veriplace.callback.includeParameters";
   
   /**
    * The property key for specifying the names of request parameters <i>not</i> to be copied to the
    * callback URL, if you don't want all parameters to be copied:
    * "veriplace.application.callback.exclude-params". Value is a comma-delimited list.
    * @since 2.1
    */
   public static final String CALLBACK_EXCLUDE_PARAMETERS =
   		"veriplace.application.callback.exclude-params";
   
   /**
    * @deprecated  Obsolete; use {@link #CALLBACK_EXCLUDE_PARAMETERS}.
    */
   @Deprecated
   public static final String CALLBACK_EXCLUDE_PARAMETERS_PROPERTY =
   		"veriplace.callback.excludeParameters";
   
   /**
    * The property key for the Veriplace server URI: "veriplace.url". If not specified, defaults to
    * {@link ClientConfiguration#DEFAULT_SERVER_URI}.
    * @since 2.1
    */
   public static final String SERVER_URI = "veriplace.server.uri";

   /**
    * @deprecated  Obsolete; use {@link #SERVER_URI}.
    */
   @Deprecated
   public static final String VERIPLACE_URL_PROPERTY = "veriplace.url";

   /**
    * The property key for specifying whether SSL is required for direct server requests:
    * "veriplace.server.secure". Value is "true" or "false". If not specified, defaults to
    * {@link ClientConfiguration#DEFAULT_SECURE}.
    * @since 2.1
    */
   public static final String SECURE = "veriplace.server.secure";
   
   /**
    * @deprecated  Obsolete; use {@link #SECURE}.
    */
   @Deprecated
   public static final String VERIPLACE_HTTPS_PROPERTY = "veriplace.https";

   /**
    * The property key for specifying the type of the request token store.
    * Supported values are "memory" and "file".
    */
   public static final String REQUEST_TOKEN_STORE_TYPE = "veriplace.client.token-store.type";

   /**
    * @deprecated  Obsolete; use {@link #REQUEST_TOKEN_STORE_TYPE}.
    */
   public static final String REQUEST_TOKEN_STORE_PROPERTY = "veriplace.requestTokenStore";

   /**
    * The property key for specifying the root directory of the request token store,
    * if using type "file".
    */
   public static final String REQUEST_TOKEN_STORE_PATH = "veriplace.client.token-store.path";

   /**
    * @deprecated  Obsolete; use {@link #REQUEST_TOKEN_STORE_PATH}.
    */
   public static final String REQUEST_TOKEN_STORE_DIRECTORY_PROPERTY = "veriplace.requestTokenStore.directory";

   /**
    * The property key for specifying the default location mode for the GetLocation API, if any:
    * "veriplace.application.location.mode".
    * @since 2.1
    */
   public static final String DEFAULT_LOCATION_MODE = "veriplace.application.location.mode";

   /**
    * @deprecated  Obsolete; use {@link #DEFAULT_LOCATION_MODE}.
    */
   @Deprecated
	public static final String DEFAULT_LOCATION_MODE_PROPERTY = "veriplace.defaultLocationMode";
	
   /**
    * The property key for specifying a {@link DefaultLocationFilter} that can suppress location
    * errors if there is a last known location: "veriplace.application.location.use-last-known".
    * Value is "true" or "false"; default is false.
    * @since 2.1
    */
   public static final String USE_LAST_KNOWN_LOCATION =
   		"veriplace.application.location.use-last-known";
   
   private static final Log logger = LogFactory.getLog(ClientFactory.class);

   protected final Properties properties;
   protected final ClientConfiguration clientConfiguration;

   public Properties getProperties() {
      return properties;
   }

   /**
    * Returns the {@link ClientConfiguration} object that will be used to construct a client.
    * You can change properties of this object before creating the client.
    * @since 2.1
    */
   public ClientConfiguration getClientConfiguration() {
      return clientConfiguration;
   }
   
   /**
    * @deprecated  Use getClientConfiguration().getTokenStore().
    */
   @Deprecated
   public TokenStore getRequestTokenStore() {
      return getClientConfiguration().getTokenStore();
   }

   /**
    * Create a new factory from a {@link Properties} object.
    * <p>
    * The following properties are required:
    * <ul>
    * <li>{@link #CONSUMER_KEY}</li>
    * <li>{@link #CONSUMER_SECRET}</li>
    * </ul>
    * <p>
    * The following properties are optional:
    * <ul>
    * <li>{@link #SERVER_URI}</li>
    * <li>{@link #APPLICATION_TOKEN_VALUE}</li>
    * <li>{@link #APPLICATION_TOKEN_SECRET}</li>
    * <li>{@link #SECURE}</li>
    * <li>{@link #CALLBACK_HOST}</li>
    * <li>{@link #CALLBACK_PORT}</li>
    * <li>{@link #CALLBACK_PATH}</li>
    * <li>{@link #CALLBACK_INCLUDE_PARAMETERS}</li>
    * <li>{@link #CALLBACK_EXCLUDE_PARAMETERS}</li>
    * <li>{@link #DEFAULT_LOCATION_MODE}</li>
    * <li>{@link #USE_LAST_KNOWN_LOCATION}</li>
    * </ul>
    */
   public DefaultClientFactory(Properties properties)
         throws ConfigurationException {
      
      this.properties = properties;
      this.clientConfiguration = createClientConfiguration(properties);
   }

   /**
    * @deprecated  Use getClientConfiguration().setTokenStore().
    */
   @Deprecated
   public DefaultClientFactory(Properties properties,
                               TokenStore requestTokenStore)
         throws ConfigurationException {
      
      this(properties);
      clientConfiguration.setTokenStore(requestTokenStore);
   }

   public Client getClient() 
      throws ConfigurationException {

      return new Client(getClientConfiguration());
   }

   private ClientConfiguration createClientConfiguration(Properties properties)
      throws ConfigurationException {
      
      ClientConfiguration config = new ClientConfiguration();

      // Consumer Key (required)
      String consumerKey = getString(CONSUMER_KEY, CONSUMER_KEY_PROPERTY);
      if (consumerKey == null) {
         throw new ConfigurationException("Missing required property: " + CONSUMER_KEY);
      }
      else {
         config.setConsumerKey(consumerKey);
      }

      // Consumer Secret (required)
      String consumerSecret = getString(CONSUMER_SECRET, CONSUMER_SECRET_PROPERTY);
      if (consumerSecret == null) {
         throw new ConfigurationException("Missing required property: " + CONSUMER_SECRET);
      }
      else {
         config.setConsumerSecret(consumerSecret);
      }

      // Protocol (optional, defaults to Rev A)
      String protocolName = getString(PROTOCOL);
      Revision protocol = ClientConfiguration.DEFAULT_PROTOCOL;
      if (protocolName != null) {
         try {
            protocol = Revision.valueOf(protocolName);
         }
         catch (IllegalArgumentException e) {
            throw new ConfigurationException("Invalid protocol name: " + protocolName);
         }
      }
      else {
         String revA = getString(REV_A_PROPERTY);
         if ((revA != null) && !Boolean.valueOf(revA)) {
            protocol = Revision.Core1_0;
         }
      }
      config.setProtocol(protocol);

      // Application Token (optional)
      String applicationTokenValue = getString(APPLICATION_TOKEN_VALUE,
            APPLICATION_TOKEN_VALUE_PROPERTY);
      String applicationTokenSecret = getString(APPLICATION_TOKEN_SECRET,
            APPLICATION_TOKEN_SECRET_PROPERTY);
      if ((applicationTokenValue != null) && (applicationTokenSecret != null)) {
         config.setApplicationToken(new Token(applicationTokenValue, applicationTokenSecret));
      }

      // Base URL (optional)
      String serverUri = getString(SERVER_URI, VERIPLACE_URL_PROPERTY);
      config.setServerUri(serverUri);

      // Secure (optional, defaults to null)
      String secureStr = getString(SECURE, VERIPLACE_HTTPS_PROPERTY);
      if (secureStr != null) {
         config.setSecure(Boolean.valueOf(secureStr));
      }

      // Callback server name (optional)
      String serverName = getString(CALLBACK_HOST, CALLBACK_SERVER_NAME_PROPERTY);
      config.setCallbackServerName(serverName);
 
      // Callback server port (optional)
      String serverPortStr = getString(CALLBACK_PORT, CALLBACK_PORT_PROPERTY);
      if (serverPortStr != null) {
         try {
            Integer serverPort = Integer.parseInt(serverPortStr);
            config.setCallbackServerPort(serverPort);
         }
         catch (NumberFormatException e) {
            logger.warn("Invalid value found for optional property: " + CALLBACK_PORT);
         }
      }

      // Callback path (optional, defaults to null)
      String callbackPath = getString(CALLBACK_PATH, CALLBACK_PATH_PROPERTY);
      config.setCallbackPath(callbackPath);

      // Callback parameters (optional)
      String includeParamsStr = getString(CALLBACK_INCLUDE_PARAMETERS,
      		CALLBACK_INCLUDE_PARAMETERS_PROPERTY);
      if (includeParamsStr != null) {
         config.setCallbackIncludeParameters(includeParamsStr.split(","));
      }
      String excludeParamsStr = getString(CALLBACK_EXCLUDE_PARAMETERS,
            CALLBACK_EXCLUDE_PARAMETERS_PROPERTY);
      if (excludeParamsStr != null) {
         config.setCallbackExcludeParameters(excludeParamsStr.split(","));
      }

      // RequestTokenStore (optional)
      String requestTokenStoreType = getString(REQUEST_TOKEN_STORE_TYPE,
               REQUEST_TOKEN_STORE_PROPERTY);
      if (requestTokenStoreType != null) {
         TokenStore tokenStore = null;
         if (requestTokenStoreType.equalsIgnoreCase("memory")) {
            tokenStore = new MemoryTokenStore();
         }
         else if (requestTokenStoreType.equalsIgnoreCase("file")) {
            String directory = getString(REQUEST_TOKEN_STORE_PATH,
                  REQUEST_TOKEN_STORE_DIRECTORY_PROPERTY);
            if (directory != null) {
               tokenStore = new FileTokenStore("requestToken",directory);
            }
            else {
               tokenStore = new FileTokenStore("requestToken");
            }
         }
         config.setTokenStore(tokenStore);
      }

      // Default Location Mode (optional)
      String defaultLocationMode = getString(DEFAULT_LOCATION_MODE, DEFAULT_LOCATION_MODE_PROPERTY);
      config.setDefaultLocationMode(defaultLocationMode);
 
      // Use last known location (optional)
      String useLastKnownStr = getString(USE_LAST_KNOWN_LOCATION);
      if (useLastKnownStr != null) {
         boolean useLastKnown = Boolean.valueOf(useLastKnownStr);
         config.setLocationFilter(new DefaultLocationFilter(useLastKnown));
      }

      return config;
   }

   private String getString(String name) {
      return getString(name, null);
   }
   
   private String getString(String name, String alternateName) {
      String s = properties.getProperty(name, "").trim();
      if (s.equals("") && (alternateName != null)) {
          s = properties.getProperty(alternateName, "").trim();
      }
      return (s.equals("")) ? null : s;
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
