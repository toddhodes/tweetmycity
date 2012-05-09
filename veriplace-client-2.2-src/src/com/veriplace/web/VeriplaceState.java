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
package com.veriplace.web;

import com.veriplace.client.GetLocationNotPermittedException;
import com.veriplace.client.Location;
import com.veriplace.client.RequestDeniedException;
import com.veriplace.client.SetLocationParameters;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.UserDiscoveryNotPermittedException;
import com.veriplace.client.UserDiscoveryParameters;
import com.veriplace.client.VeriplaceException;
import com.veriplace.oauth.consumer.Token;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Veriplace user, location, and permission information associated with the current HTTP request.
 * <p>
 * When you start processing a request, call {@link Veriplace#open(HttpServletRequest, HttpServletResponse)}
 * on the Veriplace object to obtain an instance of VeriplaceState.  (The state object
 * is cached, so you can always obtain the same one by passing the same request to open().)
 * Whenever a method such as {@link Veriplace#requireLocation(VeriplaceState)} completes
 * successfully, it updates the properties of the VeriplaceState.
 */
public class VeriplaceState {

   // package-private
   final static String REQUEST_ID_CALLBACK_PARAM = "veriplace_request_id";
   
   // package-private
   final static String USER_ID_CALLBACK_PARAM = "veriplace_user_id";

   // package-private
   final static String REQUEST_STATE_ATTRIBUTE = "veriplace_request_state";

   private final Veriplace veriplace;
   private final HttpServletRequest request;
   private final HttpServletResponse response;
   private User user = null;
   private Location location = null;
   private String locationMode = null;
   private boolean callback = false;
   private VeriplaceException lastErrorException = null;
   private Token getLocationPermissionToken = null;
   private Token setLocationPermissionToken = null;
   private Token accessToken = null;
   private boolean userInteractionAllowed = true;
   private boolean asynchronousRequestAllowed = true;
   private Map<String, String[]> callbackParameters = new HashMap<String, String[]>();
   private Long requestId = null;
   
   public VeriplaceState(Veriplace veriplace,
                         HttpServletRequest request,
                         HttpServletResponse response) {
      this.veriplace = veriplace;
      this.request = request;
      this.response = response;
   }

   /**
    * Returns a VeriplaceState instance if one has previously been attached to the given
    * HTTP request using {@link Veriplace#open(HttpServletRequest, HttpServletResponse)},
    * or null if there is none.
    */
   public static VeriplaceState getFromRequest(HttpServletRequest request) {
      return (VeriplaceState) request.getAttribute(REQUEST_STATE_ATTRIBUTE);
   }
   
   // package-private
   void attachToRequest(HttpServletRequest request) {
      request.setAttribute(REQUEST_STATE_ATTRIBUTE, this);
   }
   
   /**
    * Returns the current {@link Veriplace} instance.
    */
   public Veriplace getVeriplace() {
      return veriplace;
   }
   
   /**
    * Returns the current {@link javax.servlet.http.HttpServletRequest}. 
    */
   public HttpServletRequest getRequest() {
      return request;
   }

   /**
    * Returns the current {@link javax.servlet.http.HttpServletResponse}. 
    */
   public HttpServletResponse getResponse() {
      return response;
   }
   
   /**
    * Returns the {@link com.veriplace.client.User} object representing the current Veriplace
    * user, or null if the user has not yet been determined.  To discover the current user,
    * call {@link Veriplace#requireUser(VeriplaceState)}.
    */
   public User getUser() {
      return user;
   }

   /**
    * Sets the current Veriplace user.  You may wish to do this directly if you had previously
    * obtained a user with {@link Veriplace#requireUser(VeriplaceState)} and stored the user
    * object for future reference.
    * @see #setUser(long)
    */
   public void setUser(User user) {
      if (((user == null) && (this.user != null))
            || ((user != null) && (this.user == null))
            || ((user != null) && (this.user != null) && (user.getId() != this.user.getId()))) {
         this.user = user;
         // Reset the state in case we've already been doing requests for a different user.
         getLocationPermissionToken = null;
         setLocationPermissionToken = null;
      }
   }

   /**
    * Sets the current Veriplace user.  You may wish to do this directly if you had previously
    * obtained a user with {@link Veriplace#requireUser(VeriplaceState)} and stored the user
    * object for future reference.
    * @see #setUser(User)
    */
   public void setUser(long userId) {
      setUser(new User(userId));
   }
   
   /**
    * Returns the {@link com.veriplace.client.Location} object describing the last acquired
    * location of the current Veriplace user, or null if you have not acquired a location.
    * To acquire a location, call {@link Veriplace#requireLocation(VeriplaceState)}.
    * <p>
    * If you have used {@link Veriplace#setUserLocation(VeriplaceState, com.veriplace.client.SetLocationParameters)}
    * to update the user's location, getLocation will return the updated location details. 
    */
   public Location getLocation() {
      return location;
   }

   void setLocation(Location location) {
      this.location = location;
   }
   
   /**
    * See {@link #setLocationMode(String)}.
    */
   public String getLocationMode() {
      return locationMode;
   }
   
   /**
    * Sets the location mode to be used for subsequent location requests on this state.
    * See {@link com.veriplace.client.LocationMode} for allowable values.
    */
   public void setLocationMode(String locationMode) {
      this.locationMode = locationMode;
   }

   /**
    * See {@link #setUserInteractionAllowed(boolean)}.
    */
   public boolean isUserInteractionAllowed() {
      return userInteractionAllowed;
   }
   
   /**
    * Specifies whether the Veriplace server is allowed to prompt the end user to log in
    * or to grant location permission.  By default, it is.  If you set this value to false,
    * then user discovery or location requests may still redirect silently to the Veriplace
    * server (because OAuth authentication requires this), but if the server does not already
    * know the user's identity or if the user has not already granted permission, the request
    * will immediately fail.
    */
   public void setUserInteractionAllowed(boolean userInteractionAllowed) {
      this.userInteractionAllowed = userInteractionAllowed;
   }

   /**
    * See {@link #setAsynchronousRequestAllowed(boolean)}.
    */
   public boolean isAsynchronousRequestAllowed() {
      return asynchronousRequestAllowed;
   }

   /**
    * Specifies whether the Veriplace client can perform location requests asynchronously
    * using a background thread.  By default, it will; this requires that the application
    * be able to handle a {@link WaitingException} by displaying a progress page.  Setting
    * this flag to false causes the client to perform all requests on the main thread; it
    * will never throw a WaitingException in that case.
    */
   public void setAsynchronousRequestAllowed(boolean asynchronousRequestAllowed) {
      this.asynchronousRequestAllowed = asynchronousRequestAllowed;
   }

   /**
    * Returns true if you have obtained permission to get a user's location.
    */
   public boolean hasGetLocationPermission() {
      return (getLocationPermissionToken != null);
   }
   
   /**
    * Returns true if you have obtained permission to change a user's location.
    */
   public boolean hasSetLocationPermission() {
      return (setLocationPermissionToken != null);
   }

   /**
    * Returns true if this request originated in a callback from the Veriplace website.
    */
   public boolean isCallback() {
      return callback;
   }
   
   // package-private
   void setCallback(boolean callback) {
      this.callback = callback;
   }

   // package-private
   Token getAccessToken() {
      return accessToken;
   }
   
   // package-private
   void setAccessToken(Token token) {
      this.accessToken = token;
   }
  
   /**
    * Returns true if the last request caused an error.  Call {@link #getLastErrorException()}
    * to get the details of the error.
    */
   public boolean isError() {
      return (lastErrorException != null);
   }
   
   /**
    * Returns the last {@link com.veriplace.client.VeriplaceException} that occurred for
    * the current request, or null if the last request was successful.  Note that a
    * {@link WaitingException} will not cause this property to be set, since it is not
    * really an error.
    */
   public VeriplaceException getLastErrorException() {
      return lastErrorException;
   }
   
   // package-private
   void setLastErrorException(VeriplaceException lastErrorException) {
      this.lastErrorException = lastErrorException;
   }
   
   /**
    * Returns the names and values of all HTTP query string or form parameters that will be
    * preserved across a redirect and callback.
    */
   public Map<String, String[]> getCallbackParameters() {
      return callbackParameters;
   }

   /**
    * Specifies an additional HTTP parameter to be preserved across redirects and callbacks.
    */
   public void setCallbackParameter(String name, String value) {
      if (value == null) {
         callbackParameters.remove(name);
      }
      else {
         callbackParameters.put(name, new String[] { value });
      }
   }
   
   /**
    * Specifies an additional HTTP parameter to be preserved across redirects and callbacks.
    */
   public void setCallbackParameter(String name, Long value) {
      setCallbackParameter(name, (String) ((value == null) ? null : value.toString()));
   }
   
   /**
    * Specifies an additional HTTP parameter to be preserved across redirects and callbacks.
    */
   public void setCallbackParameter(String name, String[] values) {
      if (values == null) {
         callbackParameters.remove(name);
      }
      else {
         callbackParameters.put(name, values);
      }
   }
   
   // package-private
   void addCallbackParameters(Map<String, String[]> map) {
      callbackParameters.putAll(map);
   }
   
   /**
    * Returns the current permission token for getting location, if any.
    */
   public Token getGetLocationPermissionToken() {
      return getLocationPermissionToken;
   }
   
   /**
    * Specifies an access token for getting location.  Use this only if you're
    * reusing a stored access token that you know to be valid; otherwise just call
    * {@link Veriplace#requireGetLocationPermission(VeriplaceState)}.
    */
   public void setGetLocationPermissionToken(Token token) {
      this.getLocationPermissionToken = token;
   }
   
   /**
    * Returns the current permission token for changing location, if any.
    */
   public Token getSetLocationPermissionToken() {
      return setLocationPermissionToken;
   }
   
   /**
    * Specifies an access token for changing the user's location.  Use this only if
    * you're reusing a stored access token that you know to be valid; otherwise just call
    * {@link Veriplace#requireGetLocationPermission(VeriplaceState)}.
    */
   public void setSetLocationPermissionToken(Token token) {
      this.setLocationPermissionToken = token;
   }
   
   // package-private
   Long getRequestId() {
      return requestId;
   }
   
   // package-private
   void setRequestId(Long requestId) {
      this.requestId = requestId;
   }
 
   /**
    * Creates HTML hidden form fields for the current user identity.  Using JSP expression
    * syntax, if the attribute name "state" refers to the VeriplaceState, you can simply
    * write <tt>${state.userFields}</tt> to include these fields in a form. 
    */         
   public String getUserFields() {
      if (getUser() == null) {
         return "";
      }
      StringBuilder buf = new StringBuilder();
      addHiddenField(buf, USER_ID_CALLBACK_PARAM, String.valueOf(getUser().getId()));
      return buf.toString();
   }
   
   /**
    * Creates HTML hidden form fields for all currently defined persistent attributes.
    * Using JSP expression syntax, if the attribute name "state" refers to the VeriplaceState,
    * you can simply write <tt>${state.allFields}</tt> to include these fields in a form. 
    */         
   public String getAllFields() {
      StringBuilder buf = new StringBuilder();
      for (Map.Entry<String, String[]> entry: callbackParameters.entrySet()) {
         String name = entry.getKey();
         for (String value: entry.getValue()) {
            addHiddenField(buf, name, value);
         }
      }
      return buf.toString();
   }

   private void addHiddenField(StringBuilder buf, String name, String value) {
      if (value != null) {
         buf.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
      }
   }

   /**
    * Attempts to acquire the current Veriplace user identifier, if it isn't already present
    * in the current state.  After this method returns, call {@link #getUser()}
    * on the VeriplaceState to get the Veriplace {@link com.veriplace.client.User} object.
    * <p>
    * If the user has already been acquired, or set explicitly with
    * {@link #setUser(com.veriplace.client.User)}, the method returns immediately.  Otherwise, it
    * redirects to the Veriplace server to begin a user
    * discovery transaction.  If the server can detect the current user without interaction,
    * it redirects immediately back to your application; otherwise it prompts the user to log
    * in (unless you have disabled this with {@link #setUserInteractionAllowed(boolean)})
    * and redirects back once they have done so.
    * <p>
    * When your application receives the callback, it will be to the same URL and with the same
    * HTTP parameters as the initial request, but with an additional token to indicate that the
    * user is now available.  You do not need to check for this token; just call requireUser()
    * as you did before, and this time the method will return normally.
    * 
    * @throws RespondedException  if the client has sent a redirect response; you should simply
    *   stop handling the request and return in this case
    * @throws UserDiscoveryException  if the current user could not be determined 
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void requireUser()
      throws RespondedException,
             UserDiscoveryException,
             UnexpectedException,
             ServletException {
      veriplace.requireUserInternal(this);
   }

   /**
    * Attempts to acquire the current Veriplace user identifier using identifying information in
    * {@link com.veriplace.client.UserDiscoveryParameters}.  In order to do this, you must
    * have specified an application token and token secret in your Veriplace client configuration
    * (see {@link com.veriplace.client.factory.ClientFactory}).
    * <p>
    * If the user has already been acquired, or set explicitly with
    * {@link #setUser(com.veriplace.client.User)}, the method returns immediately.  Otherwise, it
    * submits a request to the Veriplace server which will either succeed or immediately fail;
    * there are no redirects.  If the method returns with no exceptions, call {@link #getUser()}
    * to get the user object.
    * 
    * @param parameters  the identifying properties of the desired user
    * 
    * @throws RespondedException  can happen only if you've configured an automatic error response
    *   page; return immediately in this case
    * @throws UserDiscoveryException  if the user could not be found or your application lacks privileges 
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void requireUser(UserDiscoveryParameters parameters)
      throws RespondedException,
             UserDiscoveryException,
             UnexpectedException,
             ServletException {
      veriplace.requireUserInternal(this, parameters);
   }

   /**
    * Attempts to acquire the current Veriplace user identifier, if it isn't already present
    * in the current state, without providing any identifying properties and without redirecting.
    * This will only work if you have just received a callback from a successful user discovery
    * transaction.
    * <p>
    * If the user has already been acquired, or set explicitly with
    * {@link #setUser(com.veriplace.client.User)}, the method returns immediately.  Otherwise, it
    * submits a request to the Veriplace server which will either succeed or immediately fail; there
    * are no redirects.
    * <p>
    * If the method returns with no exceptions, call {@link #getUser()} to get the user object.
    * 
    * @throws UserDiscoveryNotPermittedException  if you do not have a valid user discovery access token 
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void requireUserImmediate()
      throws UserDiscoveryNotPermittedException,
          UnexpectedException,
             ServletException {
      veriplace.requireUserImmediateInternal(this);
   }

   /**
    * Attempts to obtain permission to locate the current Veriplace user, if that permission isn't
    * already present in the current state.  If you haven't already identified the user using
    * {@link #requireUser()} or {@link #setUser(com.veriplace.client.User)}, then this calls
    * {@link #requireUser()} first.
    * <p>
    * If this method returns with no exceptions, permission is now available.  You can call
    * {@link #getGetLocationPermissionToken()} to get the permisison token, if you need
    * to save it for future reuse. 
    * <p>
    * Either the user discovery step or the permission step may cause a redirect to the Veriplace
    * site.  When Veriplace redirects back to your application, it will be to the same URL and with
    * the same HTTP parameters as the initial request, but with an additional token to indicate that
    * the permission is now available.  You do not need to check for this token; just call
    * requireGetLocationPermission(state) as you did before, and this time the method will return normally.
    * <p>
    * The Veriplace instance maintains a token cache containing the last known valid permission token for
    * each user, which it will try to reuse if you call {@link #requireLocation()}.  However,
    * it will <i>not</i> use this cache if you call requireGetLocationPermission separately from
    * requireLocation, because the token might have expired and there would be no way to know this
    * if you didn't go on to actually request a location.  Therefore, requireGetLocationPermission
    * always asks the Veriplace server for a current permission.   
    * 
    *  @throws RespondedException  if the client has sent a redirect response; you should simply
    *   stop handling the request and return in this case
    * @throws RequestDeniedException  if either the user discovery request or the permission request was
    *   denied (check the subclass of the exception to see which one) 
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void requireGetLocationPermission()
      throws RespondedException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      veriplace.requireGetLocationPermissionInternal(this);
   }

   /**
    * Attempts to obtain permission to locate the current Veriplace user, if that permission isn't
    * already present in the current state.  The user must already have been identified with
    * {@link #requireUser()} or {@link #setUser(com.veriplace.client.User)}.
    * This method will not perform any redirects and will not ask the user for permission if it wasn't
    * previously granted; it will simply fail in that case.  If this method returns with no exceptions,
    * permission is now available.  You can call {@link #getGetLocationPermissionToken()}
    * to get the permission token, if you need to save it for future reuse. 
    * <p>
    * The Veriplace instance maintains a token cache containing the last known valid permission token for
    * each user, which it will try to reuse if you call {@link #requireLocation()}.  However,
    * requireGetLocationPermissionImmediate does not use this cache; it always asks the Veriplace server
    * for a current permission.
    *   
    * @throws GetLocationNotPermittedException  if the permission request was denied
    * @throws UserDiscoveryException  if you did not provide a valid user
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void requireGetLocationPermissionImmediate()
      throws GetLocationNotPermittedException,
             UserDiscoveryException,
             UnexpectedException {
      veriplace.requireGetLocationPermissionImmediateInternal(this);
   }

   /**
    * Attempts to locate the current Veriplace user, if a location isn't already present in the
    * current state.  If you haven't already identified the user or obtained location permission,
    * then this calls {@link #requireUser()} and/or {@link #requireGetLocationPermission()}
    * first in the correct order.
    * <p>
    * If this method returns with no exceptions, call {@link VeriplaceState#getLocation()} on the
    * VeriplaceState to get the location data.
    * <p>
    * Either the user discovery step or the permission step may cause a redirect to the Veriplace
    * site.  When Veriplace redirects back to your application, it will be to the same URL and with
    * the same HTTP parameters as the initial request, but with an additional token to indicate that
    * the location is now available.  You do not need to check for this token; just call
    * requireLocation(state) as you did before, and this time the method will return normally.
    * <p>
    * Since it can take a noticeable amount of time to acquire location (if a new GPS fix is required),
    * by default all location requests are done asynchronously on a separate thread.  This allows you
    * to give the user some feedback in the meantime, such as a "please wait" page.  Veriplace handles
    * this by throwing a {@link WaitingException}, which contains a callback URL for continuing the
    * current request; if you display a page that automatically redirects to that URL after a few
    * seconds, your application will pick up where it left off, so that the next call to
    * requireLocation(state) will either acquire the result or throw another WaitingException.
    * (This process can be automated by designating a Waiting page ahead of time; see
    * {@link com.veriplace.web.views.StatusViewRenderer}.  If you use that approach, you will
    * never get a WaitingException.) 
    * 
    * @throws RespondedException  if the client has sent a redirect response; you should simply
    *   stop handling the request and return in this case
    * @throws RequestDeniedException  if either the user discovery request or the permission request was
    *   denied (check the subclass of the exception to see which one)
    * @throws WaitingException  if an asynchronous request is in progress and you should display
    *   an intermediate status page 
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void requireLocation()
      throws RespondedException,
             WaitingException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      veriplace.requireLocationInternal(this);
   }

   /**
    * Attempts to retrieve a previously retrieved location again, using its ID (from
    * {@link com.veriplace.client.Location#getId()}).  If you haven't already identified the user
    * or obtained location permission, then this calls {@link #requireUser()}
    * and/or {@link #requireGetLocationPermission()} first in the correct order,
    * which may involve redirects.  However, the location request itself happens synchronously
    * as a single server call.
    * <p>
    * If this method returns with no exceptions, call {@link #getLocation()} to get the location data.
    * 
    * @throws RespondedException  if the client has sent a redirect response; you should simply
    *   stop handling the request and return in this case
    * @throws RequestDeniedException  if either the user discovery request or the permission request was
    *   denied (check the subclass of the exception to see which one)
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void requireLocation(long locationId)
      throws RespondedException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      veriplace.requireLocationInternal(this, locationId);
   }

   /**
    * Attempts to obtain permission to change the current Veriplace user's location, if that
    * permission isn't already present in the current state.  If you haven't already identified the
    * user using {@link #requireUser()} or {@link #setUser(com.veriplace.client.User)},
    * then this calls {@link #requireUser()} first.
    * <p>
    * Either the user discovery step or the permission step may cause a redirect to the Veriplace
    * site.  When Veriplace redirects back to your application, it will be to the same URL and with
    * the same HTTP parameters as the initial request, but with an additional token to indicate that
    * the permission is now available.  You do not need to check for this token; just call
    * requireSetLocationPermission(state) as you did before, and this time the method will return normally.
    * 
    * @throws RespondedException  if the client has sent a redirect response; you should simply
    *   stop handling the request and return in this case
    * @throws RequestDeniedException  if either the user discovery request or the permission request was
    *   denied (check the subclass of the exception to see which one) 
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void requireSetLocationPermission()
      throws RespondedException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      veriplace.requireSetLocationPermissionInternal(this);
   }

   /**
    * Attempts to change the current Veriplace user's location.  If you haven't already identified
    * the user or obtained location update permission, then this calls {@link #requireUser()}
    * and/or {@link #requireSetLocationPermission()} first in the correct order.
    * <p>
    * Either the user discovery step or the permission step may cause a redirect to the Veriplace
    * site.  When Veriplace redirects back to your application, it will be to the same URL and with
    * the same HTTP parameters as the initial request, but with an additional token to indicate that
    * the location is now available.  You do not need to check for this token; just call
    * setUserLocation() as you did before, and this time the method will return normally.
    * <p>
    * Since it can take several seconds to update location (due to the address geocoding), by default
    * this request is handled asynchronously on a separate thread.  This allows you to give the user
    * some feedback in the meantime, such as a "please wait" page.  Veriplace handles
    * this by throwing a {@link WaitingException}, which contains a callback URL for continuing the
    * current request; if you display a page that automatically redirects to that URL after a few
    * seconds, your application will pick up where it left off, so that the next call to
    * setUserLocation(state) will either acquire the result or throw another WaitingException.
    * (This process can be automated by designating a Waiting page ahead of time; see
    * {@link com.veriplace.web.views.StatusViewRenderer}.  If you use that approach, you will
    * never get a WaitingException.)
    * <p>
    * After a successful location update, you can call {@link #getLocation()} to get the
    * new location details (i.e. the longitude and latitude if you provided an address string, or vice
    * versa).
    * 
    * @throws RespondedException  if the client has sent a redirect response; you should simply
    *   stop handling the request and return in this case
    * @throws RequestDeniedException  if either the user discovery request or the permission request was
    *   denied (check the subclass of the exception to see which one)
    * @throws WaitingException  if an asynchronous request is in progress and you should display
    *   an intermediate status page 
    * @throws UnexpectedException  if there was an unexpected I/O error or OAUth error from the server
    */
   public void setUserLocation(SetLocationParameters parameters)
      throws RespondedException,
             WaitingException,
             RequestDeniedException,
             UnexpectedException,
             ServletException {
      veriplace.setUserLocationInternal(this, parameters);
   }
   
   /**
    * Shortcut for getting a named string parameter from the current HTTP request.
    */
   // package-private
   String getRequestParamString(String name) {
      return request.getParameter(name);
   }
   
   /**
    * Helper method for getting a named numeric parameter from the current HTTP request.
    */
   // package-private
   Long getRequestParamLong(String name) {
      String s = request.getParameter(name);
      if (s != null) {
         try {
            long n = Long.parseLong(s);
            return new Long(n);
         }
         catch (NumberFormatException e) {
         }
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
