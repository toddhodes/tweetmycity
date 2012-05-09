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

import com.veriplace.client.ConfigurationException;
import com.veriplace.web.Veriplace;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
 * <li> <tt>veriplaceViewSuffix</tt>: Will be added to the view name to get the filename of a special page
 * (Optional; default is ".jsp") </li>
 * <li> <tt>veriplaceStateAttributeName</tt>: Attribute name for storing a reference to the VeriplaceState
 * object in the HttpServletRequest. (Optional; default is none) </li>
 * </ul>
 */
public class VeriplaceServletHelper {
   
   private static final Log logger = LogFactory.getLog(VeriplaceServletHelper.class);
   private static final String SERVLET_CONTEXT_ATTRIBUTE = "veriplace_location_context";
   private static final String SERVLET_PROPERTIES_ATTRIBUTE = "veriplace_properties_context";
   public static final String PROPERTIES_FILE_CONTEXT_PARAM = "veriplace.properties-file";
   public static final String VIEW_PREFIX_CONTEXT_PARAM = "veriplaceViewPrefix";
   public static final String VIEW_SUFFIX_CONTEXT_PARAM = "veriplaceViewSuffix";
   public static final String STATE_ATTRIBUTE_CONTEXT_PARAM = "veriplaceStateAttributeName";

   /**
    * Creates or returns a {@link com.veriplace.web.Veriplace} instance for the given ServletContext,
    * using the properties file whose name is in the context parameter "veriplace.properties-file".
    * 
    * @param servletContext  the current ServletContext
    * 
    * @return  a Veriplace instance
    * @throws IOException  if the properties file could not be read
    * @throws ConfigurationException  if the Veriplace client could not be created
    */
   public static Veriplace getSharedVeriplaceInstance(ServletContext servletContext)
         throws IOException, 
                ConfigurationException {
      return getSharedVeriplaceInstance(servletContext,
                                        servletContext.getInitParameter(PROPERTIES_FILE_CONTEXT_PARAM));
   }

