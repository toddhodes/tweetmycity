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
package com.veriplace.client;

import com.veriplace.client.store.TokenStore;
import com.veriplace.client.store.FileTokenStore;
import com.veriplace.client.factory.ClientFactory;

import com.veriplace.oauth.OAuthException;
import com.veriplace.oauth.consumer.Consumer;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Revision;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;
import com.veriplace.oauth.provider.ServiceProvider;
import com.veriplace.oauth.signature.SignatureMethod;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;

import java.security.NoSuchAlgorithmException;

import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;
import java.util.EnumSet;

/**
 * Veriplace client implementation in Java.
 * <p>
 * Users of the Veriplace client will minimally require an <i>oauth_consumer_key</i> and an
 * <i>oauth_consumer_secret</i> representing their application. In addition, some 
 * (but not all) API functions will require an <i>application-specific Access Token</i>. 
 * As a convenience, these an other parameters can be specified more concisely by
 * using the {@link ClientFactory}.
 * <p>
 * Users of the Veriplace client will primarily operate through one of the standard APIs:
 * <ul>
 * <li>{@link UserDiscoveryAPI}</li>
 * <li>{@link GetLocationAPI}</li>
 * <li>{@link SetLocationAPI}</li>
 * </ul>
 */
public class Client {

   /**
    * The default baseUrl to use for Veriplace APIs: "https://veriplace.com"
    */
   public static final String DEFAULT_BASE_URL = "https://veriplace.com";

   /**
    * The default setting for using HTTPS for machine-to-machine APIs: true
    */
   public static final boolean DEFAULT_USE_HTTPS = true;

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

   private static final Log logger = LogFactory.getLog(Client.class);

   private final ServiceProvider serviceProvider;
   private final Consumer consumer;
   private final String baseUrl;
   private final boolean useHttps;
   private final TokenStore requestTokenStore;
   private final Token applicationToken;
   private final String callbackServerName;
   private final Integer callbackServerPort;
   
   private final UserDiscoveryAPI userDiscoveryApi;
   private final GetLocationAPI getLocationApi;
   private final SetLocationAPI setLocationApi;

   /**
    * Construct a client using the default baseUrl and requestTokenStore implementation,
    * no application-specific access token, and OAuth Core Rev A.
    * <p>
    * @param consumerKey the OAuth consumer key, which is required
    * @param consumerSecret the OAuth consumer secret, which is required
    */
   public Client(String consumerKey,
                 String consumerSecret) 
      throws NoSuchAlgorithmException,
             MalformedURLException {
      this(consumerKey,
           consumerSecret,
           Revision.Core1_0RevA,
           null);
   }

