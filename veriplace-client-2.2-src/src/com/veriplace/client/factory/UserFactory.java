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

import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryParameters;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Factory implementation for translating XML data into {@link User} objects
 */
public class UserFactory {

   private static final Log logger = LogFactory.getLog(UserFactory.class);

   public User getUser(Document document) {

      XPath xpath = XPathFactory.newInstance().newXPath();
      String expression = "/user/@id";
      
      try {
         String idStr = xpath.evaluate(expression,document);
         long id = Long.parseLong(idStr);
         return new User(id);
      } catch (XPathExpressionException e) {
         logger.warn(e,e);
         return null;
      } catch (NumberFormatException e) {
         return null;
      }
   }

   public List<User> getUsers(Document document) {

      List<User> users = new LinkedList<User>();

      XPath xpath = XPathFactory.newInstance().newXPath();
      String expression = "/users/user/@id";
      
      try {
         final NodeList nl = 
            (NodeList)xpath.evaluate(expression,document,XPathConstants.NODESET);
         for (int i = 0; i < nl.getLength(); ++i) {
            users.add(new User(Long.parseLong(nl.item(i).getTextContent())));
         }
      } catch (XPathExpressionException e) {
         logger.warn(e,e);
      } catch (NumberFormatException e) {
      }

      return users;
   }

   public Map<UserDiscoveryParameters,User> getUsersByPII(Document document) {

      Map<UserDiscoveryParameters,User> users = new LinkedHashMap<UserDiscoveryParameters,User>();

      XPath xpath = XPathFactory.newInstance().newXPath();
      String expression = "/users/user";
      
      try {
         final NodeList nl = 
            (NodeList)xpath.evaluate(expression,document,XPathConstants.NODESET);
         for (int i = 0; i < nl.getLength(); ++i) {
            if (!nl.item(i).hasAttributes()) {
               continue;
            }

            Node id = nl.item(i).getAttributes().getNamedItem("id");
            Node key = nl.item(i).getAttributes().getNamedItem("key");
            Node keyType = nl.item(i).getAttributes().getNamedItem("keyType");

            if (id == null) {
               logger.warn("Attribute 'id' is missing from result");
               continue;
            }

            if (key == null) {
               logger.warn("Attribute 'key' is missing from result");
               continue;
            }

            if (keyType == null) {
               logger.warn("Attribute 'keyType' is missing from result");
               continue;
            }

            User user = new User(Long.parseLong(id.getTextContent()));
            UserDiscoveryParameters pii = null;
            if (keyType.getTextContent().equalsIgnoreCase("mobile")) {
               pii = UserDiscoveryParameters.byPhone(key.getTextContent());
            } else if (keyType.getTextContent().equalsIgnoreCase("email")) {
               pii = UserDiscoveryParameters.byEmail(key.getTextContent());
            } else if (keyType.getTextContent().equalsIgnoreCase("openid")) {
               pii = UserDiscoveryParameters.byOpenId(key.getTextContent());
            } else {
               logger.warn("Unrecognized key type: " + keyType);
               continue;
            }

            users.put(pii,user);
         }
      } catch (XPathExpressionException e) {
         logger.warn(e,e);
      } catch (NumberFormatException e) {
      }

      return users;
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
