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
package com.veriplace.example.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * A Swing component that displays Commons Logging messages.  Nothing to do with
 * Veriplace really, but may be helpful in debugging.
 */
public class LogPanel extends JPanel {

   private static final int MAX_BUFFER_SIZE = 10000;
   private static final int BUFFER_CHUNK_SIZE = 500;
   private static LogPanel instance;
   private static int[] logLevels = {
      SimpleLog.LOG_LEVEL_ERROR,
      SimpleLog.LOG_LEVEL_WARN,
      SimpleLog.LOG_LEVEL_INFO,
      SimpleLog.LOG_LEVEL_DEBUG
   };
   private static String[] logLevelNames = {
      "ERROR",
      "WARN",
      "INFO",
      "DEBUG"
   };
   
   private JComboBox logLevelComboBox;
   private JTextArea logTextArea;
   private int logLevel;
   
   public LogPanel() {
      logLevel = SimpleLog.LOG_LEVEL_INFO;
      createLayout();
   }
   
   public static LogPanel getInstance() {
      synchronized (LogPanel.class) {
         return instance;
      }
   }
   
   public void useForCommonsLogging() {
      synchronized (LogPanel.class) {
         instance = this;
         LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
               LogPanelLogAdapter.class.getName());
      }
   }
   
   protected synchronized int getLogLevel() {
      return logLevel;
   }
   
   protected synchronized void setLogLevel(int logLevel) {
      this.logLevel = logLevel;
   }
   
   protected synchronized void write(String line) {
      logTextArea.append(line);
      logTextArea.append("\n");
      logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
      if (logTextArea.getDocument().getLength() > MAX_BUFFER_SIZE) {
         try {
            logTextArea.getDocument().remove(0, BUFFER_CHUNK_SIZE);
         }
         catch (Exception e) {
         }
      }
   }
   
   private void createLayout() {
      logTextArea = new JTextArea();
      logLevelComboBox = new JComboBox();
      
      this.setLayout(new BorderLayout());
      
      JPanel topPanel = new JPanel();
      topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
      this.add(topPanel, BorderLayout.NORTH);
      topPanel.add(new JLabel("Log Level:"));
      
      for (int i = 0; i < logLevels.length; i++) {
         logLevelComboBox.addItem(logLevelNames[i]);
         if (logLevels[i] == logLevel) {
            logLevelComboBox.setSelectedIndex(i);
         }
      }
      logLevelComboBox.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            int i = logLevelComboBox.getSelectedIndex();
            if (i >= 0) {
               setLogLevel(logLevels[i]);
            }
         }
      });
      topPanel.add(logLevelComboBox);
      
      logTextArea.setEditable(false);
      logTextArea.setLineWrap(true);
      JScrollPane scroller = new JScrollPane(logTextArea,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      this.add(scroller, BorderLayout.CENTER);
   }
   
   public static class LogPanelLogAdapter extends SimpleLog {

      public LogPanelLogAdapter(String name) {
         super(name);
         this.setLevel(LOG_LEVEL_ALL);
      }

      @Override
      protected void log(int type, Object message, Throwable t) {
         LogPanel lp = LogPanel.getInstance();
         if (lp != null) {
            if (lp.getLogLevel() > type) {
               return;
            }
         }
         super.log(type, message, t);
      }
      
      @Override
      protected void write(StringBuffer buffer) {
         LogPanel lp = LogPanel.getInstance();
         if (lp != null) {
            lp.write(buffer.toString());
         }
      }
   }
}
