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
package com.veriplace.web.views;

import com.veriplace.web.VeriplaceState;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for an object that can display "status views" to the end user under certain
 * conditions.  These include errors (failed location request) and intermediate steps
 * in a request flow (a "please wait" page).  To avoid dependencies on any specific web
 * application framework, Veriplace abstracts this process into an interface.
 * <p>
 * The SDK includes simple implementations of this interface for servlets
 * ({@link com.veriplace.web.servlet.ServletStatusViewRenderer}) and Spring servlets
 * ({@link com.veriplace.web.spring.SpringStatusViewRenderer}).  If you use the
 * {@link com.veriplace.web.servlet.AbstractVeriplaceServlet} class or the JSP tags,
 * a ServletStatusViewRenderer is created for you; see {@link com.veriplace.web.servlet.VeriplaceServletHelper}
 * for how to customize its properties.  Spring applications should define their own
 * SpringStatusViewRenderer bean if desired. 
 * <p>
 * You do not need to use a StatusViewRenderer at all, as long as your code checks for
 * exceptions and returns the appropriate output.  Be sure to handle the {@link com.veriplace.web.WaitingException}
 * if you will be making asynchronous requests.
 * @since 2.0
 */
public interface StatusViewRenderer {

   /**
    * Displays an appropriate error response for a given exception.
    * @throws StatusViewException  if there was an error in rendering the view
    */
   public boolean renderErrorView(HttpServletRequest request,
                                  HttpServletResponse response,
                                  VeriplaceState state,
                                  Exception exception)
         throws StatusViewException,
								ServletException;

   /**
    * Displays appropriate content for the "please wait" condition.
    * @param callbackUrl  the URL to use for an automatic refresh
    * @throws StatusViewException  if there was an error in rendering the view
    */
   public boolean renderWaitingView(HttpServletRequest request,
                                    HttpServletResponse response,
                                    VeriplaceState state,
                                    String callbackUrl)
         throws StatusViewException,
								ServletException;
   
   /**
    * Returns true if this StatusViewRenderer is able to display content for the "please
    * wait" condition.  If it returns false, {@link #renderWaitingView(HttpServletRequest, HttpServletResponse, VeriplaceState, String)}
    * will never be called.
    */
   public boolean canRenderWaitingView();
}
