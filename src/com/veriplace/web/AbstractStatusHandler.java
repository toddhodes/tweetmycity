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
package com.veriplace.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic implementation of {@link com.veriplace.web.StatusHandler} which defines a view name for
 * each error condition and for the "please wait" page.  How to display a page, or redirect to an
 * external URL, is left up to subclasses.
 */
public abstract class AbstractStatusHandler implements StatusHandler {

   private static final Log logger = LogFactory.getLog(AbstractStatusHandler.class);
   
   private Map<String, String> viewMap;
   
   private String callbackAttributeName = "veriplace_callback";
   private String stateAttributeName = "veriplace";

   public AbstractStatusHandler() {
      viewMap = new HashMap<String, String>();
      setViewNameForStatus(RequestStatus.Waiting, "wait");
      setViewNameForStatus(RequestStatus.Error, "error");
   }
   
   /**
    * See {@link #setViewMap(Map)}.
    */
   public Map<String, String> getViewMap() {
      return viewMap;
   }
   
   /**
    * Specifies which view to display for each {@link com.veriplace.web.RequestStatus}.
    * For each map entry, the key is the name of the RequestStatus (e.g. "Waiting") and
    * the value is the view name.  The view name for {@link com.veriplace.web.RequestStatus#Error}
    * will also be used for any more specific error conditions that do not have a view of their own.
    * @throws IllegalArgumentException  if the key of an entry does not match a RequestStatus name
    */
   public void setViewMap(Map<String, String> viewMap) {
      // Validate the Map to make sure the keys are actually valid RequestStatus names.
      for (Map.Entry<String, String> kv : viewMap.entrySet()) {
         String name = kv.getKey();
         RequestStatus.fromAlias(name); // will throw an exception if there's no such RequestStatus
      }
      this.viewMap = viewMap;
   }
   
   /**
    * See {@link #setViewNameForStatus(RequestStatus, String)}.
    */
   public String getViewNameForStatus(RequestStatus status) {
      String name = viewMap.get(status.getAlias());
      if ((name == null) && (status.isError())) {
         name = viewMap.get(RequestStatus.Error.getAlias());
      }
      return name;
   }
   
   /**
    * Specifies which view to display for a given {@link com.veriplace.web.RequestStatus}.
    * The view name for {@link com.veriplace.web.RequestStatus#Error}
    * will also be used for any more specific error conditions that do not have a view of
    * their own. 
    */
   public void setViewNameForStatus(RequestStatus status, String viewName) {
      viewMap.put(status.getAlias(), viewName);
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

   public boolean handleRequestStatus(RequestStatus status, String redirectUrl, VeriplaceState state,
         HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {

      if (stateAttributeName != null) {
         logger.debug("storing state as: " + stateAttributeName);
         request.setAttribute(stateAttributeName, state);
      }
      if ((status == RequestStatus.Completed) || (status == RequestStatus.Starting)) {
         return false;
      }
      if (status == RequestStatus.RequiresRedirect) {
         logger.debug("Redirecting to external URL: " + redirectUrl);
         redirectToExternalUrl(redirectUrl, request, response);
         return true;
      }
      String viewName = getViewNameForStatus(status);
      Map<String, Object> attributes = new HashMap<String, Object>();
      if (stateAttributeName != null) {
         attributes.put(stateAttributeName, state);
      }
      if (status == RequestStatus.Waiting) {
         attributes.put(callbackAttributeName, redirectUrl);
      }
      logger.debug("Redirecting to view \"" + viewName + "\"");
      if (viewName == null) {
         return false;
      }
      renderView(viewName, state, attributes, request, response);
      return true;
   }

   /**
    * Override this method if you need to implement URL redirects in some special way.
		* <p>
		* The default implementation sends either a 302 or 303 redirect depending on
		* the HTTP protocol version.
    */
   protected void redirectToExternalUrl(String url, 
																				HttpServletRequest request,
																				HttpServletResponse response) throws IOException {
      logger.debug("Redirecting to " + url);

			/* The HTTP 1.0 specification states:
			 *
			 *******************************************************************************************
			 * If the 302 status code is received in response to a request using the POST method, 
			 * the user agent must not automatically redirect the request unless it can be confirmed 
			 * by the user, since this might change the conditions under which the request was issued.
			 *
			 *    Note: When automatically redirecting a POST request after receiving a 302 status code, 
			 *    some existing user agents will erroneously change it into a GET request. 
			 *******************************************************************************************
			 *
			 * In practice, most HTTP 1.0 User Agents handle 302 redirects after a POST as a GET. 
			 * However, the HTTP 1.1 specification added a 303 redirect specification for this scenario 
			 * and some HTTP 1.1 User Agents do not handle 302 redirects "conventionally". 
			 *
			 * So we need to send a different redirect code for 1.1 User Agents.
			 */
			if ("HTTP/1.1".equalsIgnoreCase(request.getProtocol())) {
					response.setStatus(303);
					response.setHeader("Location",response.encodeRedirectURL(url));
			} else {
					response.sendRedirect(response.encodeRedirectURL(url));
			}
   }
   
   /**
    * Override this method to implement displaying a page based on a view name.  Standard implementations
    * of this for generic servlets and Spring are provided by {@link com.veriplace.web.servlet.ServletStatusHandler}
    * and {@link com.veriplace.web.spring.SpringStatusHandler}.
    * @param viewName  name of the view to display
    * @param state  the current {@link com.veriplace.web.VeriplaceState}
    * @param attributes  any named values that Veriplace needs to pass to the view; see
    * {@link #setStateAttributeName(String)}, {@link #setCallbackAttributeName(String)}
    * @param request  the current HttpServletRequest
    * @param response  the current HttpServletResponse
    * @throws IOException
    * @throws ServletException
    */
   protected abstract void renderView(String viewName, VeriplaceState state, Map<String, Object> attributes,
         HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
}
