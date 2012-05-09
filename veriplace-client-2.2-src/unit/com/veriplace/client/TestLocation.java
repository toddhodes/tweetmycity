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

import org.junit.Test;

/**
 * Unit tests for {@link com.veriplace.client.Location}.
 */
public class TestLocation implements TestData {

   @Test
   public void testSuccessConstructor() throws Exception {
      Location location = new Location(LOCATION_ID, CREATION_DATE, EXPIRATION_DATE,
            LONGITUDE, LATITUDE, ACCURACY,
            STREET, NEIGHBORHOOD, CITY, STATE, POSTAL, COUNTRY_CODE);
      assertEquals(LOCATION_ID, location.getId());
      assertEquals(CREATION_DATE, location.getCreationDate());
      assertEquals(EXPIRATION_DATE, location.getExpirationDate());
      assertEquals(LONGITUDE, location.getLongitude());
      assertEquals(LATITUDE, location.getLatitude());
      assertEquals(ACCURACY, location.getUncertainty());
      assertEquals(STREET, location.getStreet());
      assertEquals(NEIGHBORHOOD, location.getNeighborhood());
      assertEquals(CITY, location.getCity());
      assertEquals(STATE, location.getState());
      assertEquals(POSTAL, location.getPostal());
      assertEquals(COUNTRY_CODE, location.getCountryCode());
   }

   @Test
   public void testToAddress() throws Exception {

      Location location;

      location = new Location(LOCATION_ID, CREATION_DATE, EXPIRATION_DATE,
                              LONGITUDE, LATITUDE, ACCURACY,
                              STREET, NEIGHBORHOOD, CITY, STATE, POSTAL, COUNTRY_CODE);

      assertEquals(location.getAddressLine(),STREET + ", " + CITY + ", " + STATE + " " + POSTAL);

      location = new Location(LOCATION_ID, CREATION_DATE, EXPIRATION_DATE,
                              LONGITUDE, LATITUDE, ACCURACY,
                              null, NEIGHBORHOOD, CITY, STATE, POSTAL, COUNTRY_CODE);

      assertEquals(location.getAddressLine(),CITY + ", " + STATE + " " + POSTAL);

      location = new Location(LOCATION_ID, CREATION_DATE, EXPIRATION_DATE,
                              LONGITUDE, LATITUDE, ACCURACY,
                              null, NEIGHBORHOOD, CITY, STATE, null, COUNTRY_CODE);

      assertEquals(location.getAddressLine(),CITY + ", " + STATE);
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
