<?xml version="1.0" encoding="UTF-8"?>
<%
/*
    This is an example of separating the user discovery request from the location request,
    for applications that wish to store the Veriplace user ID in a registration step and
    use it later.  The user must still go through the Veriplace login process during the
    original discovery step, but subsequent requests will not require any interaction as long
    as the application still has permission to locate the user.

	The custom tag <vp:setup> initializes the Veriplace web framework objects, specifies where
	to find our view parameters in web.xml, and provides this page with a VeriplaceState object
	called "veriplace" which we will use to make the user discovery request.
*/
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="vp" uri="http://www.veriplace.com/tags" %>

<vp:setup id="veriplace" viewparams="map" />

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

<c:choose>
  <c:when test="${not empty param['identify']}">
    <vp:require-user/>
    <c:set var="message" value="The user ID is ${veriplace.user.id}" />
  </c:when>
  <c:when test="${(not empty param['permission'] or not empty param['locate']) and empty param['id']}">
    <c:set var="message" value="Please enter the user ID first." />
  </c:when>
  <c:when test="${not empty param['permission']}">
    <c:set var="userId" value="${param['id']}" />
    <vp:require-location-permission userId="${userId}" interactive="false" />
    <c:set var="message" value="The user has granted permission." />
  </c:when>
  <c:when test="${not empty param['locate'] and not empty param['id']}">
    <c:set var="userId" value="${param['id']}" />
    <vp:require-location userId="${userId}" interactive="false" />
    <c:choose>
      <c:when test="${empty veriplace.location}">
        <c:set var="message" value="Unable to locate user ID ${userId} (bad user ID or don't have permission)" /> 
      </c:when>
      <c:otherwise>
        <c:set var="message" value="Successfully located user ID ${userId} : ${veriplace.location.latitude}, ${veriplace.location.longitude}" />
      </c:otherwise>
    </c:choose>
  </c:when>
</c:choose>

  <div id="masthead">
    Veriplace Map Example (JSP)
  </div>

  <div id="mainContent">

    <h2>
      Query User ID, Permission, Location
    </h2>
    <c:if test="${not empty message}">
      <p><b><c:out value="${message}" /></b></p>
    </c:if>

    <div class="col1">
      <p>
        Click this button to get the user ID of the current user, logging into Veriplace if necessary.
      </p>
      <form action="finduserid" method="post">
        <p>
          <input type="hidden" name="identify" value="true"/>
          <input type="submit" value="Identify"> </td>
        </p>
      </form>

      <p>
        Enter a previously obtained user ID here to ask the user for permission to get location.
      </p>
      <form action="finduserid" method="post">
        <table class="params">
          <tr>
            <td class="label"> ID:</td>
            <td> <input type="text" size="30" name="id"/> </td>
          </tr>
          <tr>
            <td></td>
            <td><input type="hidden" name="permission" value="true"/>
                <input type="submit" value="Request Permission"> </td>
          </tr>
        </table>
        <p>
        </p>
      </form>
    </div>

    <div class="col2">
      <p>
        Enter a previously obtained user ID to locate that user, if the user has already granted permission.
      </p>
      <form action="finduserid" method="post">
        <table class="params">
          <tr>
            <td class="label"> ID:</td>
            <td> <input type="text" size="30" name="id"/> </td>
          </tr>
          <tr>
            <td></td>
            <td><input type="hidden" name="locate" value="true"/>
                <input type="submit" value="Locate"> </td>
          </tr>
        </table>
      </form>
    </div>

    <br clear="all"/><br/>
    <a href=".">Go back</a>

  </div>

  <div id="footer">
  <a href="http://veriplace.com"><img src="images/powered_by.png" border="0" alt="Powered by Veriplace" width="165" height="64" /></a><br>
  (c) 2008 WaveMarket, Inc.
  </div>

</body>
</html>
