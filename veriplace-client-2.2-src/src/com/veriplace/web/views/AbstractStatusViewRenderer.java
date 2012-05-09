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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic implementation of {@link com.veriplace.web.views.StatusViewRenderer} which defines a view name for
 * each error condition and for the "please wait" page, and automatically stores the VeriplaceState
 * object and callback URL in request attributes.  How to display a page is left up to subclasses.
 * @since 2.0
 */
public abstract class AbstractStatusViewRenderer implements StatusViewRenderer {

   private static final Log logger = LogFactory.getLog(AbstractStatusViewRenderer.class);
   
   public static final String WAITING_KEY = "waiting";
   public static final String DEFAULT_ERROR_KEY = "error";
   public static final String ERROR_KEY_PREFIX = "error.";
   
   private static final String[] exceptionPackages = {
      "com.veriplace.client",
      "com.veriplace.web",
      "com.veriplace.web.views"
   };
   
   private String waitingViewName = null;
   private String defaultErrorViewName = null;
   private Map<Class<?>, String> errorViewMap;

   protected String callbackAttributeName = "veriplace_callback";
   protected String stateAttributeName = "veriplace";

   public AbstractStatusViewRenderer() {
      errorViewMap = new HashMap<Class<?>, String>();
   }
   
   /**
    * See {@link #setViewMap(Map)}.
    */
   public Map<String, String> getViewMap() {
      Map<String, String> map = new HashMap<String, String>();
      if (waitingViewName != null) {
         map.put(WAITING_KEY, waitingViewName);
      }
      if (defaultErrorViewName != null) {
         map.put(DEFAULT_ERROR_KEY, defaultErrorViewName);
      }
      for (Map.Entry<Class<?>, String> entry: errorViewMap.entrySet()) {
         String exceptionName = entry.getKey().getSimpleName();
         if (exceptionName.endsWith("Exception")) {
            exceptionName = exceptionName.substring(0, exceptionName.length() - 5);
         }
         map.put(ERROR_KEY_PREFIX + exceptionName, entry.getValue());
      }
      return map;
   }
   
   /**
    * Specifies the views to display for various conditions, using a list of name-value pairs.
    * The string keys used to identify the views are as follows:
    * <ul>
    * <li> For the "please wait" page, the key is "waiting". </li>
    * <li> For specific error conditions, the key is "error." plus the name of the corresponding exception class,
    * minus the "Exception" suffix.  For instance, if the key is "error.UserDiscovery", the value represents the
    * name of the view to use for a UserDiscoveryException.  As with {@link #setErrorViewName(Class, String)}, you
    * can specify either a general exception base class or a very specific exception. </li>
    * <li> The key "error" is used for all errors not otherwise specified, equivalent to calling
    * {@link #setDefaultErrorViewName(String)}. </li>
    * <li> Keys are case-sensitive. </li>
    * </ul>  
    * @throws IllegalArgumentException  if the key of an entry does not match one of the rules described above
    */
   public void setViewMap(Map<String, String> viewMap) {
      errorViewMap.clear();
      for (Map.Entry<String, String> entry : viewMap.entrySet()) {
         String name = entry.getKey();
         String viewName = entry.getValue();
         if (name.equalsIgnoreCase(WAITING_KEY)) {
            waitingViewName = viewName;
         }
         else if (name.equalsIgnoreCase(DEFAULT_ERROR_KEY)) {
            defaultErrorViewName = viewName;
         }
         else if (name.startsWith(ERROR_KEY_PREFIX)) {
            name = name.substring(ERROR_KEY_PREFIX.length());
            Class<?> exceptionClass = matchExceptionClass(name);
            if (exceptionClass == null) {
               throw new IllegalArgumentException("Invalid viewMap key: " + name);
            }
            errorViewMap.put(exceptionClass, viewName);
         }
         else {
            throw new IllegalArgumentException("Invalid viewMap key: " + viewName);
         }
      }
   }

   protected Class<?> matchExceptionClass(String name) {
      name = name + "Exception";
      for (String packageName : exceptionPackages) {
         try {
            return Class.forName(packageName + "." + name);
         }
         catch (ClassNotFoundException e) {
         }
      }
      return null;
   }
   
   /**
    * See {@link #setWaitingViewName(String)}.
    */
   public String getWaitingViewName() {
      return waitingViewName;
   }

   /**
    * Specifies the name of the view to use for the "please wait" condition.  This view should
    * contain an automatic refresh to whatever URL is specified in
    * {@link StatusViewRenderer#renderWaitingView(HttpServletRequest, HttpServletResponse, VeriplaceState, String)}.
    * See {@link com.veriplace.web.WaitingException}.
    */
   public void setWaitingViewName(String waitingViewName) {
      this.waitingViewName = waitingViewName;
   }

