package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import dao.VehicleDAO;
import models.Vehicle;

public class VehicleExitPanel extends JPanel {
    private JTextField plateField;
    private JTextArea detailsArea;
    private Vehicle currentVehicle;
    
    public VehicleExitPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.add(new JLabel("Plate Number:"));
        plateField = new JTextField(15);
        searchPanel.add(plateField);
        
        JButton searchBtn = new JButton("Search Vehicle");
        searchPanel.add(searchBtn);
        
       
        detailsArea = new JTextArea(10, 40);
        detailsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Vehicle Details"));
        
        
        JButton exitBtn = new JButton("Process Exit & Payment");
        exitBtn.setEnabled(false);
        
       
        searchBtn.addActionListener(e -> {
            String plate = plateField.getText().trim().toUpperCase();
            detailsArea.setText("");
            
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter plate number");
                return;
            }
            
            VehicleDAO vehicleDAO = new VehicleDAO();
            currentVehicle = vehicleDAO.findActiveVehicle(plate);
            
            if (currentVehicle != null) {
               
                long hours = java.time.Duration.between(
                    currentVehicle.getEntryTime(), 
                    java.time.LocalDateTime.now()
                ).toHours();
                if (hours < 1) hours = 1;
                
                double charges = hours * 200; 
                
                detailsArea.append("=== VEHICLE FOUND ===\n");
                detailsArea.append("Plate: " + plate + "\n");
                detailsArea.append("Type: " + currentVehicle.getVehicleType() + "\n");
                detailsArea.append("Entry: " + currentVehicle.getEntryTime() + "\n");
                detailsArea.append("Hours: " + hours + "\n");
                detailsArea.append("Charge: Rs." + charges + "\n");
                detailsArea.append("================\n");
                
                exitBtn.setEnabled(true);
                exitBtn.addActionListener(ev -> {
                    String amountStr = JOptionPane.showInputDialog(this,
                        "Total Amount: Rs." + charges + "\nEnter amount received:");
                    
                    if (amountStr != null) {
                        try {
                            double amount = Double.parseDouble(amountStr);
                            if (amount >= charges) {
                                if (vehicleDAO.processExit(plate, amount)) {
                                    JOptionPane.showMessageDialog(this,
                                        "Exit processed!\n" +
                                        "Amount: Rs." + amount + "\n" +
                                        "Change: Rs." + (amount - charges));
                                    clearForm();
                                }
                            } else {
                                JOptionPane.showMessageDialog(this, "Insufficient amount!");
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Enter valid amount!");
                        }
                    }
                });
            } else {
                detailsArea.setText("No active parking found for: " + plate);
                exitBtn.setEnabled(false);
            }
        });
        
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(exitBtn, BorderLayout.SOUTH);
    }
    
    private void clearForm() {
        plateField.setText("");
        detailsArea.setText("");
        currentVehicle = null;
    }
}