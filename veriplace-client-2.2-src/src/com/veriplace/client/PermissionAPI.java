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
import com.veriplace.oauth.message.ParameterSet;
import com.veriplace.oauth.message.RequestMethod;
import com.veriplace.oauth.message.Response;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interface for verifying or removing an application's {@link Token OAuth Access Tokens},
 * which represent permissions to locate specific users.  The methods of this API are
 * implemented as individual requests to the Veriplace server, and do not require redirects
 * or user interaction.
 * <p>
 * An application may wish to verify an Access Token if it was previously cached, because
 * users may have changed their privacy settings so that the token is no longer valid
 * and location requests will fail.  In this case the application may choose to restart
 * the OAuth User Authorization process to obtain a new token.
 * <p>
 * An application can also delete the Access Token that it previously used to locate a
 * user; this has the same effect as the user revoking permission for that application
 * through the Veriplace website.
 * @since 2.0
 */
public class PermissionAPI
   extends API {

   private static final Log logger = LogFactory.getLog(PermissionAPI.class);

   public static final String PERMISSION_PATH = "/api/1.0/permission";
   
   public PermissionAPI(Client client) {
      super(client);
   }

   @Override
   public String getRedirectURL(String callback,
                                User user) {
      throw new UnsupportedOperationException("getRedirectURL() is not supported for Permission API");
   }

   @Override
   public String getRedirectURL(String callback,
                                User user,
                                boolean immediate) {
      throw new UnsupportedOperationException("getRedirectURL() is not supported for Permission API");
   }

   /**
    * Verify an access token.
    * @param accessToken the access token permitting the user to be located
    * @return whether the access token is valid for the {@link GetLocationAPI}
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public boolean verify(Token accessToken)
         throws UnexpectedException {

      APIInfo info = APIInfo.get(getURI(null));

      try {
         doPermissionOperation(info, new ParameterSet(), accessToken);
         return true;
      }
      catch (UnknownTokenException e) {
         return false;
      }
   }

   /**
    * Remove an access token, so the application will no longer have permission to locate that user.
    * @param accessToken the access token permitting the user to be located
    * @throws UnknownTokenException  if the token was not valid
    * @throws UnexpectedException  if there was an unexpected I/O error or OAuth error
    */
   public void delete(Token accessToken)
         throws UnknownTokenException, UnexpectedException {

      APIInfo info = APIInfo.post(getURI(null));

      doPermissionOperation(info, new ParameterSet(), accessToken);
   }

   ////////////

   protected void doPermissionOperation(APIInfo info,
                                        ParameterSet parameters,
                                        Token accessToken)
         throws UnknownTokenException, UnexpectedException {

      try {
         Response response = 
               client.getProtectedResource(info,parameters,accessToken);
         // getProtectedResource throws an exception if the response is not OK
      }
      catch (VeriplaceOAuthException e) {
         switch (e.getCode()) {
         case HttpServletResponse.SC_UNAUTHORIZED:  // 401
            throw new UnknownTokenException();
         }
         throw e;
      }
   }

   @Override
   protected String getURI(User user) {
      return client.getServerDirectUri() + 
         PERMISSION_PATH;
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
