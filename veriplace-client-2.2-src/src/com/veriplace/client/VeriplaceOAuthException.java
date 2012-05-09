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

import com.veriplace.oauth.OAuthException;

import javax.servlet.http.HttpServletResponse;

/**
 * Wrapper for an {@link com.veriplace.oauth.OAuthException} when it is thrown from within
 * a Veriplace client method.
 * @since 2.0
 */
public class VeriplaceOAuthException extends UnexpectedException {

   protected final OAuthException cause;
   
   public VeriplaceOAuthException(OAuthException cause) {
      super(cause.getMessage(), cause);
      this.cause = cause;
   }

   /**
    * Returns the result code of the underlying OAuth exception.
    */
   public int getCode() {
      if (cause.getCode() == null) {
         return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      }
      return cause.getCode();
   }

   @Override
   public OAuthException getCause() {
      return cause;
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

