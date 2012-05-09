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

import com.veriplace.web.RequestStatus;
import com.veriplace.web.VeriplaceContext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides static methods for configuring {@link com.veriplace.web} objects in a servlet environment.
 * These methods look for the following <tt>context-param</tt> elements in your web.xml configuration:
 * <ul>
 * <li> <tt>veriplace.properties-file</tt>: Path to a properties file containing the Veriplace client
 * properties. </li>
 * <li> <tt>veriplaceViewPrefix</tt>: Internal path to the directory for special pages that
 * Veriplace can render ("wait", "error", etc.). </li>
 * of the same name.) </li>
 * <li> <tt>veriplaceViewSuffix</tt>: Internal path to the directory for special pages that
 * Veriplace can render ("wait", "error", etc.). (Optional; default is ".jsp") </li>
 * <li> <tt>veriplaceStateAttributeName</tt>: Attribute name for storing a reference to the VeriplaceState
 * object in the HttpServletRequest. (Optional; default is none) </li>
 * </ul>
 */
public class VeriplaceServletHelper {
   
   private static final Log logger = LogFactory.getLog(VeriplaceServletHelper.class);
   private static final String SERVLET_CONTEXT_ATTRIBUTE = "veriplace_location_context";
   private static final String PROPERTIES_FILE_CONTEXT_PARAM = "veriplace.properties-file";
   private static final String VIEW_PREFIX_CONTEXT_PARAM = "veriplaceViewPrefix";
   private static final String VIEW_SUFFIX_CONTEXT_PARAM = "veriplaceViewSuffix";
   private static final String STATE_ATTRIBUTE_CONTEXT_PARAM = "veriplaceStateAttributeName";

   /**
    * Creates or returns a {@link com.veriplace.web.VeriplaceContext} for the given ServletContext,
    * using the properties file whose name is in the context parameter "veriplace.properties-file".
    * 
    * @param servletContext  the current ServletContext
    * 
    * @return  a VeriplaceContext
    * @throws IOException  if the properties file could not be read
    * @throws MalformedURLException  if the Veriplace client could not be created
    * @throws NoSuchAlgorithmException  if the Veriplace client could not be created
    */
   public static VeriplaceContext getSharedVeriplaceContext(ServletContext servletContext)
         throws IOException, MalformedURLException, NoSuchAlgorithmException {
      return getSharedVeriplaceContext(servletContext, servletContext.getInitParameter(PROPERTIES_FILE_CONTEXT_PARAM));
   }

