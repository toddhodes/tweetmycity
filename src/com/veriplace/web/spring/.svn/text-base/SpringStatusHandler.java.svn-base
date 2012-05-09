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

import com.veriplace.web.AbstractStatusHandler;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.RequestStatus;
import com.veriplace.web.servlet.ServletStatusHandler;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.support.RequestContext;

/**
 * Implementation of {@link com.veriplace.web.StatusHandler} for the Spring MVC framework.  The
 * difference between this and {@link com.veriplace.web.servlet.ServletStatusHandler} is that
 * this uses Spring's ViewResolver rather than the basic request dispatcher forwarding
 * mechanism, and passes the LocationState attributes to the view as a Spring model rather
 * than just as attributes on the HttpServletRequest.
 * <p>
 * In your application context configuration files, you should define a SpringStatusHandler bean
 * and refer to it in the statusHandler property of your VeriplaceContext.  You can us all
 * the properties that this class inherits from {@link com.veriplace.web.AbstractStatusHandler}
 * to specify the view names for errors or wait conditions.  
 */
public class SpringStatusHandler extends ServletStatusHandler {

   private static final Log logger = LogFactory.getLog(SpringStatusHandler.class);

   private ViewResolver viewResolver;

   public ViewResolver getViewResolver() {
      return viewResolver;
   }

   public void setViewResolver(ViewResolver viewResolver) {
      this.viewResolver = viewResolver;
   }

   protected void renderView(String viewName, VeriplaceState state, Map<String, Object> attributes,
         HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      RequestContext rc = new RequestContext(request);
      try {
         View view = viewResolver.resolveViewName(viewName, rc.getLocale());
         view.render(attributes, request, response);
      }
      catch (Exception e) {
         throw new ServletException(e);
      }
   }
}
