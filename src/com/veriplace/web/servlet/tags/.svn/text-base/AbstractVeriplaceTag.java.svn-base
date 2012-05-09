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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.VariableInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractVeriplaceTag extends TagSupport {

   private static final Log logger = LogFactory.getLog(AbstractVeriplaceTag.class);

   protected String id = null;
   protected VeriplaceState veriplaceState;
   private boolean terminatedRequest;
   
   @Override
   public int doStartTag() throws JspException {
      terminatedRequest = false;
      try {
         veriplaceState = getVeriplaceState();
         if (handleTagInternal()) {
            if (id != null) {
               pageContext.setAttribute(id, getResultObject());
            }
         }
         else {
            terminatedRequest = true;
         }
      }
      catch (Exception e) {
         throw new JspException(e);
      }
      return SKIP_BODY;
   }

   @Override
   public int doEndTag() throws JspException {
      return terminatedRequest ? SKIP_PAGE : EVAL_PAGE;
   }


   public void setId(String id) {
      this.id = id;
   }

   protected abstract VeriplaceState getVeriplaceState() throws Exception;
   
   protected abstract boolean handleTagInternal() throws Exception;
   
   protected Object getResultObject() {
      return null;
   }
   
   public static abstract class ExtraInfo extends TagExtraInfo {
      
      public VariableInfo[] getVariableInfo(TagData data) {
         String id = data.getAttributeString("id"); 
         if (id == null) {
            return new VariableInfo[0];
         }
         VariableInfo v1 = new VariableInfo(
               id,
               getObjectClass().getName(),
               true,
               VariableInfo.AT_END
               );
         return new VariableInfo[] { v1 };
      }
      
      protected abstract Class getObjectClass();
   }
}
