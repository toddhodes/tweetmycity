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
import com.veriplace.client.ClientConfiguration;
import com.veriplace.client.ConfigurationException;
import com.veriplace.client.User;
import com.veriplace.client.UserDiscoveryParameters;
import com.veriplace.client.VeriplaceException;
import com.veriplace.client.factory.DefaultClientFactory;
import com.veriplace.oauth.consumer.Token;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Main window for the Veriplace Swing demo.  Provides client configuration
 * options and creates the Veriplace Client; provides status display and user
 * lookup.
 */
public class ClientWindow extends JFrame {

   public static String PROPERTIES_FILE_NAME = "veriplace-swing-demo.properties";
   
   private ClientConfiguration configuration;
   private Client client;
   
   private DemoStatusBar statusBar;
   private JTextField serverUriTextField;
   private JCheckBox secureCheckBox;
   private JTextField consumerKeyTextField;
   private JTextField consumerSecretTextField;
   private JTextField appTokenValueTextField;
   private JTextField appTokenSecretTextField;
   private JTextField phoneTextField;
   private JTextField emailTextField;
   private JButton findUserButton;
   private JButton newUserIdButton;
   private JButton newTokenButton;
   private JButton getPermittedUsersButton;
   private LogPanel logPanel;
   
   public ClientWindow(String propertiesFileName) {
      createLayout();
      readDefaultProperties(propertiesFileName);
      logPanel.useForCommonsLogging();
      createClient();
   }
   
   public Client getClient() {
      return client;
   }
   
   public void readDefaultProperties(String propertiesFileName) {
      if (propertiesFileName == null) {
         propertiesFileName = PROPERTIES_FILE_NAME;
      }
      ClientConfiguration defaults = new ClientConfiguration();
      try {
         InputStream is = new FileInputStream(propertiesFileName);
         if (is != null) {
            Properties p = new Properties();
            p.load(is);
            DefaultClientFactory factory = new DefaultClientFactory(p);
            defaults = factory.getClientConfiguration();
         }
      }
      catch (Exception e) {
         System.err.println("Unable to read configuration from " + propertiesFileName
               + ": " + e);
      }
      
      serverUriTextField.setText(defaults.getServerUri());
      secureCheckBox.setSelected((defaults.getSecure() == null) ||
            (defaults.getSecure() == true));
      consumerKeyTextField.setText(defaults.getConsumerKey());
      consumerSecretTextField.setText(defaults.getConsumerSecret());
      if (defaults.getApplicationToken() != null) {
         appTokenValueTextField.setText(
               defaults.getApplicationToken().getToken());
         appTokenSecretTextField.setText(
               defaults.getApplicationToken().getTokenSecret());
      }
      
      configuration = defaults;
   }
   
   public void updateConfiguration() {
      if (configuration == null) {
         return;
      }
      configuration.setServerUri(serverUriTextField.getText());
      configuration.setSecure(secureCheckBox.isSelected());
      configuration.setConsumerKey(consumerKeyTextField.getText());
      configuration.setConsumerSecret(consumerSecretTextField.getText());
      if (appTokenValueTextField.getText().length() == 0) {
         configuration.setApplicationToken(null);
      }
      else {
         configuration.setApplicationToken(new Token(
               appTokenValueTextField.getText(),
               appTokenSecretTextField.getText()));
      }
      createClient();
   }
   
   public boolean createClient() {
      try {
         client = new Client(configuration);
         statusBar.reset();
         return true;
      }
      catch (ConfigurationException e) {
         client = null;
         statusBar.setError(e);
         return false;
      }
   }
   
   private void doFindUser() {
      if (client == null) {
         return;
      }
      statusBar.setMessage("Finding user...");
      UserDiscoveryParameters udp = new UserDiscoveryParameters();
      if (! phoneTextField.getText().equals("")) {
         udp.setPhone(phoneTextField.getText());
      }
      if (! emailTextField.getText().equals("")) {
         udp.setEmail(emailTextField.getText());
      }
      try {
         User user = client.getUserDiscoveryAPI().getUserByParameters(udp);
         statusBar.reset();
         UserWindow w = new UserWindow(this);
         w.setUser(user);
         w.setVisible(true);
      }
      catch (VeriplaceException e) {
         statusBar.setError(e);
      }
   }
   
   private void doNewUserId() {
      UserWindow w = new UserWindow(this);
      w.setVisible(true);
   }
   
   private void doNewToken() {
      TokenWindow w = new TokenWindow(this);
      w.setVisible(true);
   }

   private void doGetPermittedUsers() {
      if (client == null) {
         return;
      }
      statusBar.setMessage("Querying users...");
      try {
         List<User> users = client.getGetLocationAPI().getPermittedUsers();
         statusBar.reset();
         UserListWindow w = new UserListWindow(this);
         w.setUsers(users);
         w.setVisible(true);
      }
      catch (VeriplaceException e) {
         statusBar.setError(e);
      }
   }
   
