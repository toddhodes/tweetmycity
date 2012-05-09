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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;

/**
 * A window for viewing a list of user IDs and performing requests with them.
 */
public class UserListWindow extends JFrame {

   private ClientWindow parent;
   private List<User> users;
   private DemoStatusBar statusBar;
   private JTable usersTable;
   private JButton getLocationPermissionButton;
   
   public UserListWindow(ClientWindow parent) {
      this.parent = parent;
      createLayout();
   }

   public List<User> getUsers() {
      return users;
   }
   
   public void setUsers(List<User> users) {
      this.users = users;
   }
   
   public void doGetLocationPermission() {
      int i = usersTable.getSelectedRow();
      if (i < 0) {
         return;
      }
      User user = users.get(i);
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
   
   public void createLayout() {
      statusBar = new DemoStatusBar(this);
      JPanel mainPanel = new JPanel();
      usersTable = new JTable();
      getLocationPermissionButton = new JButton();
      
      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(mainPanel, BorderLayout.CENTER);
      this.getContentPane().add(statusBar, BorderLayout.SOUTH);
      mainPanel.setLayout(new GridBagLayout());
      GridBagConstraints gc;

      usersTable.setModel(new UsersTableModel());
      JScrollPane scroller = new JScrollPane(usersTable);
      scroller.setMinimumSize(new Dimension(200, 180));
      scroller.setPreferredSize(new Dimension(200, 180));
      gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.anchor = GridBagConstraints.CENTER;
      gc.insets = new Insets(10, 10, 10, 10);
      gc.fill = GridBagConstraints.BOTH;
      mainPanel.add(scroller, gc);
      usersTable.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
               doGetLocationPermission();
            }
         }
      });
      
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
      
      this.setTitle("Veriplace Users");
      this.pack();
      this.setLocationRelativeTo(parent);
   }
   
   class UsersTableModel extends AbstractTableModel {
      
      public int getColumnCount() {
         return 1;
      }
      
      public int getRowCount() {
         return (users == null) ? 0 : users.size();
      }
      
      public Object getValueAt(int row, int col) {
         User user = users.get(row);
         if (col == 0) {
            return user.getId();
         }
         return null;
      }
      
      public String getColumnName(int col) {
         if (col == 0) {
            return "User ID";
         }
         return null;
      }
      
      public boolean isCellEditable(int row, int col) {
         return false;
      }
   }
}
