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
package com.veriplace.web.spring;

import com.veriplace.client.Client;
import com.veriplace.web.RespondedException;
import com.veriplace.web.Veriplace;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.WaitingException;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Base class for Veriplace-defined Spring Interceptors.  Holds a reference to a
 * {@link com.veriplace.web.Veriplace} instance, and uses it to obtain a
 * {@link com.veriplace.web.VeriplaceState} for each request.  Your controller
 * can get the VeriplaceState object by calling
 * {@link com.veriplace.web.VeriplaceState#getFromRequest(HttpServletRequest)}.
 * After the controller executes, the interceptor also adds the VeriplaceState as a
 * model object, using the name defined by the current StatusHandler's
 * {@link com.veriplace.web.views.AbstractStatusViewRenderer#getStateAttributeName()}.
 * <p>
 * Besides being a base class for the other Interceptors, this class can be used
 * by itself if you need to provide a VeriplaceState to a controller, but do not
 * need to do location/user discovery ahead of time.
 */
public class VeriplaceInterceptor 
   extends HandlerInterceptorAdapter
   implements HandlerExceptionResolver {

   private static final Log logger = LogFactory.getLog(VeriplaceInterceptor.class);
   
   protected Veriplace veriplace;
   
   public Veriplace getVeriplace() {
      return veriplace;
   }

   /**
    * Specifies the {@link com.veriplace.web.Veriplace} instance this interceptor will use.
    */
   public void setVeriplace(Veriplace veriplace) {
      this.veriplace = veriplace;
   }

   @Override
   public void postHandle(HttpServletRequest request, 
                          HttpServletResponse response, 
                          Object handler, 
                          ModelAndView mv)
      throws Exception {

      if (mv != null) {
         VeriplaceState state = veriplace.open(request, response);
         SpringStatusViewRenderer vr = (SpringStatusViewRenderer) veriplace.getStatusViewRenderer();
         String attrName = vr.getStateAttributeName();
         if (attrName != null) {
            mv.addObject(attrName, state);
         }
      }
   }

   @Override
   public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler)
         throws Exception {

      logger.debug("Entering " + this.getClass().getName());
      VeriplaceState state = veriplace.open(request, response);
      try {
         return handleInternal(state, request, response);
      }
      catch (RespondedException e) {
         return false;
      }
      catch (WaitingException e) {
         return false;
      }
   }
   
   /**
    * Override this method if you might need to use a different Veriplace {@link Client} for some
    * requests -- for instance, if the same interceptor is handling requests for more than one Veriplace
    * application, each with its own consumer key and token. A Client is a fairly lightweight object, so
    * it's acceptable to create a new one for each request if you want to.
    */
   protected Client getVeriplaceClient(HttpServletRequest request)
      throws ServletException, 
             IOException {
      return null;
   }

   /**
    * Override this method to do any necessary preprocessing of the request based on the corresponding
    * {@link com.veriplace.web.VeriplaceState}.
    * @return  true if the request can be passed on to the regular handler; false if we have already handled
    * the request (e.g. redirected it).
    */
   protected boolean handleInternal(VeriplaceState state, 
                                    HttpServletRequest request, 
                                    HttpServletResponse response)
      throws Exception {
      return true;
   }

   /**
    * Handle {@link RespondedException}s by doing nothing.
    */
   public ModelAndView resolveException(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {

      if (ex instanceof RespondedException) {
         // not an error; flow has been interrupted by a redirect to the
         // Veriplace site or to a status page
         final View NOOP = new View() {

               public String getContentType() {
                  return "";
               }
               public void render(Map model,            
                                  HttpServletRequest request,
                                  HttpServletResponse response)
                  throws Exception {
               }
            };
         return new ModelAndView(NOOP);
      }
      return null;
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
