<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<body>
   <div id="container">
      <%@ include file="/WEB-INF/jsp/include/branding.jsp" %>

      <div id="content" class="maxUsage">
         <p>Thanks for using this demo! You've used up your free locates.</p>
         
         <div id="location-container">
            <div id="map"></div>
            <p class="note">To locate a different phone, make sure you are not
               signed in at <a href="http://veriplace.com/">veriplace.com</a>,
               then <a href=".">start over</a>.</p>
         </div>
         
         <div id="demoStatus-container">
            <h3>What's next?</h3>
            <ul>
               <li>This site is for demonstration purposes only. You have
                   used up your free locates.</li>
               <li>It was built using freely available
                  <a href="http://developer.veriplace.com/devportal/downloads"/>SDKs</a>
                  and <a href="http://developer.veriplace.com/devportal/developerguide">location APIs</a>.</li>
               <li>Visit <a href="http://developer.veriplace.com"/>developer.veriplace.com</a>
                   to location-enable your own site or application.</li>
               <li>Questions? Reach us at <a href="mailto:contact@veriplace.com">contact@veriplace.com</a>.</li>
            </ul>
         </div>
         <div class="clear"></div>
      </div>
      
      <%@ include file="/WEB-INF/jsp/include/footer.jsp" %>

   </div>
</body>
</html>
