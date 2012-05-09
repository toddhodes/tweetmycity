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

import com.veriplace.web.Veriplace;
import com.veriplace.web.VeriplaceState;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractVeriplaceTag extends TagSupport {

   private static final Log logger = LogFactory.getLog(AbstractVeriplaceTag.class);
   
   protected Veriplace veriplace;
   protected VeriplaceState veriplaceState;

   protected void initVeriplaceState() throws JspException {
      veriplaceState = getVeriplaceState();
      veriplace = veriplaceState.getVeriplace();
  }
   
   protected VeriplaceState getVeriplaceState() throws JspException {
      if (veriplace != null) {
         VeriplaceState state = veriplace.open((HttpServletRequest) pageContext.getRequest(),
                                               (HttpServletResponse) pageContext.getResponse());
         if (state == null) {
            throw new IllegalStateException("Veriplace setup was not defined");
         }
         return state;
      }
      return VeriplaceState.getFromRequest((HttpServletRequest) pageContext.getRequest());
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
