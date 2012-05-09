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
package com.veriplace.client.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.veriplace.client.Location;
import com.veriplace.client.MalformedResponseException;
import com.veriplace.client.PositionFailureException;
import com.veriplace.client.TestData;
import com.veriplace.client.UpdateFailureException;

import java.util.Date;

import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Unit tests for {@link com.veriplace.client.factory.LocationFactory}.
 */
public class TestLocationFactory 
   implements TestData {

   protected LocationFactory factory = new LocationFactory();
   protected DocumentFactory documentFactory = new DocumentFactory();
   
   @Test
   public void testGetLocation() throws Exception {

      Document document = documentFactory.getDocument(LOCATION_DOCUMENT.getBytes());

      Location location = factory.getLocation(document);
      assertNotNull(location);

      assertEquals((long)location.getId(),998);
      assertEquals(location.getCreationDate(),new Date(1238591655000L));
      assertEquals(location.getExpirationDate(),new Date(1239196455000L));
      assertEquals((double)location.getLongitude(),103.0,0.0);
      assertEquals((double)location.getLatitude(),-34.44,0.0);
      assertNull(location.getStreet());
      assertNull(location.getNeighborhood());
      assertNull(location.getCity());
      assertNull(location.getState());
      assertNull(location.getPostal());
      assertNull(location.getCountryCode());
   }

   @Test
   public void testGetLocationWithAddress() throws Exception {

      Document document = documentFactory.getDocument(LOCATION_ADDRESS_DOCUMENT.getBytes());

      Location location = factory.getLocation(document);
      assertNotNull(location);

      assertEquals((long)location.getId(),998);
      assertEquals(location.getCreationDate(),new Date(1238591655000L));
      assertEquals(location.getExpirationDate(),new Date(1239196455000L));
      assertEquals((double)location.getLongitude(),103.0,0.0);
      assertEquals((double)location.getLatitude(),-34.44,0.0);
      assertEquals(location.getStreet(),"123 Main St");
      assertEquals(location.getNeighborhood(),"Downtown");
      assertEquals(location.getCity(),"Anywhere");
      assertEquals(location.getState(),"ST");
      assertEquals(location.getPostal(),"99999");
      assertEquals(location.getCountryCode(),"US");
   }

   @Test
   public void testGetLocationError() throws Exception {

      Document document = documentFactory.getDocument(LOCATION_ERROR_DOCUMENT.getBytes());

      try {
         factory.getLocation(document);
         fail("exception expected");
      } catch (PositionFailureException error) {
         assertNotNull(error);
         assertEquals(ERROR_CODE, error.getCode());
         assertEquals(ERROR_MESSAGE, error.getMessage());

         Location location = error.getCachedLocation();
         assertNull(location);
      }
   }

   @Test
   public void testGetLocationErrorWithCachedLocation() throws Exception {

      Document document = documentFactory.getDocument(LOCATION_ERROR_CACHED_DOCUMENT.getBytes());

      try {
         factory.getLocation(document);
         fail("exception expected");
      } catch (PositionFailureException error) {
         assertNotNull(error);
         assertEquals(ERROR_CODE, error.getCode());
         assertEquals(ERROR_MESSAGE, error.getMessage());

         Location location = error.getCachedLocation();
         assertNotNull(location);
         assertEquals((long)location.getId(),998);
         assertEquals(location.getCreationDate(),new Date(1238591655000L));
         assertEquals(location.getExpirationDate(),new Date(1239196455000L));
         assertEquals((double)location.getLongitude(),103.0,0.0);
         assertEquals((double)location.getLatitude(),-34.44,0.0);
         assertNull(location.getStreet());
         assertNull(location.getNeighborhood());
         assertNull(location.getCity());
         assertNull(location.getState());
         assertNull(location.getPostal());
         assertNull(location.getCountryCode());
      }
   }

   @Test
   public void testGetLocationMalformedResponse() throws Exception {

      Document document = documentFactory.getDocument(LOCATION_MALFORMED_DOCUMENT.getBytes());

      try {
         factory.getLocation(document);
         fail("exception expected");
      } catch (MalformedResponseException error) {
      }
   }

   @Test
   public void testSetLocation() throws Exception {

      Document document = documentFactory.getDocument(LOCATION_UPDATE_DOCUMENT.getBytes());

      Location location = factory.getLocationUpdate(document);
      assertNotNull(location);

      assertNull(location.getId());
      assertNull(location.getCreationDate());
      assertNull(location.getExpirationDate());
      assertEquals((double)location.getLongitude(),103.0,0.0);
      assertEquals((double)location.getLatitude(),-34.44,0.0);
      assertNull(location.getStreet());
      assertNull(location.getNeighborhood());
      assertNull(location.getCity());
      assertNull(location.getState());
      assertNull(location.getPostal());
      assertNull(location.getCountryCode());
   }

   @Test
   public void testSetLocationError() throws Exception {

      Document document = documentFactory.getDocument(LOCATION_UPDATE_ERROR_DOCUMENT.getBytes());

      try {
         factory.getLocationUpdate(document);
         fail("exception expected");
      } catch (UpdateFailureException error) {
         assertNotNull(error);
         assertEquals(error.getCode(),301);
         assertEquals(error.getMessage(),"Ambiguous Location");
         assertEquals(error.getSuggestions().size(),2);
         assertEquals(error.getSuggestions().get(0),"Suggestion 1");
         assertEquals(error.getSuggestions().get(1),"Suggestion 2");
      }
   }
   @Test
   public void testSetLocationMalformedResponse() throws Exception {

      Document document = documentFactory.getDocument(LOCATION_UPDATE_MALFORMED_DOCUMENT.getBytes());

      try {
         factory.getLocationUpdate(document);
         fail("exception expected");
      } catch (MalformedResponseException error) {
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
