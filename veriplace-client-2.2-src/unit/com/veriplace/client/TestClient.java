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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.veriplace.client.factory.CallbackFactory;
import com.veriplace.client.factory.DefaultCallbackFactory;
import com.veriplace.client.store.FileTokenStore;
import com.veriplace.client.store.MemoryTokenStore;
import com.veriplace.client.store.TokenStore;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Revision;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Unit tests for {@link com.veriplace.client.Client}.
 */
public class TestClient extends TestBase {

   @Test
   public void testConstructorBasic() throws Exception {
      // Basic constructor sets consumer key & secret,
      // uses defaults for everything else
      client = new Client(CONSUMER_KEY, CONSUMER_SECRET);
      assertNotNull(client.getConsumer());
      assertEquals(CONSUMER_KEY, client.getConsumer().getConsumerKey());
      assertEquals(CONSUMER_SECRET, client.getConsumer().getConsumerSecret());
      
      // usual defaults
      assertEquals(Revision.Core1_0RevA, client.getConsumer().getRevision());
      assertEquals(ClientConfiguration.DEFAULT_SERVER_URI, client.getServerUri());
      assertNull(client.getApplicationToken());
      assertFalse(client.hasApplicationToken());
      assertEquals(FileTokenStore.class, client.getRequestTokenStore().getClass());

      CallbackFactory cf = client.getCallbackFactory();
      assertTrue(cf instanceof DefaultCallbackFactory);
      DefaultCallbackFactory dcf = (DefaultCallbackFactory) cf;
      assertNull(dcf.getOverrideServerName());
      assertNull(dcf.getOverrideServerPort());
      assertNull(dcf.getOverridePath());
      assertNull(dcf.getIncludeParameters());
      assertNull(dcf.getExcludeParameters());
   }

   @Test
   public void testConstructorWithInvalidBaseUrl() throws Exception {
      // Constructor throws ConfigurationException if base URL is malformed
      String badBaseUrl = "what is this?:/no";
      ClientConfiguration config = new ClientConfiguration(CONSUMER_KEY, CONSUMER_SECRET);
      config.setServerUri(badBaseUrl);
      try {
         client = new Client(config);
         fail("Expected ConfigurationException");
      }
      catch (ConfigurationException e) {
      }
   }

   @Test
   public void testConstructorWithEverything() throws Exception {
      // constructor from ClientConfiguration copies all available properties
      String altBaseUrl = "https://ecalpirev.com";
      TokenStore altStore = new MemoryTokenStore();
      boolean useHttps = false;
      String defaultMode = "bozo";
      CallbackFactory myCallbackFactory = new DefaultCallbackFactory();
      LocationFilter myFilter = new DefaultLocationFilter(false);
      ClientConfiguration config = new ClientConfiguration(CONSUMER_KEY, CONSUMER_SECRET,
            Revision.Core1_0, APP_TOKEN, altBaseUrl, useHttps, myCallbackFactory,
            altStore, defaultMode, myFilter);
      client = new Client(config);
      assertNotNull(client.getConsumer());
      assertEquals(CONSUMER_KEY, client.getConsumer().getConsumerKey());
      assertEquals(CONSUMER_SECRET, client.getConsumer().getConsumerSecret());
      assertEquals(Revision.Core1_0, client.getConsumer().getRevision());
      assertNotNull(client.getApplicationToken());
      assertEquals(APP_TOKEN.getToken(), client.getApplicationToken().getToken());
      assertEquals(APP_TOKEN.getTokenSecret(), client.getApplicationToken().getTokenSecret());
      assertEquals(altBaseUrl, client.getServerUri());
      assertEquals(useHttps, client.isSecure());
      assertSame(myCallbackFactory, client.getCallbackFactory());
      assertSame(altStore, client.getRequestTokenStore());
      assertEquals(defaultMode, client.getGetLocationAPI().getDefaultLocationMode());
      assertSame(myFilter, client.getGetLocationAPI().getLocationFilter());      
   }

   @Test
   public void testApisAvailable() throws Exception {
      assertNotNull(client.getUserDiscoveryAPI());
      assertNotNull(client.getGetLocationAPI());
      assertNotNull(client.getSetLocationAPI());
      assertNotNull(client.getPermissionAPI());
   }

   @Test
   public void testIsCallback() throws Exception {
      // Client detects callback requests based on whether there's an oauth token parameter
      String tokenValue = "mytoken";

      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setParameter("irrelevantParameter", "foo");
      assertFalse(client.isCallback(request));
      
      request.setParameter("oauth_token", tokenValue);
      assertTrue(client.isCallback(request));
   }
   
