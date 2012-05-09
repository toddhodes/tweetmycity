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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.RequestMethod;

import java.util.Date;

import org.junit.Test;

/**
 * Unit tests for {@link com.veriplace.client.GetLocationAPI}.  These tests verify that the
 * API sends the correct parameters to the server (although we do not check OAuth signatures
 * here) and correctly decodes both success and failure responses.
 */
public class TestGetLocationAPI extends TestBase {

   @Test
   public void testGetRedirectURL() throws Exception {
      String callback = createCallback();
      String expectedUrl = createUserAuthRedirectUrl(REQUEST_TOKEN, LOCATION_REQUEST_URI);

      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      String url = client.getGetLocationAPI().getRedirectURL(callback, USER);
      assertEquals(expectedUrl, url);
   }
   
   @Test
   public void testGetLocationSuccess() throws Exception {
      String locationMode = "area";
      
      prepareLocationRequest(ACCESS_TOKEN, locationMode, 200, LOCATION_DOCUMENT);
      
      Location location = client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, locationMode);
      
      assertNotNull(location);
      assertEquals(LOCATION_ID, location.getId());
      // we'll check the other location properties in TestLocationFactory
   }

   @Test
   public void testGetLocationPositionFailure() throws Exception {
      // server can return an error result in a location document,
      // even if HTTP response code is 200
      String locationMode = "area";
      
      prepareLocationRequest(ACCESS_TOKEN, locationMode, 200, LOCATION_ERROR_DOCUMENT);

      try {
         client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, locationMode);
         fail("position failure expected");
      } catch (PositionFailureException e) {
         assertEquals(ERROR_CODE, e.getCode());
         assertEquals(ERROR_MESSAGE, e.getMessage());
      }
   }

   @Test
   public void testGetLocationBillingFailureWithDocument() throws Exception {
      // if HTTP response code is 403, should get a specific exception indicating billing failure
      String locationMode = "area";
      
      prepareLocationRequest(ACCESS_TOKEN, locationMode, 403, null);
      
      try {
         client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, locationMode);
         fail("Expected BillingDeclinedException");
      }
      catch (GetLocationBillingDeclinedException e) {
      }
   }

   @Test
   public void testGetLocationNotPermitted() throws Exception {
      // for HTTP error 401, throw GetLocationNotPermittedException 
      String locationMode = "area";
      
      prepareLocationRequest(ACCESS_TOKEN, locationMode, 401, null);
      
      try {
         client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, locationMode);
         fail("Expected GetLocationNotPermittedException");
      }
      catch (GetLocationNotPermittedException e) {
         assertNotNull(e.getCause());
         assertEquals(VeriplaceOAuthException.class, e.getCause().getClass());
         assertEquals(401, ((VeriplaceOAuthException) e.getCause()).getCode());
      }
   }

   @Test
   public void testGetLocationBadParameter() throws Exception {
      // for HTTP error 400, throw BadParameterException 
      String locationMode = "area";
      
      mockServer.addStep()
            .setExpectedMethod(RequestMethod.POST)
            .setExpectedRelativeUrl(LOCATION_REQUEST_URI)
            .setExpectedToken(ACCESS_TOKEN)
            .setExpectedParameter("mode", locationMode)
            .setResponseCode(400);
      
      try {
         client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, locationMode);
         fail("Expected BadParameterException");
      }
      catch (BadParameterException e) {
      }
   }

   @Test
   public void testGetLocationOtherError() throws Exception {
      // for HTTP errors other than 400, 401, 403, or 404, throw VeriplaceOAuthException
      
      String locationMode = "area";
      
      prepareLocationRequest(ACCESS_TOKEN, locationMode, 500, null);
      
      try {
         client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, locationMode);
         fail("Expected VeriplaceOAuthException");
      }
      catch (VeriplaceOAuthException e) {
         assertNotNull(e.getCause());
         assertEquals(500, e.getCode());
      }
   }

   @Test
   public void testGetLocationByIdSuccess() throws Exception {
      mockServer.addStep()
            .setExpectedMethod(RequestMethod.GET)
            .setExpectedRelativeUrl(LOCATION_REQUEST_URI + "/" + LOCATION_ID)
            .setExpectedToken(ACCESS_TOKEN)
            .setResponseCode(200)
            .setResponseBody(LOCATION_DOCUMENT);
      
      Location location = client.getGetLocationAPI().getLocationById(ACCESS_TOKEN, USER, LOCATION_ID);
      
      assertNotNull(location);
      assertEquals(LOCATION_ID, location.getId());
   }
   
   @Test
   public void testGetLocationByIdNotFound() throws Exception {
      // For not found error (404), throw GetLocationException with no nested OAuth details
      
      Long badLocationId = LOCATION_ID + 100;
      
      mockServer.addStep()
            .setExpectedMethod(RequestMethod.GET)
            .setExpectedRelativeUrl("/api/1.0/users/" + USER_ID + "/locations/" + badLocationId)
            .setExpectedToken(ACCESS_TOKEN)
            .setResponseCode(404);
      
      try {
         client.getGetLocationAPI().getLocationById(ACCESS_TOKEN, USER, badLocationId);
         fail("Expected GetLocationException");
      }
      catch (GetLocationException e) {
         assertEquals(GetLocationException.class, e.getClass());  // not a subclass
      }
   }
   
   @Test
   public void testGetLocationAccessToken() throws Exception {
      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      
      mockServer.shouldGrantUserAuthorizationRedirect(REQUEST_TOKEN, VERIFIER,
            client.getServerDirectUri() + LOCATION_REQUEST_URI, "", true);
      
      mockServer.shouldGrantAccessToken(REQUEST_TOKEN, VERIFIER, ACCESS_TOKEN);
      
      Token result = client.getGetLocationAPI().getLocationAccessToken(USER);
      
      assertNotNull(result);
      assertEquals(ACCESS_TOKEN.getToken(), result.getToken());
      assertEquals(ACCESS_TOKEN.getTokenSecret(), result.getTokenSecret());
   }
    
   @Test
   public void testGetLocationAccessTokenFailure() throws Exception {
      // For invalid token error (401), throw GetLocationNotPermittedException
      
      mockServer.shouldGrantRequestToken(REQUEST_TOKEN);
      
      mockServer.shouldGrantUserAuthorizationRedirect(REQUEST_TOKEN, VERIFIER,
            client.getServerDirectUri() + LOCATION_REQUEST_URI, "", true);
      
      mockServer.shouldRefuseAccessToken(REQUEST_TOKEN, VERIFIER);
      
      try {
         Token result = client.getGetLocationAPI().getLocationAccessToken(USER);
         fail("Expected GetLocationNotPermittedException");
      }
      catch (GetLocationNotPermittedException e) {
      }
   }

   @Test
   public void testGetLocationFilterSuppressesException() throws Exception {
      // PositionFailureException can be suppressed by a LocationFilter

      Location myLocation = createAlternateLocation();
      useLocationFilter(new TestLocationFilterReturnsSuccess(myLocation));
      
      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_ERROR_DOCUMENT);

      Location location = client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, null);
      assertEquals(myLocation.getId(), location.getId());
   }

   @Test
   public void testGetLocationFilterChangesLocation() throws Exception {
      // LocationFilter can change the return value of a successful request

      Location myLocation = createAlternateLocation();
      useLocationFilter(new TestLocationFilterReturnsSuccess(myLocation));
      
      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_DOCUMENT);

      Location location = client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, null);
      assertEquals(myLocation.getId(), location.getId());
   }

   @Test
   public void testGetLocationFilterThrowsException() throws Exception {
      // LocationFilter can change a successful request to an error

      useLocationFilter(new TestLocationFilterReturnsException(new GetLocationBillingDeclinedException()));

      prepareLocationRequest(ACCESS_TOKEN, null, 200, LOCATION_DOCUMENT);

      try {
         client.getGetLocationAPI().getLocation(ACCESS_TOKEN, USER, null);
         fail("Expected GetLocationBillingDeclinedException");
      }
      catch (GetLocationBillingDeclinedException e) {
      }
   }
   
   protected void useLocationFilter(LocationFilter locationFilter) throws Exception {
      ClientConfiguration config = new ClientConfiguration(CONSUMER_KEY, CONSUMER_SECRET);
      config.setTokenStore(tokenStore);
      config.setLocationFilter(locationFilter);
      client = new Client(config);
      client.getConsumer().setClient(mockServer);
   }

   protected Location createAlternateLocation() {
      long id = LOCATION_ID + 100;
      return new Location(id, new Date(), new Date(), LONGITUDE, LATITUDE, ACCURACY,
            STREET, NEIGHBORHOOD, CITY, STATE, POSTAL, COUNTRY_CODE); 
   }
   
   class TestLocationFilterReturnsSuccess implements LocationFilter {

      private Location myLocation;
      
      public TestLocationFilterReturnsSuccess(Location myLocation) {
         this.myLocation = myLocation;
      }
      
      public Location filterLocation(Location returnedLocation,
            PositionFailureException returnedException)
            throws GetLocationException {
         return myLocation;
      }
   }

   class TestLocationFilterReturnsException implements LocationFilter {

      private GetLocationException myException;
      
      public TestLocationFilterReturnsException(GetLocationException myException) {
         this.myException = myException;
      }
      
      public Location filterLocation(Location returnedLocation,
            PositionFailureException returnedException)
            throws GetLocationException {
         throw myException;
      }
   }
}
