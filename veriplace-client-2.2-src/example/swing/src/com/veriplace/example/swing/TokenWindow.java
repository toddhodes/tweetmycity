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

import com.veriplace.client.Client;
import com.veriplace.client.Location;
import com.veriplace.client.LocationMode;
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * A window for viewing or editing an access token and performing requests
 * with it.
 */
public class TokenWindow extends JFrame {

   private ClientWindow parent;
   private DemoStatusBar statusBar;
   private JTextField tokenValueTextField;
   private JTextField tokenSecretTextField;
   private JButton verifyButton;
   private JButton deleteButton;
   private JTextField userIdTextField;
   private JComboBox locationModeComboBox;
   private JTextField timeoutTextField;
   private JButton getLocationButton;
   private JTextField locationIdTextField;
   private JButton getLocationByIdButton;
   
   public TokenWindow(ClientWindow parent) {
      this.parent = parent;
      createLayout();
   }
   
   public Token getToken() {
      String value = tokenValueTextField.getText().trim();
      if (value.equals("")) {
         return null;
      }
      String secret = tokenSecretTextField.getText().trim();
      return new Token(value, secret);
   }
   
   public void setToken(Token token) {
      String value, secret;
      if (token == null) {
         value = secret = "";
      }
      else {
         value = token.getToken();
         secret = token.getTokenSecret();
      }
      tokenValueTextField.setText(value);
      tokenSecretTextField.setText(secret);
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
      String idStr = (user == null) ? "" : String.valueOf(user.getId());
      userIdTextField.setText(idStr);
   }
   
   public void doVerify() {
      Token token = getToken();
      Client client = parent.getClient();
      if ((token == null) || (client == null)) {
         return;
      }
      try {
         boolean success = client.getPermissionAPI().verify(token);
         if (success) {
            statusBar.setMessage("The token is valid");
         }
         else {
            statusBar.setMessage("The token is not valid");
         }
      }
      catch (VeriplaceException e) {
         statusBar.setError(e);
      }
   }
   
   public void doDelete() {
      Token token = getToken();
      Client client = parent.getClient();
      if ((token == null) || (client == null)) {
         return;
      }
      try {
         client.getPermissionAPI().delete(token);
         statusBar.setMessage("The token has been deleted");
      }
      catch (VeriplaceException e) {
         statusBar.setError(e);
      }
   }
   
   public void doGetLocation() {
      Token token = getToken();
      User user = getUser();
      Client client = parent.getClient();
      if ((token == null) || (user == null) || (client == null)) {
         return;
      }
      String mode = locationModeComboBox.getSelectedItem().toString();
      Integer timeout = null;
      String timeoutStr = timeoutTextField.getText().trim();
      if (! timeoutStr.equals("")) {
         try {
            timeout = Integer.parseInt(timeoutStr);
         }
         catch (NumberFormatException e) {
         }
      }
      try {
         statusBar.setMessage("Locating...");
         Location location = client.getGetLocationAPI().getLocation(token, user,
               mode, timeout);
         statusBar.reset();
         LocationWindow w = new LocationWindow(parent);
         w.setLocation(location);
         w.setVisible(true);
      }
      catch (VeriplaceException e) {
         statusBar.setError(e);
      }
   }
   
   public void doGetLocationById() {
      Token token = getToken();
      User user = getUser();
      Client client = parent.getClient();
      if ((token == null) || (client == null)) {
         return;
      }
      long locationId = 0;
      String locationIdStr = locationIdTextField.getText().trim();
      if (! locationIdStr.equals("")) {
         try {
            locationId = Long.parseLong(locationIdStr);
         }
         catch (NumberFormatException e) {
            return;
         }
      }
      try {
         statusBar.setMessage("Locating...");
         Location location = client.getGetLocationAPI().getLocationById(token,
               user, locationId);
         statusBar.reset();
         LocationWindow w = new LocationWindow(parent);
         w.setLocation(location);
         w.setVisible(true);
      }
      catch (VeriplaceException e) {
         statusBar.setError(e);
      }
   }
   
