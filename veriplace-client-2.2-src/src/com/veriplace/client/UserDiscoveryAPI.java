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

import com.veriplace.client.factory.UserFactory;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

   public static final String EMAIL = "email";
   public static final String MOBILE = "mobile";
   public static final String OPENID = "openid";

   protected final UserFactory userFactory = new UserFactory();
   
   public UserDiscoveryAPI(Client client) {
      super(client);
   }

   /**
    * Find an enrolled user by supplying one of the supported identifying parameters
    * in {@link UserDiscoveryParameters}.
    * <p>
    * This request uses the application-specific Access Token.
    * @param parameters  a {@link UserDiscoveryParameters} object
    * @return the user or null if none
    * @throws UserDiscoveryException  if the server refused the user discovery request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public User getUserByParameters(UserDiscoveryParameters parameters)
      throws UserDiscoveryException, 
             UnexpectedException {
      ParameterSet ps = new ParameterSet();
      if ((parameters == null) || (! parameters.isSpecified())) {
         throw new BadParameterException("Invalid or missing UserDiscoveryParameters");
      }
      if (client.getApplicationToken() == null) {
         throw new UnexpectedException("Application token must be configured");
      }
      addParameters(parameters, ps);
      APIInfo info = APIInfo.get(getURI(null));
      return doGetUser(info, ps, client.getApplicationToken());
   }
   
   /**
    * Find an enrolled user by supplying a known email address.
    * <p>
    * This request uses the application-specific Access Token.
    * @param email the user's email address
    * @return the user or null if none
    * @throws UserDiscoveryException  if the server refused the user discovery request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public User getUserByEmail(String email)
      throws UserDiscoveryException, 
             UnexpectedException {
      return getUserByParameters(UserDiscoveryParameters.byEmail(email));
   }
   
   /**
    * Find an enrolled user by supplying a known mobile number.
    * <p>
    * This request uses the application-specific Access Token.
    * @param mobileNumber the user's mobile number
    * @return the user or null if none
    * @throws UserDiscoveryException  if the server refused the user discovery request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public User getUserByMobileNumber(String mobileNumber)
      throws UserDiscoveryException, 
             UnexpectedException {
      return getUserByParameters(UserDiscoveryParameters.byPhone(mobileNumber));
   }

   /**
    * Find an enrolled user by supplying a known OpenId.
    * <p>
    * This request uses the application-specific Access Token.
    * @param openId the user's OpenID
    * @return the user or null if none
    * @throws UserDiscoveryException  if the server refused the user discovery request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public User getUserByOpenId(String openId)
      throws UserDiscoveryException, 
             UnexpectedException { 
      return getUserByParameters(UserDiscoveryParameters.byOpenId(openId));
   }

   /**
    * Find an enrolled user by supplying a one-time Access Token.
    * @param accessToken the access token permitting the user to be discovered
    * @return the user or null if none
    * @throws UserDiscoveryException  if the server refused the user discovery request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public User getUser(Token accessToken)
      throws UserDiscoveryException, 
             UnexpectedException {
      
      ParameterSet parameters = new ParameterSet();

      APIInfo info = APIInfo.get(getURI(null));

      return doGetUser(info,
                       parameters,
                       accessToken);
   }

   /**
    * Find multiple users by supplying the supported identifying parameters
    * in {@link UserDiscoveryParameters}.
    * <p>
    * This request uses the application-specific Access Token.
    * @param list a list of {@link UserDiscoveryParameters} object
    * @throws UserDiscoveryException  if the server refused the user discovery request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Map<UserDiscoveryParameters,User> getUsersByParameters(List<UserDiscoveryParameters> list)
      throws UserDiscoveryException, 
             UnexpectedException {

      if (list == null) {
         throw new BadParameterException("Missing UserDiscoveryParameters list");
      }
      if (client.getApplicationToken() == null) {
         throw new UnexpectedException("Application token must be configured");
      }

      ParameterSet parameters = new ParameterSet();
      APIInfo info = APIInfo.get(getURI(null));
      int count = 0;
      for (UserDiscoveryParameters udp: list) {
         if (udp.isSpecified()) {
            addParameters(udp, parameters);
            count++;
         }
      }
      if (count == 0) {
         return Collections.emptyMap();
      }
      if (count == 1) {
         return Collections.singletonMap(list.get(0),
               doGetUser(info, parameters, client.getApplicationToken()));
      }

      return doGetUsersByPII(info,parameters,client.getApplicationToken(),NO_TIMEOUT);
   }
   

   /////////////////////

   /**
    * Actually retrieve the user resource
    * @return  a User object; will not be null
    * @throws UserDiscoveryException  if the server refused the user discovery request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   protected User doGetUser(APIInfo info,
                            ParameterSet parameters,
                            Token accessToken)
      throws UserDiscoveryException, 
             UnexpectedException {
                               
      try {
         Response response = 
            client.getProtectedResource(info,parameters,accessToken);
         Document document = documentFactory.getDocument(response.getBytes());
         return userFactory.getUser(document);
      }
      catch (VeriplaceOAuthException e) {
         switch (e.getCode()) {
         case HttpServletResponse.SC_BAD_REQUEST:  // 400
            throw new BadParameterException(e.getMessage());
         case HttpServletResponse.SC_UNAUTHORIZED:  // 401
            throw new UserDiscoveryNotPermittedException(e);
         case HttpServletResponse.SC_NOT_FOUND:  // 404
            throw new UserNotFoundException();
         }
         throw selectUnexpectedException(e);
      }
   }

   /**
    * Actually retrieve the users resource
    * @return a Map of User object; will not be null
    * @throws UserDiscoveryException  if the server refused the user discovery request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    * @since 2.2
    */
   protected Map<UserDiscoveryParameters,User> doGetUsersByPII(APIInfo info,
                                                               ParameterSet parameters,
                                                               Token accessToken,
                                                               Integer timeout)
      throws UserDiscoveryException, 
             UnexpectedException {
                               
      try {
         Response response = 
            client.getProtectedResource(info,parameters,client.getApplicationToken(),timeout);
         Document document = documentFactory.getDocument(response.getBytes());
         return userFactory.getUsersByPII(document);
      }
      catch (VeriplaceOAuthException e) {
         switch (e.getCode()) {
         case HttpServletResponse.SC_BAD_REQUEST:  // 400
            throw new BadParameterException(e.getMessage());
         case HttpServletResponse.SC_UNAUTHORIZED:  // 401
            throw new UserDiscoveryNotPermittedException(e);
         }
         throw selectUnexpectedException(e);
      }
   }

   @Override
   protected String getURI(User user) {
      return client.getServerDirectUri() + 
         USER_DISCOVERY_PATH;
   }
   
   private void addParameters(UserDiscoveryParameters udp, ParameterSet p) {
      if (udp.getPhone() != null) {
         p.put(MOBILE, udp.getPhone());
      }
      if (udp.getEmail() != null) {
         p.put(EMAIL, udp.getEmail());
      }
      if (udp.getOpenId() != null) {
         p.put(OPENID, udp.getOpenId());
      }
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