   /**
    * See {@link #setDefaultErrorViewName(String)}.
    */
   public String getDefaultErrorViewName() {
      return defaultErrorViewName;
   }

   /**
    * Specifies the name of the view to use for error conditions that were not otherwise specified
    * with {@link #setErrorViewName(Class, String)}.
    */
   public void setDefaultErrorViewName(String defaultErrorViewName) {
      this.defaultErrorViewName = defaultErrorViewName;
   }

   /**
    * Specifies the name of the view to use for error conditions that are thrown as the specified
    * exception class.
    * <p>
    * This includes any exception subclasses derived from that class, if they are
    * not covered by a more specific setErrorViewName rule.  For instance, given the
    * following configuration--
    * <pre>
    *    statusViewRenderer.setErrorViewName(UserDiscoveryException.class, "userFailed");
    *    statusViewRenderer.setErrorViewName(UserDiscoveryNotPermittedException.class, "badPermission");
    * </pre>
    * <p>
    * --a UserDiscoveryNotPermittedException will be handled with the view "badPermission", but
    * a UserNotFoundException (which is derived from UserDiscoveryException) will be handled
    * with the view "userFailed".  
    */
   public void setErrorViewName(Class<?> exceptionClass, String viewName) {
      errorViewMap.put(exceptionClass, viewName);
   }
   
   /**
    * Returns the name of the error view to use for a particular exception class, as defined
    * by {@link #setErrorViewName(Class, String)}.
    */
   public String getErrorViewName(Class<?> exceptionClass) {
      for (Class<?> tryClass = exceptionClass; tryClass != null;
            tryClass = tryClass.getSuperclass()) {
         if (errorViewMap.containsKey(tryClass)) {
            return errorViewMap.get(tryClass);
         }
      }
      return defaultErrorViewName;
   }
   
   /**
    * See {@link #setCallbackAttributeName(String)}.
    */
   public String getCallbackAttributeName() {
      return callbackAttributeName;
   }
   
   /**
    * Specifies that when the framework is displaying a "please wait" view, or any view
    * that includes an automatic redirect, the redirect URL should be stored as a request
    * attribute (or model object, if you are using Spring) with the given name.
    * @param callbackAttributeName
    */
   public void setCallbackAttributeName(String callbackAttributeName) {
      this.callbackAttributeName = callbackAttributeName;
   }
   
   /**
    * See {@link #setStateAttributeName(String)}.
    */
   public String getStateAttributeName() {
      return stateAttributeName;
   }
   
   /**
    * Specifies that the framework should always store a reference to the current
    * {@link com.veriplace.web.VeriplaceState} using the given attribute name. It will be
    * stored as an attribute on the current HttpServletRequest, and if you are using the
    * Spring framework, it will also be available as a model object with the same name.
    */
   public void setStateAttributeName(String stateAttributeName) {
      this.stateAttributeName = stateAttributeName;
   }

   public boolean canRenderWaitingView() {
      return (waitingViewName != null);
   }

   public boolean renderWaitingView(HttpServletRequest request,
                                    HttpServletResponse response,
                                    VeriplaceState state,
                                    String callbackUrl)
      throws StatusViewException,
             ServletException {
      
      logger.info("Rendering waiting view");
      if (waitingViewName == null) {
         return false;
      }
      if (stateAttributeName != null) {
         request.setAttribute(stateAttributeName, state);
      }
      if (callbackAttributeName != null) {
         request.setAttribute(callbackAttributeName, callbackUrl);
      }
      return renderViewInternal(request, response, state, waitingViewName);
   }
   
   public boolean renderErrorView(HttpServletRequest request,
                                  HttpServletResponse response,
                                  VeriplaceState state,
                                  Exception exception)
      throws StatusViewException,
             ServletException {

      logger.info("Rendering error view for: " + exception);

      String viewName = getErrorViewName(exception.getClass());
      if (viewName == null) {
         return false;
      }
      
      if (stateAttributeName != null) {
         request.setAttribute(stateAttributeName, state);
      }
      return renderViewInternal(request, response, state, viewName);
   }
   
   /**
    * Override this method to implement displaying a page based on a view name.  Standard implementations
    * of this for generic servlets and Spring are provided by {@link com.veriplace.web.servlet.ServletStatusViewRenderer}
    * and {@link com.veriplace.web.spring.SpringStatusViewRenderer}.
    * <p>
    * 
    * @param request  the current HttpServletRequest
    * @param response  the current HttpServletResponse
    * @param state  the current {@link com.veriplace.web.VeriplaceState}
    * @param viewName  name of the view to display
    * @throws StatusViewException  for any error that prevents the page from being displayed
    */
   protected abstract boolean renderViewInternal(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 VeriplaceState state,
                                                 String viewName)
      throws StatusViewException,
             ServletException;
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
