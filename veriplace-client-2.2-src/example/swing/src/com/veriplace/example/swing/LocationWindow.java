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

import com.veriplace.client.Location;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A window that displays the properties of a Location object.
 */
public class LocationWindow extends JFrame {

   private JFrame parent;
   private JTextField locationIdTextField;
   private JLabel[] locationDataLabels;
   
   public LocationWindow(JFrame parent) {
      this.parent = parent;
      createLayout();
   }
   
   public void setLocation(Location location) {
      if (location == null) {
         locationIdTextField.setText("");
         for (JLabel label: locationDataLabels) {
            label.setText("");
         }
      }
      else {
         locationIdTextField.setText(String.valueOf(location.getId()));
         String[] values = new String[] {
            location.getCreationDate().toString(),
            location.getExpirationDate().toString(),
            String.valueOf(location.getLongitude()),
            String.valueOf(location.getLatitude()),
            String.valueOf(location.getUncertainty()),
            location.getStreet(),
            location.getNeighborhood(),
            location.getCity(),
            location.getState(),
            location.getPostal(),
            location.getCountryCode()
         };
         for (int i = 0; i < locationDataLabels.length; i++) {
            locationDataLabels[i].setText(values[i]);
         }
      }
   }
   
   public void createLayout() {
      locationIdTextField = new JTextField();
      locationDataLabels = new JLabel[11];
      for (int i = 0; i < locationDataLabels.length; i++) {
         locationDataLabels[i] = new JLabel();
      }
      
      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(new GridBagLayout());
      getContentPane().add(mainPanel);
      
      GridBagConstraints gc = new GridBagConstraints();
      gc.gridx = gc.gridy = 0;
      gc.anchor = GridBagConstraints.EAST;
      gc.insets = new Insets(10, 10, 10, 10);
      mainPanel.add(new JLabel("Location ID:"), gc);
      
      locationIdTextField.setEditable(false);
      gc.gridx++;
      gc.anchor = GridBagConstraints.WEST;
      gc.insets = new Insets(10, 0, 10, 10);
      gc.ipadx = 200;
      mainPanel.add(locationIdTextField, gc);

      String[] captions = new String[] {
            "Created:", "Expires:", "Longitude:", "Latitude:", "Uncertainty:",
            "Address:", "Neighborhood:", "City:", "State:", "Postasl Code:",
            "Country:"
      };
      gc = new GridBagConstraints();
      gc.gridx = 0;
      gc.gridy = 1;
      gc.anchor = GridBagConstraints.EAST;
      gc.insets = new Insets(0, 10, 10, 10);
      for (String caption: captions) {
         mainPanel.add(new JLabel(caption), gc);
         gc.gridy++;
      }
      
      gc.gridx = 1;
      gc.gridy = 1;
      gc.anchor = GridBagConstraints.WEST;
      gc.insets = new Insets(0, 0, 10, 10);
      for (JLabel label: locationDataLabels) {
         mainPanel.add(label, gc);
         gc.gridy++;
      }
      
      this.setTitle("Veriplace Location");
      this.pack();
      this.setLocationRelativeTo(parent);
   }
}
