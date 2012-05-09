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
 * Default implementation of {@link LocationFilter}.  Can be configured to pass
 * location results and errors unchanged, or to suppress errors as long as an
 * earlier cached location is available.
 * @since 2.1
 */
public class DefaultLocationFilter implements LocationFilter {
   
   private boolean useCachedLocation;

   /**
    * Constructs a DefaultLocationFilter that does no post-processing.
    */
   public DefaultLocationFilter() {
      this(false);
   }
   
   /**
    * Constructs a DefaultLocationFilter that can optionally suppress certain
    * errors if the application would like to use cached locations.  This applies
    * only under the following conditions:
    * <ul>
    * <li> useCachedLocation is true </li>
    * <li> your location request throws a {@link PositionFailureException} </li>
    * <li> the server returned a last known location along with the error </li>
    * </ul>
    * <p>
    * If all of those conditions are true, then your request will return the
    * last known location instead of throwing an exception.
    * @param useCachedLocation  true if the filter should return cached locations
    *   in certain cases; false to retain the default unfiltered behavior
    */
   public DefaultLocationFilter(boolean useCachedLocation) {
      this.useCachedLocation = useCachedLocation;
   }
   
   public Location filterLocation(Location returnedLocation,
         PositionFailureException returnedException)
         throws GetLocationException {
      
      if (returnedException != null) {
         if (useCachedLocation) {
            if (returnedException.getCachedLocation() != null) {
               return returnedException.getCachedLocation();
            }
         }
      }
      // Returning null tells the client to return the original value if there
      // was one, or to throw the original exception.
      return null;
   }
 
   /**
    * Returns true if this filter will returned cached locations.
    */
   public boolean isUseCachedLocation() {
      return useCachedLocation;
   }
}
