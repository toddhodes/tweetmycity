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

import com.veriplace.client.User;
import com.veriplace.client.VeriplaceException;
import com.veriplace.oauth.consumer.Token;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A window for viewing or editing a user ID and performing requests with it.
 */
public class UserWindow extends JFrame {

   private ClientWindow parent;
   private DemoStatusBar statusBar;
   private JTextField userIdTextField;
   private JButton getLocationPermissionButton;
   
   public UserWindow(ClientWindow parent) {
      this.parent = parent;
      createLayout();
   }
   
   public void doGetLocationPermission() {
      User user = getUser();
      if (user == null) {
         return;
      }
      statusBar.setMessage("Requesting permission...");
      try {
         Token token = parent.getClient().getGetLocationAPI().getLocationAccessToken(user);
         statusBar.reset();
         TokenWindow w = new TokenWindow(parent);
         w.setToken(token);
         w.setUser(user);
         w.setVisible(true);
      }
      catch (VeriplaceException e) {
         statusBar.setError(e);
      }
   }
   
   public User getUser() {
      String idStr = userIdTextField.getText().trim();
      if (idStr == "") {
         return null;
      }
      try {
         long id = Long.parseLong(idStr);
         return new User(id);
      }
      catch (NumberFormatException e) {
         return null;
      }
   }
   
   public void setUser(User user) {
      userIdTextField.setText((user == null) ? "" : String.valueOf(user.getId()));
   }
   
   public void createLayout() {
      statusBar = new DemoStatusBar(this);
      JPanel mainPanel = new JPanel();
      userIdTextField = new JTextField();
      getLocationPermissionButton = new JButton();

      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(mainPanel, BorderLayout.CENTER);
      this.getContentPane().add(statusBar, BorderLayout.SOUTH);
      mainPanel.setLayout(new GridBagLayout());
      GridBagConstraints gc;

      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.anchor = GridBagConstraints.EAST;
      gc.insets = new Insets(10, 10, 10, 10);
      mainPanel.add(new JLabel("User ID:"), gc);
      
      gc = new GridBagConstraints();
      gc.gridx = 1;
      gc.gridy = 0;
      gc.anchor = GridBagConstraints.WEST;
      gc.insets = new Insets(10, 0, 10, 10);
      gc.fill = GridBagConstraints.HORIZONTAL;
      gc.ipadx = 180;
      mainPanel.add(userIdTextField, gc);
      
      getLocationPermissionButton.setText("Get Location Permission");
      getLocationPermissionButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            doGetLocationPermission();
         }
      });
      gc = new GridBagConstraints();
      gc.gridx = 0;
      gc.gridy = 2;
      gc.gridwidth = 2;
      gc.insets = new Insets(10, 10, 10, 10);
      mainPanel.add(getLocationPermissionButton, gc);
      
      this.setTitle("Veriplace User");
      this.pack();
      this.setLocationRelativeTo(parent);
   }
}
