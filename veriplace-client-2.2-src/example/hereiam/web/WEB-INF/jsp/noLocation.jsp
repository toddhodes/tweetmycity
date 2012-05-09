<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<body>
   <div id="container">

      <%@ include file="/WEB-INF/jsp/include/branding.jsp" %>

      <div id="content">
         <form action="." method="post">
            <input type="hidden" name="start"/>
            <input type="hidden" name="acceptTerms" value="1"/>
            <p>Veriplace is still waiting for your phone to report its
               location.</p>
            <p>Your phone must be on and in service for this to work. You'll
               have best results when near a window or outdoors. In some cases,
               it can take a few moments for your phone to report its first
               location.</p>
            <button class="button tryAgain" type="submit">Try Again</button>
         </form>
      </div>
  
      <%@ include file="/WEB-INF/jsp/include/footer.jsp" %>

   </div>
</body>
</html>