   @Test
   public void testGetRequestToken() throws Exception {
      String tokenValue = "mytoken";

      MockHttpServletRequest request = new MockHttpServletRequest();

			request.setParameter("irrelevantParameter", "foo");
			try {
					client.getRequestToken(request);
					fail("exception expected");
			} catch (UnexpectedException e) {
			}
      
      request.setParameter("oauth_token", tokenValue);
      assertEquals(tokenValue, client.getRequestToken(request));
   }

   @Test
   public void testGetAccessTokenNoToken() throws Exception {
      // getAccessToken fails if there's no oauth token parameter
      MockHttpServletRequest request = new MockHttpServletRequest();
			try {
					client.getAccessToken(request);
					fail("exception expected");
			} catch (UnexpectedException e) {
			}
   }

   @Test
   public void testGetAccessTokenNotFound() throws Exception {
      // getAccessToken fails if there's no oauth token parameter
      String tokenValue = "mytoken";

      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setParameter("oauth_token", tokenValue);
			try {
					client.getAccessToken(request);
					fail("exception expected");
			} catch (UnexpectedException e) {
			}
   }

   @Test
   public void testGetAccessTokenSuccess() throws Exception {
      // getAccessToken submits request token to server and gets access token back
      tokenStore.add(REQUEST_TOKEN);
      
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setParameter("oauth_token", REQUEST_TOKEN.getToken());
      request.setParameter("oauth_verifier", VERIFIER);
      
      mockServer.shouldGrantAccessToken(REQUEST_TOKEN, VERIFIER, ACCESS_TOKEN);
      
      Token accessToken = client.getAccessToken(request);
      assertNotNull(accessToken);
      assertEquals(ACCESS_TOKEN_VALUE, accessToken.getToken());
      assertEquals(ACCESS_TOKEN_SECRET, accessToken.getTokenSecret());
      
      // request token is removed from token store after successful exchange
      assertNull(tokenStore.get(REQUEST_TOKEN.getToken()));
   }

   @Test
   public void testGetAccessTokenFailure() throws Exception {
      // getAccessToken submits request token to server and gets an error
      tokenStore.add(REQUEST_TOKEN);
      
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setParameter("oauth_token", REQUEST_TOKEN.getToken());
      request.setParameter("oauth_verifier", VERIFIER);

      mockServer.shouldRefuseAccessToken(REQUEST_TOKEN, VERIFIER);

      try {
					client.getAccessToken(request);
					fail("exception expected");
			} catch (UnexpectedException e) {
			}

      // request token is not removed from token store after failed exchange
      assertNotNull(tokenStore.get(REQUEST_TOKEN.getToken()));
   }
   
   @Test
   public void testGetBaseUrlWithHttps() throws Exception {
      String insecureUrl = "http://foo.com";
      String secureUrl = "https://foo.com";

      ClientConfiguration config = new ClientConfiguration(CONSUMER_KEY, CONSUMER_SECRET);
      config.setServerUri(insecureUrl);
      config.setSecure(false);
      client = new Client(config);
      assertEquals(insecureUrl, client.getServerDirectUri());
      
      config.setSecure(true);
      client = new Client(config);
      assertEquals(secureUrl, client.getServerDirectUri());
   }

   @Test
   public void testGetUserDiscoveryRequestToken() throws Exception {
      String callback = "http://foo.com/bar";
      
      mockServer.shouldGrantRequestToken(REQUEST_TOKEN)
            .setExpectedParameter("veriplace_long_timeout", "1")
            .setExpectedParameter("oauth_callback", callback);
      
      String result = client.getUserDiscoveryRequestToken(callback);
      assertEquals(REQUEST_TOKEN_VALUE, result);
   }
   
   @Test
   public void testGetUserDiscoveryUrl() throws Exception {
      String url = client.getUserDiscoveryUrl(REQUEST_TOKEN_VALUE);
      String expectedUrl = client.getServerUri() + "/api/user?oauth_token=" + REQUEST_TOKEN_VALUE;
      assertEquals(expectedUrl, url);
   }
   
   @Test
   public void testGetApplicationInfoUrl() throws Exception {
      String url = client.getApplicationInfoUrl();
      String expectedUrl = client.getServerUri() + "/application?oauth_consumer_key=" + CONSUMER_KEY;
      assertEquals(expectedUrl, url);
   }
}
