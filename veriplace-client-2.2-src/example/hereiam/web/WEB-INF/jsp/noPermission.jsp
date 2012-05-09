<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ include file="/WEB-INF/jsp/include/head.jsp" %>
<body>
   <div id="container">
      <%@ include file="/WEB-INF/jsp/include/branding.jsp" %>
      
      <div id="content">
         <form action="login" method="get">
            <p>Here I Am doesn't have permission to access your location
               right now.</p>
            <button class="button startOver" type="submit">Start Over</button>
         </form>
      </div>
      
      <%@ include file="/WEB-INF/jsp/include/footer.jsp" %>

   </div>
</body>
</html>
