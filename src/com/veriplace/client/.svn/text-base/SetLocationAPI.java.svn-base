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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.IOException;

/**
 * Interface for submitting a new user location to Veriplace -- not yet supported
 * in the current platform.  In a future release, this feature will be enabled
 * selectively for applications with additional security constraints, allowing
 * them to override Veriplace's usual sources of location data.  In the current
 * release, calls to setLocation will be checked for validity, but will not affect
 * any subsequent location queries.
 * <p>
 * User location can only be updated by referencing a valid Veriplace {@link User}.
 * Users can be identified using the {@link UserDiscoveryAPI}.
 * <p>
 * An OAuth Access Token is required to update a User's location, which may be
 * retrieved by redirecting the User Agent to Veriplace as part of the OAuth 
 * user authorization process. Access Tokens may be cached.
 * <p>
 * A typical update flow will look something like: 
 * <pre>
 * // construct a callback URL
 * String callback = client.prepareCallback(request) + "/callback";
 * // construct the redirect URL for user authorization
 * String redirectUrl = client.getSetLocationAPI().getRedirectURL(callback,user);
 * // redirect the User Agent
 * response.sendRedirect(redirectUrl);
 * ...
 * // handle the callback
 * if (client.isCallback(request)) {
 *   // retrieve the Access Token, if any
 *   Token accessToken = client.getAccessToken(request);
 *   if (accessToken != null) {
 *     // update location
 *     Location location = client.getSetLocationAPI().setLocation(accessToken,user,location);
 *   }
 * }
 * </pre>
 */
public class SetLocationAPI 
   extends API {

   private static final Log logger = LogFactory.getLog(SetLocationAPI.class);

   public static final String LOCATION_PATH = "/location";

   protected final LocationFactory locationFactory = new LocationFactory();

   public SetLocationAPI(Client client) {
      super(client);
   }

   /**
    * Set a user's location
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param longitude the longitude
    * @param latitude the latitude
    * @return the updated location
    */
   public Location setLocation(Token accessToken,
                               User user,
                               String longitude,
                               String latitude) {
      return setLocation(accessToken,user,longitude,latitude,null);
   }


   /**
    * Set a user's location
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param longitude the longitude
    * @param latitude the latitude
    * @param accuracy the accuracy
    * @return the updated location
    */
   public Location setLocation(Token accessToken,
                               User user,
                               String longitude,
                               String latitude,
                               String accuracy) {
      ParameterSet parameters = new ParameterSet();
      parameters.put("latitude", latitude);
      parameters.put("longitude", longitude);
      parameters.put("accuracy", accuracy);
                          
      return doSetLocation(parameters, accessToken, user);
   }

   /**
    * Set a user's location
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param location the location
    * @return the updated location
    */
   public Location setLocation(Token accessToken,
                               User user,
                               String location) {
      ParameterSet parameters = new ParameterSet();
      parameters.put("location", location);

      return doSetLocation(parameters, accessToken, user);
   }

   ////////////

   protected Location doSetLocation(ParameterSet parameters,
                                    Token accessToken,
                                    User user) {

      Response response = 
         client.getProtectedResource(this,user,parameters,accessToken);

      if (response != null &&
          response.isOk()) {
         Document document = documentFactory.getDocument(response.getBytes());
         return locationFactory.getLocationUpdate(document);
      } else {
         return null;
      }
   }

   @Override
   protected String getURI(User user) {
      return
         client.getBaseUrlWithHttps() + 
         UserDiscoveryAPI.USER_DISCOVERY_PATH +
         user.getId() + 
         LOCATION_PATH;
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
