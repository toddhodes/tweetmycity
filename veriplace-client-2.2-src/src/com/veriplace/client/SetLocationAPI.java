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

import com.veriplace.client.factory.LocationFactory;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

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
    * @param parameters a {@link SetLocationParameters} object containing any valid
    *    combination of properties
    * @return the updated location
    * @throws SetLocationException  if the server denied the request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Location setLocation(Token accessToken,
                               User user,
                               SetLocationParameters parameters)
         throws SetLocationException, UnexpectedException {
      if (! parameters.isValid()) {
         throw new BadParameterException("Invalid or missing SetLocationParameters");
      }
      if (parameters.getAddress() != null) {
         return setLocation(accessToken, user, parameters.getAddress());
      }
      return setLocation(accessToken, user,
            parameters.getLongitude().toString(), parameters.getLatitude().toString(),
            (parameters.getAccuracy() == null) ? null : parameters.getAccuracy().toString());
   }
   
   /**
    * Set a user's location
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param longitude the longitude
    * @param latitude the latitude
    * @return the updated location
    * @throws SetLocationException  if the server denied the request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Location setLocation(Token accessToken,
                               User user,
                               String longitude,
                               String latitude)
         throws SetLocationException, UnexpectedException {
      
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
    * @throws SetLocationException  if the server denied the request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Location setLocation(Token accessToken,
                               User user,
                               String longitude,
                               String latitude,
                               String accuracy)
         throws SetLocationException, UnexpectedException {
      
      ParameterSet parameters = new ParameterSet();
      parameters.put("latitude", latitude);
      parameters.put("longitude", longitude);
      parameters.put("accuracy", accuracy);
                          
      APIInfo info = APIInfo.post(getURI(user));

      return doSetLocation(info, parameters, accessToken, user);
   }

   /**
    * Set a user's location
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param location the location
    * @return the updated location
    * @throws SetLocationException  if the server denied the request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Location setLocation(Token accessToken,
                               User user,
                               String location)
         throws SetLocationException, UnexpectedException {
      
      ParameterSet parameters = new ParameterSet();
      parameters.put("location", location);

      APIInfo info = APIInfo.post(getURI(user));

      return doSetLocation(info, parameters, accessToken, user);
   }
   
   /**
    * Get an access token representing permission to set a user's location, if the user has previously
    * granted that permission.  If so, the token can then be passed to one of the setLocation methods
    * to set the location; or you can simply use this method to test whether permission is available.
    * @param user the user
    * @return the access token; will not be null
    * @throws SetLocationException  if permission is not available
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Token getSetLocationAccessToken(User user)
         throws SetLocationException, UnexpectedException {
      ParameterSet emptyParams = new ParameterSet();

      APIInfo info = APIInfo.post(getURI(user));

      try {
         return getAccessToken(info, emptyParams);
      }
      catch (VeriplaceOAuthException e) {
         switch (e.getCode()) {
         case HttpServletResponse.SC_UNAUTHORIZED:  // 401
            throw new SetLocationException(e);
         }
         throw selectUnexpectedException(e);
      }
   }

   ////////////

   /**
    * @return  the Location object from a successful response
    * @throws SetLocationException  if the server denied the request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   protected Location doSetLocation(APIInfo info,
                                    ParameterSet parameters,
                                    Token accessToken,
                                    User user)
         throws SetLocationNotPermittedException,
                UpdateFailureException,
                UnexpectedException {

      try {
         Response response = 
            client.getProtectedResource(info,parameters,accessToken);
         // getProtectedResource guarantees that the response is OK
         
         Document document = documentFactory.getDocument(response.getBytes());
         return locationFactory.getLocationUpdate(document);
         // LocationFactory will throw an UpdateFailureException if the document
         // contains an error response.
      }
      catch (VeriplaceOAuthException e) {
         switch (e.getCode()) {
         case HttpServletResponse.SC_BAD_REQUEST:  // 400
            throw new BadParameterException(e.getMessage());
         case HttpServletResponse.SC_UNAUTHORIZED:  // 401
            throw new SetLocationNotPermittedException(e);
         }
         throw selectUnexpectedException(e);
      }
   }

   @Override
   protected String getURI(User user) {
      return client.getServerDirectUri() + 
         UserDiscoveryAPI.USER_DISCOVERY_PATH +
         user.getId() + 
         LOCATION_PATH;
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
