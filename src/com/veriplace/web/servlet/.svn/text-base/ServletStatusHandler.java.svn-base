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
package com.veriplace.web.servlet;

import com.veriplace.web.AbstractStatusHandler;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.RequestStatus;
import com.veriplace.web.StatusHandler;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of {@link com.veriplace.web.StatusHandler} for a servlet-based application, using
 * the default request dispatching system.
 */
public class ServletStatusHandler extends AbstractStatusHandler {

   private static final Log logger = LogFactory.getLog(ServletStatusHandler.class);

   private String viewPrefix = "/WEB-INF/jsp/";
   private String viewSuffix = ".jsp";

   public String getViewPrefix() {
      return viewPrefix;
   }

   public void setViewPrefix(String viewPrefix) {
      this.viewPrefix = viewPrefix;
   }

   public String getViewSuffix() {
      return viewSuffix;
   }

   public void setViewSuffix(String viewSuffix) {
      this.viewSuffix = viewSuffix;
   }

   protected void renderView(String viewName, VeriplaceState state, Map<String, Object> attributes,
         HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
      if (attributes != null) {
         for (Map.Entry<String, Object> attr : attributes.entrySet()) {
            request.setAttribute(attr.getKey(), attr.getValue());
         }
      }
      viewName = viewPrefix + viewName + viewSuffix;
      request.getRequestDispatcher(viewName).forward(request, response);
   }
}
