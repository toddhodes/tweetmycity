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

import com.veriplace.client.factory.DocumentFactory;

import com.veriplace.oauth.OAuthException;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.message.Parameter;
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.Request;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.RequestType;
import com.veriplace.oauth.message.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for Veriplace APIs.
 */
public abstract class API {

   private static final Log logger = LogFactory.getLog(API.class);

   /**
    * Parameter to specify infinite client-side timeout for location requests.
    */
   public static final Integer NO_TIMEOUT = null;

   protected final Client client;
   protected final DocumentFactory documentFactory = new DocumentFactory();

   public API(Client client) {
      this.client = client;
   }

   /**
    * Get the URL for User Agent redirection for OAuth user authorization.
    * @param callback the OAuth callback url
    * @param user the user to be located
    * @return the OAuth redirection url 
    */
   public String getRedirectURL(String callback,
                                User user) 
      throws TransportException,
             VeriplaceOAuthException {
      return getRedirectURL(callback,user,false);
   }

   /**
    * Get the URL to use for User redirection to get permission to obtain a User's location.
    * @param callback the OAuth callback url
    * @param user the user to be located
    * @param immediate should responses return immediately if user interaction would be required?
    * @return the OAuth redirection url 
    */
   public String getRedirectURL(String callback,
                                User user,
                                boolean immediate) 
      throws TransportException,
             VeriplaceOAuthException {
      String uri = getURI(user);
      return client.getRedirectURL(callback, immediate, uri);
   }

   /**
    * Get the URI for the resource controlled by this API.
    * <p>
    * This URI is used in the OAuth user authorization redirect to identify
    * what resource needs to be authorized.
    * @param user the user that authorizes this resource, if any
    */
   protected abstract String getURI(User user);

   /**
    * Get an access token representing permission to get some resource, but do not get the resource.
    * @param parameters parameters to be sent in the authorization request
    * @return the access token; will not be null
    * @throws OAuthException  if the server returns an OAuth error
    * @throws TransportException  if there is an I/O exception in communication with the server
    */
   protected Token getAccessToken(APIInfo info, ParameterSet parameters)
         throws VeriplaceOAuthException, TransportException {

      // callback is a placeholder, but should be a valid URL
      final String callback = "http://example.com";

      final Token requestToken = client.getRequestToken(callback);
      if (requestToken == null) {
         throw new IllegalStateException("Unexpected null result from getRequestToken");
      }

      final String authorizationUrl = 
         client.getRedirectURL(requestToken, callback, true, info.getURI());
      if (authorizationUrl == null) {
         throw new IllegalStateException("Unexpected null result from getRedirectURL");
      }

      try {
         URL url = new URL(authorizationUrl);
         Request request = new Request(url, RequestMethod.GET, RequestType.UserAuthorization, parameters);
         Response response = client.getConsumer().getClient().getResponse(request, true);
         int code = response.getCode();
         
         switch (code) {
            // 302 redirect
         case HttpServletResponse.SC_FOUND:
            // 303 redirect
         case HttpServletResponse.SC_SEE_OTHER:
            
            // The User Authorization URL sent a redirect, extract the callback URL
            final String location = response.getHeaderValue("location");

            if (location == null) {
               logger.warn("HTTP redirect unexpectedly did not contain 'Location' header");
               throw new TransportException("Malformed redirect response from server");
            }
            
            String verifier = null;
            // For Rev A, the redirect location (callback) contains the oauth_verifier
            final String[] parts = location.split(Parameter.Verifier.getKey() + "=");
            if (parts.length > 1) {
               verifier = parts[1].split("&")[0];
            } else {
               logger.debug("oauth_verifier not found in callback: " + location);
            }
            
            try {
               // Attempt to get an access token
               Token token = client.getConsumer().getAccessToken(requestToken, verifier);
               if (token == null) {
                  throw new IllegalStateException("Unexpected null result from getAccessToken");
               }
               return token;
            }
            catch (OAuthException e) {
               logger.debug("Access token was not available; permission denied.");
               // An exception here means an Access Token wasn't available
               // Try granting permission directly for your application in the Privacy Manager
               // There should now be a permission request visible in the sidebar
               throw new VeriplaceOAuthException(e);
            }
         default:
            logger.info("Unexpected response status code: " + code + " for authorization url: " + authorizationUrl);
            throw new TransportException("Unexpected response code from server");
         }
      }
      catch (MalformedURLException e) {
         logger.info(e,e);
         throw new TransportException(e);
      }
      catch (IOException e) {
         logger.info(e,e);
         throw new TransportException(e);
      }
   }

   protected UnexpectedException selectUnexpectedException(VeriplaceOAuthException e) {
      switch (e.getCode()) {
         case HttpServletResponse.SC_BAD_REQUEST:  // 400
            return new BadParameterException(e.getMessage());
      }
      return e;
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


