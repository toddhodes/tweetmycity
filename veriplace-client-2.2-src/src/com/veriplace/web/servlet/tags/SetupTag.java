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

import com.veriplace.client.Client;
import com.veriplace.client.factory.DefaultClientFactory;
import com.veriplace.web.Veriplace;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.servlet.ServletStatusViewRenderer;
import com.veriplace.web.servlet.VeriplaceServletHelper;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.VariableInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SetupTag extends AbstractVeriplaceActionTag {

   private static final Log logger = LogFactory.getLog(SetupTag.class);

   private String viewParamsName;
   private String propertiesId;
   private String veriplaceUrl;
   private Boolean useHttps;
   private String consumerKey;
   private String consumerSecret;
   
   @Override
   protected VeriplaceState getVeriplaceState() throws JspException {
      try {
         Veriplace veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(pageContext.getServletContext());
         logger.debug("Got shared veriplace instance: " + veriplace);
         Client customClient = getCustomClient(veriplace);
         logger.debug("Got custom client: " + customClient);
         if ((customClient != null) || (viewParamsName != null)) {
            if (customClient == null) {
               logger.debug("Creating new veriplace instance for custom client");
               veriplace = new Veriplace(veriplace, veriplace.getClient());
            } else {
               logger.debug("Creating new veriplace instance for view params:" + viewParamsName);
               veriplace = new Veriplace(veriplace, customClient);
            }
         }
         if (viewParamsName != null) {
            logger.debug("Setting view params for veriplace instance");
            ServletStatusViewRenderer vr = VeriplaceServletHelper.getViewRendererFromViewParams(pageContext.getServletContext(), viewParamsName);
            veriplace.setStatusViewRenderer(vr);
         }
         VeriplaceState state = veriplace.open((HttpServletRequest) pageContext.getRequest(),
                                               (HttpServletResponse) pageContext.getResponse());
         logger.debug("Initialized veriplace state: " + state);
         return state;
      }
      catch (Exception e) {
         logger.debug("Unable to get veriplace state: " + e);
         throw new JspException(e);
      }
   }
   
   @Override
   protected boolean handleTagInternal() throws Exception {
      if (propertiesId != null) {
         pageContext.setAttribute(propertiesId,
               VeriplaceServletHelper.getSharedVeriplaceProperties(pageContext.getServletContext()));
      }
      return true;
   }
   
   @Override
   protected Object getResultObject() {
      return veriplaceState;
   }

   public void setConsumerKey(String consumerKey) {
      this.consumerKey = consumerKey;
   }

   public void setConsumerSecret(String consumerSecret) {
      this.consumerSecret = consumerSecret;
   }

   public void setVeriplaceUrl(String veriplaceUrl) {
      this.veriplaceUrl = veriplaceUrl;
   }

   public void setUseHttps(Boolean useHttps) {
      this.useHttps = useHttps;
   }

   public void setViewparams(String viewParamsName) {
      this.viewParamsName = viewParamsName;
   }
   
   public void setPropertiesId(String propertiesId) {
      this.propertiesId = propertiesId;
   }
  
   protected Client getCustomClient(Veriplace veriplace) throws Exception {
      if ((veriplaceUrl == null) && (consumerKey == null) && (consumerSecret == null)) {
         return null;
      }
      Properties props = (Properties)veriplace.getProperties().clone();
      if (consumerKey != null) {
         props.setProperty(DefaultClientFactory.CONSUMER_KEY, consumerKey);
      }
      if (consumerSecret != null) {
         props.setProperty(DefaultClientFactory.CONSUMER_SECRET, consumerSecret);
      }
      if (veriplaceUrl != null) {
         props.setProperty(DefaultClientFactory.SERVER_URI, veriplaceUrl);
      }
      if (useHttps != null) {
         props.setProperty(DefaultClientFactory.SECURE, String.valueOf(useHttps));
      }
      return new DefaultClientFactory(props).getClient();
   }

   public static class ExtraInfo extends AbstractVeriplaceActionTag.ExtraInfo {
      
      @Override
      public VariableInfo[] getVariableInfo(TagData data) {
         String id = data.getAttributeString("id");
         String propertiesId = data.getAttributeString("propertiesId");
         VariableInfo v1 = null, v2 = null;
         if (id != null) {
            v1 = new VariableInfo(
                  id,
                  getObjectClass().getName(),
                  true,
                  VariableInfo.AT_END);
         }
         if (propertiesId != null) {
            v2 = new VariableInfo(
                  propertiesId,
                  java.util.Properties.class.getName(),
                  true,
                  VariableInfo.AT_END);
         }
         if ((v1 != null) && (v2 != null)) {
            return new VariableInfo[] { v1, v2 };
         }
         if (v1 != null) {
            return new VariableInfo[] { v1 };
         }
         if (v2 != null) {
            return new VariableInfo[] { v2 };
         }
         return new VariableInfo[0];
      }
      
      @Override
      protected Class getObjectClass() {
         return VeriplaceState.class;
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
