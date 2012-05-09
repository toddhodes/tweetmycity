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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for an object that can respond to the current state of a user/location request in some
 * way that may include sending a response page.  You should not have to write your own implementation
 * of this unless your application is <i>not</i> using the standard Veriplace redirect/callback mechanism.
 */
public interface StatusHandler {

   /**
    * See {@link #setCallbackAttributeName(String)}.
    */
   public String getCallbackAttributeName();
   
   /**
    * Specifies that when the framework is displaying a "please wait" view, or any view
    * that includes an automatic redirect, the redirect URL should be stored as a request
    * attribute (or model object, if you are using Spring) with the given name.
    * @param callbackAttributeName
    */
   public void setCallbackAttributeName(String callbackAttributeName);
   
   /**
    * See {@link #setStateAttributeName(String)}.
    */
   public String getStateAttributeName();
   
   /**
    * Specifies that the framework should always store a reference to the current
    * {@link com.veriplace.web.VeriplaceState} using the given attribute name. It will be
    * stored as an attribute on the current HttpServletRequest, and if you are using the
    * Spring framework, it will also be available as a model object with the same name.
    */
   public void setStateAttributeName(String stateAttributeName);
   
   /**
    * Handles the condition specified by the state's {@link com.veriplace.web.VeriplaceState#getRequestStatus()} method.
    * 
    * @return true if the handler has sent its own response; false if the caller should still send a response.
    */
   public boolean handleRequestStatus(RequestStatus status, String redirectUrl, VeriplaceState state,
         HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException;
}
