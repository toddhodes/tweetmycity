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
package com.veriplace.web.servlet;

import static org.junit.Assert.*;

import com.veriplace.client.ConfigurationException;
import com.veriplace.client.TestData;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.factory.DefaultClientFactory;
import com.veriplace.web.Veriplace;

import java.util.Properties;

import javax.servlet.http.HttpServlet;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class TestVeriplaceServletHelper implements TestData {

   private static final String PROPERTIES_FILE_NAME = "etc/servlet-test.properties";
   
   private MockServlet servlet;
   private MockServletConfig config;
   private MockServletContext context;
   
   @Before
   public void setUp() throws Exception {
      context = new MockServletContext();
      servlet = new MockServlet();
      config = new MockServletConfig(context, "servlet");
      servlet.init(config);
   }

   @Test
   public void testCreateFromPropertiesFilenameInContextParam() throws Exception {
      context.addInitParameter("veriplace.properties-file", PROPERTIES_FILE_NAME);
      
      Veriplace veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(context);
      validateInstance(veriplace);
   }
   
   @Test
   public void testCreateFromPropertiesFilenameSpecified() throws Exception {
      Veriplace veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(context,
            PROPERTIES_FILE_NAME);
      validateInstance(veriplace);
   }
   
   @Test
   public void testCreateFromProperties() throws Exception {
      Properties p = new Properties();
      p.setProperty(DefaultClientFactory.CONSUMER_KEY, CONSUMER_KEY);
      p.setProperty(DefaultClientFactory.CONSUMER_SECRET, CONSUMER_SECRET);
      p.setProperty(DefaultClientFactory.SERVER_URI, "http://veriplace.com");

      Veriplace veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(context, p);
      validateInstance(veriplace);
   }
   
   @Test
   public void testGetSharedProperties() throws Exception {
      Properties p = new Properties();
      p.setProperty(DefaultClientFactory.CONSUMER_KEY, CONSUMER_KEY);
      p.setProperty(DefaultClientFactory.CONSUMER_SECRET, CONSUMER_SECRET);
      p.setProperty(DefaultClientFactory.SERVER_URI, "http://veriplace.com");

      try {
         // We haven't loaded the properties yet
         VeriplaceServletHelper.getSharedVeriplaceProperties(context);
         fail("expected ConfigurationException");
      }
      catch (ConfigurationException e) {
      }
      
      Veriplace veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(context, p);
      validateInstance(veriplace);
      
      Properties p2 = VeriplaceServletHelper.getSharedVeriplaceProperties(context);
      assertNotNull(p2);
      assertEquals(p, p2);
   }
   
   @Test
   public void testReuseSharedInstance() throws Exception {
      Properties p = new Properties();
      p.setProperty(DefaultClientFactory.CONSUMER_KEY, CONSUMER_KEY);
      p.setProperty(DefaultClientFactory.CONSUMER_SECRET, CONSUMER_SECRET);
      p.setProperty(DefaultClientFactory.SERVER_URI, "http://veriplace.com");

      Veriplace veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(context, p);
      validateInstance(veriplace);
      
      Veriplace veriplace2 = VeriplaceServletHelper.getSharedVeriplaceInstance(context, p);
      assertSame(veriplace, veriplace2);
   }

   @Test
   public void testCreateForJSP() throws Exception {
      Veriplace veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(servlet,
            PROPERTIES_FILE_NAME, null);      
      validateInstance(veriplace);
      
      Veriplace veriplace2 = VeriplaceServletHelper.getSharedVeriplaceInstance(context,
            PROPERTIES_FILE_NAME);
      assertSame(veriplace, veriplace2);
   }
   
   @Test
   public void testCreateForJSPWithViewPrefix() throws Exception {
      String prefix = "/WEB-INF/boo/";
      Veriplace veriplace = VeriplaceServletHelper.getSharedVeriplaceInstance(servlet,
            PROPERTIES_FILE_NAME, prefix);
      validateInstance(veriplace);
      assertEquals(prefix, ((ServletStatusViewRenderer) veriplace.getStatusViewRenderer()).getViewPrefix());
      
      Veriplace veriplace2 = VeriplaceServletHelper.getSharedVeriplaceInstance(context,
            PROPERTIES_FILE_NAME);
      assertSame(veriplace, veriplace2);
   }
   
   @Test
   public void testGetViewRendererFromViewParams() throws Exception {
      String prefix = "/WEB-INF/Foo";
      String suffix = ".html";
      String waitingView = "hurryup";
      String errorView = "human";
      String userErrorView = "fail";
      
      context.addInitParameter("veriplace.views.foo.prefix", prefix);
      context.addInitParameter("veriplace.views.foo.suffix", suffix);
      context.addInitParameter("veriplace.views.foo.waiting", waitingView);
      context.addInitParameter("veriplace.views.foo.error", errorView);
      context.addInitParameter("veriplace.views.foo.error.UserDiscovery", userErrorView);
      
      ServletStatusViewRenderer ssvr = VeriplaceServletHelper.getViewRendererFromViewParams(context, "foo");
      assertNotNull(ssvr);
      assertEquals(prefix, ssvr.getViewPrefix());
      assertEquals(suffix, ssvr.getViewSuffix());
      assertEquals(waitingView, ssvr.getWaitingViewName());
      assertEquals(errorView, ssvr.getDefaultErrorViewName());
      assertEquals(userErrorView, ssvr.getErrorViewName(UserDiscoveryException.class));
   }
   
   protected void validateInstance(Veriplace veriplace) {
      assertNotNull(veriplace);
      assertEquals(CONSUMER_KEY, veriplace.getClient().getConsumer().getConsumerKey());
      assertEquals(CONSUMER_SECRET, veriplace.getClient().getConsumer().getConsumerSecret());
      assertNotNull(veriplace.getStatusViewRenderer());
      assertEquals(ServletStatusViewRenderer.class, veriplace.getStatusViewRenderer().getClass());
   }
   
   static class MockServlet extends HttpServlet {
      public MockServlet() {
         
      }
   }
}
