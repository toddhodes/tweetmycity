
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
import twitter4j.http.AccessToken;


/**
 */
public class TwitterOAuth
   extends ClientServlet {

   private static final Log logger = LogFactory.getLog(TwitterOAuth.class);

   @Override
   protected void doGet(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      // if this is the callback, we'll have an oath_token param
      String oauth_token = request.getParameter("oauth_token");
      boolean doFinish = (oauth_token != null && !"".equals(oauth_token));

      if (doFinish) {
         doFinish(request, response);
      } else {
         doStart(request, response);
      }
   }


   protected void doStart(HttpServletRequest request,
                          HttpServletResponse response)
      throws ServletException,
             IOException {

      String vpuser = request.getParameter("vpuser");
      request.getSession().setAttribute("vpuser", vpuser);

      StringBuilder buf = new StringBuilder();

      response.setContentType("text/html");
      buf.append("<html>");
      buf.append(" <head profile='http://www.w3.org/2005/10/profile'>");
      buf.append("       <title>TweetMyCity</title>");
      buf.append("       <meta http-equiv='content-type' content='text/html'/>");
      buf.append("       <link rel='shortcut icon' type='image/ico' href='/tweetmycity/images/favicon.ico' />");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/normalize.css'/>");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/typography.css'/>");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/graphics.css'/>");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/layout.css'/>");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/forms.css'/>");
      buf.append("    <!--[if IE]>");
      buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE.css' />");
      buf.append("    <![endif]-->");
      buf.append("    <!--[if IE7]>");
      buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE7.css' />");
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

      // content
      //buf.append("      <h2>Get Started</h2>");
      //buf.append("      <p>Done!  Next, let's link your Twitter account.</p>");
      buf.append("      <h2>Veriplace account signed in</h2>");
      buf.append("  <p>You are successfully signed into Veriplace.");
      buf.append("     Next, we'll link your Veriplace account ");
      buf.append("    (for locating your phone) with your Twitter account (for tweeting).");
      buf.append("  </p>");


      String authUrl = Tweet.startOAuth();
      buf.append("      <p><a class='button continue' href='" + authUrl + "'>Continue</a></p>");
      //buf.append("      <p>(You will be returned here afterward automatically.)</p>");


      buf.append("     </div>");
      buf.append("     <!-- /content -->  ");
      buf.append("     <div id='footer'>  ");
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
      buf.append("     </div>  ");
      buf.append("     <!-- /footer -->  ");
      buf.append("  </div>  ");
      buf.append("  <!-- container -->  ");
      buf.append("</body>");
      buf.append("</html>");

      response.getOutputStream().write(buf.toString().getBytes());
   }



   public void doFinish(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      String oauth_token = request.getParameter("oauth_token");
      logger.info("twitter oauth_token = " + oauth_token);

      String vpuser = (String)request.getSession().getAttribute("vpuser");
      logger.info("vpuser from session = " + vpuser);
      User user = userFromString(vpuser);
      logger.info("vpuser = " + user);


      StringBuilder buf = new StringBuilder();

      response.setContentType("text/html");
      buf.append("<html>");
      buf.append(" <head profile='http://www.w3.org/2005/10/profile'>");
      buf.append("       <title>TweetMyCity</title>");
      buf.append("       <meta http-equiv='content-type' content='text/html'/>");
      buf.append("       <link rel='shortcut icon' type='image/ico' href='/tweetmycity/images/favicon.ico' />");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/normalize.css'/>");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/typography.css'/>");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/graphics.css'/>");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/layout.css'/>");
      buf.append("       <link rel='stylesheet' href='/tweetmycity/css/forms.css'/>");
      buf.append("    <!--[if IE]>");
      buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE.css' />");
      buf.append("    <![endif]-->");
      buf.append("    <!--[if IE7]>");
      buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE7.css' />");
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

      // content
      if (user == null) {
         // lost our session state...
         buf.append("      <h2>Oops!</h2>");
         buf.append("      <p>Seems we've encountered an error, losing your Veriplace");
         buf.append("         user information on returning from Twitter.  Let's try this again...");
         buf.append("      </p>");         
         buf.append("        <a class='button continue' href='user' tabindex='100'>OK</a>");

      } else {
         
         AccessToken at = Tweet.finishOAuth();
         logger.info("accesstoken = " + at);

         if (at == null) {
            // did not have access to twitter
            buf.append("      <h2>Oops!</h2>");
            buf.append("      <p>Seems we do not have valid Twitter");
            buf.append("         user information.  Let's try this again...");
            buf.append("      </p>");         
            buf.append("        <a class='button continue' href='user' tabindex='100'>OK</a>");

         } else {
            // success
            buf.append("      <h2>Twitter access granted</h2>");
            buf.append("      <p>Twitter access has been authorized.</p>");
            buf.append("      <p>You can revoke this authorization");
            buf.append("         at any time by visiting your Twitter account, under");
            buf.append("         <i>Settings&nbsp;-->&nbsp;Connections&nbsp;-->&nbsp;Revoke Access</i>");
            buf.append("      </p>");

            // store user info
            UserStore us = new UserStore();
            us.addUser(user.getId(),
                       at.getToken(),
                       at.getTokenSecret());      
         
            //Tweet.updateStatusViaOAuth(user.getId(),
            //                           (new SimpleDateFormat()).format(System.currentTimeMillis()));

            buf.append("      <p>Now, choose an optional Nickname for your device, and");
            buf.append("         we'll go ahead and try our first TweetMyCity!");
            buf.append("      </p>");
            buf.append("  <form id='signIn' action='location' method='post'>");
            buf.append("  <fieldset class='optional'>");
            buf.append("     <label for='deviceDesc'>Device Nickname (optional):</label>");
            buf.append("     <input id='deviceDesc' class='deviceDesc' name='deviceDesc' type='text' ");
            buf.append("            tabindex='100' />");
            buf.append("                       <span class='deviceDesc-example'>e.g. My Phone</span>");
            buf.append("     <input type='hidden' name='user' value='" + user.getId() + "'/>");
            buf.append("  </fieldset>");
            
            buf.append("  <button class='button continue' type='submit' value='Continue' tabindex='120'>");
            buf.append("  Continue</button>");
            buf.append("  </form>");
         }
      }

      buf.append("     </div>");
      buf.append("     <!-- /content -->  ");
      buf.append("     <div id='footer'>  ");
      buf.append("        <ul>  ");
      buf.append("           <li><a href='about.html'>about</a></li>  ");
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
      buf.append("     </div>  ");
      buf.append("     <!-- /footer -->  ");
      buf.append("  </div>  ");
      buf.append("  <!-- container -->  ");
      buf.append("</body>");
      buf.append("</html>");

      response.getOutputStream().write(buf.toString().getBytes());
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

