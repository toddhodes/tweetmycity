<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<head profile="http://www.w3.org/2005/10/profile">
<body>
   <div id="container">

      <%@ include file="/WEB-INF/jsp/include/branding.jsp" %>

      <div id="content">
         <form method="post">
            <input type="hidden" name="start"/>
            <p>We are now going to ask Veriplace for your location.</p>
            <p class="note"><b>Developer Note:</b> This demo uses
               <a href="http://oauth.net/">OAuth</a>, an open protocol for
               delegated authority, to interact with Veriplace and obtain your
               location.</p>
            <button class="button continue" type="submit">Continue</button>
         </form>
      </div>
  
      <%@ include file="/WEB-INF/jsp/include/footer.jsp" %>

   </div>
</body>
</html>
