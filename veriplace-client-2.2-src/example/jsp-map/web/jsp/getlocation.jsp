<?xml version="1.0" encoding="UTF-8"?>
<%
/*
	Displays the location of the current Veriplace user.

	By using the custom tag <vp:require-location>, this page triggers a location discovery request,
	which may redirect to an external Veriplace page or to an error page.  The rest of this JSP will
	not execute unless and until it has a valid location.
	
	The <vp:require-user> tag triggers a user discovery request before the location request.  This
	would happen anyway because a location request requires a user, but making the user request
	explicitly allows us to specify whether or not user interaction is allowed (by default, it is).
	
	The <vp:setup> tag initializes the Veriplace web framework objects, and specifies where to find
	our view parameters (location of error page, etc.) in web.xml.
 */
%>
<%@ taglib prefix="vp" uri="http://www.veriplace.com/tags" %>

<vp:setup id="veriplace" viewparams="map" propertiesId="sharedProperties"/>
<vp:require-user interactive="${empty param.immediate}" />
<vp:require-location interactive="${empty param.immediate}" />

<html>
<head>
	<title>Veriplace Example</title>
	<link rel="stylesheet" href="css/veriplace-example.css"/>
</head>
<body>

	<div id="masthead">
    	Veriplace Map Example
	</div>

	

	<div id="mainContent">
	
		<div id="map">
			<a href="http://maps.google.com/maps?f=q&hl=en&geocode=&q=<vp:latitude/>+<vp:longitude/>&ie=UTF8&z=16&iwloc=addr">
				<img width="400" height="350"
					src="http://maps.google.com/staticmap?center=<vp:latitude/>,<vp:longitude/>&zoom=14&size=400x350&maptype=mobile&markers=<vp:latitude/>,<vp:longitude/>,blue&key=${sharedProperties['googleApiKey']}" />
			</a>
		</div>
	
		<div id="location">
			Get location attempt succeeded to within <vp:accuracy/> meters. <br/>
		    Longitude: <vp:longitude/> <br/>
		    Latitude: <vp:latitude/> <br/>
            Address: <vp:address-line/>
		    
			<form action="locate" method="get">
				<vp:user-fields/>
				<input type="submit" value="Locate This User Again" />
			</form>
	
			<p>
				<a href=".">Start over<a>
			</p>
		</div>
	
	</div>
  
  <div id="footer">
  <a href="http://veriplace.com"><img src="images/powered_by.png" border="0" alt="Powered by Veriplace" width="165" height="64" /></a><br>
  (c) 2008 WaveMarket, Inc.
  </div>
  
</body>
</html>
