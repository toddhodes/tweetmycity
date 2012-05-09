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
import com.veriplace.client.factory.DefaultCallbackFactory;
import com.veriplace.client.store.FileTokenStore;
import com.veriplace.client.store.TokenStore;
import com.veriplace.oauth.OAuthException;
import com.veriplace.oauth.consumer.Consumer;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;
import com.veriplace.oauth.message.Revision;
import com.veriplace.oauth.provider.ServiceProvider;
import com.veriplace.oauth.signature.SignatureMethod;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Veriplace client implementation in Java.
 * <p>
 * Users of the Veriplace client will minimally require an <i>oauth_consumer_key</i> and an
 * <i>oauth_consumer_secret</i> representing their application. In addition, some 
 * (but not all) API functions will require an <i>application-specific Access Token</i>. 
 * As a convenience, these and other parameters can be specified more concisely by
 * using the {@link com.veriplace.client.factory.DefaultClientFactory}.
 * <p>
 * Users of the Veriplace client will primarily operate through one of the standard APIs:
 * <ul>
 * <li>{@link UserDiscoveryAPI}</li>
 * <li>{@link GetLocationAPI}</li>
 * <li>{@link SetLocationAPI}</li>
 * <li>{@link PermissionAPI}</li>
 * </ul>
 */
public class Client {

   /**
    * The URL path, after the baseUrl, for request token requests.
    */
   public static final String REQUEST_TOKEN_PATH = "/api/requestToken";

   /**
    * The URL path, after the baseUrl, for user authorization requests.
    */
   public static final String USER_AUTHORIZATION_PATH = "/api/userAuthorization";

   /**
    * The URL path, after the baseUrl, for access token requests.
    */
   public static final String ACCESS_TOKEN_PATH = "/api/accessToken";

   /**
    * The URL path, after the baseUrl, for user authorization requests,
    * for user discovery. 
    * <p>
    * This form uses a "pretty" URL that is more suitable for out-of-band
    * communications.
    */
   public static final String PRETTY_USER_DISCOVERY_USER_AUTHORIZATION_PATH = "/api/user";

   /**
    * The URL path, after the baseUrl, for request token requests.
    */
   public static final String APPLICATION_INFO_PATH = "/application";

   public static final String LONG_TIMEOUT = "veriplace_long_timeout";

   private static final Log logger = LogFactory.getLog(Client.class);

   private final ServiceProvider serviceProvider;
   private final Consumer consumer;
   private final String serverUri;
   private final boolean secure;
   private final TokenStore requestTokenStore;
   private final Token applicationToken;
   private final CallbackFactory callbackFactory;
   
   private final UserDiscoveryAPI userDiscoveryApi;
   private final GetLocationAPI getLocationApi;
   private final SetLocationAPI setLocationApi;
   private final PermissionAPI permissionApi;

   /**
    * Minimal client constructor, requiring only a consumer key and secret.  This shortcut is
    * equivalent to constructing a {@link ClientConfiguration} with the same parameters.
    * <p>
    * This constructor uses the default baseUrl and requestTokenStore implementations,
    * does not configure an application-specific access token, and uses 
    * {@link Revision#Core1_0RevA OAuth Core 1.0 Rev A}.
    * <p>
    * @param consumerKey the OAuth consumer key, which is required
    * @param consumerSecret the OAuth consumer secret, which is required
    */
   public Client(String consumerKey,
                 String consumerSecret) 
      throws ConfigurationException {
      
      this(new ClientConfiguration(consumerKey, consumerSecret));
   }

