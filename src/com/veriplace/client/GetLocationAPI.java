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

import com.veriplace.client.factory.LocationFactory;

import com.veriplace.oauth.OAuthException;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;

import org.w3c.dom.Document;

import org.apache.commons.httpclient.HttpStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.IOException;
import java.util.Date;

/**
 * Interface for obtaining a user's location from Veriplace.
 * <p>
 * User location can only be obtained by referencing a valid Veriplace {@link User}.
 * Users can be identified using the {@link UserDiscoveryAPI}.
 * <p>
 * An OAuth Access Token is required to obtain a User's location, which may be
 * retrieved by redirecting the User Agent to Veriplace as part of the OAuth 
 * user authorization process. Access Tokens may be cached.
 * <p>
 * A typical update flow will look something like: 
 * <pre>
 * // construct a callback URL
 * String callback = client.prepareCallback(request) + "/callback";
 * // construct the redirect URL for user authorization
 * String redirectUrl = client.getGetLocationAPI().getRedirectURL(callback,user);
 * // redirect the User Agent
 * response.sendRedirect(redirectUrl);
 * ...
 * // handle the callback
 * if (client.isCallback(request)) {
 *   // retrieve the Access Token, if any
 *   Token accessToken = client.getAccessToken(request);
 *   if (accessToken != null) {
 *     // update location
 *     Location location = client.getGetLocationAPI().getLocation(accessToken,user);
 *   }
 * }
 * </pre>
 */
public class GetLocationAPI
   extends API {

   private static final Log logger = LogFactory.getLog(GetLocationAPI.class);

   public static final String LOCATIONS_PATH = "/locations";

   protected final LocationFactory locationFactory = new LocationFactory();

   public GetLocationAPI(Client client) {
      super(client);
   }

   /**
    * Get a user's location
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @return the user's location
    */
   public Location getLocation(Token accessToken,
                               User user) {
      return getLocation(accessToken,user,null);
   }

   /**
    * Get a user's location
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param mode the location request mode; see {@link com.veriplace.client.LocationMode}
    * @return the user's location
    */
   public Location getLocation(Token accessToken,
                               User user,
                               String mode) {

      ParameterSet parameters = new ParameterSet();
      if (mode != null) {
         parameters.put("mode",mode);
      }

      return doGetLocation(parameters, accessToken, user);
   }

   ////////////

   protected Location doGetLocation(ParameterSet parameters,
                                    Token accessToken,
                                    User user) {

      Response response = 
         client.getProtectedResource(this,user,parameters,accessToken);

      if (response != null){

         if (response.isOk()) {

            Document document = documentFactory.getDocument(response.getBytes());
            return locationFactory.getLocation(document);
         } else if (response.getCode() == HttpStatus.SC_FORBIDDEN) {
            /* 403 errors indicate a billing authorization problem.
             * Return a non-null Location object to pass this code up the stack.
             */
            return new Location(null,
                                new Date(),
                                null,
                                response.getCode(),
                                response.getReasonPhrase());            
         }
      } 

      return null;
   }

   @Override
   protected String getURI(User user) {
      return
         client.getBaseUrlWithHttps() + 
         UserDiscoveryAPI.USER_DISCOVERY_PATH +
         user.getId() + 
         LOCATIONS_PATH;
   }

   @Override
   protected RequestMethod getRequestMethod() {
      return RequestMethod.POST;
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