   /**
    * Creates or returns a {@link com.veriplace.web.VeriplaceContext} for the given ServletContext, which
    * will be shared by all servlets within the same application.
    * 
    * @param servletContext  the current ServletContext
    * @param propertiesFileName  path of the properties file
    * 
    * @return  a VeriplaceContext
    * @throws IOException  if the properties file could not be read
    * @throws MalformedURLException  if the Veriplace client could not be created
    * @throws NoSuchAlgorithmException  if the Veriplace client could not be created
    */
   public static VeriplaceContext getSharedVeriplaceContext(ServletContext servletContext,
         String propertiesFileName)
         throws IOException, MalformedURLException, NoSuchAlgorithmException {
      synchronized (servletContext) {
         if (servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE) == null) {
            if ((propertiesFileName == null) || propertiesFileName.equals("")) {
               throw new IllegalArgumentException("Veriplace properties file path not specified");
            }
            logger.debug("Creating new shared VeriplaceContext");
            VeriplaceContext vc = new VeriplaceContext(propertiesFileName);
            useContext(servletContext, vc);
            return vc;
         }
         else {
            logger.debug("Returning existing shared VeriplaceContext");
            return (VeriplaceContext) servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE);
         }
      }
   }

   /**
    * Creates or returns a {@link com.veriplace.web.VeriplaceContext} for the given ServletContext, which
    * will be shared by all servlets within the same application.
    * 
    * @param servletContext  the current ServletContext
    * @param properties  properties for initializing the Veriplace client
    * 
    * @return  a VeriplaceContext
    * @throws MalformedURLException  if the Veriplace client could not be created
    * @throws NoSuchAlgorithmException  if the Veriplace client could not be created
    */
   public static VeriplaceContext getSharedVeriplaceContext(ServletContext servletContext,
         Properties properties)
         throws MalformedURLException, NoSuchAlgorithmException {
      synchronized (servletContext) {
         if (servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE) == null) {
            logger.debug("Creating new shared VeriplaceContext");
            VeriplaceContext vc = new VeriplaceContext(properties);
            useContext(servletContext, vc);
            return vc;
         }
         else {
            logger.debug("Returning existing shared VeriplaceContext");
            return (VeriplaceContext) servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE);
         }
      }
   }

   /**
    * Creates or returns a {@link com.veriplace.web.VeriplaceContext} for the given servlet.  The object is
    * associated with the ServletContext, so it is shared by all servlets within the same application.
    * This method is mainly useful for code that is executed by a non-extensible servlet class, such as a
    * JSP.
    * 
    * @param servlet  an active servlet
    * @param propertiesFileName  path of the properties file
    * @param viewPrefix  path to any status pages that Veriplace may need to display (a shortcut for
    * initializing this commonly used property of {@link com.veriplace.web.servlet.ServletStatusHandler})
    * 
    * @return  a VeriplaceContext
    * @throws IOException  if the properties file could not be read
    * @throws MalformedURLException  if the Veriplace client could not be created
    * @throws NoSuchAlgorithmException  if the Veriplace client could not be created
    */
   public static VeriplaceContext getSharedVeriplaceContext(Servlet servlet, String propertiesFileName, String viewPrefix)
         throws IOException, MalformedURLException, NoSuchAlgorithmException {
      VeriplaceContext vc = getSharedVeriplaceContext(servlet.getServletConfig().getServletContext(),
            propertiesFileName);
      if ((vc != null) && (viewPrefix != null)) {
         ((ServletStatusHandler) vc.getStatusHandler()).setViewPrefix(viewPrefix);
      }
      return vc;
   }

   /**
    * Returns a {@link com.veriplace.web.VeriplaceContext} for the given servlet, if one has already been
    * created by a previous call to {@link #getSharedVeriplaceContext(Servlet, String, String)}; otherwise
    * returns null.
    */
   public static VeriplaceContext getSharedVeriplaceContext(Servlet servlet)
         throws IOException, MalformedURLException, NoSuchAlgorithmException {
      return getSharedVeriplaceContext(servlet.getServletConfig().getServletContext());
   }
   
   /**
    * Creates a {@link com.veriplace.web.StatusHandler} using a set of servlet context parameters, as follows
    * (where NAME is the string passed to this method):
    * <ul>
    * <li> veriplace.views.NAME.prefix: path/prefix to view names (e.g. "/WEB-INF/jsp/myViews/" </li>
    * <li> veriplace.views.NAME.suffix: view name suffix (e.g. ".jsp") </li>
    * <li> veriplace.views.NAME.wait: name of "wait" view (default is "wait") </li>
    * <li> veriplace.views.NAME.error: name of general error view (default is "error") </li>
    * <li> veriplace.views.NAME.error.user: name of user discovery error view (default is none) </li>
    * <li> veriplace.views.NAME.error.location: name of location discovery error view (default is none) </li>
    * </ul>
    */
   public static ServletStatusHandler getStatusHandlerFromViewParams(ServletContext sc, String paramsName) {
      String base = "veriplace.views." + paramsName + ".";
      ServletStatusHandler sh = new ServletStatusHandler();
      String prefix = sc.getInitParameter(base + "prefix");
      if (prefix != null) {
         sh.setViewPrefix(prefix);
      }
      String suffix = sc.getInitParameter(base + "suffix");
      if (suffix != null) {
         sh.setViewSuffix(suffix);
      }
      for (RequestStatus rs : RequestStatus.values()) {
         if (rs.getAlias() != null) {
            String viewName = sc.getInitParameter(base + rs.getAlias());
            if (viewName != null) {
               sh.setViewNameForStatus(rs, viewName);
            }
         }
      }
      return sh;
   }
   
   protected static void useContext(ServletContext sc, VeriplaceContext vc) {
      ServletStatusHandler sh = new ServletStatusHandler();
      String prefix = sc.getInitParameter(VIEW_PREFIX_CONTEXT_PARAM);
      if (prefix != null) {
         sh.setViewPrefix(prefix);
      }
      String suffix = sc.getInitParameter(VIEW_SUFFIX_CONTEXT_PARAM);
      if (suffix != null) {
         sh.setViewSuffix(suffix);
      }
      String stateAttr = sc.getInitParameter(STATE_ATTRIBUTE_CONTEXT_PARAM);
      if (stateAttr != null) {
         sh.setStateAttributeName(stateAttr);
      }
      vc.setStatusHandler(sh);
      sc.setAttribute(SERVLET_CONTEXT_ATTRIBUTE, vc);
   }
}