   /**
    * Creates or returns a {@link com.veriplace.web.Veriplace} instance for the given ServletContext, which
    * will be shared by all servlets within the same application.
    * 
    * @param servletContext  the current ServletContext
    * @param propertiesFileName  path of the properties file
    * 
    * @return  a Veriplace instance
    * @throws IOException  if the properties file could not be read
    * @throws ConfigurationException  if the Veriplace client could not be created
    */
   public static Veriplace getSharedVeriplaceInstance(ServletContext servletContext,
                                                      String propertiesFileName)
      throws IOException, 
             ConfigurationException {

      synchronized (servletContext) {
         if (servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE) == null) {
            if ((propertiesFileName == null) || propertiesFileName.equals("")) {
               throw new IllegalArgumentException("Veriplace properties file path not specified");
            }
            Properties p = new Properties();
            p.load(new FileInputStream(propertiesFileName));
            return getSharedVeriplaceInstance(servletContext, p);
         } else {
            logger.debug("Returning existing shared Veriplace instance");
            return (Veriplace) servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE);
         }
      }
   }

   /**
    * Creates or returns a {@link com.veriplace.web.Veriplace} instance for the given ServletContext, which
    * will be shared by all servlets within the same application.
    * 
    * @param servletContext  the current ServletContext
    * @param properties  properties for initializing the Veriplace client
    * 
    * @return  a Veriplace instance
    * @throws ConfigurationException  if the Veriplace client could not be created
    */
   public static Veriplace getSharedVeriplaceInstance(ServletContext servletContext,
                                                      Properties properties)
      throws ConfigurationException {

      synchronized (servletContext) {
         if (servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE) == null) {
            logger.debug("Creating new shared Veriplace instance");
            Veriplace v = new Veriplace(properties);
            useContext(servletContext, v, properties);
            return v;
         } else {
            logger.debug("Returning existing shared Veriplace instance");
            return (Veriplace) servletContext.getAttribute(SERVLET_CONTEXT_ATTRIBUTE);
         }
      }
   }

   /**
    * Creates or returns a {@link com.veriplace.web.Veriplace} instance for the given servlet.  The object is
    * associated with the ServletContext, so it is shared by all servlets within the same application.
    * This method is mainly useful for code that is executed by a non-extensible servlet class, such as a
    * JSP.
    * 
    * @param servlet  an active servlet
    * @param propertiesFileName  path of the properties file
    * @param viewPrefix  path to any status pages that Veriplace may need to display (a shortcut for
    * initializing this commonly used property of {@link com.veriplace.web.servlet.ServletStatusViewRenderer})
    * 
    * @return  a Veriplace instance
    * @throws IOException  if the properties file could not be read
    * @throws ConfigurationException  if the Veriplace client could not be created
    */
   public static Veriplace getSharedVeriplaceInstance(Servlet servlet, 
                                                      String propertiesFileName, 
                                                      String viewPrefix)
      throws IOException, 
             ConfigurationException {

      Veriplace v = getSharedVeriplaceInstance(servlet.getServletConfig().getServletContext(),
                                               propertiesFileName);
      if ((v != null) && (viewPrefix != null)) {
         ((ServletStatusViewRenderer) v.getStatusViewRenderer()).setViewPrefix(viewPrefix);
      }
      return v;
   }

   /**
    * Returns a {@link com.veriplace.web.Veriplace} instance for the given servlet, if one has already been
    * created by a previous call to {@link #getSharedVeriplaceInstance(Servlet, String, String)}; otherwise
    * returns null.
    */
   public static Veriplace getSharedVeriplaceInstance(Servlet servlet)
      throws IOException, 
             ConfigurationException {
      return getSharedVeriplaceInstance(servlet.getServletConfig().getServletContext());
   }
   
   /**
    * Returns the properties that were used to configure the Veriplace instance for the current servlet.
    * @throws ConfigurationException  if the properties file was not specified or could not be read
    */
   public static Properties getSharedVeriplaceProperties(ServletContext servletContext)
         throws ConfigurationException {
      Properties p = (Properties)
            servletContext.getAttribute(SERVLET_PROPERTIES_ATTRIBUTE);
      if (p == null) {
         throw new ConfigurationException("no configuration properties were specified");
      }
      return p;
   }
   
   /**
    * Creates a {@link com.veriplace.web.views.StatusViewRenderer} using a set of servlet context parameters, as follows
    * (where NAME is the string passed to this method):
    * <ul>
    * <li> veriplace.views.NAME.prefix: path/prefix to view names (e.g. "/WEB-INF/jsp/myViews/" </li>
    * <li> veriplace.views.NAME.suffix: view name suffix (e.g. ".jsp") </li>
    * <li> veriplace.views.NAME.waiting: name of "please wait" view (default is "wait") </li>
    * <li> veriplace.views.NAME.error: name of general error view (default is "error") </li>
    * <li> veriplace.views.NAME.error.Something: name of view to use for the exception class "SomethingException" </li>
    * </ul>
    */
   public static ServletStatusViewRenderer getViewRendererFromViewParams(ServletContext sc, 
                                                                         String paramsName) {
      String base = "veriplace.views." + paramsName + ".";
      ServletStatusViewRenderer vr = new ServletStatusViewRenderer();
      Map<String, String> viewMap = new HashMap<String, String>();
      for (Enumeration<?> names = sc.getInitParameterNames(); names.hasMoreElements();) {
         String name = (String) names.nextElement();
         if (name.startsWith(base)) {
            String value = sc.getInitParameter(name);
            name = name.substring(base.length());
            if (name.equals("prefix")) {
               vr.setViewPrefix(value);
            }
            else if (name.equals("suffix")) {
               vr.setViewSuffix(value);
            }
            else {
               viewMap.put(name, value);
            }
         }
      }
      vr.setViewMap(viewMap);
      return vr;
   }
   
   protected static void useContext(ServletContext sc, 
                                    Veriplace v,
                                    Properties properties) {
      ServletStatusViewRenderer vr = new ServletStatusViewRenderer();
      String prefix = sc.getInitParameter(VIEW_PREFIX_CONTEXT_PARAM);
      if (prefix != null) {
         vr.setViewPrefix(prefix);
      }
      String suffix = sc.getInitParameter(VIEW_SUFFIX_CONTEXT_PARAM);
      if (suffix != null) {
         vr.setViewSuffix(suffix);
      }
      String stateAttr = sc.getInitParameter(STATE_ATTRIBUTE_CONTEXT_PARAM);
      if (stateAttr != null) {
         vr.setStateAttributeName(stateAttr);
      }
      v.setStatusViewRenderer(vr);
      sc.setAttribute(SERVLET_CONTEXT_ATTRIBUTE, v);
      sc.setAttribute(SERVLET_PROPERTIES_ATTRIBUTE, properties);
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
