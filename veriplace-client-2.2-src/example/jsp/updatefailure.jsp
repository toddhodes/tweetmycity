<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Veriplace Example</title>
	<meta http-equiv="Cache-Control" content="max-age=0"/>
	<meta http-equiv="Cache-Control" content="no-cache"/>
	<meta http-equiv="Cache-Control" content="must-revalidate"/>
	<meta http-equiv="content-type" content="text/html"/>
	<link rel="stylesheet" href="css/veriplace-example.css"/>
</head>
<body>

  <div id="masthead">
    Veriplace Example
  </div>

  <div id="mainContent">
    Veriplace was unable to update your location:
		<c:out value="${veriplace.lastErrorException.message}" /> <br />

		<c:if test="${not empty veriplace.lastErrorException.suggestions}">
			Did you mean?
			<ul>
			<c:forEach var="suggestion" items="${veriplace.lastErrorException.suggestions}">
				<li><a href="setlocation?veriplace_user_id=${veriplace.user.id}&address=${suggestion}&set=1">
						<c:out value="${suggestion}" /></a></li>
			</c:forEach>
			</ul>
		</c:if>

    <br clear="all"/><br/>

    <a href="setlocation">Try again</a><br />
    <a href=".">Go back</a>
  </div>

  <div id="footer">
  <a href="http://veriplace.com"><img src="images/powered_by.png" border="0" alt="Powered by Veriplace" width="165" height="64" /></a><br>
  (c) 2008 WaveMarket, Inc.
  </div>

</body>
</html>
