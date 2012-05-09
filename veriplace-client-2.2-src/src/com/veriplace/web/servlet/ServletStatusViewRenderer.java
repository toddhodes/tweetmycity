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
package com.veriplace.web.servlet;

import com.veriplace.web.VeriplaceState;
import com.veriplace.web.views.AbstractStatusViewRenderer;
import com.veriplace.web.views.StatusViewException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Subclass of {@link com.veriplace.web.views.AbstractStatusViewRenderer} for use with
 * servlet applications. Given a view name, this class renders the view by passing the
 * name to the {@link javax.servlet.RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse)}
 * method of the current request dispatcher. It also prepends and appends an optional
 * prefix and suffix to the view name; see {@link #setViewPrefix(String)} and
 * {@link #setViewSuffix(String)}.
 */
public class ServletStatusViewRenderer extends AbstractStatusViewRenderer {

   private static final Log logger = LogFactory.getLog(ServletStatusViewRenderer.class);

   private String viewPrefix = "/WEB-INF/jsp/";
   private String viewSuffix = ".jsp";

	/**
	 * See {@link #setViewPrefix(String)}.
	 */
   public String getViewPrefix() {
      return viewPrefix;
   }

   /**
    * Sets the prefix to prepend to every view name before passing it to the request
    * dispatcher. The default is "/WEB-INF/jsp/".
    */
   public void setViewPrefix(String viewPrefix) {
      this.viewPrefix = viewPrefix;
   }

   /**
    * See {@link #setViewSuffix(String)}.
    */
   public String getViewSuffix() {
      return viewSuffix;
   }

   /**
    * Sets the suffix to append to every view name before passing it to the request
    * dispatcher. The default is ".jsp".
    */
   public void setViewSuffix(String viewSuffix) {
      this.viewSuffix = viewSuffix;
   }

   public boolean renderViewInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     VeriplaceState state,
                                     String viewName)
         throws StatusViewException,
                ServletException {
      
      viewName = viewPrefix + viewName + viewSuffix;

      logger.debug("Forwarding to: " + viewName);
      if (stateAttributeName != null) {
         request.setAttribute(stateAttributeName, state);
      }
      try {
         request.getRequestDispatcher(viewName).forward(request, response);
      }
      catch (IOException e) {
         throw new StatusViewException(e);
      }
      catch (ServletException e) {
         throw e;
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