   /**
    * @deprecated  Obsolete; use ClientConfiguration constructor instead.
    */
   @Deprecated
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision) 
      throws ConfigurationException {
      
      this(consumerKey,
           consumerSecret,
           revision,
           null);
   }

   /**
    * @deprecated  Obsolete; use ClientConfiguration constructor instead.
    */
   @Deprecated
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision,
                 Token applicationToken) 
      throws ConfigurationException {
      
      this(consumerKey,
           consumerSecret,
           revision,
           applicationToken,
           null,
           null);
   }

   /**
    * @deprecated  Obsolete; use ClientConfiguration constructor instead.
    */
   @Deprecated
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision,
                 Token applicationToken, 
                 String baseUrl,
                 TokenStore requestTokenStore) 
      throws ConfigurationException {
      
      this(consumerKey,
           consumerSecret,
           revision,
           applicationToken,
           baseUrl,
           null,
           null,
           null,
           requestTokenStore);
   }
   
   /**
    * @deprecated  Obsolete; use ClientConfiguration constructor instead.
    */
   @Deprecated
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision,
                 Token applicationToken, 
                 String baseUrl,
                 Boolean secure,
                 String callbackServerName,
                 Integer callbackServerPort,
                 TokenStore requestTokenStore) 
      throws ConfigurationException {

      this(consumerKey, consumerSecret, revision, applicationToken, baseUrl, secure,
           new DefaultCallbackFactory(callbackServerName, callbackServerPort),
           requestTokenStore, null);
   }
   
   /**
    * @deprecated  Obsolete; use ClientConfiguration constructor instead.
    */
   @Deprecated
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision,
                 Token applicationToken, 
                 String baseUrl,
                 Boolean secure,
                 CallbackFactory callbackFactory,
                 TokenStore requestTokenStore,
                 String defaultLocationMode) 
      throws ConfigurationException {
      
      this(new ClientConfiguration(consumerKey, consumerSecret, revision, applicationToken,
            baseUrl, secure, callbackFactory, requestTokenStore, defaultLocationMode, null));
   }

   public Client(ClientConfiguration config)
      throws ConfigurationException {

      if ((config.getConsumerKey() == null) || config.getConsumerKey().equals("")) {
         throw new ConfigurationException("Missing required property: consumer key");
      }
      if ((config.getConsumerSecret() == null) || config.getConsumerSecret().equals("")) {
         throw new ConfigurationException("Missing required property: consumer secret");
      }
      
      if ((config.getServerUri() == null) || config.getServerUri().equals("")) {
         this.serverUri = ClientConfiguration.DEFAULT_SERVER_URI;
      }
      else {
         this.serverUri = config.getServerUri();
      }
      this.secure = (config.getSecure() == null) ? ClientConfiguration.DEFAULT_SECURE :
            config.getSecure();
      Revision revision = (config.getProtocol() == null) ? ClientConfiguration.DEFAULT_PROTOCOL :
            config.getProtocol();
      
      try {
         // use base url with https, which may require https
         URL requestTokenUrl = new URL(getServerDirectUri() + REQUEST_TOKEN_PATH);
         URL accessTokenUrl = new URL(getServerDirectUri() + ACCESS_TOKEN_PATH);
   
         // use base url as is, which may skip https
         URL userAuthorizationUrl = new URL(getServerUri() + USER_AUTHORIZATION_PATH);
   
         this.serviceProvider = new ServiceProvider(requestTokenUrl,
                                                    userAuthorizationUrl,
                                                    accessTokenUrl,
                                                    EnumSet.of(SignatureMethod.HMAC_SHA1));
      }
      catch (MalformedURLException e) {
         throw new ConfigurationException("Invalid server URI: "
               + config.getServerUri(), e);
      }
      try {
         this.consumer = new Consumer(config.getConsumerKey(),
                                      config.getConsumerSecret(),
                                      serviceProvider,
                                      revision);
      }
      catch (NoSuchAlgorithmException e) {
         throw new ConfigurationException(
               "Unexpected signature algorithm error", e);
      }
      
      this.applicationToken = config.getApplicationToken();

      if (config.getCallbackFactory() != null) {
         this.callbackFactory = config.getCallbackFactory();
      }
      else {
         this.callbackFactory = new DefaultCallbackFactory(
               config.getCallbackServerName(),
               config.getCallbackServerPort(),
               config.getCallbackPath(),
               config.getCallbackExcludeParameters(),
               config.getCallbackIncludeParameters());
      }
      
      if (config.getTokenStore() == null) {
         this.requestTokenStore = new FileTokenStore("requestToken");
      }
      else {
         this.requestTokenStore = config.getTokenStore();
      }

      this.userDiscoveryApi = new UserDiscoveryAPI(this);
      this.getLocationApi = new GetLocationAPI(this, config.getDefaultLocationMode(), config.getLocationFilter());
      this.setLocationApi = new SetLocationAPI(this);
      this.permissionApi = new PermissionAPI(this);
   }

   /**
    * Get the UserDiscovery API client.
    */
   public UserDiscoveryAPI getUserDiscoveryAPI() {
      return userDiscoveryApi;
   }

   /**
    * Get the GetLocation API client.
    */
   public GetLocationAPI getGetLocationAPI() {
      return getLocationApi;
   }

   /**
    * Get the SetLocation API client.
    */
   public SetLocationAPI getSetLocationAPI() {
      return setLocationApi;
   }

   /**
    * Get the Permission API client.
    * @since 2.0
    */
   public PermissionAPI getPermissionAPI() {
      return permissionApi;
   }

   public CallbackFactory getCallbackFactory() {
      return callbackFactory;
   }
   
   /**
    * Is this request an OAuth callback.
    * @param request the callback request
    */
   public boolean isCallback(HttpServletRequest request) {
      try {
         getRequestToken(request);
         return true;
      } catch (InvalidCallbackException e) {
         return false;
      }
   }

   /**
    * Returns the request token value from the OAuth callback request.  This is mainly useful
    * if you are receiving a callback from a user discovery process that was previously started
    * with {@link #getUserDiscoveryUrl(String)}, and need to match the current callback to the
    * previously stored token for a particular user.
    * @param request
    * @return a request token (not including the token secret) or null if none.
    */
   public String getRequestToken(HttpServletRequest request) 
      throws InvalidCallbackException {

      String token = request.getParameter(Parameter.Token.getKey());
      if (token == null) {
         throw new InvalidCallbackException("Callback did not contain: 'oauth_token'");
      }
      return token;
   }
   
   /**
    * Get an access token for an OAuth callback request 
    * @param request the callback request
    * @return a valid access token or null if none
    */
   public Token getAccessToken(HttpServletRequest request) 
      throws InvalidCallbackException,
             TransportException,
             VeriplaceOAuthException {

      String token = getRequestToken(request);

      Token requestToken = requestTokenStore.get(token);

      if (requestToken == null) {
         // request tokens are removed after use (see below),
         // so this scenario is fairly common on page reloads
         logger.debug("Could not find request token for: " + token);
         throw new InvalidCallbackException("Request token not found for: " + token);
      }

      String verifier = request.getParameter(Parameter.Verifier.getKey());

      Token ret = null;
      try {
         // exchange for access token
         ret = consumer.getAccessToken(requestToken,verifier);
      } catch (OAuthException e) {
         logger.info("No access token was available for: " + token);
         logger.debug(e,e);
         throw new VeriplaceOAuthException(e);
      } catch (IOException e) {
         logger.info(e,e);
         throw new TransportException(e);
      }

      requestTokenStore.remove(requestToken);

      return ret;
   }

   /**
    * Utility method for constructing a Veriplace redirect URL for OAuth user authorization.
    * <p>
    * This version obtains an OAuth request token before constructing the redirect URL.
    * @param callback the OAuth callback
    * @param immediate should responses return immediately if user interaction would be required?
    * @param uri the resource for which authorization is requested (e.g. location)
    */
   protected String getRedirectURL(String callback,
                                   boolean immediate,
                                   String uri) 
      throws TransportException,
             VeriplaceOAuthException {

      return getRedirectURL(getRequestToken(callback),callback,immediate,uri);
   }

   /**
    * Utility method for obtaining an OAuth request token.
    * <p>
    * @param callback the OAuth callback, which is required for Rev A.
    */
   protected Token getRequestToken(String callback) 
      throws TransportException,
             VeriplaceOAuthException {
      try {
         Token requestToken = 
            consumer.getRevision() == Revision.Core1_0 ?
            consumer.getRequestToken() :
            consumer.getRequestToken(callback);

         requestTokenStore.add(requestToken);
         return requestToken;
      } catch (OAuthException e) {
         logger.info(e,e);
         throw new VeriplaceOAuthException(e);
      } catch (IOException e) {
         logger.info(e,e);
         throw new TransportException(e);
      }
   }

   /**
    * Utility method for constructing a Veriplace redirect URL for OAuth user authorization,
    * given an OAuth request token.
    * <p>
    * @param requestToken the OAuth request token
    * @param callback the OAuth callback
    * @param immediate should responses return immediately if user interaction would be required?
    * @param uri the resource for which authorization is requested (e.g. location)
    */
   protected String getRedirectURL(Token requestToken,
                                   String callback,
                                   boolean immediate,
                                   String uri) 
      throws TransportException,
             VeriplaceOAuthException {

      ParameterSet parameters = new ParameterSet();

      // 'uri' is required
      parameters.put("uri",uri);

      // 'callback' is required for non-Rev A
      if (consumer.getRevision() == Revision.Core1_0) {
         parameters.put(Parameter.Callback, callback);
      }

      // 'immediate' is optional and defaults to false
      if (immediate) {
         parameters.put("immediate","true");
      }

      try {
         return consumer.getUserAuthorizationUrl(parameters,requestToken);
      } catch (OAuthException e) {
         logger.info(e,e);
         throw new VeriplaceOAuthException(e);
      } catch (IOException e) {
         logger.info(e,e);
         throw new TransportException(e);
      }
   }

   /**
    * Utility method for retrieving the protected resource used by a particular API.
    * @return  the successful response; will not be null
    * @throws TransportException  if there is an I/O error in communication with the server
    * @throws VeriplaceOAuthException  if the server denied the request
    */
   protected Response getProtectedResource(APIInfo info,
                                           ParameterSet parameters,
                                           Token accessToken)
         throws TransportException, VeriplaceOAuthException {
      return getProtectedResource(info,parameters,accessToken,null);
   }

   /**
    * Utility method for retrieving the protected resource used by a particular API.
    * @return  the successful response; will not be null
    * @throws TransportException  if there is an I/O error in communication with the server
    * @throws VeriplaceOAuthException  if the server denied the request
    */
   protected Response getProtectedResource(APIInfo info,
                                           ParameterSet parameters,
                                           Token accessToken,
                                           Integer timeout)
         throws TransportException, VeriplaceOAuthException {
      RequestMethod requestMethod = info.getRequestMethod();

      URL url = null;
      try {
         url = new URL(info.getURI());
      } catch (MalformedURLException e) {
         logger.warn(e,e);
         throw new TransportException(e);
      }

      try {
         Response response = consumer.getProtectedResource(url,
                                                           requestMethod,
                                                           parameters,
                                                           accessToken,
                                                           timeout,
                                                           SignatureMethod.HMAC_SHA1);
         if (! response.isOk()) {
            throw new OAuthException(response.getCode(), response.getReasonPhrase());
         }
         return response;
      }
      catch (OAuthException e) {
         logger.info("Unable to obtain resource for access token: " + accessToken == null ? null : accessToken.getToken());
         logger.info(e.getMessage());
         logger.debug(e,e);
         throw new VeriplaceOAuthException(e);
      }
      catch (IOException e) {
         logger.info(e,e);
         throw new TransportException(e);
      }
   }
   
   /*** Accessors used by API implementations within this package ***/

   /**
    * Returns the base URI for the Veriplace server.
    * @see #getServerDirectUri()
    * @since 2.1
    */
   public String getServerUri() {
      return serverUri;
   }
   
   /**
    * @deprecated  Obsolete; use {@link #getServerUri()}.
    */
   @Deprecated
   public String getBaseUrl() {
      return getServerUri();
   }

   /**
    * Returns the base URI for all direct communications with the Veriplace
    * server (all requests except user authorization redirects).  If
    * {@link #isSecure()} is true then this will use HTTPS.
    * @since 2.1
    */
   public String getServerDirectUri() {
      if (secure) {
         return serverUri.replace("http://","https://");
      } else {
         return serverUri;
      }
   }

   /**
    * @deprecated  Obsolete; use {@link #getServerDirectUri()}.
    */
   @Deprecated
   public String getBaseUrlWithHttps() {
      return getServerDirectUri();
   }
   
   /**
    * @since 2.1
    */
   public boolean isSecure() {
      return secure;
   }

   /**
    * @deprecated  Obsolete; use {@link #isSecure()}.
    */
   @Deprecated
   public boolean getUseHttps() {
      return isSecure();
   }
   
   public Consumer getConsumer() {
      return consumer;
   }

   public boolean hasApplicationToken() {
      return (applicationToken != null);
   }
   
   protected Token getApplicationToken() {
      return applicationToken;
   }

   protected TokenStore getRequestTokenStore() {
      return requestTokenStore;
   }
   
   /**
    * Obtains a request token value which can be used to perform user discovery.  Pass
    * this value to {@link #getUserDiscoveryUrl(String)}, and also save the request token
    * value so you can identify which user the callback is coming from (see
    * {@link #getRequestToken(HttpServletRequest)}).
    * @param callback  the callback URL to which Veriplace should redirect the user after
    *   user discovery has finished
    * @return  a request token string (not including the token secret)
    */
   public String getUserDiscoveryRequestToken(String callback) 
      throws TransportException,
             VeriplaceOAuthException {

      ParameterSet parameters = new ParameterSet();
      parameters.put(LONG_TIMEOUT, "1");
      try {
         if (consumer.getRevision() == Revision.Core1_0) {
            throw new IllegalArgumentException("Can't get user discovery request token for pre-1.0A OAuth");
         }
         Token requestToken = consumer.getRequestToken(callback, parameters);
         requestTokenStore.add(requestToken);
         return requestToken.getToken();
      } catch (OAuthException e) {
         logger.info(e,e);
         throw new VeriplaceOAuthException(e);
      } catch (IOException e) {
         logger.info(e,e);
         throw new TransportException(e);
      }
   }
   
   /**
    * Constructs a URL, using a request token previously obtained from
    * {@link #getUserDiscoveryRequestToken(String)}, which will tell the Veriplace server
    * to initiate user discovery and then call your application (using the callback previously
    * specified in getUserDiscoveryRequestToken).  You can then ask an end user to visit this
    * URL to sign up with Veriplace.
    */
   public String getUserDiscoveryUrl(String requestToken) {
      return getBaseUrl() + PRETTY_USER_DISCOVERY_USER_AUTHORIZATION_PATH
            + "?" + Parameter.Token.getKey() + "=" + requestToken;
   }
   
   /**
    * Constructs a URL, using the previously configured application consumer key, which will
    * display the application's information page on the Veriplace web site.
    */
   public String getApplicationInfoUrl() {
      return getBaseUrl() + APPLICATION_INFO_PATH
            + "?" + Parameter.ConsumerKey.getKey() + "=" + consumer.getConsumerKey();
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
