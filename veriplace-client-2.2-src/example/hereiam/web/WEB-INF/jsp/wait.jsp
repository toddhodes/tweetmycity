<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title>Here I Am - A Veriplace Demo</title>
   <link rel="stylesheet" href="css/hereiam.css"/>
   
   <meta http-equiv="Cache-Control" content="max-age=0"/>
   <meta http-equiv="Cache-Control" content="no-cache"/>
   <meta http-equiv="Cache-Control" content="must-revalidate"/>

   <meta http-equiv="refresh" content="2;url=${veriplace_callback}" > 
   <%-- This auto-refresh directive is what makes the "please wait"
        mechanism work.  Veriplace will set the variable "veriplace_callback"
        to the URL of the current request, possibly adding other query string
        parameters for internal use.  Each time the page is refreshed, the
        servlet will come to the same requireLocation() call, and either detect
        that the request is finished and proceed, or display this page again. --%>
</head>
<body>
   <div id="container">

      <%@ include file="/WEB-INF/jsp/include/branding.jsp" %>

      <div id="content">
         <p>Please wait...</p>
         <img src="images/spinner.gif" width="33" height="33" alt="processing"/>
      </div>
  
      <%@ include file="/WEB-INF/jsp/include/footer.jsp" %>

   </div>
</body>
</html>
