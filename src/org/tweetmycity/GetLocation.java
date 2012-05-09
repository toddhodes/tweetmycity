/* Copyright 2008 WaveMarket, Inc.
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
package org.tweetmycity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.veriplace.client.Location;
import com.veriplace.client.User;
import com.veriplace.oauth.consumer.Token;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.Status;


/**
  servlet that makes a GetLocation request using the Veriplace Client library.
 */
public class GetLocation
   extends ClientServlet {

   private static final Log logger = LogFactory.getLog(GetLocation.class);

   @Override
   protected void doGet(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      logger.info("doGet");

      User user = getUser(request);
      if (user != null) logger.info("GET GetLocation: have a valid user");

      if (user == null) {
         logger.info("GET GetLocation: no valid user");
         doDiscoverUser(request,response);
      } else if (client.isCallback(request)) {
         logger.info("GET GetLocation: doCallback");
         doCallback(request,response,user);
      } else {
         //doDiscoverUser(request,response);
         doForm(request,response,user);
      }
   }


   /**
    * Delegate to {@link UserDiscovery} servlet, e.g. if we don't know the user.
    */
   protected void doDiscoverUser(HttpServletRequest request,
                                 HttpServletResponse response)
      throws ServletException,
             IOException {

      String callback = client.prepareCallback(request) + request.getRequestURI();
      String redirectUrl = client.getUserDiscoveryAPI().getRedirectURL(callback,null);
      logger.info("redirect url = " + redirectUrl);
      response.sendRedirect(redirectUrl);
   }

   /**
    * Show a simple html page and form.
    */
   protected void doForm(HttpServletRequest request,
                         HttpServletResponse response,
                         User user)
      throws ServletException,
             IOException {

      StringBuilder buf = new StringBuilder();
       // ignore this -- never should get here in noormal flow
      buf.append("<html>");
      buf.append(" <body>");
      buf.append("  <h2>Bad request</h2>");
      buf.append("  <p>Your request was invalid</p>");
      buf.append(" </body>");
      buf.append("</html>");

      response.setContentType("text/html");
      response.getOutputStream().write(buf.toString().getBytes());
   }

   protected void doCallback(HttpServletRequest request,
                             HttpServletResponse response,
                             User user)
      throws ServletException,
             IOException {

      // retrieve the Access Token, if any
      Token accessToken = client.getAccessToken(request);
      if (accessToken != null) {
         // get user
         Location location = client.getGetLocationAPI().getLocation(accessToken, user, "FREEDOM");

         TmcUser tmc = (new UserStore()).get(user.getId());

         String stat = Tweet.tryTweet(tmc, location);

         // show user
         StringBuilder buf = new StringBuilder();
         buf.append("<html>");

         buf.append(" <head profile='http://www.w3.org/2005/10/profile'>");
         buf.append("       <title>TweetMyCity</title>");
         buf.append("       <meta http-equiv='content-type' content='text/html'/>");
         buf.append("       <link rel='shortcut icon' type='image/ico' href='/tweetmycity/images/favicon.ico' />");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/normalize.css'/>");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/typography.css'/>");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/graphics.css'/>");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/footer.css'/>");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/branding.css'/>");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/layout.css'/>");
         buf.append("       <link rel='stylesheet' href='/tweetmycity/css/forms.css'/>");
         
         buf.append("    <!--[if IE]>");
         buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE.css' />");
         buf.append("    <![endif]-->");
         buf.append("    <!--[if IE 6]>");
         buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE6.css' />");
         buf.append("    <![endif]-->");
         buf.append(" </head>");

         buf.append(" <body>");
         buf.append("   <div id='container'>");
         buf.append("     <div id='branding'>");
         buf.append("        <h1><a href='user'>TweetMyCity</a></h1>");
         buf.append("     </div>");
         buf.append("     <!-- /branding -->");
         
         buf.append("     <div id='content'>");


         if (!empty(location)) {
            if (stat == null) {
               buf.append("        <p>Success! Tweet My City will post on your behalf ");
               buf.append("              when you arrive in a new city.");
               buf.append("           To turn this off later, simply go to veriplace.com");
               buf.append("           and turn off location sharing.");
               buf.append("        </p>");
               buf.append("     <p>Additionally, we discovered your current location, ");
               buf.append("        and tried tweeting your current city, but, alas,");
               buf.append("        Twitter seems to be either down or unresponsive right now.");
               buf.append("        Don't worry, though, we'll keep trying in the future!</p>");
            } else {
               buf.append("        <p>Success! Tweet My City will post on your behalf ");
               buf.append("              when you arrive in a new city.");
               buf.append("           To turn this off later, simply go to veriplace.com");
               buf.append("           and turn off location sharing.");
               buf.append("          Your first tweet is: ");
               buf.append("     <p><strong>" + stat + "</strong></p>");
               /*
               buf.append("        <p>Success! We've discovered your current location and ");
               buf.append("           tweeted it, and, Tweet My City will post on your ");
               buf.append("           behalf when you arrive in a new city.");
               buf.append("           To turn this off later, simply go to veriplace.com");
               buf.append("           and turn off location sharing.");
               buf.append("        </p>");
               */
               /*
               buf.append("        <p>Success! Tweet My City will post on your behalf ");
               buf.append("              when you arrive in a new city.");
               buf.append("           To turn this off later, simply go to veriplace.com");
               buf.append("           and turn off location sharing.");
               buf.append("        </p>");
               buf.append("     <p>Additionally, We've discovered your current location, ");
               buf.append("        and you've tweeted your current city as:</p>");
               buf.append("     <p><strong>" + stat + "</strong></p>");
               */
            }
         } else {
               buf.append("        <p>Success! Tweet My City will post on your behalf ");
               buf.append("              when you arrive in a new city.");
               buf.append("           To turn this off later, simply go to veriplace.com");
               buf.append("           and turn off location sharing.");
               buf.append("        </p>");
               buf.append("     <p>We were not able to locate you right now, ");
               buf.append("        but we'll keep trying!</p>");
         }

         buf.append("        <a class='button ok' href='user' tabindex='100'>OK</a>");
         buf.append("     </div>");
         buf.append("     <!-- /content -->");
         
         buf.append("     <div id='footer'>");
         buf.append("        <ul>  ");
         buf.append("           <li><a href='about.html'>about</a>  ");
         buf.append("           <li><a href='supportedPhones.html'>supported phones</a></li>  ");
         buf.append("           <li><a href='terms.html' ");
         buf.append("                  onclick='window.open(\"terms.html\", \"Terms_Of_Service_Agreement\", ");
         buf.append("                  \"width=600,height=500,status=yes,scrollbars=yes,resizable=no\"); ");
         buf.append("                  return false;'>terms of service agreement</a></li>  ");
         buf.append("           <li><a href='http://veriplace.com/privacy/' ");
         buf.append("                  onclick='window.open(\"http://veriplace.com/privacy\", \"Privacy_Policy\", ");
         buf.append("                  \"width=600,height=500,status=yes,scrollbars=yes,resizable=no\"); ");
         buf.append("                  return false;'>privacy policy</a></li>  ");
         buf.append("           <li><a href='faqs.html'>faqs</a></li>  ");
         buf.append("           <li id='protectedBy'>  ");
         buf.append("              <div class='protectedBy-container'>  ");
         buf.append("                 <div class='protectedBy-content'>  ");
         buf.append("                    <span class='protectedBy'>protected by</span> <div  ");
         buf.append("                          class='veriplace'><a href='http://www.veriplace.com'  ");
         buf.append("                          title='http://www.veriplace.com'>Veriplace &reg;</a></div>  ");
         buf.append("                 </div>  ");
         buf.append("              </div>  ");
         buf.append("           </li>  ");
         buf.append("        </ul>  ");
         buf.append("        <div class='clear'></div>  ");
         buf.append("     </div>");
         buf.append("     <!-- /footer -->");
         buf.append("   </div>  ");
         buf.append("   <!-- /container -->  ");
         buf.append(" </body>");
         buf.append("</html>");

         response.setContentType("text/html");
         response.getOutputStream().write(buf.toString().getBytes());
      } else {
         // either access was not granted by the user 
         // or the page has been reloaded and the original request token 
         // is no longer valid
         doForm(request,response,user);
      }
   }

   /**
    * On a post, locate user
    */
   @Override
   protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
      throws ServletException,
             IOException {

      logger.info("doPost " + request.getQueryString());
      User user = getUser(request);
      logger.info("user = " + user);

      if (user == null) {
         doDiscoverUser(request,response);
      } else {

         // add device description to user
         long vpUserId = user.getId();
         UserStore us = new UserStore();
         TmcUser tmc = us.get(vpUserId);
         String deviceDesc = request.getParameter("deviceDesc");
         logger.info("dev = " + deviceDesc);
         if (deviceDesc != null && !"".equals(deviceDesc)) {
            tmc.updateDeviceDescription(deviceDesc);
            us.update(tmc);
         }          

         // construct callback url
         String callback = 
            client.prepareCallback(request) + 
            request.getRequestURI() + 
            "?user=" + 
            user.getId();
         // construct the redirect URL for user authorization
         String redirectUrl = client.getGetLocationAPI().getRedirectURL(callback,user);
         // redirect the User Agent
         response.sendRedirect(redirectUrl);
      }
   }


   public static boolean empty(Location location) {
       return location == null 
              || empty(location.getCity()) 
              || empty(location.getState());

   }

   public static boolean empty(String s) {
       if (s == null) return true;
       if ("".equals(s.trim())) return true;
       return false;
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

