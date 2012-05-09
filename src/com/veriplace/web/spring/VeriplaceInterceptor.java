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
package com.veriplace.web.spring;

import com.veriplace.client.Client;
import com.veriplace.web.VeriplaceContext;
import com.veriplace.web.VeriplaceState;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Base class for Veriplace-defined Spring Interceptors.  Holds a reference to a
 * {@link com.veriplace.web.VeriplaceContext}, and uses it to obtain a
 * {@link com.veriplace.web.VeriplaceState} for each request.  Your controller
 * can get the VeriplaceState object by calling
 * {@link com.veriplace.web.VeriplaceState#getFromRequest(ServletRequest)}.
 * After the controller executes, the interceptor also adds the VeriplaceState as a
 * model object, using the name defined by the current StatusHandler's
 * {@link com.veriplace.web.StatusHandler#getStateAttributeName()}.
 * <p>
 * Besides being a base class for the other Interceptors, this class can be used
 * by itself if you need to provide a VeriplaceState to a controller, but do not
 * need to do location/user discovery ahead of time.
 */
public class VeriplaceInterceptor implements HandlerInterceptor {

   private static final Log logger = LogFactory.getLog(VeriplaceInterceptor.class);

   private VeriplaceContext veriplaceContext;
   
   public VeriplaceContext getVeriplaceContext() {
      return veriplaceContext;
   }

   /**
    * Specifies the {@link com.veriplace.web.VeriplaceContext} this interceptor will use.
    */
   public void setVeriplaceContext(VeriplaceContext manager) {
      this.veriplaceContext = manager;
   }
   
   public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e)
         throws Exception {
   }

   public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv)
         throws Exception {
      if (mv != null) {
         VeriplaceState state = VeriplaceState.getFromRequest(request);
         String attrName = state.getContext().getStatusHandler().getStateAttributeName();
         if (attrName != null) {
            mv.addObject(attrName, state);
         }
      }
   }

   public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
         throws Exception {

      logger.debug("Entering " + this.getClass().getName());
      VeriplaceState state = veriplaceContext.useRequest(request, response, getVeriplaceClient(request));
      return handleInternal(state, request, response);
   }
   
   /**
    * Override this method if you might need to use a different Veriplace {@link Client} for some
    * requests -- for instance, if the same interceptor is handling requests for more than one Veriplace
    * application, each with its own consumer key and token. A Client is a fairly lightweight object, so
    * it's acceptable to create a new one for each request if you want to.
    */
   protected Client getVeriplaceClient(HttpServletRequest request)
         throws ServletException, IOException {
      return null;
   }

   /**
    * Override this method to do any necessary preprocessing of the request based on the corresponding
    * {@link com.veriplace.web.VeriplaceState}.
    * @return  true if the request can be passed on to the regular handler; false if we have already handled
    * the request (e.g. redirected it).
    */
   protected boolean handleInternal(VeriplaceState state, HttpServletRequest request, HttpServletResponse response)
         throws Exception {
      return true;
   }
}
