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

import com.veriplace.client.Client;
import com.veriplace.client.factory.ClientFactory;
import com.veriplace.web.VeriplaceContext;
import com.veriplace.web.VeriplaceState;
import com.veriplace.web.servlet.ServletStatusHandler;
import com.veriplace.web.servlet.VeriplaceServletHelper;

import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

public class SetupTag extends AbstractVeriplaceTag {

   private String id;
   private String viewParamsName;
   private String veriplaceUrl;
   private Boolean useHttps;
   private String consumerKey;
   private String consumerSecret;
   
   @Override
   protected VeriplaceState getVeriplaceState() throws Exception {
      VeriplaceContext veriplaceContext = VeriplaceServletHelper.getSharedVeriplaceContext(
            pageContext.getServletContext());
      Client client = getCustomClient(veriplaceContext);
      VeriplaceState state = veriplaceContext.useRequest((HttpServletRequest) pageContext.getRequest(),
            (HttpServletResponse) pageContext.getResponse(), client);
      if (viewParamsName != null) {
         ServletStatusHandler sh = VeriplaceServletHelper.getStatusHandlerFromViewParams(
               pageContext.getServletContext(), viewParamsName);
         state.setStatusHandler(sh);
      }
      return state;
   }
   
   @Override
   protected boolean handleTagInternal() throws Exception {
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
  
   protected Client getCustomClient(VeriplaceContext context) throws Exception {
      if ((veriplaceUrl == null) && (consumerKey == null) && (consumerSecret == null)) {
         return null;
      }
      Properties props = (Properties) context.getProperties().clone();
      if (consumerKey != null) {
         props.setProperty(ClientFactory.CONSUMER_KEY_PROPERTY, consumerKey);
      }
      if (consumerSecret != null) {
         props.setProperty(ClientFactory.CONSUMER_SECRET_PROPERTY, consumerSecret);
      }
      if (veriplaceUrl != null) {
         props.setProperty(ClientFactory.VERIPLACE_URL_PROPERTY, veriplaceUrl);
      }
      if (useHttps != null) {
         props.setProperty(ClientFactory.VERIPLACE_HTTPS_PROPERTY, String.valueOf(useHttps));
      }
      return new ClientFactory().getClient(props);
   }
   
   public static class ExtraInfo extends AbstractVeriplaceTag.ExtraInfo {

      @Override
      protected Class getObjectClass() {
         return VeriplaceState.class;
      }
   }
}
