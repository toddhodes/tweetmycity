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
package com.veriplace.web.servlet.tags;

import com.veriplace.client.Location;

public class AddressLineFieldTag extends AbstractVeriplaceLocationFieldTag {

   private String separator = ", ";
   private StringBuilder buffer;
   
   public void setSeparator(String s) {
      separator = s;
   }
   
   @Override
   protected Object getValue(Location location) {
      buffer = new StringBuilder();
      addField(location.getStreet(), separator);
      addField(location.getNeighborhood(), separator);
      addField(location.getCity(), separator);
      addField(location.getState(), ", ");
      addField(location.getPostal(), "  ");
      return buffer.toString();
   }   
   
   protected void addField(String s, String sep) {
      if ((s != null) && !s.equals("")) {
         if (buffer.length() > 0) {
            buffer.append(sep);
         }
         buffer.append(s);
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