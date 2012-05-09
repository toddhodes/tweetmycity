/* Copyright 2010 WaveMarket, Inc.
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
package com.veriplace.demo;

import com.veriplace.client.ConfigurationException;
import com.veriplace.client.User;
import com.veriplace.demo.model.DemoUser;
import com.veriplace.demo.persist.DemoUserManager;
import com.veriplace.web.Veriplace;
import com.veriplace.web.servlet.ServletStatusViewRenderer;
import com.veriplace.web.servlet.VeriplaceServletHelper;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;

/**
 * Maintains the global state of the application, including the Veriplace client
 * object, the user list, and the trial period settings.
 */
public class Application {
   
   private static Application instance;
   private static final long MILLISECONDS_PER_HOUR = 60 * 60 * 1000;

   private DemoUserManager demoUserManager;
   private Veriplace veriplace;
   private int maxLocatesPerUser;
   private int maxDaysToLocate;
   private long maxMillisecondsToLocate;
   
   /**
    * Returns (or creates) the single static instance of this class.
    */
   public static Application getInstance(ServletContext context)
         throws IOException, ConfigurationException {
      
      synchronized(Application.class) {
         if (instance == null) {
            instance = new Application(context);
         }
      }
      return instance;
   }
   
   /**
    * Constructs the application object, using the servlet context to get
    * configuration settings from <tt>web.xml</tt>.  This will be called during
    * application startup, when the first servlet is loaded.
    */
   protected Application(ServletContext context)
         throws IOException, ConfigurationException {
      
      demoUserManager = new DemoUserManager();

      // Initialize global settings from context-params in web.xml.
      maxDaysToLocate = Integer.parseInt(
            context.getInitParameter("maxDaysToLocate"));
      maxLocatesPerUser = Integer.parseInt(
            context.getInitParameter("maxLocatesPerUser"));
      maxMillisecondsToLocate = maxDaysToLocate * 24 * MILLISECONDS_PER_HOUR;

      // Get properties filename from context-param; configure Veriplace.
      String propertiesFileName = context.getInitParameter("veriplacePropertiesFile");
      veriplace = new Veriplace(propertiesFileName);
      
      // Configure the Veriplace web client to use our "please wait" JSP.
      // By default, the ServletStatusViewRenderer class looks for files in
      // /WEB-INF/jsp/ and adds a .jsp suffix.  See the comments in wait.jsp
      // on how this page is used.
      ServletStatusViewRenderer ssvr = new ServletStatusViewRenderer();
      ssvr.setWaitingViewName("wait");
      veriplace.setStatusViewRenderer(ssvr);
   }

   /**
    * Returns the object that stores the user list.
    */
   public DemoUserManager getDemoUserManager() {
      return demoUserManager;
   }

   /**
    * Returns the Veriplace web client object.
    */
   public Veriplace getVeriplace() {
      return veriplace;
   }
   
    /**
    * Returns the maximum number of times a particular user is allowed to use
    * this application again.
    */
   public int getRemainingLocates(DemoUser demoUser) {
      int remainingLocates = maxLocatesPerUser - demoUser.getLocateCount();
      if (remainingLocates < 0) {
         return 0;
      }
      if (demoUser.getFirstLocateTime() != null) {
         if ((System.currentTimeMillis() - demoUser.getFirstLocateTime().getTime())
               >= maxMillisecondsToLocate) {
            return 0;
         }
      }
      return remainingLocates;
   }
   
   /**
    * Returns the number of hours (rounding up) that remain for a particular
    * user to do additional location requests.
    */
   public int getRemainingHours(DemoUser demoUser) {
      long millisLeft = maxMillisecondsToLocate -
            (System.currentTimeMillis() - demoUser.getFirstLocateTime().getTime());
      if (millisLeft < 0) {
         return 0;
      }
      return (int) ((millisLeft + MILLISECONDS_PER_HOUR - 1)
            / MILLISECONDS_PER_HOUR);
   }
}
