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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * A simple status bar component that can display a message or an exceptoin.
 */
public class DemoStatusBar extends JPanel {

   private static final Color MESSAGE_COLOR = Color.blue;
   private static final Color ERROR_COLOR = Color.red;

   private JFrame owner;
   private JLabel messageLabel;
   private JLabel detailLabel;
   private Exception exception;
   
   public DemoStatusBar(JFrame owner) {
      this.owner = owner;
      createLayout();
   }
   
   public void reset() {
      messageLabel.setText(" ");
      detailLabel.setVisible(false);
   }
   
   public void setMessage(String text) {
      exception = null;
      messageLabel.setText(text);
      messageLabel.setForeground(MESSAGE_COLOR);
      detailLabel.setVisible(false);
      this.invalidate();
      this.repaint();
   }
   
   public void setError(Exception error) {
      exception = error;
      messageLabel.setText(error.getClass().getSimpleName());
      messageLabel.setForeground(ERROR_COLOR);
      detailLabel.setText(error.getMessage());
      detailLabel.setVisible(true);
      this.invalidate();
      this.repaint();
   }
   
   private void showErrorDetails() {
      if (exception == null) {
         return;
      }
      ErrorWindow w = new ErrorWindow(owner);
      w.setException(exception);
      w.setVisible(true);
   }
   
   private void createLayout() {
      setLayout(new FlowLayout(FlowLayout.LEFT));
      messageLabel = new JLabel(" ");
      detailLabel = new JLabel();
      add(messageLabel);
      add(detailLabel);
      setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      
      MouseListener clickToShowDetails = new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent arg0) {
            showErrorDetails();
         }
      };
      messageLabel.addMouseListener(clickToShowDetails);
      detailLabel.addMouseListener(clickToShowDetails);
   }
}
