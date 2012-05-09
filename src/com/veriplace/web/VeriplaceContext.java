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
package com.veriplace.web;

import com.veriplace.client.Client;
import com.veriplace.client.User;
import com.veriplace.client.factory.ClientFactory;
import com.veriplace.client.store.MemoryUserTokenStore;
import com.veriplace.client.store.UserTokenStore;
import com.veriplace.client.util.GetLocationRequestManager;
import com.veriplace.client.util.SetLocationRequestManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides a simplified mechanism for web applications to issue user/location queries through
 * the Veriplace Client.  This object has the following:
 * <ul>
 * <li> a {@link com.veriplace.client.Client}, which can be initialized automatically from a
 * properties file 
 * <li> a cache for Oauth access tokens
 * <li> a list of outstanding location requests
 * <li> the ability to create a {@link com.veriplace.web.VeriplaceState} for a request.
 * </ul>  
 */
public class VeriplaceContext {

   private static final Log logger = LogFactory.getLog(VeriplaceContext.class);
   
   private Client client;
   private Properties properties;
   private StatusHandler statusHandler;
   private GetLocationRequestManager getLocationRequestManager;
   private SetLocationRequestManager setLocationRequestManager;
   private UserTokenStore locationTokenStore;
   private UserTokenStore setLocationTokenStore;

   protected VeriplaceContext() {
      locationTokenStore = new MemoryUserTokenStore();
      setLocationTokenStore = new MemoryUserTokenStore();
      getLocationRequestManager = new GetLocationRequestManager();
      setLocationRequestManager = new SetLocationRequestManager();
   }

   /**
    * Creates a VeriplaceContext using an existing {@link com.veriplace.client.Client}.
    */
   public VeriplaceContext(Client client) {
      this();
      setClient(client);
   }

   /**
    * Creates a VeriplaceContext and initializes its {@link com.veriplace.client.Client}
    * using a Properties object, with the keys defined by {@link com.veriplace.client.factory.ClientFactory}.
    */
   public VeriplaceContext(Properties properties)
         throws MalformedURLException, NoSuchAlgorithmException {
      this();
      this.properties = properties;
      setClient(new ClientFactory().getClient(properties));
   }

   /**
    * Creates a VeriplaceContext and initializes its {@link com.veriplace.client.Client}
    * using properties file, with the keys defined by {@link com.veriplace.client.factory.ClientFactory}.
    */
   public VeriplaceContext(String propertiesFileName)
         throws IOException, MalformedURLException, NoSuchAlgorithmException {
      this();
      properties = new Properties();
      FileInputStream fis = new FileInputStream(propertiesFileName);
      properties.load(fis);
      setClient(new ClientFactory().getClient(properties));
   }

   private void setClient(Client client) {
      if (client == null) {
         throw new IllegalArgumentException();
      }
      this.client = client;
   }
   
   public Client getClient() {
      return client;
   }
   
   public Properties getProperties() {
      return properties;
   }

   /**
    * See {@link #setStatusHandler(StatusHandler)}.
    */
   public StatusHandler getStatusHandler() {
      return statusHandler;
   }
   
   /**
    * Designates a {@link com.veriplace.web.StatusHandler} object which will be consulted after
    * every step in a user/location request, to perform redirects or display special pages if necessary.
    * This should normally be an instance of {@link com.veriplace.web.servlet.ServletStatusHandler},
    * except for Spring applications which should use {@link com.veriplace.web.spring.SpringStatusHandler}. 
    * If you do not have a StatusHandler, you can still make a request but you will have to examine
    * the VeriplaceState and {@link com.veriplace.web.RequestStatus} properties afterward and perform
    * any necessary requests yourself. 
    */
   public void setStatusHandler(StatusHandler statusHandler) {
      this.statusHandler = statusHandler;
   }
   
   /**
    * See {@link #setGetLocationRequestManager(GetLocationRequestManager)}.
    */
   public GetLocationRequestManager getGetLocationRequestManager() {
      return getLocationRequestManager;
   }
   
   /**
    * Designates a {@link com.veriplace.client.util.GetLocationRequestManager} object to provide background
    * processing of location requests. By default, it will use an implementation based on
    * {@link com.veriplace.client.util.MemoryRequestStore}.
    */
   public void setGetLocationRequestManager(GetLocationRequestManager lrm) {
      getLocationRequestManager = lrm;
   }
   
   /**
    * See {@link #setSetLocationRequestManager(SetLocationRequestManager)}.
    */
   public SetLocationRequestManager getSetLocationRequestManager() {
      return setLocationRequestManager;
   }
   
   /**
    * Designates a {@link com.veriplace.client.util.SetLocationRequestManager} object to provide background
    * processing of set-location requests. By default, it will use an implementation based on
    * {@link com.veriplace.client.util.MemoryRequestStore}.
    */
   public void setSetLocationRequestManager(SetLocationRequestManager lrm) {
      setLocationRequestManager = lrm;
   }

   /**
    * See {@link #setGetLocationTokenStore(UserTokenStore)}.
    */
   public UserTokenStore getGetLocationTokenStore() {
      return locationTokenStore;
   }
   
   /**
    * See {@link #setSetLocationTokenStore(UserTokenStore)}.
    */
   public UserTokenStore getSetLocationTokenStore() {
      return setLocationTokenStore;
   }
   
   /**
    * Designates a {@link com.veriplace.client.store.UserTokenStore} object to provide cacheing of
    * Veriplace access tokens. By default, it will use a {@link com.veriplace.client.store.MemoryUserTokenStore}.
    */
   public void setGetLocationTokenStore(UserTokenStore store) {
      locationTokenStore = store;
   }

   /**
    * Designates a {@link com.veriplace.client.store.UserTokenStore} object to provide cacheing of
    * Veriplace access tokens for set-location requests. By default, it will use a
    * {@link com.veriplace.client.store.MemoryUserTokenStore}.
    */
   public void setSetLocationTokenStore(UserTokenStore store) {
      setLocationTokenStore = store;
   }

   /**
    * Creates a {@link com.veriplace.web.VeriplaceState} for the given servlet request;
    * or if this has already been done for that request, returns the previously created instance.
    */
   public VeriplaceState useRequest(HttpServletRequest request, HttpServletResponse response) {
      return useRequest(request, response, null);
   }

   /**
    * Creates a {@link com.veriplace.web.VeriplaceState} for the given servlet request;
    * or if this has already been done for that request, returns the previously created instance.
    * You may optionally specify a different {@link Client} to use for this request.
    */
   public VeriplaceState useRequest(HttpServletRequest request, HttpServletResponse response, Client client) {
      if (client == null) {
         client = this.client;
      }
      VeriplaceState state = VeriplaceState.getFromRequest(request);
      if (state == null) {
         logger.debug("creating state, client = " + client.getConsumer().getConsumerKey() + "/" + client.getConsumer().getConsumerSecret());
         state = new VeriplaceState(this, client, request, response);
         state.attachToRequest(request);
      }
      return state;
   }
}
