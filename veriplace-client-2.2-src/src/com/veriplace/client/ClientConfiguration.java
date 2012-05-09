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
package com.veriplace.client;

import com.veriplace.client.factory.CallbackFactory;
import com.veriplace.client.store.FileTokenStore;
import com.veriplace.client.store.TokenStore;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Revision;

/**
 * Encapsulates the various properties that can be configured for a Veriplace
 * {@link Client}.
 * @since 2.1
 */
public class ClientConfiguration {
   
   /**
    * The default baseUrl to use for Veriplace APIs: "http://veriplace.com"
    */
   public static final String DEFAULT_SERVER_URI = "http://veriplace.com";

   /**
    * The default setting for using HTTPS for machine-to-machine APIs: true
    */
   public static final boolean DEFAULT_SECURE = true;

   /**
    * The default OAuth protocol version.
    */
   public static final Revision DEFAULT_PROTOCOL = Revision.Core1_0RevA;
   
   private String consumerKey;
   private String consumerSecret;
   private String serverUri;
   private Boolean secure;
   private Revision protocol;
   private Token applicationToken;
   private CallbackFactory callbackFactory;
   private String callbackServerName;
   private Integer callbackServerPort;
   private String callbackPath;
   private String[] callbackIncludeParameters;
   private String[] callbackExcludeParameters;
   private String defaultLocationMode;
   private LocationFilter locationFilter;
   private TokenStore tokenStore;
   
   /**
    * See {@link #setConsumerKey(String)}.
    */
   public String getConsumerKey() {
      return consumerKey;
   }
   
   public void setConsumerKey(String consumerKey) {
      this.consumerKey = consumerKey;
   }
   
   /**
    * See {@link #setConsumerSecret(String)}.
    */
   public String getConsumerSecret() {
      return consumerSecret;
   }
   
   public void setConsumerSecret(String consumerSecret) {
      this.consumerSecret = consumerSecret;
   }
   
   /**
    * See {@link #setServerUri(String)}.
    */
   public String getServerUri() {
      return serverUri;
   }
   
   /**
    * Specifies the base URL of the Veriplace server.  If not specified, this defaults to
    * {@link #DEFAULT_SERVER_URI}.
    */
   public void setServerUri(String serverUri) {
      this.serverUri = serverUri;
   }
   
   /**
    * See {@link #setSecure(Boolean)}.
    */
   public Boolean getSecure() {
      return secure;
   }
   
   /**
    * Specifies whether to use HTTPs for all direct communications with the Veriplace server
    * (that is, all requests other than user authorization redirects).  If not specified,
    * this defaults to {@link #DEFAULT_SECURE}.
    */
   public void setSecure(Boolean secure) {
      this.secure = secure;
   }
   
   /**
    * See {@link #setProtocol(Revision)}.
    */
   public Revision getProtocol() {
      return protocol;
   }
   
   public void setProtocol(Revision protocol) {
      this.protocol = protocol;
   }
   
   /**
    * See {@link #setApplicationToken(Token)}.
    */
   public Token getApplicationToken() {
      return applicationToken;
   }

   /**
    * Specifies the application-specific access token issued for this application.
    * This token is only required for some interfaces.
    */
   public void setApplicationToken(Token applicationToken) {
      this.applicationToken = applicationToken;
   }
   
   /**
    * See {@link #setCallbackFactory(CallbackFactory)}.
    */
   public CallbackFactory getCallbackFactory() {
      return callbackFactory;
   }
   
   public void setCallbackFactory(CallbackFactory callbackFactory) {
      this.callbackFactory = callbackFactory;
   }
   
   /**
    * See {@link #setCallbackServerName(String)}.
    */
   public String getCallbackServerName() {
      return callbackServerName;
   }
   
   public void setCallbackServerName(String callbackServerName) {
      this.callbackServerName = callbackServerName;
   }
   
   /**
    * See {@link #setCallbackServerPort(Integer)}.
    */
   public Integer getCallbackServerPort() {
      return callbackServerPort;
   }
   
   public void setCallbackServerPort(Integer callbackServerPort) {
      this.callbackServerPort = callbackServerPort;
   }
   
   /**
    * See {@link #setCallbackPath(String)}.
    */
   public String getCallbackPath() {
      return callbackPath;
   }
   
   public void setCallbackPath(String callbackPath) {
      this.callbackPath = callbackPath;
   }
   
   /**
    * See {@link #setCallbackIncludeParameters(String[])}.
    */
   public String[] getCallbackIncludeParameters() {
      return callbackIncludeParameters;
   }
   
   public void setCallbackIncludeParameters(String[] callbackIncludeParameters) {
      this.callbackIncludeParameters = callbackIncludeParameters;
   }
   
   /**
    * See {@link #setCallbackExcludeParameters(String[])}.
    */
   public String[] getCallbackExcludeParameters() {
      return callbackExcludeParameters;
   }
   
   public void setCallbackExcludeParameters(String[] callbackExcludeParameters) {
      this.callbackExcludeParameters = callbackExcludeParameters;
   }
   
   /**
    * See {@link #setDefaultLocationMode(String)}.
    */
   public String getDefaultLocationMode() {
      return defaultLocationMode;
   }
   
   /**
    * Specifies the {@link LocationMode location mode} that should be used for {@link GetLocationAPI}
    * requests if no other mode is specified.
    */
   public void setDefaultLocationMode(String defaultLocationMode) {
      this.defaultLocationMode = defaultLocationMode;
   }
   
   /**
    * See {@link #setLocationFilter(LocationFilter)}.
    */
   public LocationFilter getLocationFilter() {
      return locationFilter;
   }
   
   /**
    * Specifies an object that can apply post-processing to returned locations.
    * See {@link LocationFilter}.
    */
   public void setLocationFilter(LocationFilter locationFilter) {
      this.locationFilter = locationFilter;
   }
   
   /**
    * See {@link #setTokenStore(TokenStore)}.
    */
   public TokenStore getTokenStore() {
      return tokenStore;
   }
   
   /**
    * Specifies an object that can store request tokens between operations.
    * The default is an instance of {@link FileTokenStore}.
    */
   public void setTokenStore(TokenStore tokenStore) {
      this.tokenStore = tokenStore;
   }
   
   /**
    * Default constructor that initializes no properties.
    */
   public ClientConfiguration() {
   }
   
   /**
    * Shortcut constructor for initializing only the required consumer key and consumer secret.
    */
   public ClientConfiguration(String consumerKey, String consumerSecret) {
      this.consumerKey = consumerKey;
      this.consumerSecret = consumerSecret;
   }
   
   /**
    * Constructor for initializing most of the optional properties.
    */
   public ClientConfiguration(String consumerKey,
                              String consumerSecret,
                              Revision protocol,
                              Token applicationToken, 
                              String serverUri,
                              Boolean secure,
                              CallbackFactory callbackFactory,
                              TokenStore requestTokenStore,
                              String defaultLocationMode,
                              LocationFilter locationFilter) {
      this(consumerKey, consumerSecret);
      this.protocol = protocol;
      this.applicationToken = applicationToken;
      this.serverUri = serverUri;
      this.secure = secure;
      this.callbackFactory = callbackFactory;
      this.tokenStore = requestTokenStore;
      this.defaultLocationMode = defaultLocationMode;
      this.locationFilter = locationFilter;
   }
}
