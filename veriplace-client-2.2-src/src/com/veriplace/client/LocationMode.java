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
 * String constants representing the allowable location modes. See
 * {@link com.veriplace.client.GetLocationAPI#getLocation(com.veriplace.oauth.consumer.Token, User, String)}.
 */
public interface LocationMode {

   /**
    * A high-accuracy request that may have higher latency and cost.
    */
   public static final String ZOOM = "zoom";
   
   /**
    * An approximate location request with lower latency and cost.
    */
   public static final String AREA = "area";
   
   /**
    * A request that queries previously cached locations, and will never trigger a new GPS fix.
    * This location mode is free of charge until you reach a high per-month request volume; after
    * that point, it will fail with a {@link GetLocationBillingDeclinedException} if your
    * developer account does not have a positive balance.
    * <p>
    * For Veriplace-enabled smartphones, an attempt to generate a new cached location occurs at
    * regular intervals.  A new location is also generated if a different application locates the
    * same user using an on-demand mode ({@link #ZOOM} or {@link #AREA}).
    * <p>
    * If you make a FREEDOM mode request when you have already queried the most recent location
    * and there isn't yet a new location, you will get a {@link PositionFailureException}, whose
    * {@link PositionFailureException#getCachedLocation()} method will return the most recent
    * cached location.  To suppress this exception and just get the most recent location at all
    * times, you can configure the {@link Client} with a custom {@link LocationFilter}, or, more
    * simply, set the property {@link com.veriplace.client.factory.DefaultClientFactory#USE_LAST_KNOWN_LOCATION}
    * to "true". 
    */
   public static final String FREEDOM = "freedom";
}
