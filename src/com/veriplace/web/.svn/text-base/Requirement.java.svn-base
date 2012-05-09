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

/**
 * Interface for a logical requirement that may require multiple request cycles to complete.
 * It operates on an HttpServletRequest, and stores the state of the requirement as attributes
 * in the request.
 */
interface Requirement {

   /**
    * Attempts to complete the requirement.  If successful, it returns true; if not, it
    * returns false and we should check its properties to see where we should go next.
    * Whether successful or not, it will update the HttpServletRequest's attributes to
    * reflect the state of the requirement.
    */
   public boolean complete(VeriplaceState veriplaceState);
 
   /**
    * Discards any information obtained so far for this requirement, so it will start over
    * the next time complete() is called.
    */
   public void reset(VeriplaceState veriplaceState);
   
   /**
    * Gets the names of the request attributes that are used to store this requirement's state.
    */
   public String[] getAttributeNames();
}
