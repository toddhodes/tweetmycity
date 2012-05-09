/* Copyright 2010 WaveMarket, Inc.
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
package com.veriplace.demo.web;

import com.veriplace.client.ConfigurationException;
import com.veriplace.demo.Application;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Base class for all the servlets in this application. This just provides
 * access to the global {@link Application} object, and a few shortcut methods.
 */
public abstract class BaseServlet extends HttpServlet {

   protected static final String DEMO_USER_SESSION_KEY = "demoUser";
   protected static final String VERIPLACE_USER_SESSION_KEY = "veriplaceUser";
   protected static final String LOCATION_SESSION_KEY = "location";
   
   protected Application application;

   /**
    * Obtains or creates the global {@link Application} object. Subclasses
    * can override this method to get parameters from <tt>web.xml</tt>.
    */
   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      try {
         application = Application.getInstance(getServletContext());
      }
      catch (Exception e) {
         throw new ServletException(e);
      }
   }

   /**
    * Sends an HTML page, using a JSP file as a template. Variables can be
    * inserted into the template by attaching them to the request object
    * with {@link HttpServletRequest#setAttribute(String, Object)} first.
    * @param request  the current HTTP request
    * @param response  the current HTTP response
    * @param viewName  name of a file in <tt>/WEB-INF/jsp/</tt>
    */
   protected void showPage(HttpServletRequest request,
         HttpServletResponse response, String viewName)
         throws ServletException, IOException {

      getServletContext().getRequestDispatcher("/WEB-INF/jsp/" + viewName).
            forward(request, response);
   }
   
   /**
    * Store an object in the current HTTP session. Just a simple way to
    * preserve some state when we redirect to one of our own pages, without
    * exposing these values in URL query strings.
    */
   protected void putInSession(HttpServletRequest request,
         String name, Object value) {
      HttpSession httpSession = request.getSession(true);
      httpSession.setAttribute(name, value);
   }

   /**
    * Retrieve an object from the current HTTP session.
    */
   protected Object getFromSession(HttpServletRequest request, String name) {
      HttpSession httpSession = request.getSession(true);
      return httpSession.getAttribute(name);
   }
}