   /**
    * Construct a client using the default baseUrl and requestTokenStore implementation
    * and no application-specific access token.
    * <p>
    * @param consumerKey the OAuth consumer key, which is required
    * @param consumerSecret the OAuth consumer secret, which is required
    * @param revision the OAuth revision to use
    */
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision) 
      throws NoSuchAlgorithmException,
             MalformedURLException {
      this(consumerKey,
           consumerSecret,
           revision,
           null);
   }

   /**
    * Construct a client using the default baseUrl and requestTokenStore implemenation.
    * <p>
    * @param consumerKey the OAuth consumer key, which is required
    * @param consumerSecret the OAuth consumer secret, which is required
    * @param revision the OAuth revision to use
    * @param applicationToken the application-specific access token issued for this application, 
    * which is only required for some interfaces
    */
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision,
                 Token applicationToken) 
      throws NoSuchAlgorithmException,
             MalformedURLException {
      this(consumerKey,
           consumerSecret,
           revision,
           applicationToken,
           null,
           null,
           null,
           null,
           null);
   }

   /**
    * Construct a client.
    * <p>
    * @param consumerKey the OAuth consumer key, which is required
    * @param consumerSecret the OAuth consumer secret, which is required
    * @param revision the OAuth revision to use
    * @param applicationToken the application-specific access token issued for this application, 
    * which is only required for some interfaces
    * @param baseUrl the base URL of the location platform, which defaults to {@link #DEFAULT_BASE_URL}
    * @param requestTokenStore interface for storing request tokens between operations, 
    * which defaults to an instance of {@link FileTokenStore}
    */
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision,
                 Token applicationToken, 
                 String baseUrl,
                 TokenStore requestTokenStore) 
      throws NoSuchAlgorithmException,
             MalformedURLException {
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
    * Construct a client.
    * <p>
    * @param consumerKey the OAuth consumer key, which is required
    * @param consumerSecret the OAuth consumer secret, which is required
    * @param revision the OAuth revision to use
    * @param applicationToken the application-specific access token issued for this application, 
    * which is only required for some interfaces
    * @param baseUrl the base URL of the location platform, which defaults to {@link #DEFAULT_BASE_URL}
    * @param callbackServerName the hostname for callback URLs, if it might be different from what is returned by
    *    {@link javax.servlet.http.HttpServletRequest#getServerName} (e.g. due to load-balancing configuration);
    *    null otherwise
    * @param callbackServerPort the port number for callback URLs, if it might be different from what is returned by
    *    {@link javax.servlet.http.HttpServletRequest#getServerPort}; null otherwise
    * @param requestTokenStore interface for storing request tokens between operations, 
    * which defaults to an instance of {@link FileTokenStore}
    */
   public Client(String consumerKey,
                 String consumerSecret,
                 Revision revision,
                 Token applicationToken, 
                 String baseUrl,
                 Boolean useHttps,
                 String callbackServerName,
                 Integer callbackServerPort,
                 TokenStore requestTokenStore) 
      throws NoSuchAlgorithmException,
             MalformedURLException {

      this.baseUrl = baseUrl == null ? DEFAULT_BASE_URL : baseUrl;
      this.useHttps = useHttps == null ? DEFAULT_USE_HTTPS : useHttps;

      // use base url with https, which may require https
      URL requestTokenUrl = new URL(getBaseUrlWithHttps() + REQUEST_TOKEN_PATH);
      URL accessTokenUrl = new URL(getBaseUrlWithHttps() + ACCESS_TOKEN_PATH);

      // use base url as is, which may skip https
      URL userAuthorizationUrl = new URL(getBaseUrl() + USER_AUTHORIZATION_PATH);

      this.serviceProvider = new ServiceProvider(requestTokenUrl,
                                                 userAuthorizationUrl,
                                                 accessTokenUrl,
                                                 EnumSet.of(SignatureMethod.HMAC_SHA1));
                                            
      this.consumer = new Consumer(consumerKey,
                                   consumerSecret,
                                   serviceProvider,
                                   revision);

      this.applicationToken = applicationToken;

      this.callbackServerName = callbackServerName;
      this.callbackServerPort = callbackServerPort;
      
      this.requestTokenStore = 
         requestTokenStore == null ?
         new FileTokenStore("requestToken") :
         requestTokenStore;

      this.userDiscoveryApi = new UserDiscoveryAPI(this);
      this.getLocationApi = new GetLocationAPI(this);
      this.setLocationApi = new SetLocationAPI(this);
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
    * Generate the base url for a callback from the current request.
    * <p>
    * The base url consists of the URL scheme, server name (host), and
    * optionally the port.
    */
   public StringBuilder prepareCallback(HttpServletRequest request) {
      String host = getCallbackServerName(request);
      int port = getCallbackServerPort(request);
      StringBuilder callback = new StringBuilder();
      callback.append(request.getScheme());
      callback.append("://");
      callback.append(host);
      if (port != 80 && port != 443) {
         callback.append(":");
         callback.append(port);
      }   
      return callback;
   }

   /**
    * Is this request an OAuth callback.
    * @param request the callback request
    */
   public boolean isCallback(HttpServletRequest request) {

      String token = request.getParameter(Parameter.Token.getKey());

      return token != null;
   }

   /**
    * Get an access token for an OAuth callback request 
    * @param request the callback request
    * @return a valid access token or null if none
    */
   public Token getAccessToken(HttpServletRequest request) {

      String token = request.getParameter(Parameter.Token.getKey());

      if (token == null) {
         logger.debug("Not a callback");
         return null;
      }

      Token requestToken = requestTokenStore.get(token);

      if (requestToken == null) {
         // request tokens are removed after use (see below),
         // so this scenario is fairly common on page reloads
         logger.debug("Could not find request token for: " + token);
         return null;
      }

      String verifier = request.getParameter(Parameter.Verifier.getKey());

      Token ret = null;
      try {
         // exchange for access token
         ret = consumer.getAccessToken(requestToken,verifier);
      } catch (OAuthException e) {
         logger.info("No access token was available for: " + token);
         logger.debug(e,e);
         return null;
      } catch (IOException e) {
         logger.info(e,e);
         return null;
      }

      requestTokenStore.remove(requestToken);

      return ret;
   }

   /**
    * Utility method for constructing a Veriplace redirect URL for OAuth user authorization.
    * <p>
    * @param callback the OAuth callback
    * @param immediate should responses return immediately if user interaction would be required?
    * @param uri the resource for which authorization is requested (e.g. location)
    */
   protected String getRedirectURL(String callback,
                                   boolean immediate,
                                   String uri) {

      ParameterSet parameters = new ParameterSet();

      // 'uri' and 'callback' are required
      parameters.put("uri",uri);
      if (consumer.getRevision() == Revision.Core1_0) {
         parameters.put(Parameter.Callback, callback);
      }

      // 'immediate' is optional and defaults to false
      if (immediate) {
         parameters.put("immediate","true");
      }

      try {
         Token requestToken = 
            consumer.getRevision() == Revision.Core1_0 ?
            consumer.getRequestToken() :
            consumer.getRequestToken(callback);

         requestTokenStore.add(requestToken);

         return consumer.getUserAuthorizationUrl(parameters,requestToken);
      } catch (OAuthException e) {
         logger.info(e,e);
         return null;
      } catch (IOException e) {
         logger.info(e,e);
         return null;
      }
   }

   /**
    * Utility method for retrieving the protected resource used by a particular API.
    */
   protected Response getProtectedResource(API api,
                                           User user,
                                           ParameterSet parameters,
                                           Token accessToken) {

      RequestMethod requestMethod = api.getRequestMethod();

      URL url = null;
      try {
         url = new URL(api.getURI(user));
      } catch (MalformedURLException e) {
         logger.warn(e,e);
         return null;
      }

      try {
         return consumer.getProtectedResource(url,
                                              requestMethod,
                                              parameters,
                                              accessToken,
                                              SignatureMethod.HMAC_SHA1);
      } catch (OAuthException e) {
         logger.info("Unable to obtain resource for access token: " + accessToken.getToken());
         logger.info(e.getMessage());
         logger.debug(e,e);
         return new Response(e.getCode(),e.getMessage(),null,null,null);
      } catch (IOException e) {
         logger.info(e,e);
         return null;
      }
   }

   /**
    * Retrieve the hostname from the request, unless it's been overridden.
    */
   protected String getCallbackServerName(HttpServletRequest request) {
      if (callbackServerName != null) {
         return callbackServerName;
      }
      return request.getServerName();
   }
   
   /**
    * Retrieve the port from the request, unless it's been overridden.
    */
   protected int getCallbackServerPort(HttpServletRequest request) {
      if (callbackServerPort != null) {
         return callbackServerPort.intValue();
      }
      return request.getServerPort();
   }
   
   /*** Accessors used by API implementations within this package ***/

   public String getBaseUrl() {
      return baseUrl;
   }

   public String getBaseUrlWithHttps() {
      if (useHttps) {
         return baseUrl.replace("http://","https://");
      } else {
         return baseUrl;
      }
   }

   public boolean getUseHttps() {
      return useHttps;
   }

   public Consumer getConsumer() {
      return consumer;
   }

   protected Token getApplicationToken() {
      return applicationToken;
   }

   protected TokenStore getRequestTokenStore() {
      return requestTokenStore;
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


