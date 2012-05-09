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

import com.veriplace.client.factory.UserFactory;

import com.veriplace.oauth.OAuthException;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;

import org.w3c.dom.Document;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.IOException;

/**
 * Interface for obtaining user identity information from Veriplace.
 * <p>
 * User identity can be obtained in one of two ways:
 * <ul>
 * <li>Using some known personally identifiable information, such as an email address</li>
 * <li>Using User Agent redirection to get the current user to identify themself</li>
 * </ul>
 * Both requests use OAuth; in the former case, the {@link Client#getApplicationToken application-specific Access Token}
 * is used to sign the protected resource request whereas in the latter case, a successful
 * redirect results in Veriplace issuing a one-time Access Token.
 * <p>
 * A typical flow involving User Agent redirection will look something like: 
 * <pre>
 * // construct a callback URL
 * String callback = client.prepareCallback(request) + "/callback";
 * // construct the redirect URL for user authorization
 * String redirectUrl = client.getUserDiscoveryAPI().getRedirectURL(callback,null);
 * // redirect the User Agent
 * response.sendRedirect(redirectUrl);
 * ...
 * // handle the callback
 * if (client.isCallback(request)) {
 *   // retrieve the Access Token, if any
 *   Token accessToken = client.getAccessToken(request);
 *   if (accessToken != null) {
 *     // get user
 *     User user = client.getUserDiscoveryAPI().getUser(accessToken);
 *   }
 * }
 * </pre>
 */
public class UserDiscoveryAPI
   extends API {

   private static final Log logger = LogFactory.getLog(UserDiscoveryAPI.class);

   public static final String USER_DISCOVERY_PATH = "/api/1.0/users/";

   protected final UserFactory userFactory = new UserFactory();
   
   public UserDiscoveryAPI(Client client) {
      super(client);
   }

   /**
    * Find an enrolled user by supplying a known email address.
    * <p>
    * This request uses the application-specific Access Token.
    * @param email the user's email address
    * @return the user or null if none
    */
   public User getUserByEmail(String email) {
      ParameterSet parameters = new ParameterSet();
      parameters.put("email",email);

      return doGetUser(parameters,
                       client.getApplicationToken());
   }
   
   /**
    * Find an enrolled user by supplying a known mobile number.
    * <p>
    * This request uses the application-specific Access Token.
    * @param mobileNumber the user's mobile number
    * @return the user or null if none
    */
   public User getUserByMobileNumber(String mobileNumber) {
      ParameterSet parameters = new ParameterSet();
      parameters.put("mobile",mobileNumber);

      return doGetUser(parameters,
                       client.getApplicationToken());
   }

   /**
    * Find an enrolled user by supplying a known OpenId.
    * <p>
    * This request uses the application-specific Access Token.
    * @param openId the user's OpenID
    * @return the user or null if none
    */
   public User getUserByOpenId(String openId){ 

      ParameterSet parameters = new ParameterSet();
      parameters.put("openid",openId);

      return doGetUser(parameters,
                       client.getApplicationToken());
   }

   /**
    * Find an enrolled user by supplying a one-time Access Token.
    * @param accessToken the access token permitting the user to be discovered
    * @return the user or null if none
    */
   public User getUser(Token accessToken) {
      
      ParameterSet parameters = new ParameterSet();
      return doGetUser(parameters,
                       accessToken);
   }

   /////////////////////

   /**
    * Actually retrieve the user resource
    */
   protected User doGetUser(ParameterSet parameters,
                            Token accessToken) {
                               
      Response response = 
         client.getProtectedResource(this,null,parameters,accessToken);

      if (response != null &&
          response.isOk()) {
         Document document = documentFactory.getDocument(response.getBytes());
         return userFactory.getUser(document);
      } else {
         return null;
      }
   }

   @Override
   protected String getURI(User user) {
      return 
         client.getBaseUrlWithHttps() + 
         USER_DISCOVERY_PATH;
   }

   @Override
   protected RequestMethod getRequestMethod() {
      return RequestMethod.GET;
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
