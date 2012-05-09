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

/**
 * Thrown to indicate that a {@link GetLocationAPI} method failed because Veriplace
 * could not obtain the user's location, although your request was valid. 
 * <p>
 * Position failures are classified by a numeric code and may return cached location
 * data from a previous request.
 * @since 2.0
 */
public class PositionFailureException extends GetLocationException {


   /**
    * Error code for location being unavailable, for example if the device
    * is powered off or out of network coverage.
    */
   public static final int POSITION_FAILURE = 100;

   /**
    * Error code for position determination being termporarily unavailable, for
    * example if a carrier's infrastructure is undergoing maintenance.
    */
   public static final int POSITION_DETERMINATION_TEMPORARILY_UNAVAILABLE = 110;

   /**
    * Error code for position data being restricted by a user privacy setting.
    */
   public static final int RESTRICTED = 200;

   private final int code;
   private final Location cachedLocation;
   
   public PositionFailureException(String message, 
                                   int code,
                                   Location cachedLocation) {
      super(message);
      this.code = code;
      this.cachedLocation = cachedLocation;
   }
   
   public int getCode() {
      return code;
   }

   public boolean isPositionFailure() {
      return code == POSITION_FAILURE;
   }
   
   public boolean isPositionDeterminationTemporarilyUnavailable() {
      return code == POSITION_DETERMINATION_TEMPORARILY_UNAVAILABLE;
   }

   public boolean isRestricted() {
      return code == RESTRICTED;
   }

   public Location getCachedLocation() {
      return cachedLocation;
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
