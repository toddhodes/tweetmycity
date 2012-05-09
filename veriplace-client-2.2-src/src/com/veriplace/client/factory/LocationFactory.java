/* Copyright 2008-2010 WaveMarket, Inc.
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
import com.veriplace.client.MalformedResponseException;
import com.veriplace.client.PositionFailureException;
import com.veriplace.client.UpdateFailureException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Factory implementation for translating XML into {@link Location} objects.
 */
public class LocationFactory {

   private static final Log logger = LogFactory.getLog(LocationFactory.class);

   /**
    * XML Schema xs:dateTime syntax.
    */
   private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

   /**
    * Parse a Get Location API response.
    * @throws PositionFailureException  if the response contained an error message
    * @throws MalformedResponseException  if the response was malformed
    */
   public Location getLocation(Document document) 
      throws PositionFailureException,
             MalformedResponseException {
      
      XPath xpath = XPathFactory.newInstance().newXPath();
      
      Node location = null;
      try {
         location = (Node) xpath.evaluate("/location",document,XPathConstants.NODE);
      } catch (XPathExpressionException e) {
         final String message = "Response did not contain a 'location' element";
         logger.info(message);
         throw new MalformedResponseException(message);
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
         final String message = "Could not parse location response";
         logger.warn(message);
         throw new MalformedResponseException(message);
      } catch (NumberFormatException e) {
         final String message = "Malformed id in location response";
         logger.warn(message);
         throw new MalformedResponseException(message);
      }
      
      SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
      format.setTimeZone(TimeZone.getTimeZone("UTC"));

      Date creationDate = null;
      Date expirationDate = null;

      try {
         creationDate = format.parse(created);
         expirationDate = format.parse(expires);
      } catch (ParseException e) {
         final String message = "Could not parse location response date";
         logger.warn(message);
         throw new MalformedResponseException(message);
      }
          
      try {
         return getPosition(xpath,location,id,creationDate,expirationDate);
      } catch (XPathExpressionException e) {
         // no position, probably a position error, below
      } catch (NumberFormatException e) {
         final String message = "Could not parse location response";
         logger.warn(message);
         throw new MalformedResponseException(message);
      }

      try {
         throw getPositionError(xpath,location,id,creationDate,expirationDate);
      } catch (XPathExpressionException e) {
         final String message = "Could not parse location response";
         logger.warn(message);
         throw new MalformedResponseException(message);
      } catch (NumberFormatException e) {
         final String message = "Could not parse location response";
         logger.warn(message);
         throw new MalformedResponseException(message);
      }
   }

   /**
    * Parse a Set Location API response.
    * @throws UpdateFailureException  if the response contained an error message
    * @throws MalformedResponseException  if the response was malformed
    */
   public Location getLocationUpdate(Document document) 
      throws UpdateFailureException, MalformedResponseException {
      
      XPath xpath = XPathFactory.newInstance().newXPath();
      
      Node update = null;
      try {
         update = (Node) xpath.evaluate("/update",document,XPathConstants.NODE);
      } catch (XPathExpressionException e) {
         final String message = "Response did not contain an 'update' element";
         logger.info(message);
         throw new MalformedResponseException(message);
      }

      try {
         return getPosition(xpath,update,null,null,null);
      } catch (XPathExpressionException e) {
         // no position, probably an update error, below
      } catch (NumberFormatException e) {
         final String message = "Could not parse update response";
         logger.warn(message);
         throw new MalformedResponseException(message);
      }

      try {
         throw getUpdateError(xpath,update);
      } catch (XPathExpressionException e) {
         final String message = "Could not parse location response";
         logger.warn(message);
         throw new MalformedResponseException(message);
      } catch (NumberFormatException e) {
         final String message = "Could not parse location response";
         logger.warn(message);
         throw new MalformedResponseException(message);
      }
   }

   /**
    * Get a "positionError" and parse node from parent.
    */
   protected PositionFailureException getPositionError(XPath xpath, 
                                                       Node parent,
                                                       Long id,
                                                       Date creationDate,
                                                       Date expirationDate) 
      throws XPathExpressionException,
             NumberFormatException {

      final Node positionError = 
         (Node)xpath.evaluate("positionError",parent,XPathConstants.NODE);
      
      final int code = Integer.parseInt(xpath.evaluate("code",positionError));
      final String message = xpath.evaluate("message",positionError);
      
      try {
         final Location cachedLocation = 
            getPosition(xpath,positionError,"cachedPosition",id,creationDate,expirationDate);
         return new PositionFailureException(message,code,cachedLocation);
      } catch (Exception e) {
         return new PositionFailureException(message,code,null);
      }
   }

   /**
    * Get an "updateError" and parse node from parent.
    */
   protected UpdateFailureException getUpdateError(XPath xpath, 
                                                   Node parent) 
      throws XPathExpressionException,
             NumberFormatException {

      final Node updateError = 
         (Node)xpath.evaluate("updateError",parent,XPathConstants.NODE);
      
      final int code = Integer.parseInt(xpath.evaluate("code",updateError));
      final String message = xpath.evaluate("message",updateError);
      final List<String> suggestions = new LinkedList<String>();

      try {
         final NodeList nl = 
            (NodeList)xpath.evaluate("suggestion",updateError,XPathConstants.NODESET);
         for (int i = 0; i < nl.getLength(); ++i) {
            suggestions.add(nl.item(i).getTextContent());
         }
      } catch (XPathExpressionException e) {
         // no suggestions
      }
         
      return new UpdateFailureException(message,code,suggestions);
   }

   /**
    * Get a "position" and parse node from parent.
    */
   protected Location getPosition(XPath xpath, 
                                  Node parent,
                                  Long id,
                                  Date creationDate,
                                  Date expirationDate) 
      throws XPathExpressionException,
             NumberFormatException {

      return getPosition(xpath,parent,"position",id,creationDate,expirationDate);
   }

   /**
    * Get a "position" and parse node from parent.
    */
   protected Location getPosition(XPath xpath, 
                                  Node parent,
                                  String name,
                                  Long id,
                                  Date creationDate,
                                  Date expirationDate) 
      throws XPathExpressionException,
             NumberFormatException {

      final Node position = (Node) xpath.evaluate(name,parent,XPathConstants.NODE);
      return parseLocation(xpath,position,id,creationDate,expirationDate);
   }

   /**
    * Decode a "position" node.
    */
   protected Location parseLocation(XPath xpath, 
                                    Node position,
                                    Long id,
                                    Date creationDate,
                                    Date expirationDate) 
      throws XPathExpressionException,
             NumberFormatException {
      
      final String longitude = xpath.evaluate("longitude",position);
      final String latitude = xpath.evaluate("latitude",position);
      final String accuracy = xpath.evaluate("accuracy",position);

      final String street = xpath.evaluate("street",position);
      final String city = xpath.evaluate("city",position);
      final String neighborhood = xpath.evaluate("neighborhood",position);
      final String state = xpath.evaluate("state",position);
      final String postal = xpath.evaluate("postal",position);
      final String countryCode = xpath.evaluate("countryCode",position);

      return new Location(id,
                          creationDate,
                          expirationDate,
                          Double.parseDouble(longitude),
                          Double.parseDouble(latitude),
                          Double.parseDouble(accuracy),
                          normalize(street),
                          normalize(neighborhood),
                          normalize(city),
                          normalize(state),
                          normalize(postal),
                          normalize(countryCode));
   }

   /**
    * Normalize empty strings to null.
    */
   protected String normalize(String s) {
      if (s == null) {
         return s;
      }
      s = s.trim();
      if (s.length() == 0) {
         return null;
      }
      return s;
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
