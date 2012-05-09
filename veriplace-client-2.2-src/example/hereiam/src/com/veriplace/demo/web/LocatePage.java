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
import com.veriplace.client.GetLocationNotPermittedException;
import com.veriplace.client.Location;
import com.veriplace.client.LocationMode;
import com.veriplace.client.PositionFailureException;
import com.veriplace.client.User;
import com.veriplace.client.VeriplaceException;
import com.veriplace.demo.model.DemoUser;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.web.RespondedException;
import com.veriplace.web.VeriplaceState;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Controller for performing a location request once the user has been
 * identified.  If the user has exceeded the trial period, or if permission
 * wasn't granted, show the appropriate warning page.  Otherwise, acquire a
 * location, store it in the current session, and redirect to the page that
 * displays the location.
 * <p>
 * This class makes up to four Veriplace API requests, always trying the
 * fastest methods first:
 * <ol> 
 * <li> If a permission token was previously obtained for this user, check
 *   whether the permission is still valid. </li>
 * <li> If permission had not been obtained yet, or if it was revoked,
 *   ask the user for permission interactively. </li>
 * <li> Attempt to get a location using {@link LocationMode#FREEDOM Freedom}
 *   mode, which is quick and free of charge if Veriplace already has a
 *   recent location for the user; this is likely to work for smartphones. </li>
 * <li> If no such location was available, make a {@link LocationMode#ZOOM Zoom}
 *   mode request, which contacts the phone to obtain a new location,
 *   incurring a charge to the application's account if successful (although
 *   you can use simulated phone numbers for testing free of charge). </li>
 * </ol>
 * <p>
 * Steps 1 and 3 are done in one step as synchronous web service calls.
 * Step 2 may require user interaction, by redirecting to the Veriplace site;
 * step 4 may take some time, so it displays an automatically refreshing
 * progress page until the request is complete. However, the Veriplace
 * <a href="http://developer.veriplace.com/docs/java/api/com/veriplace/web/package-summary.html">Web
 * Tier</a> methods hide the details of the latter steps, redirecting if needed
 * and preserving the previous HTTP parameters, so that when the servlet is
 * called again after the redirect or refresh, it will proceed up to the same
 * API call, receive the result and continue.
 */
public class LocatePage extends BaseServlet {

   private static final Log logger = LogFactory.getLog(LocatePage.class);
   private static final String SKIP_FREEDOM_REQUEST_PARAM = "notCached";
   
   private String mapPageUrl;
   private String maxUsageView;
   private String noLocationView;
   private String noPermissionView;
   private String errorView;

   /**
    * Overridden to get configuration parameters from <tt>web.xml</tt>.
    * <ul>
    * <li> <tt>mapPageUrl</tt>: the relative URL to redirect to once we
    *   have a location </li>
    * <li> <tt>maxUsageView</tt>: name of the JSP to show if the user has
    *   exceeded the limit </li>
    * <li> <tt>noLocationView</tt>: name of the JSP to show if no location
    *   is available </li>
    * <li> <tt>noPermissionView</tt>: name of the JSP to show if the user
    *   did not grant permission or revoked it </li>
    * <li> <tt>errorView</tt>: name of the JSP to show in case of an
    *   unexpected error </li>
    */
   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      mapPageUrl = config.getInitParameter("mapPageUrl");
      maxUsageView = config.getInitParameter("maxUsageView");
      noLocationView = config.getInitParameter("noLocationView");
      noPermissionView = config.getInitParameter("noPermissionView");
      errorView = config.getInitParameter("errorView");
   }

   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {

      // Get the user identifier that we previously stored in the HTTP session.
      final User user = (User) getFromSession(request,
            VERIPLACE_USER_SESSION_KEY);
      
      VeriplaceState vs = application.getVeriplace().open(request, response);
      vs.setUser(user);
      
      DemoUser demoUser = application.getDemoUserManager().getOrCreateDemoUser(user);
      
      // Have they already reached the maximum usage?
      if (application.getRemainingLocates(demoUser) <= 0) {
         logger.info("User " + demoUser.getVeriplaceUser().getId()
               + " reached usage limit");
         showPage(request, response, maxUsageView);
         return;
      }
      
      // Try using the stored permission token, if any.
      Token accessToken = demoUser.getAccessToken();
      if (accessToken != null) {
         try {
            // Ask Veriplace whether this token is still good.  This is a
            // synchronous web service call with no redirect.
            if (vs.getVeriplace().getClient().getPermissionAPI().verify(accessToken)) {
               // Put the access token in the VeriplaceState so it will be used
               // by the location request below.
               vs.setGetLocationPermissionToken(accessToken);
            }
            else {
               logger.debug("Stored access token for user "
                     + demoUser.getVeriplaceUser().getId() + " is no longer valid");
               // The user may have revoked permission for this application.
               accessToken = null;
            }
         }
         catch (VeriplaceException e) {
            logger.warn("Unexpected error in verifying token: " + e);
            logger.debug(e, e);
         }
      }
      
      if (accessToken == null) {
         // Attempt to get permission interactively.  This involves a redirect
         // to the Veriplace site.  When Veriplace redirects back to us, we
         // will go through the same code above, but this time when we call
         // requireGetLocationPermission() it will detect the new access token
         // we've received and will not redirect again.
         try {
            vs.requireGetLocationPermission();
         }
         catch (GetLocationNotPermittedException e) {
            // The user did not grant permission.
            showPage(request, response, noPermissionView);
            return;
         }
         catch (RespondedException e) {
            // The Veriplace client has triggered a redirect.
            return;
         }
         catch (VeriplaceException e) {
            // Catch-all for unexpected Veriplace protocol errors.
            logger.warn("Unexpected error in permission request: " + e);
            logger.debug(e, e);
            showPage(request, response, errorView);
            return;
         }
         // If requireGetLocationPermission() returned without an exception,
         // then we've been granted permission.  The VeriplaceState object
         // now contains a permission token, so we can make a location
         // request.  We could also have just made the location request to
         // start with, and it would perform the permission request implicitly;
         // but doing this part first gives us a chance to save the access
         // token.
         logger.debug("Storing access token for user "
               + demoUser.getVeriplaceUser().getId());
         demoUser.setAccessToken(vs.getGetLocationPermissionToken());
         application.getDemoUserManager().save(demoUser);
      }
      
      // Try getting location with Freedom mode first.  This should either
      // succeed or fail immediately, so we don't want to display a "please
      // wait" page.
      Location location = null;
      boolean isNewLocation = true;
      // See below for the purpose of SKIP_FREEDOM_REQUEST_PARAM.
      if (request.getParameter(SKIP_FREEDOM_REQUEST_PARAM) == null) {
         try {
            // Turning off asychronousRequestAllowed prevents Veriplace from
            // displaying the "please wait" page at this point.
            vs.setAsynchronousRequestAllowed(false);
            vs.setLocationMode(LocationMode.FREEDOM);
            vs.requireLocation();
            
            // If requireLocation() returned without an exception, then we
            // now have a location.
            location = vs.getLocation();
         }
         catch (PositionFailureException e) {
            // We weren't able to get a location that way.  Fall through to do
            // a zoom mode request.  However, we need to remember that we've
            // already done the first request, in case the page gets refreshed
            // (as will normally happen, due to the "please wait" display) and
            // we execute the code above again.  A simple way to do this is to
            // set an HTTP parameter to be passed back to us in that case.
            vs.setCallbackParameter(SKIP_FREEDOM_REQUEST_PARAM, "1");
         }
         catch (VeriplaceException e) {
            // Catch-all for unexpected Veriplace client errors.
            logger.warn("Unexpected error in first location request: " + e);
            logger.debug(e, e);
            showPage(request, response, errorView);
            return;
         }
      }
      if (location == null) {
         // Try a Zoom mode request.  Now we'll turn the asynchronous mode back
         // on, meaning that Veriplace will display the auto-refreshing "wait"
         // page until the request has finished.  This is important for any
         // application that is accessed through WAP browsers on non-smartphones,
         // because such browsers generally don't behave well if a page takes too
         // long to load, and don't support other approaches such as AJAX.
         try {
            vs.setAsynchronousRequestAllowed(true);
            vs.setLocationMode(LocationMode.ZOOM);
            vs.requireLocation();
            
            // If requireLocation() returned without an exception, then we
            // now have a location.
            location = vs.getLocation();
         }
         catch (RespondedException e) {
            // The Veriplace client sent the "wait" page to the user, so stop
            // handling this request for now; we'll get another call when the
            // page refreshes.  Meanwhile, the location request is occurring on
            // a background thread.
            return;
         }
         catch (PositionFailureException e) {
            // The location request failed.  The phone may be off, or may not
            // have good reception.  However, Veriplace may still have sent us
            // the user's last known location; if so, we can display it, but we
            // won't count it toward the user's trial period limit.
            if (e.getCachedLocation() != null) {
               location = e.getCachedLocation();
               isNewLocation = false;
            }
            else {
               showPage(request, response, noLocationView);
               return;
            }
         }
         catch (VeriplaceException e) {
            // Catch-all for unexpected Veriplace client errors.
            logger.warn("Unexpected error in location request: " + e);
            showPage(request, response, errorView);
            return;
         }
      }

      // Update the user object:  increment location count (if it's a new
      // location), and store the time if this was the first location request.
      if ((demoUser.getLocateCount() == 0) || isNewLocation) {
         demoUser.setLocateCount(demoUser.getLocateCount() + 1);
         if (demoUser.getLocateCount() == 1) {
            demoUser.setFirstLocateTime(new Date());
         }
      }
      application.getDemoUserManager().save(demoUser);
      
      // Stash the demo user and location in the web session, where the
      // MapPage controller will find them.
      putInSession(request, DEMO_USER_SESSION_KEY, demoUser);
      putInSession(request, LOCATION_SESSION_KEY, location);
      
      // Redirect to the result page.  We use a redirect here instead of
      // calling showPage(), so that if the user refreshes the browser it will
      // not cause a new location request.
      response.sendRedirect(mapPageUrl);
   }
}
