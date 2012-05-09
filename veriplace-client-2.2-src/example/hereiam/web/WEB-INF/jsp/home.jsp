<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<body>
   <div id="container">

      <%@ include file="/WEB-INF/jsp/include/branding.jsp" %>

      <div id="content">
         <c:if test="${mustAcceptTermsError}">
            <p class="error">You must accept the Terms of Service Agreement to
               use Here I Am.</p>
         </c:if>
         <form method="post">
            <input type="hidden" name="start"/>
            <p>Test whether your phone is one of the 100s of millions of phones
               that can be remotely located with Veriplace.</p>

            <fieldset class="checkbox">
               <input type="checkbox" id="acceptTerms" name="acceptTerms" value="1"/>
               <label for="acceptTerms">By clicking <b>I Accept</b> and using
                  Veriplace's Here I Am, I agree to the <a href="tos">Here I Am
                  Terms of Service Agreement</a>.</label>
            </fieldset>

            <button class="button iAcceptLocateMyPhone" type="submit">I Accept,
               Locate My Phone</button>
         </form>
      </div>
  
      <%@ include file="/WEB-INF/jsp/include/footer.jsp" %>
      
   </div>
</body>
</html>
