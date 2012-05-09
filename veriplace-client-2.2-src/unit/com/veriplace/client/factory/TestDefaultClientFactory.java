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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.veriplace.client.ClientConfiguration;
import com.veriplace.client.ConfigurationException;
import com.veriplace.client.DefaultLocationFilter;
import com.veriplace.client.LocationFilter;
import com.veriplace.client.TestData;
import com.veriplace.oauth.message.Revision;

import java.util.Properties;

import org.junit.Test;

/**
 * Unit tests for {@link com.veriplace.client.factory.DefaultClientFactory}.
 */
public class TestDefaultClientFactory implements TestData {

   private static final String ALT_SERVER_URI = "http://verrrriplace.com";
   
   @Test
   public void testConfigurationFromProperties() throws Exception {
      String callbackHost = "foo";
      String callbackPort = "8080";
      String locationMode = "fooMode";

      Properties p = new Properties();
      p.put("veriplace.server.uri", ALT_SERVER_URI);
      p.put("veriplace.server.secure", "false");
      p.put("veriplace.server.protocol", "Core1_0");
      p.put("veriplace.application.consumer.key", CONSUMER_KEY);
      p.put("veriplace.application.consumer.secret", CONSUMER_SECRET);
      p.put("veriplace.application.token.value", APP_TOKEN_VALUE);
      p.put("veriplace.application.token.secret", APP_TOKEN_SECRET);
      p.put("veriplace.application.callback.host", callbackHost);
      p.put("veriplace.application.callback.port", callbackPort);
      p.put("veriplace.application.location.mode", locationMode);
      DefaultClientFactory f = new DefaultClientFactory(p);
      ClientConfiguration cc = f.getClientConfiguration();
      
      assertEquals(ALT_SERVER_URI, cc.getServerUri());
      assertEquals(false, cc.getSecure());
      assertEquals(Revision.Core1_0, cc.getProtocol());
      assertEquals(CONSUMER_KEY, cc.getConsumerKey());
      assertEquals(CONSUMER_SECRET, cc.getConsumerSecret());
      assertNotNull(cc.getApplicationToken());
      assertEquals(APP_TOKEN_VALUE, cc.getApplicationToken().getToken());
      assertEquals(APP_TOKEN_SECRET, cc.getApplicationToken().getTokenSecret());
      assertEquals(locationMode, cc.getDefaultLocationMode());
      assertNull(cc.getLocationFilter());
   }

   @Test
   public void testConfigurationFromPropertiesEmptyAppToken() throws Exception {
      // If applicationToken.* properties are present, but have empty values,
      // don't create an app token.
      
      String callbackHost = "foo";
      String callbackPort = "8080";
      
      Properties p = new Properties();
      p.put("veriplace.server.uri", ALT_SERVER_URI);
      p.put("veriplace.server.secure", "false");
      p.put("veriplace.server.protocol", "Core1_0");
      p.put("veriplace.application.consumer.key", CONSUMER_KEY);
      p.put("veriplace.application.consumer.secret", CONSUMER_SECRET);
      p.put("veriplace.application.token.value", "");
      p.put("veriplace.application.token.secret", "");
      p.put("veriplace.application.callback.host", callbackHost);
      p.put("veriplace.application.callback.port", callbackPort);
      DefaultClientFactory f = new DefaultClientFactory(p);
      ClientConfiguration cc = f.getClientConfiguration();
      
      assertNull(cc.getApplicationToken());

      assertEquals(ALT_SERVER_URI, cc.getServerUri());
      assertEquals(false, cc.getSecure());
      assertEquals(Revision.Core1_0, cc.getProtocol());
      assertEquals(CONSUMER_KEY, cc.getConsumerKey());
      assertEquals(CONSUMER_SECRET, cc.getConsumerSecret());
   }

   @Test
   public void testRequiredProperties() throws Exception {
      Properties p = new Properties();
      p.put("veriplace.application.consumer.key", CONSUMER_KEY);
      p.put("veriplace.application.consumer.secret", CONSUMER_SECRET);
      p.put("veriplace.server.uri", ALT_SERVER_URI);
      ClientConfiguration cc =
            new DefaultClientFactory(p).getClientConfiguration();
      assertNotNull(cc);
      
      Properties p1 = (Properties) p.clone();
      p1.remove("veriplace.application.consumer.key");
      try {
         cc = new DefaultClientFactory(p1).getClientConfiguration();
         fail("Expected ConfigurationException");
      }
      catch (ConfigurationException e) {
      }

      Properties p2 = (Properties) p.clone();
      p2.remove("veriplace.application.consumer.secret");
      try {
         cc = new DefaultClientFactory(p2).getClientConfiguration();
         fail("Expected ConfigurationException");
      }
      catch (ConfigurationException e) {
      }

      Properties p3 = (Properties) p.clone();
      p3.remove("veriplace.server.uri");
      cc = new DefaultClientFactory(p3).getClientConfiguration();
      // This should *not* throw an exception - server URI is not required.
   }
   
   @Test
   public void testDefaultLocationFilter() throws Exception {
      Properties p = new Properties();
      p.put("veriplace.application.consumer.key", CONSUMER_KEY);
      p.put("veriplace.application.consumer.secret", CONSUMER_SECRET);
      p.put("veriplace.application.location.use-last-known", "true");
      
      ClientConfiguration cc =
            new DefaultClientFactory(p).getClientConfiguration();
      LocationFilter lf = cc.getLocationFilter();
      assertNotNull(lf);
      assertEquals(DefaultLocationFilter.class, lf.getClass());
      assertTrue(((DefaultLocationFilter) lf).isUseCachedLocation());

      p.put("veriplace.application.location.use-last-known", "false");

      cc = new DefaultClientFactory(p).getClientConfiguration();
      lf = cc.getLocationFilter();
      assertNotNull(lf);
      assertEquals(DefaultLocationFilter.class, lf.getClass());
      assertFalse(((DefaultLocationFilter) lf).isUseCachedLocation());
   }
   
   @Test
   public void testObsoletePropertyNames() throws Exception {
      // Can use deprecated property names interchangeably with new ones
      
      Properties p = new Properties();
      p.put("veriplace.url", ALT_SERVER_URI);
      p.put("veriplace.https", "false");
      p.put("veriplace.rev_a", "false");
      p.put("consumer.key", CONSUMER_KEY);
      p.put("consumer.secret", CONSUMER_SECRET);
      p.put("applicationToken.value", APP_TOKEN_VALUE);
      p.put("applicationToken.secret", APP_TOKEN_SECRET);
      
      DefaultClientFactory f = new DefaultClientFactory(p);
      ClientConfiguration cc = f.getClientConfiguration();
      
      assertEquals(ALT_SERVER_URI, cc.getServerUri());
      assertEquals(false, cc.getSecure());
      assertEquals(Revision.Core1_0, cc.getProtocol());
      assertEquals(CONSUMER_KEY, cc.getConsumerKey());
      assertEquals(CONSUMER_SECRET, cc.getConsumerSecret());
      assertNotNull(cc.getApplicationToken());
      assertEquals(APP_TOKEN_VALUE, cc.getApplicationToken().getToken());
      assertEquals(APP_TOKEN_SECRET, cc.getApplicationToken().getTokenSecret());
   }
}
