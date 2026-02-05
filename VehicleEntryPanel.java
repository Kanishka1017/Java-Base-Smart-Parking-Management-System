package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import dao.ParkingSlotDAO;
import dao.VehicleDAO;
import models.Vehicle;

public class VehicleEntryPanel extends JPanel {
    private JTextField plateField;
    private JComboBox<String> typeCombo;
    private JComboBox<String> slotCombo;
    private JLabel statusLabel;
    private ParkingSlotDAO slotDAO;
    
    public VehicleEntryPanel() {
        slotDAO = new ParkingSlotDAO();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Park Vehicle"));
        
        formPanel.add(new JLabel("Plate Number:"));
        plateField = new JTextField();
        formPanel.add(plateField);
        
        formPanel.add(new JLabel("Vehicle Type:"));
        typeCombo = new JComboBox<>(new String[]{"Car", "Bike", "Truck"});
        typeCombo.addActionListener(e -> loadAvailableSlots());
        formPanel.add(typeCombo);
        
        formPanel.add(new JLabel("Select Slot:"));
        slotCombo = new JComboBox<>();
        formPanel.add(slotCombo);
        
        JButton parkBtn = new JButton("Park Vehicle");
        JButton clearBtn = new JButton("Clear");
        
        parkBtn.addActionListener(e -> parkVehicle());
        clearBtn.addActionListener(e -> clearForm());
        
        formPanel.add(parkBtn);
        formPanel.add(clearBtn);
        
        add(formPanel, BorderLayout.NORTH);
        
        
        statusLabel = new JLabel("Ready", SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createTitledBorder("Status"));
        add(statusLabel, BorderLayout.CENTER);
        
      
        JPanel slotsPanel = new JPanel(new GridLayout(0, 6, 5, 5));
        slotsPanel.setBorder(BorderFactory.createTitledBorder("All Slots"));
        add(new JScrollPane(slotsPanel), BorderLayout.SOUTH);
        
       
        loadAvailableSlots();
        showAllSlots(slotsPanel);
    }
    
    private void loadAvailableSlots() {
        String type = (String) typeCombo.getSelectedItem();
        slotCombo.removeAllItems();
        
        for (String slot : slotDAO.getAvailableSlots(type)) {
            slotCombo.addItem(slot);
        }
        
        if (slotCombo.getItemCount() == 0) {
            slotCombo.addItem("No slots available");
            statusLabel.setText("No " + type + " slots available");
        } else {
            statusLabel.setText(slotCombo.getItemCount() + " slots available");
        }
    }
    
    private void parkVehicle() {
        String plate = plateField.getText().trim().toUpperCase();
        String type = (String) typeCombo.getSelectedItem();
        String slot = (String) slotCombo.getSelectedItem();
        
        if (plate.isEmpty() || "No slots available".equals(slot)) {
            JOptionPane.showMessageDialog(this, "Please fill all fields correctly");
            return;
        }
        
       
        VehicleDAO vehicleDAO = new VehicleDAO();
        if (vehicleDAO.findActiveVehicle(plate) != null) {
            JOptionPane.showMessageDialog(this, "This vehicle is already parked!");
            return;
        }
        
        
        Vehicle vehicle = new Vehicle();
        vehicle.setPlateNumber(plate);
        vehicle.setVehicleType(type);
        
        
        if (slotDAO.updateSlotStatus(slot, "Occupied")) {
            JOptionPane.showMessageDialog(this, 
                "Vehicle parked successfully!\n" +
                "Plate: " + plate + "\n" +
                "Slot: " + slot + "\n" +
                "Type: " + type);
            clearForm();
            loadAvailableSlots();
        }
    }
    
    private void showAllSlots(JPanel panel) {
        panel.removeAll();
        
        for (models.ParkingSlot slot : new ParkingSlotDAO().getAllSlots()) {
            JButton btn = new JButton(slot.getSlotNumber());
            btn.setPreferredSize(new Dimension(70, 50));
            
            if (slot.isAvailable()) {
                btn.setBackground(Color.GREEN);
            } else {
                btn.setBackground(Color.RED);
            }
            
            btn.setEnabled(false);
            panel.add(btn);
        }
        
        panel.revalidate();
        panel.repaint();
    }
    
    private void clearForm() {
        plateField.setText("");
        loadAvailableSlots();
    }
}
