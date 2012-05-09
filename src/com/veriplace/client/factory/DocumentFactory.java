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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Factory for creating XML Documents from byte[] data.
 */
public class DocumentFactory {

   private static final Log logger = LogFactory.getLog(DocumentFactory.class);

   public Document getDocument(byte[] bytes) {

      try {
         return 
            DocumentBuilderFactory.newInstance().
            newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
      } catch (ParserConfigurationException e) {
         logger.warn(e,e);
         return null;
      } catch (SAXException e) {
         logger.info(e,e);
         return null;
      } catch (IOException e) {
         logger.info(e,e);
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
