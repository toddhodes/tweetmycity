<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<body>
   <div id="container">

      <%@ include file="/WEB-INF/jsp/include/branding.jsp" %>

      <div id="content" class="map">
         <div id="location-container">
            <%-- Location --%>   
            <p class="locationInfo">
               Your phone was last seen within
               <b><c:out value="${location.accuracy}"/> meters</b> of
               <c:if test="${not empty location.street}">
                  <b><c:out value="${location.addressLine}"/></b>
               </c:if>
               (<c:out value="${location.longitude}"/>, <c:out
                value="${location.latitude}"/>)
            </p>
            <p class="locationDate"><fmt:formatDate
               value="${location.creationDate}" pattern="MM/dd/yy hh:mm aa"/></p>
            
            <div id="map">
               (map would go here)
               <!-- A JavaScript-based map control could be placed here, with its
                    coordinates set to (${location.longitude}, ${location.latitude}). -->
            </div>
            
            <p class="note">To locate a different phone, make sure you are not
               signed in at <a href="http://veriplace.com/">veriplace.com</a>,
               then <a href=".">start over</a>.</p>
         </div>
      
         <%-- demo status info --%>
         <div id="demoStatus-container">
            <h3>What's next?</h3>
            <ul>
               <c:if test="${remainingLocates >= 0}">
               <li><p>This site is for demonstration purposes only:</p>
                  <c:choose>
                     <c:when test="${remainingLocates == 0}">
                        You have used up your free demonstration locates.
                     </c:when>
                     <c:otherwise>
                        You may use this site
                        <c:choose>
                           <c:when test="${remainingLocates == 1}">
                              <b>1 more time</b>
                           </c:when>
                           <c:otherwise>
                              <b><c:out value="${remainingLocates}"/> more times</b>
                           </c:otherwise>
                        </c:choose>
                        in the next
                        <c:choose>
                           <c:when test="${remainingDays > 1}">
                              <b><c:out value="${remainingDays}"/> days</b>.
                           </c:when>
                           <c:otherwise>
                              <b><c:out value="${remainingHours}"/> hours</b>.
                           </c:otherwise>
                        </c:choose>
                     </c:otherwise>
                  </c:choose>
              </li>
              </c:if>
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
