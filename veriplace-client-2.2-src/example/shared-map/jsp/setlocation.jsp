<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="vp" uri="http://www.veriplace.com/tags" %>
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
    Veriplace Map Example
  </div>

  <div id="mainContent">

    <h2>
      Set Location
    </h2>

		<%-- This awkward condition works for both the servlet and spring example. --%>
    <c:if test="${showResult or command.specified}">
	  <c:if test="${empty veriplace.location}">
	    <p>
	      <b>Unable to set the location (invalid parameters, or permission was denied).</b>
	    </p>
	  </c:if>
	  <c:if test="${not empty veriplace.location}">
	    <p>
	      <b>The location was successfully updated.  The new location is:</b><br/>
	      Longitude: <vp:longitude/> <br/>
		  Latitude: <vp:latitude/> <br/>
          Address: <vp:address-line/>
	    </p>
	  </c:if>
    </c:if>
    
    <p>
      You can specify the new location in one of two ways:
    </p>
    
    <div class="col1">
      <h3>
        By longitude and latitude
      </h3>
      <form action="setlocation" method="post">
        ${veriplace.userFields}
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
            <td> <input type="text"  size="3" name="accuracy"/> meters </td>
          </tr>
        </table>
        <br>
				<input type="hidden" name="set" value="Set" />
        <input type="submit" value="Set" />
      </form>
   </div>
   
   <div class="col2">
      <h3>
        By address lookup
      </h3>
      <p>
        Enter an arbitrary string describing a place to be geocoded:
      </p>
      <form action="setlocation" method="post">
        ${veriplace.userFields}
        <input type="text" size="40" name="address"/>
        <br/><br/>
				<input type="hidden" name="set" value="Set" />
        <input type="submit" value="Set" />
      </form>
    </div>
    
    <br clear="all"/><br/>
    
    <p>
	NOTE:  This example is provided for future reference, but the current version of the
	Veriplace platform does not support setting user location; it will accept the request
	(and translate address strings to longitude and latitude), but the new location will
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
