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
 * Interface for an object that can apply post-processing to returned locations.  If
 * present, {@link GetLocationAPI} passes all new locations returned by the server through
 * this filter.
 * @since 2.1
 */
public interface LocationFilter {

   /**
    * Transforms the result of a location request.  If this method returns null, the filter has
    * no effect.  If it returns a location object, that becomes the return value of the location
    * request, even if the request would normally have failed.  It can also throw a different
    * exception, regardless of whether the request would normally have failed or succeeded.
    * @param returnedLocation  the valid location returned by the server, or null if an error occurred 
    * @param returnedException  the exception that would normally be thrown if an error occurred
    * @return  a new location to return; null to keep the original location, or to throw the original
    *   exception if there was an exception
    * @throws GetLocationException  if the location request should fail
    */
   public Location filterLocation(Location returnedLocation, PositionFailureException returnedException)
         throws GetLocationException;
}
