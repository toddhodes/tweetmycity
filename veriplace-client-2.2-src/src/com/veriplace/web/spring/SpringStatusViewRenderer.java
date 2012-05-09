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

import com.veriplace.web.VeriplaceState;
import com.veriplace.web.views.AbstractStatusViewRenderer;
import com.veriplace.web.views.StatusViewException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.support.RequestContext;

/**
 * Implementation of {@link com.veriplace.web.views.StatusViewRenderer} for the Spring MVC framework.  The
 * difference between this and {@link com.veriplace.web.servlet.ServletStatusViewRenderer} is that
 * this uses Spring's ViewResolver rather than the basic request dispatcher forwarding
 * mechanism, and passes the LocationState attributes to the view as a Spring model rather
 * than just as attributes on the HttpServletRequest.
 * <p>
 * In your application context configuration files, you should define a SpringStatusViewRenderer bean
 * and refer to it in the statusViewRenderer property of your {@link com.veriplace.web.Veriplace} instance.
 * You can use all the properties that this class inherits from {@link com.veriplace.web.views.AbstractStatusViewRenderer}
 * to specify the view names for errors or wait conditions.  
 */
public class SpringStatusViewRenderer extends AbstractStatusViewRenderer {

   private ViewResolver viewResolver;

   public ViewResolver getViewResolver() {
      return viewResolver;
   }

   public void setViewResolver(ViewResolver viewResolver) {
      this.viewResolver = viewResolver;
   }

   protected boolean renderViewInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        VeriplaceState state,
                                        String viewName)
      throws StatusViewException {

      RequestContext rc = new RequestContext(request);
      Map<String, Object> attributes = new HashMap<String, Object>();
      if (callbackAttributeName != null) {
         attributes.put(callbackAttributeName, request.getAttribute(callbackAttributeName));
      }
      if (stateAttributeName != null) {
         attributes.put(stateAttributeName, request.getAttribute(stateAttributeName));
      }
      
      try {
         View view = viewResolver.resolveViewName(viewName, rc.getLocale());
         view.render(attributes, request, response);
      }
      catch (Exception e) {
         throw new StatusViewException(e);
      }
      return true;
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
