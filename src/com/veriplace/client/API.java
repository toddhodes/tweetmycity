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
package com.veriplace.client;

import com.veriplace.client.factory.DocumentFactory;

import com.veriplace.oauth.message.RequestMethod;

/**
 * Base class for Veriplace APIs.
 */
public abstract class API {

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
                                User user) {
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
                                boolean immediate) {
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
    * Get the request method for this URI, e.g. GET or POST
    */
   protected abstract RequestMethod getRequestMethod();
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


