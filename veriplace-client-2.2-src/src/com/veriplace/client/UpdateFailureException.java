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

import java.util.List;

/**
 * Thrown to indicate that a {@link SetLocationAPI} method failed because Veriplace
 * could not understand the submitted location, although your request was valid. 
 * <p>
 * Update failures are classified by a numeric code and may return alternative
 * suggestions, e.g. if there was ambiguity.
 * @since 2.0
 */
public class UpdateFailureException extends SetLocationException {

   /**
    * Error code for location update data that cannot be validated.
    */
   public static final int INVALID_LOCATION = 300;

   /**
    * Error code for location update data that was ambigious.
    */
   public static final int AMBIGUOUS_LOCATION = 301;

   private final int code;
   private final List<String> suggestions;
   
   public UpdateFailureException(String message, 
                                 int code,
                                 List<String> suggestions) {
      super(message);
      this.code = code;
      this.suggestions = suggestions;
   }
   
   public int getCode() {
      return code;
   }

   public boolean isInvalid() {
      return code == INVALID_LOCATION;
   }

   public boolean isAmbiguous() {
      return code == AMBIGUOUS_LOCATION;
   }
   
   public List<String> getSuggestions() {
      return suggestions;
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
