package org.tweetmycity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.veriplace.client.Location;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.http.RequestToken;
import twitter4j.http.AccessToken;


public class Tweet {

   private static final Log logger = LogFactory.getLog(Tweet.class);
 

   private static final String consumer_key = "4lUrVENZYCIcfx5U3L45Ig";
   private static final String consumer_secret = "q1O8Rytr6HZy8cZKEMs9oxRawplHRjC56yCFqaFhI";

   private static final int MAX_RETRY_COUNT = 10;


   public static String startOAuth() {
      Twitter twitter = new Twitter();
      twitter.setSource("TweetMyCity.org");
      twitter.setOAuthConsumer(consumer_key, consumer_secret);

      //RequestToken requestToken = null;
      try {
         requestToken = twitter.getOAuthRequestToken();
         logger.debug("requestToken = " + requestToken);
      }  catch (TwitterException te) {
         logger.error(te);
      }

      String authUrl = requestToken.getAuthorizationURL();
      logger.info("returning auth url:" + authUrl);

      return authUrl;
   }
   static RequestToken requestToken = null;


   public static AccessToken finishOAuth() {
      Twitter twitter = new Twitter();
      twitter.setSource("TweetMyCity.org");
      twitter.setOAuthConsumer(consumer_key, consumer_secret);

      logger.debug("requestToken = " + requestToken);
      AccessToken accessToken = null;
      int retry = 0;
      while (accessToken == null && retry < 12) {
         try{
            accessToken = requestToken.getAccessToken();
         } catch (TwitterException te) {
            if(401 == te.getStatusCode()){
               logger.error("Unable to get the access token.");
            } else {
               logger.error(te);
            }
         }
         logger.debug("retrying");
         retry++;
      }

      try {
         logger.info("accessToken = " + accessToken);
         twitter.setOAuthAccessToken(accessToken);
         logger.info("creds = " + twitter.verifyCredentials());
         //Status status = twitter.updateStatus("update using oauth credentials");
         //logger.info("Successfully updated the status to [" + status.getText() + "].");
      }  catch (TwitterException te) {
         te.printStackTrace();
         logger.error(te);
      }

      return accessToken;
   }



   public static void updateStatusViaOAuth(long vpuserid, String statusMsg) {
      Twitter twitter = new Twitter();
      twitter.setSource("TweetMyCity.org");
      twitter.setOAuthConsumer(consumer_key, consumer_secret);
      AccessToken accessToken = (new UserStore()).get(vpuserid).getAccessToken();
      twitter.setOAuthAccessToken(accessToken);
      try {
         Status status = twitter.updateStatus(statusMsg);
         logger.info("Successfully updated the status to [" + status.getText() + "].");
      }  catch (TwitterException te) {
         logger.error(te);
      }
   }




   public static String tryTweet(TmcUser tmc, Location location) {
      if (!GetLocation.empty(location)) {
         String cityState = location.getCity() + ", " + location.getState();
         if (!cityState.equals(tmc.getLastCityState())) {

            // not same as last time
            logger.info("tweeting the location, it's not the same as last time: " + cityState);

            // tweet the city
            String stat = tweet(tmc, location);
            
            if (stat != null) {
               // ... and update and save new location
               tmc.updateLastCity(cityState);
               (new UserStore()).update(tmc);
            } else {
               logger.info("could not tweet, not updating last location: " + cityState);
            }

            return stat;
         } else {
            logger.info("not tweeting the location, it's the same as last time: " + cityState);
         }
      } else {
         logger.info("No location available.  Did not update the status");
      }
      return null;
   }


   public static String tweet(TmcUser tmc, Location location) {
      return tweet(tmc, location, 0);
   }


   public static String tweet(TmcUser tmc, Location location, int retryCount) {

      Twitter twitter = new Twitter();
      twitter.setOAuthConsumer(consumer_key, consumer_secret);
      twitter.setOAuthAccessToken(tmc.getAccessToken());
      twitter.setSource("TweetMyCity.org");

      String stat = "" //"TweetMyCity.org: " / "@tweet_my_city: "
         + tmc.getDeviceDescription()
         + " is now in "
         + location.getCity() + ", " + location.getState();
      try {
         User twitterUser = twitter.verifyCredentials();
         logger.info("creds = " + twitterUser);

         Status status = twitter.updateStatus(stat);
         logger.info("Successfully updated the status to [" + status.getText() + "].");
      } catch (twitter4j.TwitterException te) {
         String msg = te.getMessage();
         int code = te.getStatusCode();
         logger.info("Got twitter exception: " + code + ": " + msg);
         if (code == 401) {
            // have no user credentials, delete the user
            logger.info("got a 401: remove user " + tmc);
            (new UserStore()).remove(tmc);
         }
         if (code == 408) {
            if (retryCount < MAX_RETRY_COUNT) {
               logger.info("retrying");
               return tweet(tmc, location, ++retryCount);
            } else {
               logger.warn("exceeded max retry count tweeting.  giving up.");
            }
         }
         // if exceedeed retry or got non-408 error, didn't update successfully
         return null;
      }
     return stat;
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


