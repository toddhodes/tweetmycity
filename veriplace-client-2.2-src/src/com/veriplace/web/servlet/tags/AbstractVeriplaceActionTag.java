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

import com.veriplace.client.VeriplaceException;
import com.veriplace.web.RespondedException;
import com.veriplace.web.WaitingException;
import com.veriplace.web.views.StatusViewException;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractVeriplaceActionTag extends AbstractVeriplaceTag {

   private static final Log logger = LogFactory.getLog(AbstractVeriplaceActionTag.class);
   
   protected String id = null;
   private boolean terminatedRequest;
   
   @Override
   public int doStartTag() throws JspException {
      logger.debug("Starting tag: " + this.getClass().getSimpleName());
      terminatedRequest = false;
      try {
         initVeriplaceState();
         if (handleTagInternal()) {
            if (id != null) {
               pageContext.setAttribute(id, getResultObject());
            }
         }
         else {
            terminatedRequest = true;
         }
      }
      catch (RespondedException e) {
         terminatedRequest = true;
      }
      catch (WaitingException e) {
         // unexpected: should be handled within Veriplace for interactive requests
         // and should not be thrown for non-interactive ones
         terminatedRequest = true;
      }
      catch (VeriplaceException e) {
         try {
            // render error page
            veriplace.getStatusViewRenderer().renderErrorView(veriplaceState.getRequest(),
                                                              veriplaceState.getResponse(),
                                                              veriplaceState,
                                                              e);
         } catch (StatusViewException sve) {
            // expected
         } catch (ServletException se) {
            throw new JspException(se);
         }
         terminatedRequest = true;
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

/*
** Local Variables:
**   c-basic-offset: 3
**   tab-width: 3
**   indent-tabs-mode: nil
** End:
**
** ex: set softtabstop=3 tabstop=3 expandtab cindent shiftwidth=3
*/
