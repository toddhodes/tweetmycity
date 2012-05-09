
package org.tweetmycity;

import com.veriplace.client.Location;
import com.veriplace.client.User;
import com.veriplace.client.Client;
import com.veriplace.client.store.TokenStore;
import com.veriplace.client.store.MemoryTokenStore;
import com.veriplace.oauth.consumer.Token;
import com.veriplace.oauth.OAuthException;
import com.veriplace.oauth.message.Revision;

import java.net.URL;
import java.net.HttpURLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import java.text.SimpleDateFormat;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.Status;


/**
 * tweet all our subscribers' cities.
 */
public class UpdateSubscribers
   extends ClientServlet {

   private static final Log logger = LogFactory.getLog(UpdateSubscribers.class);

   @Override
   protected void doGet(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      boolean doText = "true".equalsIgnoreCase(request.getParameter("text"));

      StringBuilder buf = new StringBuilder();

      if (doText) {
         response.setContentType("text/plain");
         buf.append((new SimpleDateFormat()).format(System.currentTimeMillis()) + "\n");
         buf.append("Updating subscribers:\n");
      } else {
         response.setContentType("text/html");
         buf.append("<html>");
         buf.append(" <head>");
         buf.append("       <title>TweetMyCity</title>");
         buf.append("       <meta http-equiv='content-type' content='text/html'/>");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/tmc.css'/>");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/layout.css'/>");
         buf.append(" </head>");

         buf.append(" <body>");
         buf.append("   <div class='background_image'>");
         buf.append("     <div class='text_properties'>");
         buf.append("      <h2>Update Subscribers</h2>");
         buf.append("      <p>" + (new SimpleDateFormat()).format(System.currentTimeMillis()) + "</p>");
         buf.append("      <p>Updating the following:</p><pre>");
      }

      for (TmcUser tmcUser : (new UserStore()).getUsers()) {
         buf.append(tmcUser + "\n");
      }

      if (doText) {
         buf.append("\n");
      } else {
         buf.append("       </pre>");
         buf.append("     </div>");
         buf.append("   </div>  ");
         buf.append(" </body>");
         buf.append("</html>");
      }

      startUpdateThread();

      response.getOutputStream().write(buf.toString().getBytes());
   }


   protected void updateAll() {
      for (TmcUser tmcUser : (new UserStore()).getUsers()) {
         Location location = getLocation(tmcUser);
         String status = Tweet.tryTweet(tmcUser, location);
      }
   }


   protected void startUpdateThread() {
      new Thread(new Runnable() { public void run() { 
         logger.info("starting update thread");
         updateAll(); 
         logger.info("finished update thread");
      } } ).start();
   }

   protected Location getLocation(TmcUser tmcUser) {
      logger.info("getting location for " + tmcUser);

      // This callback is required by the OAuth standard, but is unused
      String callback = "http://veriplace.com";

      // We require the special "Application Token" to issue User Discovery *queries*
      // For your application, you can find this value in the Developer Portal
      Token applicationToken = new Token("DHgu0Ky1zUS8llHxMXt0",
                                         "nJs2pAxco6wmsOASgmOV");

      // We'll need to use our own token store below.
      TokenStore tokenStore = new MemoryTokenStore();

      Client client = null;
      try {
         // Create the Veriplace client
         client = new Client("RfhjzYkyYxOL6lrWgrOe",
                             "pHVlIQP1ofd3kpDbe9BV",
                             Revision.Core1_0RevA,
                             applicationToken,
                             "http://veriplace.com",
                             tokenStore);
      } catch (java.security.NoSuchAlgorithmException nsae) {
         logger.error(nsae);
         return null;
      } catch (java.net.MalformedURLException mue) {
         logger.error(mue);
         return null;
      }

      User user = new User(tmcUser.getUserId());

      int code = -1;
      HttpURLConnection connection = null;
      try {
         // Veriplace's implementation of OAuth User Authorization supports an "immediate" flag
         // If true, Veriplace will grant an Access Token is permission is already granted
         // and will bypass all UI, performing the callback immediately
         boolean immediate = true;
         URL authorizationUrl = 
            new URL(client.getGetLocationAPI().getRedirectURL(callback,user,immediate));

         // GET this URL, but do not follow redirects
         connection = (HttpURLConnection)authorizationUrl.openConnection();
         connection.setInstanceFollowRedirects(false);
         code = connection.getResponseCode();
         logger.info("response code: " + code);
      } catch (IOException ioe) {
         logger.error(ioe);
         return null;
      }

      if (code == 302) {
         // The User Authorization URL sent a redirect, extract the callback URL
         String location_header = connection.getHeaderField("Location");
         logger.debug("location:" + location_header);

         // The callback URL contains the oauth_token and oauth_verifier values (as of Rev A)
         String oauth_token = location_header.split("oauth_token=")[1].split("&")[0];
         String oauth_verifier = location_header.split("oauth_verifier=")[1].split("&")[0];
         logger.debug("callback oauth_token: " + oauth_token);
         logger.debug("callback oauth_verifier: " + oauth_verifier);


         // Retrieve the request token from storage
         Token requestToken = tokenStore.get(oauth_token);
         logger.debug("requestToken: " + requestToken.getToken());

         try {
            // Attempt to get an access token
            Token accessToken = client.getConsumer().getAccessToken(requestToken,oauth_verifier);
            logger.debug("accessToken: " + accessToken.getToken());

            // We got an access token, now make a location request
            // If our application was provisioned for it, we can try cached location by setting the mode
            String mode = null;//"cached";
            Location location = client.getGetLocationAPI().getLocation(accessToken,user,mode);

            if (location == null) {
               // If we didn't get back a location object, it means we encountered a rare
               // race condition where the access token was revoked between when we retrieved it
               // and when the location request was issued
               logger.info("Could not obtain location");
               return null;
            }

            // We got location, but was it successful?
            if (location.getLongitude() != null &&
                location.getLatitude() != null) {
               // Yes!
               logger.info(location.getLatitude() + " " + location.getLongitude());
               logger.info("user is in "+ location.getCity() + ", " + location.getState());
               return location;
            } else {
               // Sadly, no...
               logger.info("have a location, but, no longlat: " + location.getMessage());
               return null;
            }
         } catch (OAuthException e) {
            // An exception here means an Access Token wasn't available
            // Try granting permission directly for your application in the Privacy Manager
            // There should now be a permission request visible in the sidebar
            logger.info(e);
            logger.info("user has revoked our location permission -- remove them.");
            (new UserStore()).remove(tmcUser);
            return null;
         } catch (IOException ioe) {
            // from Token accessToken = client.getConsumer().getAccessToken(requestToken,oauth_verifier);
            logger.error(ioe);
            return null;
         } finally {
            tokenStore.remove(requestToken);
         }
      } else {
         logger.warn("Unexpected http response code: " + code);
         return null;
      }
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

