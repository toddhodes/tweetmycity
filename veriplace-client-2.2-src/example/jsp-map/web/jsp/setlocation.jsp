<?xml version="1.0" encoding="UTF-8"?>
<%
/*
	Provides a form for setting a new location for the current Veriplace user, either by
	longitude/latitude or with a geocoding query string (e.g. an address or city/state).
	If the request is successful, the form is redisplayed with the details of the new
	location.
	
	NOTE:  This example is provided for future reference, but the current version of the
	Veriplace platform does not support setting user location; it will accept the request
	(and translate geocoding strings to longitude & latitude), but the new location will
	not be passed to any subsequent location queries.  In a future relase, this feature
	will be enabled selectively for applications with additional security constraints.

	The custom tag <vp:require-set-location> causes this page to acquire a token from Veriplace
	which can be used to set the location.  If the application does not have permission to get
	this token, the request is redirected to an error page.
	
	The <vp:setup> tag initializes the Veriplace web framework objects, and specifies where to find
	our view parameters (location of error page, etc.) in web.xml.
*/
%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="vp" uri="http://www.veriplace.com/tags" %>

<vp:setup id="veriplace" viewparams="map" />
<vp:require-set-location />

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
    Veriplace Map Example (JSP)
  </div>

  <div id="mainContent">

    <h2>
      Set Location
    </h2>

<c:if test="${not empty param['set']}">

<c:set var="longitude" value="${param['longitude']}" />
<c:set var="latitude" value="${param['latitude']}" />
<c:set var="uncertainty" value="${param['uncertainty']}" />
<c:set var="address" value="${param['address']}" />

<c:choose>
  <c:when test="${not empty longitude and not empty latitude and not empty uncertainty}">
	  <vp:require-location-update longitude="${longitude}" latitude="${latitude}" uncertainty="${uncertainty}" />
  </c:when>
  <c:when test="${not empty param['address']}">
 	  <vp:require-location-update address="${address}" />
 </c:when>
</c:choose>

<c:choose>
 <c:when test="${veriplace.error}">
    <p>
      <b>Unable to set the location (invalid parameters, or permission was denied).</b>
    </p>
 </c:when>
 <c:otherwise>
    <p>
      <b>The location was successfully updated.  The new location is:</b><br/>
	    Longitude: <vp:longitude /> <br/>
	    Latitude: <vp:latitude /> <br/>
        Address: <vp:address-line />
    </p>
 </c:otherwise>
</c:choose>

</c:if>

    <p>
      You can specify a new location in one of two ways:
    </p>

    <div class="col1">     
      <h3>
        By longitude and latitude
      </h3>
      <form action="setlocation" method="post">
        <vp:user-fields/>
        <input type="hidden" name="set" value="1"/>
        <table border="0" cellpadding="0" cellspacing="0">
          <tr>
            <td> Longitude&nbsp;(required):&nbsp;&nbsp;&nbsp;</td>
            <td> <input type="text" size="11" name="longitude"/> </td>
          </tr>
          <tr>
            <td> Latitude&nbsp;(required):&nbsp;&nbsp;&nbsp;</td>
            <td> <input type="text" size="11" name="latitude"/> </td>
          </tr>
          <tr>
            <td> Accuracy&nbsp;(optional):&nbsp;&nbsp;&nbsp;</td>
            <td> <input type="text"  size="3" name="uncertainty"/> meters </td>
          </tr>
        </table>
        <input type="submit" value="Set" />
      </form>
    </div>
    <div class="col2">
      <h3>
        By geocoding lookup
      </h3>
      <p>
        Enter an arbitrary string describing a place to be geocoded:
      </p>
      <form action="setlocation" method="post">
        <vp:user-fields/>
        <input type="hidden" name="set" value="1"/>
        <input type="text" size="40" name="address"/>
        <br/><br/>
        <input type="submit" value="Set" />
      </form>
    </div>

    <br clear="all"/><br/>
    
    <p>
	NOTE:  This example is provided for future reference, but the current version of the
	Veriplace platform does not support setting user location; it will accept the request
	(and translate geocoding strings to longitude and latitude), but the new location will
	not be passed to any subsequent location queries.  In a future relase, this feature
	will be enabled selectively for applications with additional security constraints.
    </p>
    
    <a href=".">Go back</a>

  </div>
  
  <div id="footer">
  <a href="http://veriplace.com"><img src="images/powered_by.png" border="0" alt="Powered by Veriplace" width="165" height="64" /></a><br>
  (c) 2008 WaveMarket, Inc.
  </div>

</body>
</html>