   private void createLayout() {
      statusBar = new DemoStatusBar(this);
      serverUriTextField = new JTextField();
      secureCheckBox = new JCheckBox();
      consumerKeyTextField = new JTextField();
      consumerSecretTextField = new JTextField();
      appTokenValueTextField = new JTextField();
      appTokenSecretTextField = new JTextField();
      phoneTextField = new JTextField();
      emailTextField = new JTextField();
      findUserButton = new JButton();
      newUserIdButton = new JButton();
      newTokenButton = new JButton();
      getPermittedUsersButton = new JButton();
      logPanel = new LogPanel();
      
      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(statusBar, BorderLayout.SOUTH);
      
      JPanel mainPanel = new JPanel();
      this.getContentPane().add(mainPanel, BorderLayout.CENTER);
      mainPanel.setLayout(new GridBagLayout());
      GridBagConstraints gc;

      JPanel configPanel = new JPanel();
      TitledBorder configPanelBorder = new TitledBorder("Client Configuration");
      configPanel.setBorder(configPanelBorder);
      configPanel.setLayout(new GridBagLayout());
      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.gridheight = 4;
      gc.insets = new Insets(10, 10, 10, 10);
      gc.anchor = GridBagConstraints.NORTHWEST;
      gc.fill = GridBagConstraints.BOTH;
      mainPanel.add(configPanel, gc);

      DocumentListener updateListener = new DocumentListener() {
         public void changedUpdate(DocumentEvent arg0) {
            updateConfiguration();
         }
         public void insertUpdate(DocumentEvent arg0) {
            updateConfiguration();
         }
         public void removeUpdate(DocumentEvent arg0) {
            updateConfiguration();
         }
      };
      
      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.insets = new Insets(10, 10, 10, 10);
      gc.anchor = GridBagConstraints.EAST;
      configPanel.add(new JLabel("Server URI:"), gc);
      gc.gridy += 2;
      gc.insets.top = 0;
      configPanel.add(new JLabel("Consumer Key:"), gc);
      gc.gridy++;
      configPanel.add(new JLabel("Consumer Secret:"), gc);
      gc.gridy++;
      configPanel.add(new JLabel("Application Token:"), gc);
      gc.gridy++;
      configPanel.add(new JLabel("Token Secret:"), gc);
      gc = new GridBagConstraints();
      gc.gridx = 1;
      gc.gridy = 0;
      gc.anchor = GridBagConstraints.WEST;
      gc.insets = new Insets(10, 0, 10, 10);
      gc.fill = GridBagConstraints.HORIZONTAL;
      gc.ipadx = 140;
      
      serverUriTextField.getDocument().addDocumentListener(updateListener);
      configPanel.add(serverUriTextField, gc);
      gc.gridy++;
      gc.insets.top = 0;
      
      secureCheckBox.setText("Secure");
      secureCheckBox.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent arg0) {
            updateConfiguration();
         }
      });
      gc.ipadx = 0;
      configPanel.add(secureCheckBox, gc);
      gc.gridy++;
      gc.ipadx = 150;
      
      consumerKeyTextField.getDocument().addDocumentListener(updateListener);
      configPanel.add(consumerKeyTextField, gc);
      gc.gridy++;
      
      consumerSecretTextField.getDocument().addDocumentListener(updateListener);
      configPanel.add(consumerSecretTextField, gc);
      gc.gridy++;
      
      appTokenValueTextField.getDocument().addDocumentListener(updateListener);
      configPanel.add(appTokenValueTextField, gc);
      gc.gridy++;

      appTokenSecretTextField.getDocument().addDocumentListener(updateListener);
      configPanel.add(appTokenSecretTextField, gc);

      JPanel findPanel = new JPanel();
      TitledBorder findPanelBorder = new TitledBorder("Find User");
      findPanel.setBorder(findPanelBorder);
      findPanel.setLayout(new GridBagLayout());
      gc = new GridBagConstraints();
      gc.gridx = 1;
      gc.gridy = 0;
      gc.insets = new Insets(10, 0, 10, 10);
      gc.anchor = GridBagConstraints.NORTHEAST;
      mainPanel.add(findPanel, gc);
      
      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.insets = new Insets(10, 10, 10, 10);
      gc.anchor = GridBagConstraints.EAST;
      findPanel.add(new JLabel("Mobile Number:"), gc);
      gc.gridy++;
      gc.insets.top = 0;
      findPanel.add(new JLabel("or Email:"), gc);

      gc = new GridBagConstraints();
      gc.gridx = 1;
      gc.gridy = 0;
      gc.insets = new Insets(10, 0, 10, 10);
      gc.ipadx = 100;
      findPanel.add(phoneTextField, gc);
      gc.gridy++;
      gc.insets.top = 0;
      findPanel.add(emailTextField, gc);
      
      findUserButton.setText("Find");
      findUserButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            doFindUser();
         }
      });
      gc = new GridBagConstraints();
      gc.gridx = 2;
      gc.gridy = 0;
      gc.gridheight = 2;
      gc.insets = new Insets(10, 10, 10, 10);
      findPanel.add(findUserButton, gc);
      
      newUserIdButton.setText("Enter a User ID Manually");
      newUserIdButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            doNewUserId();
         }
      });
      gc = new GridBagConstraints();
      gc.gridx = 1;
      gc.gridy = 1;
      gc.insets = new Insets(10, 10, 10, 10);
      mainPanel.add(newUserIdButton, gc);
      
      newTokenButton.setText("Enter an Access Token Manually");
      newTokenButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            doNewToken();
         }
      });
      gc.gridy++;
      mainPanel.add(newTokenButton, gc);

      getPermittedUsersButton.setText("List All Permitted Users");
      getPermittedUsersButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            doGetPermittedUsers();
         }
      });
      gc.gridy++;
      mainPanel.add(getPermittedUsersButton, gc);

      gc = new GridBagConstraints();
      gc.gridx = 0;
      gc.gridy = 4;
      gc.gridwidth = 2;
      gc.ipady = 100;
      gc.weightx = 1;
      gc.weighty = 1;
      gc.insets = new Insets(0, 10, 10, 10);
      gc.anchor = GridBagConstraints.SOUTH;
      gc.fill = GridBagConstraints.BOTH;
      mainPanel.add(logPanel, gc);

      this.setTitle("Veriplace Client Demo");
      this.pack();
      this.setLocationRelativeTo(null);
   }
}