   public void createLayout() {
      statusBar = new DemoStatusBar(this);
      tokenValueTextField = new JTextField();
      tokenSecretTextField = new JTextField();
      verifyButton = new JButton();
      deleteButton = new JButton();
      userIdTextField = new JTextField();
      locationModeComboBox = new JComboBox();
      timeoutTextField = new JTextField();
      getLocationButton = new JButton();
      locationIdTextField = new JTextField();
      getLocationByIdButton = new JButton();

      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(statusBar, BorderLayout.SOUTH);

      JPanel mainPanel = new JPanel();
      this.getContentPane().add(mainPanel, BorderLayout.CENTER);
      mainPanel.setLayout(new GridBagLayout());
      GridBagConstraints gc;

      JPanel tokenPanel = new JPanel();
      tokenPanel.setLayout(new GridBagLayout());
      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.insets = new Insets(10, 10, 10, 10);
      gc.anchor = GridBagConstraints.NORTH;
      gc.fill = GridBagConstraints.BOTH;
      mainPanel.add(tokenPanel, gc);

      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.anchor = GridBagConstraints.EAST;
      gc.insets = new Insets(10, 10, 0, 10);
      tokenPanel.add(new JLabel("Token Value:"), gc);
      gc.gridy++;
      tokenPanel.add(new JLabel("Token Secret:"), gc);
      
      gc = new GridBagConstraints();
      gc.gridx = 1;
      gc.gridy = 0;
      gc.anchor = GridBagConstraints.WEST;
      gc.insets = new Insets(10, 0, 0, 10);
      gc.ipadx = 200;
      tokenPanel.add(tokenValueTextField, gc);
      gc.gridy++;
      tokenPanel.add(tokenSecretTextField, gc);
      gc.gridy++;
      gc.ipadx = 0;
      
      verifyButton.setText("Verify This Token");
      verifyButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            doVerify();
         }
      });
      tokenPanel.add(verifyButton, gc);
      
      deleteButton.setText("Delete This Token");
      deleteButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            doDelete();
         }
      });
      gc.gridy++;
      tokenPanel.add(deleteButton, gc);
      
      JPanel getLocationPanel = new JPanel();
      getLocationPanel.setBorder(new TitledBorder("Get Location"));
      getLocationPanel.setLayout(new GridBagLayout());
      gc = new GridBagConstraints();
      gc.gridx = 0;
      gc.gridy = 1;
      gc.insets = new Insets(0, 10, 0, 10);
      gc.anchor = GridBagConstraints.NORTH;
      gc.fill = GridBagConstraints.BOTH;
      mainPanel.add(getLocationPanel, gc);

      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.anchor = GridBagConstraints.EAST;
      gc.insets = new Insets(10, 10, 10, 10);
      getLocationPanel.add(new JLabel("For User ID:"), gc);
      gc.gridy++;
      gc.insets.top = 0;
      getLocationPanel.add(new JLabel("Mode:"), gc);
      gc.gridy++;
      getLocationPanel.add(new JLabel("Timeout:"), gc);

      gc = new GridBagConstraints();
      gc.gridx = 1;
      gc.gridy = 0;
      gc.anchor = GridBagConstraints.WEST;
      gc.insets = new Insets(10, 0, 10, 10);
      
      gc.ipadx = 180;
      gc.gridwidth = 2;
      getLocationPanel.add(userIdTextField, gc);
      gc.gridy++;
      gc.ipadx = 0;
      gc.insets.top = 0;
      
      locationModeComboBox.addItem("");
      locationModeComboBox.addItem(LocationMode.ZOOM);
      locationModeComboBox.addItem(LocationMode.AREA);
      locationModeComboBox.addItem(LocationMode.FREEDOM);
      getLocationPanel.add(locationModeComboBox, gc);
      gc.gridy++;
      
      gc.ipadx = 40;
      gc.gridwidth = 1;
      getLocationPanel.add(timeoutTextField, gc);
      gc.gridx++;
      getLocationPanel.add(new JLabel("seconds"), gc);
      gc.gridy++;
      
      getLocationButton.setText("Get Location");
      getLocationButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            doGetLocation();
         }
      });
      gc.gridx = 1;
      gc.gridwidth = 2;
      getLocationPanel.add(getLocationButton, gc);

      JPanel getLocationByIdPanel = new JPanel();
      getLocationByIdPanel.setBorder(new TitledBorder("Get Location"));
      getLocationByIdPanel.setLayout(new GridBagLayout());
      gc = new GridBagConstraints();
      gc.gridx = 0;
      gc.gridy = 2;
      gc.insets = new Insets(0, 10, 10, 10);
      gc.anchor = GridBagConstraints.NORTH;
      gc.fill = GridBagConstraints.BOTH;
      mainPanel.add(getLocationByIdPanel, gc);

      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.anchor = GridBagConstraints.EAST;
      gc.insets = new Insets(10, 10, 10, 10);
      getLocationByIdPanel.add(new JLabel("Location ID:"), gc);

      gc = new GridBagConstraints();
      gc.gridx = 1;
      gc.gridy = 0;
      gc.anchor = GridBagConstraints.WEST;
      gc.insets = new Insets(10, 0, 10, 10);
      
      gc.ipadx = 180;
      gc.gridwidth = 2;
      getLocationByIdPanel.add(locationIdTextField, gc);
      gc.gridy++;
      gc.ipadx = 0;
      gc.insets.top = 0;
      
      getLocationByIdButton.setText("Get Location by Location ID");
      getLocationByIdButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            doGetLocationById();
         }
      });
      getLocationByIdPanel.add(getLocationByIdButton, gc);

      this.setTitle("Veriplace Access Token");
      this.pack();
      this.setLocationRelativeTo(parent);
   }
}
