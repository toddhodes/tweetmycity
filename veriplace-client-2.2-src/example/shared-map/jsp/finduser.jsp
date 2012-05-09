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
    Veriplace Map Example
  </div>

  <div id="mainContent">

    <h2>
      Find User
    </h2>

    <c:if test="${not empty error}">
      <p>
	    <b>${error}</b>
      </p>
	</c:if>

    <p>
      Enter one of the following criteria to locate the user.
    </p>
    
    <div class="col1">
      <form action="finduser" method="post">
        <table class="params">
          <tr>
            <td class="label"> Phone:</td>
            <td> <input type="text" size="11" name="phone"/> </td>
          </tr>
          <tr>
            <td></td>
            <td> <input type="submit" name="find" value="Find"> </td>
          </tr>
        </table>
      </form>
      <br/>
      <form action="finduser" method="post">
        <table class="params">
          <tr>
            <td class="label"> Email:</td>
            <td> <input type="text" size="25" name="email"/> </td>
          </tr>
          <tr>
            <td></td>
            <td> <input type="submit" name="find" value="Find"> </td>
          </tr>
        </table>
      </form>
    </div>

    <div class="col2">
      <form action="finduser" method="post">
        <table class="params">
          <tr>
            <td class="label"> OpenID:</td>
            <td> <input type="text" size="11" name="openid"/> </td>
          </tr>
          <tr>
            <td></td>
            <td> <input type="submit" name="find" value="Find"> </td>
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
