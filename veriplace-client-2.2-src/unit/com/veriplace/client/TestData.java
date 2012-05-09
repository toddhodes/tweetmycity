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

import com.veriplace.oauth.consumer.Token;

import java.util.Date;

/**
 * Constants used by Veriplace client unit tests.
 */
public interface TestData {

   public static final String CONSUMER_KEY = "testapp";
   public static final String CONSUMER_SECRET = "secret";
   public static final String APP_TOKEN_VALUE = "wondertwin";
   public static final String APP_TOKEN_SECRET = "powers";
   public static final String REQUEST_TOKEN_VALUE = "mytoken";
   public static final String REQUEST_TOKEN_SECRET= "mysecret";
   public static final String VERIFIER = "veryfide";
   public static final String ACCESS_TOKEN_VALUE = "okey";
   public static final String ACCESS_TOKEN_SECRET= "dokey";
   public static final Token REQUEST_TOKEN = new Token(REQUEST_TOKEN_VALUE, REQUEST_TOKEN_SECRET);
   public static final Token ACCESS_TOKEN = new Token(ACCESS_TOKEN_VALUE, ACCESS_TOKEN_SECRET);
   public static final Token APP_TOKEN = new Token(APP_TOKEN_VALUE, APP_TOKEN_SECRET);
   
   public static final String USER_REQUEST_URI = "/api/1.0/users/";

   public static final long USER_ID = 999L;
   public static final User USER = new User(USER_ID);
   public static final String USER_DOCUMENT = "<user id=\"999\"/>";

   public static final String USERS_DOCUMENT = "<users>" 
       + "<user id=\"999\"/>"
       + "<user id=\"997\"/>"
       + "</users>";

   public static final String USERS_BY_PII_DOCUMENT = "<users>" 
       + "<user id=\"999\" key=\"1115551212\" keyType=\"mobile\"/>"
       + "<user id=\"997\" key=\"1115551213\" keyType=\"mobile\"/>"
       + "</users>";

   public static final String LOCATION_REQUEST_URI = "/api/1.0/users/" + USER_ID + "/locations";

   public static final Long LOCATION_ID = 998L;
   public static final Date CREATION_DATE = new Date();
   public static final Date EXPIRATION_DATE = new Date(System.currentTimeMillis() + 100000);
   public static final Double LONGITUDE = 103.0;
   public static final Double LATITUDE = -34.44;
   public static final Double ACCURACY = 32.50;
   public static final String STREET = "17 Highbrow Street";
   public static final String NEIGHBORHOOD = "St. John's Wood";
   public static final String CITY = "London";
   public static final String STATE = "England";
   public static final String POSTAL = "NW8";
   public static final String COUNTRY_CODE = "GBT";
   public static final int OK_CODE = 0;
   public static final int ERROR_CODE = 100;
   public static final String ERROR_MESSAGE = "NO YOU CAN'T";
   
   public static final String LOCATION_DOCUMENT = "<location id=\"998\">"
      + "<created>2009-04-01T13:14:15Z</created>"
      + "<expires>2009-04-08T13:14:15Z</expires>"
      + "<position>"
      + "<longitude>103.0</longitude>"
      + "<latitude>-34.44</latitude>"
      + "<accuracy>32.50</accuracy>"
      + "<street></street>"
      + "<neighborhood></neighborhood>"
      + "<city></city>"
      + "<state></state>"
      + "<postal></postal>"
      + "<countryCode></countryCode>"
      + "</position>"
      + "</location>";
   public static final String LOCATION_ADDRESS_DOCUMENT = "<location id=\"998\">"
      + "<created>2009-04-01T13:14:15Z</created>"
      + "<expires>2009-04-08T13:14:15Z</expires>"
      + "<position>"
      + "<longitude>103.0</longitude>"
      + "<latitude>-34.44</latitude>"
      + "<accuracy>32.50</accuracy>"
      + "<street>123 Main St</street>"
      + "<neighborhood>Downtown</neighborhood>"
      + "<city>Anywhere</city>"
      + "<state>ST</state>"
      + "<postal>99999</postal>"
      + "<countryCode>US</countryCode>"
      + "</position>"
      + "</location>";
   public static final String LOCATION_ERROR_DOCUMENT = "<location id=\"998\">"
      + "<created>2009-04-01T13:14:15Z</created>"
      + "<expires>2009-04-08T13:14:15Z</expires>"
      + "<positionError>"
      + "<code>100</code>"
      + "<message>NO YOU CAN'T</message>"
      + "</positionError>"
      + "</location>";
   public static final String LOCATION_ERROR_CACHED_DOCUMENT = "<location id=\"998\">"
      + "<created>2009-04-01T13:14:15Z</created>"
      + "<expires>2009-04-08T13:14:15Z</expires>"
      + "<positionError>"
      + "<code>100</code>"
      + "<message>NO YOU CAN'T</message>"
      + "<cachedPosition>"
      + "<longitude>103.0</longitude>"
      + "<latitude>-34.44</latitude>"
      + "<accuracy>32.50</accuracy>"
      + "<street></street>"
      + "<neighborhood></neighborhood>"
      + "<city></city>"
      + "<state></state>"
      + "<postal></postal>"
      + "<countryCode></countryCode>"
      + "</cachedPosition>"
      + "</positionError>"
      + "</location>";
   public static final String LOCATION_MALFORMED_DOCUMENT = "<location id=\"998\">"
      + "<created>2009-04-01T13:14:15Z</created>"
      + "<expires>2009-04-08T13:14:15Z</expires>"
      + "<position>"
      + "<longitude>x103</longitude>"
      + "<latitude>-34.44</latitude>"
      + "<accuracy>32.50</accuracy>"
      + "<street>123 Main St</street>"
      + "<neighborhood>Downtown</neighborhood>"
      + "<city>Anywhere</city>"
      + "<state>ST</state>"
      + "<postal>99999</postal>"
      + "<countryCode>US</countryCode>"
      + "</position>"
      + "</location>";
   public static final String LOCATION_UPDATE_DOCUMENT = "<update>"
      + "<position>"
      + "<longitude>103.0</longitude>"
      + "<latitude>-34.44</latitude>"
      + "<accuracy>32.50</accuracy>"
      + "<street></street>"
      + "<neighborhood></neighborhood>"
      + "<city></city>"
      + "<state></state>"
      + "<postal></postal>"
      + "<countryCode></countryCode>"
      + "</position>"
      + "</update>";
   public static final String LOCATION_UPDATE_ERROR_DOCUMENT = "<update>"
      + "<updateError>"
      + "<code>301</code>"
      + "<message>Ambiguous Location</message>"
			+ "<suggestion>Suggestion 1</suggestion>"
			+ "<suggestion>Suggestion 2</suggestion>"
      + "</updateError>"
      + "</update>";
   public static final String LOCATION_UPDATE_MALFORMED_DOCUMENT = "<update>"
      + "</update>";
}
