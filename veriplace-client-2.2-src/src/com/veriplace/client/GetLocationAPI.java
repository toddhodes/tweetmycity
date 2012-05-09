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
import com.veriplace.client.factory.UserFactory;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import java.util.List;

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
   public static final String PERMISSIONS_PATH = "/api/1.0/permissions";

   protected final LocationFactory locationFactory = new LocationFactory();
   protected final UserFactory userFactory = new UserFactory();
   protected final String defaultLocationMode;
   protected final LocationFilter locationFilter;
   
   public GetLocationAPI(Client client,
                         String defaultLocationMode,
                         LocationFilter locationFilter) {
      super(client);
      this.defaultLocationMode = defaultLocationMode;
      this.locationFilter = locationFilter;
   }

   /**
    * Get the default location mode for this client, if any.
    * @since 2.0
    */
   public String getDefaultLocationMode() {
      return defaultLocationMode;
   }
   
   /**
    * Get the {@link LocationFilter} implementation for this client, if any.
    * @since 2.1
    */
   public LocationFilter getLocationFilter() {
      return locationFilter;
   }

   /**
    * Get a user's location.
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @return the user's location
    * @throws GetLocationException  if the server refused the location request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Location getLocation(Token accessToken,
                               User user)
         throws GetLocationException, UnexpectedException {
      
      return getLocation(accessToken,user,defaultLocationMode,NO_TIMEOUT);
   }

   /**
    * Get a user's location.
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param timeout timeout in milliseconds
    * @return the user's location
    * @throws GetLocationException  if the server refused the location request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Location getLocation(Token accessToken,
                               User user,
                               int timeout)
         throws GetLocationException, UnexpectedException {
      
      return getLocation(accessToken,user,defaultLocationMode,timeout);
   }

   /**
    * Get a user's location.
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param mode the location request mode; see {@link com.veriplace.client.LocationMode}
    * @return the user's location
    * @throws GetLocationException  if the server refused the location request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Location getLocation(Token accessToken,
                               User user,
                               String mode)
         throws GetLocationException, UnexpectedException {
      return getLocation(accessToken,user,mode,NO_TIMEOUT);
   }

   /**
    * Get a user's location.
    * @param accessToken the access token permitting the user to be located
    * @param user the user
    * @param mode the location request mode; see {@link com.veriplace.client.LocationMode}
    * @param timeout timeout in milliseconds, if any
    * @return the user's location
    * @throws GetLocationException  if the server refused the location request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public Location getLocation(Token accessToken,
                               User user,
                               String mode,
                               Integer timeout)
         throws GetLocationException, UnexpectedException {
      ParameterSet parameters = new ParameterSet();
      if (mode != null) {
         parameters.put("mode",mode);
      }

      APIInfo info = APIInfo.post(getURI(user));

      return doGetLocation(info,parameters,accessToken,user,timeout,true);
   }

   /**
    * Get a previously obtained location again by its location ID (from
    * {@link com.veriplace.client.Location#getId()}).  This requires an access token for
    * permission to locate the user in question.
    * @throws GetLocationException  if the server refused the location request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    * @since 2.0
    */
   public Location getLocationById(Token accessToken,
                                   User user,
                                   long locationId)
         throws GetLocationException, UnexpectedException {
      
      ParameterSet parameters = new ParameterSet();

      APIInfo info = APIInfo.get(getURI(user) + "/" + locationId);

      return doGetLocation(info,parameters,accessToken,user,NO_TIMEOUT,false);
   }
   
   /**
    * Get an access token representing permission to locate a user, if the user has previously granted
    * that permission.  If so, the token can then be passed to {@link #getLocation(Token, User)} to get
    * the location; or you can simply use this method to test whether permission is available.
    * @param user the user
    * @return the access token; will not be null
    * @throws GetLocationNotPermittedException  if permission is not available
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    * @since 2.0
    */
   public Token getLocationAccessToken(User user)
         throws GetLocationNotPermittedException, UnexpectedException {

      ParameterSet emptyParams = new ParameterSet();

      APIInfo info = APIInfo.get(getURI(user));

      try {
         return getAccessToken(info,emptyParams);
      }
      catch (VeriplaceOAuthException e) {
         switch (e.getCode()) {
         case HttpServletResponse.SC_UNAUTHORIZED:  // 401
            throw new GetLocationNotPermittedException(e);
         }
         throw selectUnexpectedException(e);
      }
   }

   /**
    * Get a list of users granting permission.
    * @throws UserDiscoveryNotPermittedException if the service refused the get permissions request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    * @since 2.2
    */
   public List<User> getPermittedUsers()
      throws UserDiscoveryNotPermittedException, UnexpectedException {

      ParameterSet emptyParams = new ParameterSet();

      APIInfo info = APIInfo.get(client.getServerDirectUri() + PERMISSIONS_PATH);

      return doGetPermittedUsers(info,emptyParams,NO_TIMEOUT);
   }

   /**
    * Get a list of users granting permission with paging.
    * @param first the index of the first result, starting with zero
    * @param max the maximum number of results
    * @throws UserDiscoveryNotPermittedException if the service refused the get permissions request
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    * @since 2.2
    */
   public List<User> getPermittedUsers(int first, int max)
      throws UserDiscoveryNotPermittedException, UnexpectedException {

      ParameterSet parameters = new ParameterSet();
      parameters.put("first",String.valueOf(first));
      parameters.put("max",String.valueOf(max));

      APIInfo info = APIInfo.get(client.getServerDirectUri() + PERMISSIONS_PATH);

      return doGetPermittedUsers(info,parameters,NO_TIMEOUT);
   }
   
   ////////////

   /**
    * Submits a request to the Get Location API.
    * @return  a Location object; will not be null
    * @throws GetLocationException  if the server refused the location request
    * @throws BadParameterException  if the server did not understand the location request
    * @throws VeriplaceOAuthException  if there was an unexpected failure in OAuth token exchange
    * @throws MalformedResponseException  if the server returned an unreadable response
    * @throws TransportException  if there was an I/O error in communication with the server
    */
   protected Location doGetLocation(APIInfo info,
                                    ParameterSet parameters,
                                    Token accessToken,
                                    User user,
                                    Integer timeout,
                                    boolean useFilter)
         throws GetLocationException,
                BadParameterException,
                VeriplaceOAuthException,
                MalformedResponseException,
                TransportException {

      try {
         Response response = 
            client.getProtectedResource(info,parameters,accessToken,timeout);
         Document document = documentFactory.getDocument(response.getBytes());
         Location location = locationFactory.getLocation(document);
         if (locationFilter != null) {
            Location newLocation = locationFilter.filterLocation(location, null);
            if (newLocation != null) {
               location = newLocation;
            }
         }
         return location;
      }
      catch (PositionFailureException e) {
         if (locationFilter != null) {
            Location location = locationFilter.filterLocation(null, e);
            if (location != null) {
               return location;
            }
         }
         throw e;
      }
      catch (VeriplaceOAuthException e) {
         switch (e.getCode()) {
         case HttpServletResponse.SC_BAD_REQUEST:  // 400
            throw new BadParameterException(e.getMessage());
         case HttpServletResponse.SC_UNAUTHORIZED:  // 401
            throw new GetLocationNotPermittedException(e);
         case HttpServletResponse.SC_FORBIDDEN:  // 403
            throw new GetLocationBillingDeclinedException();
         case HttpServletResponse.SC_NOT_FOUND:  // 404
            // if GetLocationById fails to find an id...
            throw new GetLocationException();
         }
         throw e;
      }
   }

   /**
    * Submits a request to the Get Permissions API.
    * @return  a list of users object; will not be null
    * @throws BadParameterException  if the server did not understand the request
    * @throws VeriplaceOAuthException  if there was an unexpected failure in OAuth token exchange
    * @throws MalformedResponseException  if the server returned an unreadable response
    * @throws TransportException  if there was an I/O error in communication with the server
    * @throws UserDiscoveryNotPermittedException if the service refused the get permissions request
    */
   protected List<User> doGetPermittedUsers(APIInfo info,
                                            ParameterSet parameters,
                                            Integer timeout) 
      throws UserDiscoveryNotPermittedException, UnexpectedException {

      if (client.getApplicationToken() == null) {
         throw new UnexpectedException("Application token must be configured");
      }

      try {
         Response response = 
            client.getProtectedResource(info,parameters,client.getApplicationToken(),timeout);
         Document document = documentFactory.getDocument(response.getBytes());
         return userFactory.getUsers(document);
      } catch (VeriplaceOAuthException e) {
         switch (e.getCode()) {
         case HttpServletResponse.SC_BAD_REQUEST:  // 400
            throw new BadParameterException(e.getMessage());
         case HttpServletResponse.SC_UNAUTHORIZED:  // 401
            throw new UserDiscoveryNotPermittedException(e);
         }
         throw e;
      }
   }

   @Override
   protected String getURI(User user) {
      return client.getServerDirectUri() + 
         UserDiscoveryAPI.USER_DISCOVERY_PATH +
         user.getId() + 
         LOCATIONS_PATH;
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
