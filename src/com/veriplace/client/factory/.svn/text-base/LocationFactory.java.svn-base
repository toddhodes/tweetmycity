/* Copyright 2008-2009 WaveMarket, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.veriplace.client.factory;

import com.veriplace.client.Location;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

/**
 * Factory implementation for translating XML into {@link Location} objects.
 */
public class LocationFactory {

   private static final Log logger = LogFactory.getLog(LocationFactory.class);

   /**
    * XML Schema xs:dateTime syntax.
    */
   private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

   public Location getLocation(Document document) {
      
      XPath xpath = XPathFactory.newInstance().newXPath();
      
      Node location = null;
      try {
         location = (Node) xpath.evaluate("/location",document,XPathConstants.NODE);
      } catch (XPathExpressionException e) {
         // probably an async response
         logger.info("Location not available synchronously");
         return null;
      }

      Long id = null;
      String created = null;
      String expires = null;

      try {
         String idStr = xpath.evaluate("@id",location);
         id = Long.parseLong(idStr);
         created = xpath.evaluate("created",location);
         expires = xpath.evaluate("expires",location);
      } catch (XPathExpressionException e) {
         logger.warn("Invalid location");
         return null;
      } catch (NumberFormatException e) {
         logger.warn("Invalid location");
         return null;
      }
      
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
      format.setTimeZone(TimeZone.getTimeZone("UTC"));

      Date creationDate = null;
      Date expirationDate = null;

      try {
         creationDate = format.parse(created);
         expirationDate = format.parse(expires);
      } catch (ParseException e) {
         logger.warn("Invalid location: " + e);
         return null;
      }
          
      return getLocation(document, id, creationDate, expirationDate, xpath, location);
   }

   public Location getLocationUpdate(Document document) {
      
      XPath xpath = XPathFactory.newInstance().newXPath();
      
      Node location = null;
      try {
         location = (Node) xpath.evaluate("/update",document,XPathConstants.NODE);
      } catch (XPathExpressionException e) {
         logger.info("Unable to update location");
         return null;
      }

      return getLocation(document, null, null, null, xpath, location);
   }

   protected Location getLocation(Document document, 
                                  Long id,
                                  Date creationDate,
                                  Date expirationDate,
                                  XPath xpath, 
                                  Node location) {

      Node position = null;

      try {
         position = (Node) xpath.evaluate("position",location,XPathConstants.NODE);

         String longitude = xpath.evaluate("longitude",position);
         String latitude = xpath.evaluate("latitude",position);
         String accuracy = xpath.evaluate("accuracy",position);

         String street = xpath.evaluate("street",position);
         String city = xpath.evaluate("city",position);
         String neighborhood = xpath.evaluate("neighborhood",position);
         String state = xpath.evaluate("state",position);
         String postal = xpath.evaluate("postal",position);
         String countryCode = xpath.evaluate("countryCode",position);

         return new Location(id,
                             creationDate,
                             expirationDate,
                             Double.parseDouble(longitude),
                             Double.parseDouble(latitude),
                             Double.parseDouble(accuracy),
                             street,
                             neighborhood,
                             city,
                             state,
                             postal,
                             countryCode,
                             0,
                             null);
      } catch (XPathExpressionException e) {
         // no position, probably an error
      } catch (NumberFormatException e) {
         // bad parse
         logger.warn("Invalid location: " + e);
         return null;
      }

      Node positionError = null;
      Node cachedPosition = null;

      try {
         positionError = (Node) xpath.evaluate("positionError",location,XPathConstants.NODE);

         int code = Integer.parseInt(xpath.evaluate("code",positionError));
         String message = xpath.evaluate("message",positionError);

         cachedPosition = (Node) xpath.evaluate("cachedPosition",positionError,XPathConstants.NODE);

         if (cachedPosition != null) {

            String longitude = xpath.evaluate("longitude",cachedPosition);
            String latitude = xpath.evaluate("latitude",cachedPosition);
            String accuracy = xpath.evaluate("accuracy",cachedPosition);
            
            String street = xpath.evaluate("street",cachedPosition);
            String city = xpath.evaluate("city",cachedPosition);
            String neighborhood = xpath.evaluate("neighborhood",cachedPosition);
            String state = xpath.evaluate("state",cachedPosition);
            String postal = xpath.evaluate("postal",cachedPosition);
            String countryCode = xpath.evaluate("countryCode",cachedPosition);

            return new Location(id,
                                creationDate,
                                expirationDate,
                                Double.parseDouble(longitude),
                                Double.parseDouble(latitude),
                                Double.parseDouble(accuracy),
                                street,
                                neighborhood,
                                city,
                                state,
                                postal,
                                countryCode,
                                code,
                                message);
         } else {
            return new Location(id,
                                creationDate,
                                expirationDate,
                                code,
                                message);
         }
      } catch (XPathExpressionException e) {
         // neither position nor error
         logger.warn("Invalid location");
         return null;
      }
   }
}


/*
** Local Variables:
**   c-basic-offset: 3
**   tab-width: 3
**   indent-tabs-mode: nil
** End:
**
** ex: set softtabstop=3 tabstop=3 expandtab cindent shiftwidth=3
*/
