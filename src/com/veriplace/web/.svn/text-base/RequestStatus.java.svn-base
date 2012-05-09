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
 * Enumerated type representing the stages of a user/location request.
 */
public enum RequestStatus {

   /**
    * Indicates that we are still processing the request locally.
    */
   Starting(false),
   
   /**
    * Indicates that the request has been completed.
    */
   Completed(false, "completed"),

   /**
    * Indicates that we need to redirect to an external Veriplace URL to complete the request.
    */
   RequiresRedirect(false),

   /**
    * Indicates that we should display a "please wait" page with an automatic refresh.
    */
   Waiting(false, "wait"),

   /**
    * Indicates a general error condition.  If a more specific error occurs and there is no view
    * defined for that error type, use the view for this one.
    */
   Error(true, "error"),
   
   /**
    * Indicates that the user discovery process failed or was cancelled. 
    */
   UserDiscoveryError(true, "error.user"),

   /**
    * Indicates that the location request was cancelled or that we do not have permission to get the
    * location.
    */
   LocationError(true, "error.location"),

   /**
    * Indicates that the set location request was cancelled or that we do not have permission to
    * set the location.
    */
   SetLocationError(true, "error.setlocation");
   
   
   private boolean isError;
   private String alias;
   
   private RequestStatus(boolean isError) {
      this.isError = isError;
   }
   
   private RequestStatus(boolean isError, String alias) {
      this.isError = isError;
      this.alias = alias;
   }
   
   /**
    * Returns true if this is an error state.
    */
   public boolean isError() {
      return isError;
   }
   
   /**
    * Returns the name to use when mapping a view name to this request status.
    */
   public String getAlias() {
      return alias;
   }
   
   /**
    * Returns the RequestStatus corresponding to an alias name.
    */
   public static RequestStatus fromAlias(String alias) {
      for (RequestStatus rs : values()) {
         if (alias.equals(rs.getAlias())) {
            return rs;
         }
      }
      throw new IllegalArgumentException("unknown RequestStatus alias");
   }
}
