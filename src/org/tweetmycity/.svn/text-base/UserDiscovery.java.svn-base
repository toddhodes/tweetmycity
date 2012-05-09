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

import com.veriplace.client.User;
import com.veriplace.oauth.consumer.Token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

/**
 * Sample servlet that makes a UserDiscovery request using the Veriplace Client library.
 */
public class UserDiscovery 
   extends ClientServlet {

   private static final Log logger = LogFactory.getLog(UserDiscovery.class);

   @Override
   protected void doGet(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      if (client.isCallback(request)) {
         doCallback(request,response);
      } else {
         doForm(request,response);
      }
   }

   /**
    * Show a simple html page and form.
    */
   protected void doForm(HttpServletRequest request,
                         HttpServletResponse response)
      throws ServletException,
             IOException {


      StringBuilder buf = new StringBuilder();
      buf.append("<html>");
      buf.append(" <head profile='http://www.w3.org/2005/10/profile'>");
      buf.append("    <title>TweetMyCity</title>");
      buf.append("    <meta http-equiv='content-type' content='text/html'/>");
      buf.append("    <link rel='shortcut icon' type='image/ico' href='/tweetmycity/images/favicon.ico' />");
      buf.append("    <link rel='stylesheet' href='/tweetmycity/css/normalize.css'/>");
      buf.append("    <link rel='stylesheet' href='/tweetmycity/css/typography.css'/>");
      buf.append("    <link rel='stylesheet' href='/tweetmycity/css/graphics.css'/>");
      buf.append("    <link rel='stylesheet' href='/tweetmycity/css/layout.css'/>");
      buf.append("    <link rel='stylesheet' href='/tweetmycity/css/forms.css'/>");
      
      buf.append("    <!--[if IE]>");
      buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE.css' />");
      buf.append("    <![endif]-->");
      buf.append("    <!--[if IE7]>");
      buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE7.css' />");
      buf.append("    <![endif]-->");
      buf.append("    <!--[if IE 6]>");
      buf.append("       <link rel='stylesheet' type='text/css' media='screen' href='css/fixes_IE6.css' />");
      buf.append("    <![endif]-->");
      buf.append("    <script type='text/javascript'>");
      buf.append("    //<![CDATA[\n ");
      buf.append("       function checkTOS(tos) {");
      buf.append("          if (!tos.checked) { return false; }");
      buf.append("       }\n");
      buf.append("    //]]> ");
      buf.append("    </script> ");
      buf.append(" </head>");

      buf.append(" <body>");
      buf.append("   <div id='container'>");
      buf.append("     <div id='branding'>");
      buf.append("        <h1><a href='user'>TweetMyCity</a></h1>");
      buf.append("     </div>");
      buf.append("     <!-- /branding -->");
         
      buf.append("     <div id='content'>");
      buf.append("        <p>TweetMyCity is a simple utility that follows your phone&#39;s ");
      buf.append("          location and posts to Twitter when you arrive in a new city.</p>");
      buf.append("         <form id='acceptTOS' method='post' onsubmit='return checkTOS(tos)'>");
      buf.append("            <fieldset class='checkbox'>");
      buf.append("               <input type='checkbox' name='tos' id='tos' value='true' />");
      buf.append("               <label for='tos'>By clicking <strong>I Accept</strong> and using Tweet My City, ");
      buf.append("               I agree to the <a href='terms.html' ");
      buf.append("               onclick='window.open(\"terms.html\", \"Terms_Of_Service_Agreement\", ");
      buf.append("               \"width=600,height=500,status=yes,scrollbars=yes,resizable=no\"); ");
      buf.append("               return false;'>Tweet My City Terms of Service Agreement</a>.</label>  ");
      buf.append("            </fieldset>");
      buf.append("            <button class='button iAcceptGetStarted' type='submit' ");
      buf.append("                    value='Get Started' tabindex='100'>Get Started</button>");
      buf.append("         </form>");
      buf.append("     </div>");
      buf.append("     <!-- /content -->");
      buf.append("     <div id='footer'>");
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
      buf.append("     </div>");
      buf.append("     <!-- /footer -->");
      buf.append("   </div>  ");
      buf.append("   <!-- /container -->");
      buf.append(" </body>");
      buf.append("</html>");

      response.setContentType("text/html");
      response.getOutputStream().write(buf.toString().getBytes());
   }

   protected void doCallback(HttpServletRequest request,
                             HttpServletResponse response)
      throws ServletException,
             IOException {

      // retrieve the Access Token, if any
      Token accessToken = client.getAccessToken(request);
      if (accessToken != null) {
         // get user
         User user = client.getUserDiscoveryAPI().getUser(accessToken);

         if (user != null) {
            // we have some text below for this case, but, don't really need it.
            response.sendRedirect("oauth?vpuser=" + user.getId());
            return;
         }

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

         if (user != null) {
            buf.append("  <h2 id='header-getStarted'>Get Started</h2>");
            buf.append("  <br/><p>First, let's find your Veriplace account ");
            buf.append("     (for locating your phone).</p>");
            //buf.append("  To do so, you need to give permission for location requests on Veriplace.");
            //buf.append("  Be sure to choose 'on an ongoing basis' when asked.</p>");
            //buf.append("  <p><a href='location?user=" + user.getId() + "'>Get Location</a></p>");

            buf.append("  <p><a class='button continue' ");
            buf.append("        href='oauth?vpuser=" + user.getId() + "'>Continue</a></p>");

         } else {
            buf.append("  <h2>Cannot link your accounts</h2>");
            buf.append("  <p>We could not discover your Veriplace user ID. Please try again.</p>");
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
         buf.append("           <li><a href='faqs.html'>faqs</a>  ");
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

         
         response.setContentType("text/html");
         response.getOutputStream().write(buf.toString().getBytes());
      } else {
         // either access was not granted by the user 
         // or the page has been reloaded and the original request token 
         // is no longer valid
         doForm(request,response);
      }
   }

   /**
    * On a post, perform user discovery 
    */
   @Override
   protected void doPost(HttpServletRequest request,
                        HttpServletResponse response)
      throws ServletException,
             IOException {

      // construct callback url
      String callback = 
         client.prepareCallback(request) + 
         request.getRequestURI();
      // construct the redirect URL for user authorization
      String redirectUrl = client.getUserDiscoveryAPI().getRedirectURL(callback,null);

      // redirect the User Agent
      response.sendRedirect(redirectUrl);
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

