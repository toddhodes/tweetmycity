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
	
		<div id="map">
			<a href="http://maps.google.com/maps?f=q&hl=en&geocode=&q=<vp:latitude/>+<vp:longitude/>&ie=UTF8&z=16&iwloc=addr">
				<img width="400" height="350"
					src="http://maps.google.com/staticmap?center=<vp:latitude/>,<vp:longitude/>&zoom=14&size=400x350&maptype=mobile&markers=<vp:latitude/>,<vp:longitude/>,blue&key=${googleApiKey}" />
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
