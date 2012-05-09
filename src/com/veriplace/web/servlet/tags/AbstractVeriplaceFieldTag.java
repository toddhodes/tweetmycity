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
package com.veriplace.web.servlet.tags;

import com.veriplace.web.VeriplaceState;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

abstract class AbstractVeriplaceFieldTag extends TagSupport {

   protected VeriplaceState veriplaceState;
   
   @Override
   public int doStartTag() throws JspException {
      veriplaceState = VeriplaceState.getFromRequest(pageContext.getRequest());
      if (veriplaceState == null) {
         throw new IllegalStateException("Veriplace setup was not defined");
      }
      Object value = getValue();
      if (value != null) {
         try {
            pageContext.getOut().write(String.valueOf(value));
         }
         catch (IOException e) {
            throw new JspException(e);
         }
      }
      return SKIP_BODY;
   }

   protected abstract Object getValue();
}
