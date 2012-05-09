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

import com.veriplace.client.User;

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
