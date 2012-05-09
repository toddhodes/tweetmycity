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

import com.veriplace.client.GetLocationNotPermittedException;
import com.veriplace.client.Location;
import com.veriplace.client.PositionFailureException;
import com.veriplace.client.UserDiscoveryNotPermittedException;
import com.veriplace.client.VeriplaceOAuthException;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A window that displays details of a Veriplace exception.
 */
public class ErrorWindow extends JFrame {

   private JFrame parent;
   private JLabel typeLabel;
   private JLabel messageLabel;
   private JLabel statusCodeLabel;
   private JLabel oauthDetailsLabel;
   private JButton cachedLocationButton;
   private Location cachedLocation;
   
   public ErrorWindow(JFrame parent) {
      this.parent = parent;
      createLayout();
   }
   
   public void setException(Exception exception) {
      typeLabel.setText(exception.getClass().getName());
      messageLabel.setText(exception.getMessage());
      showDetails(exception);
      if (exception instanceof PositionFailureException) {
         cachedLocation = ((PositionFailureException) exception).getCachedLocation();
      }
      else {
         cachedLocation = null;
      }
      cachedLocationButton.setVisible(cachedLocation != null);
      this.pack();
      this.setLocationRelativeTo(parent);
   }
   
   private void showDetails(Exception e) {
      if (e instanceof VeriplaceOAuthException) {
         VeriplaceOAuthException oe = (VeriplaceOAuthException) e;
         statusCodeLabel.setText(String.valueOf(oe.getCode()));
         if (oe.getCause().getProblem() == null) {
            oauthDetailsLabel.setText(oe.getCause().getMessage());
         }
         else {
            oauthDetailsLabel.setText(oe.getCause().getProblem().toString());
         }
      }
      else if (e instanceof GetLocationNotPermittedException) {
         showDetails(((GetLocationNotPermittedException) e).getCause());
      }
      else if (e instanceof UserDiscoveryNotPermittedException) {
         showDetails(((UserDiscoveryNotPermittedException) e).getCause());
      }
      else {
         statusCodeLabel.setText("n/a");
         oauthDetailsLabel.setText("n/a");
      }
   }
   
   public void createLayout() {
      typeLabel = new JLabel();
      messageLabel = new JLabel();
      statusCodeLabel = new JLabel();
      oauthDetailsLabel = new JLabel();
      cachedLocationButton = new JButton();
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new GridBagLayout());
      getContentPane().add(mainPanel);
      
      GridBagConstraints gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.anchor = GridBagConstraints.EAST;
      gc.insets = new Insets(10, 10, 10, 10);
      String[] captions = new String[] {
            "Type:", "Message:", "Status Code:", "OAuth Details:"
      };
      for (String caption: captions) {
         mainPanel.add(new JLabel(caption), gc);
         gc.gridy++;
         gc.insets.top = 0;
      }
      
      gc.gridx = 1;
      gc.gridy = 0;
      gc.anchor = GridBagConstraints.WEST;
      gc.insets = new Insets(10, 10, 10, 10);
      
      mainPanel.add(typeLabel, gc);
      gc.gridy++;
      gc.insets.top = 0;
      mainPanel.add(messageLabel, gc);
      gc.gridy++;
      mainPanel.add(statusCodeLabel, gc);
      gc.gridy++;
      mainPanel.add(oauthDetailsLabel, gc);
      
      gc.gridx = 2;
      mainPanel.add(cachedLocationButton, gc);
      cachedLocationButton.setVisible(false);
      
      this.setTitle("Veriplace Error Details");
   }
}
