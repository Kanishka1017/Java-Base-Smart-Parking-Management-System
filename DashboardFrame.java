package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class DashboardFrame extends JFrame {
    private String currentUser;
    
    public DashboardFrame(String username) {
        this.currentUser = username;
        
        setTitle("Parking System - Welcome " + username);
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });
        fileMenu.add(logoutItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
        
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Vehicle Entry", createVehicleEntryPanel());
        tabbedPane.addTab("Vehicle Exit", createVehicleExitPanel());
        tabbedPane.addTab("Parking Slots", createSlotManagementPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        
        add(tabbedPane);
        setVisible(true);
    }
    
    
    private Connection getConnection() {
        try {
            
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/smart_parking_db", "root", "");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            return null;
        }
    }
    
    
    private JPanel createVehicleEntryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        
        formPanel.add(new JLabel("Plate Number:"));
        JTextField plateField = new JTextField();
        formPanel.add(plateField);
        
        formPanel.add(new JLabel("Vehicle Type:"));
        String[] types = {"Car", "Bike", "Truck"};
        JComboBox<String> typeCombo = new JComboBox<>(types);
        formPanel.add(typeCombo);
        
        formPanel.add(new JLabel("Available Slots:"));
        JTextArea slotsArea = new JTextArea(4, 20);
        slotsArea.setEditable(false);
        JScrollPane slotsScroll = new JScrollPane(slotsArea);
        formPanel.add(slotsScroll);
        
        JButton checkBtn = new JButton("Check Available Slots");
        JButton parkBtn = new JButton("Park Vehicle");
        formPanel.add(checkBtn);
        formPanel.add(parkBtn);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        
        JTextArea statusArea = new JTextArea(10, 50);
        statusArea.setEditable(false);
        panel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        
        
        checkBtn.addActionListener(e -> {
            String vehicleType = (String) typeCombo.getSelectedItem();
            slotsArea.setText("");
            
            try {
                Connection conn = getConnection();
                if (conn == null) return;
                
                
                String capacitySql = "SELECT " +
                                   "COUNT(*) as total, " +
                                   "SUM(CASE WHEN status = 'Available' THEN 1 ELSE 0 END) as available " +
                                   "FROM parking_slots WHERE slot_type = ?";
                PreparedStatement capacityStmt = conn.prepareStatement(capacitySql);
                capacityStmt.setString(1, vehicleType);
                ResultSet capacityRs = capacityStmt.executeQuery();
                
                if (capacityRs.next()) {
                    int total = capacityRs.getInt("total");
                    int available = capacityRs.getInt("available");
                    slotsArea.append("=== " + vehicleType + " SLOTS ===\n");
                    slotsArea.append("Available: " + available + "/" + total + "\n\n");
                }
                capacityRs.close();
                capacityStmt.close();
                
                
                String sql = "SELECT slot_number FROM parking_slots WHERE slot_type = ? AND status = 'Available'";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, vehicleType);
                ResultSet rs = pstmt.executeQuery();
                
                int count = 0;
                while (rs.next()) {
                    slotsArea.append(rs.getString("slot_number") + "\n");
                    count++;
                }
                
                if (count == 0) {
                    slotsArea.append("--- No slots available ---");
                }
                
                conn.close();
                
            } catch (Exception ex) {
                statusArea.append("Error checking slots: " + ex.getMessage() + "\n");
            }
        });
        
        parkBtn.addActionListener(e -> {
            String plate = plateField.getText().trim().toUpperCase();
            String type = (String) typeCombo.getSelectedItem();
            
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Enter plate number");
                return;
            }
            
            try {
                Connection conn = getConnection();
                if (conn == null) return;
                
               
                String checkSql = "SELECT * FROM vehicles WHERE plate_number = ? AND exit_time IS NULL";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, plate);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    statusArea.append("Error: Vehicle already parked\n");
                    return;
                }
                
                
                String capacitySql = "SELECT " +
                                   "COUNT(*) as total, " +
                                   "SUM(CASE WHEN status = 'Available' THEN 1 ELSE 0 END) as available " +
                                   "FROM parking_slots WHERE slot_type = ?";
                PreparedStatement capacityStmt = conn.prepareStatement(capacitySql);
                capacityStmt.setString(1, type);
                ResultSet capacityRs = capacityStmt.executeQuery();
                
                if (capacityRs.next()) {
                    int totalSlots = capacityRs.getInt("total");
                    int availableSlots = capacityRs.getInt("available");
                    
                    
                    if (availableSlots == 0) {
                        statusArea.append("ERROR: All " + totalSlots + " " + type + " slots are FULL!\n");
                        JOptionPane.showMessageDialog(panel,
                            "🚫 PARKING FULL!\n" +
                            "No available " + type + " slots.\n" +
                            "Total " + type + " slots: " + totalSlots + "\n" +
                            "Available: " + availableSlots + "\n\n" +
                            "Please wait for a vehicle to exit.");
                        capacityRs.close();
                        capacityStmt.close();
                        checkStmt.close();
                        conn.close();
                        return; 
                    }
                }
                capacityRs.close();
                capacityStmt.close();
               
                
                String slotSql = "SELECT slot_id, slot_number FROM parking_slots WHERE slot_type = ? AND status = 'Available' LIMIT 1";
                PreparedStatement slotStmt = conn.prepareStatement(slotSql);
                slotStmt.setString(1, type);
                ResultSet slotRs = slotStmt.executeQuery();
                
                if (slotRs.next()) {
                    int slotId = slotRs.getInt("slot_id");
                    String slotNum = slotRs.getString("slot_number");
                    
                    
                    String parkSql = "INSERT INTO vehicles (plate_number, vehicle_type, slot_id) VALUES (?, ?, ?)";
                    PreparedStatement parkStmt = conn.prepareStatement(parkSql);
                    parkStmt.setString(1, plate);
                    parkStmt.setString(2, type);
                    parkStmt.setInt(3, slotId);
                    parkStmt.executeUpdate();
                    
                   
                    String updateSql = "UPDATE parking_slots SET status = 'Occupied' WHERE slot_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, slotId);
                    updateStmt.executeUpdate();
                    
                    statusArea.append("Success: " + plate + " parked in slot " + slotNum + "\n");
                    plateField.setText("");
                    checkBtn.doClick(); 
                    
                } else {
                    statusArea.append("Error: No slots available for " + type + "\n");
                }
                
                conn.close();
                
            } catch (Exception ex) {
                statusArea.append("Error parking vehicle: " + ex.getMessage() + "\n");
            }
        });
        
       
        checkBtn.doClick();
        
        return panel;
    }
    
   
    private JPanel createVehicleExitPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
       
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.add(new JLabel("Plate Number:"));
        JTextField plateField = new JTextField(15);
        searchPanel.add(plateField);
        
        JButton searchBtn = new JButton("Search Vehicle");
        searchPanel.add(searchBtn);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        
        
        JTextArea detailsArea = new JTextArea(10, 50);
        detailsArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(detailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        

        JButton exitBtn = new JButton("Process Exit & Payment");
        exitBtn.setEnabled(false);
        panel.add(exitBtn, BorderLayout.SOUTH);
        
        
        searchBtn.addActionListener(e -> {
            String plate = plateField.getText().trim().toUpperCase();
            detailsArea.setText("");
            exitBtn.setEnabled(false);
            
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Enter plate number");
                return;
            }
            
            try {
                Connection conn = getConnection();
                if (conn == null) return;
                
                String sql = "SELECT v.*, s.slot_number FROM vehicles v " +
                            "JOIN parking_slots s ON v.slot_id = s.slot_id " +
                            "WHERE v.plate_number = ? AND v.exit_time IS NULL";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, plate);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                   
                    java.sql.Timestamp entryTime = rs.getTimestamp("entry_time");
                    long hours = (System.currentTimeMillis() - entryTime.getTime()) / (1000 * 60 * 60);
                    if (hours < 1) hours = 1;
                    
                    String vehicleType = rs.getString("vehicle_type");
                    int rate = getRate(vehicleType);
                    double charges = hours * rate;
                    
                   
                    detailsArea.append("=== VEHICLE FOUND ===\n");
                    detailsArea.append("Plate: " + plate + "\n");
                    detailsArea.append("Type: " + vehicleType + "\n");
                    detailsArea.append("Slot: " + rs.getString("slot_number") + "\n");
                    detailsArea.append("Entry: " + entryTime + "\n");
                    detailsArea.append("Hours: " + hours + "\n");
                    detailsArea.append("Rate: Rs." + rate + "/hour\n");
                    detailsArea.append("Total: Rs." + charges + "\n");
                    detailsArea.append("===================\n");
                    
                    exitBtn.setEnabled(true);
                    
                   
                    final String slotNumber = rs.getString("slot_number");
                    final String vehiclePlate = plate;
                    final double totalCharges = charges;
                    final long parkingHours = hours;
                    final int parkingRate = rate;

                    
                    ActionListener[] existingListeners = exitBtn.getActionListeners();
                    for (ActionListener listener : existingListeners) {
                        exitBtn.removeActionListener(listener);
                    }

                    
                    exitBtn.addActionListener(new ActionListener() {
                      
                        public void actionPerformed(ActionEvent ev) {
                            try {
                                String amountStr = JOptionPane.showInputDialog(panel,
                                    "🚗 VEHICLE EXIT BILL\n" +
                                    "====================\n" +
                                    "Plate: " + vehiclePlate + "\n" +
                                    "Slot: " + slotNumber + "\n" +
                                    "Hours: " + parkingHours + "\n" +
                                    "Rate: Rs." + parkingRate + "/hour\n" +
                                    "====================\n" +
                                    "TOTAL: Rs." + totalCharges + "\n" +
                                    "====================\n\n" +
                                    "Enter amount received:");
                                
                                if (amountStr == null || amountStr.trim().isEmpty()) {
                                    return;
                                }
                                
                                double amount = Double.parseDouble(amountStr);
                                
                                if (amount < totalCharges) {
                                    JOptionPane.showMessageDialog(panel,
                                        "❌ INSUFFICIENT AMOUNT!\n\n" +
                                        "Total Bill: Rs." + totalCharges + "\n" +
                                        "Amount Given: Rs." + amount + "\n" +
                                        "Short by: Rs." + (totalCharges - amount));
                                    return;
                                }
                                
                                
                                Connection exitConn = getConnection();
                                if (exitConn == null) {
                                    JOptionPane.showMessageDialog(panel, "Database connection failed!");
                                    return;
                                }
                                
                                try {
                                    
                                    String updateVehicleSql = "UPDATE vehicles SET exit_time = NOW(), amount_paid = ? " +
                                                            "WHERE plate_number = ? AND exit_time IS NULL";
                                    PreparedStatement vehicleStmt = exitConn.prepareStatement(updateVehicleSql);
                                    vehicleStmt.setDouble(1, totalCharges);
                                    vehicleStmt.setString(2, vehiclePlate);
                                    int vehicleUpdated = vehicleStmt.executeUpdate();
                                    
                                   
                                    String updateSlotSql = "UPDATE parking_slots SET status = 'Available' " +
                                                         "WHERE slot_number = ?";
                                    PreparedStatement slotStmt = exitConn.prepareStatement(updateSlotSql);
                                    slotStmt.setString(1, slotNumber);
                                    int slotUpdated = slotStmt.executeUpdate();
                                    
                                    if (vehicleUpdated > 0 && slotUpdated > 0) {
                                       
                                        JOptionPane.showMessageDialog(panel,
                                            "✅ VEHICLE EXIT SUCCESSFUL!\n\n" +
                                            "Vehicle: " + vehiclePlate + "\n" +
                                            "Slot " + slotNumber + " is now AVAILABLE\n" +
                                            "Parking Duration: " + parkingHours + " hours\n" +
                                            "Total Bill: Rs." + totalCharges + "\n" +
                                            "Amount Paid: Rs." + amount + "\n" +
                                            "Change: Rs." + (amount - totalCharges) + "\n\n" +
                                            "Thank you for using our parking!");
                                        
                                        
                                        plateField.setText("");
                                        detailsArea.setText("");
                                        exitBtn.setEnabled(false);
                                    } else {
                                        JOptionPane.showMessageDialog(panel,
                                            "❌ Exit failed!\n" +
                                            "Vehicle might have already exited or slot not found.");
                                    }
                                    
                                    
                                    vehicleStmt.close();
                                    slotStmt.close();
                                    exitConn.close();
                                    
                                } catch (SQLException e) {
                                    JOptionPane.showMessageDialog(panel, 
                                        "❌ Database Error: " + e.getMessage());
                                    try { exitConn.close(); } catch (SQLException ex) {}
                                }
                                
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(panel, 
                                    "❌ Please enter a valid number!\nExample: 500, 500.50");
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(panel, 
                                    "❌ Error: " + e.getMessage());
                            }
                        }
                    });
                    
                } else {
                    detailsArea.setText("No active parking found for: " + plate);
                }
                
                conn.close();
                
            } catch (Exception ex) {
                detailsArea.setText("Error: " + ex.getMessage());
            }
        });
        
        return panel;
    }
    
    
    private JPanel createSlotManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton refreshBtn = new JButton("Refresh Slots");
        panel.add(refreshBtn, BorderLayout.NORTH);
        
        
        String[] columns = {"Slot Number", "Type", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        refreshBtn.addActionListener(e -> {
            try {
                Connection conn = getConnection();
                if (conn == null) return;
                
                
                model.setRowCount(0);
                
                
                String summarySql = "SELECT slot_type, " +
                                  "COUNT(*) as total, " +
                                  "SUM(CASE WHEN status = 'Available' THEN 1 ELSE 0 END) as available " +
                                  "FROM parking_slots GROUP BY slot_type";
                Statement summaryStmt = conn.createStatement();
                ResultSet summaryRs = summaryStmt.executeQuery(summarySql);
                
                model.addRow(new Object[]{"=== CAPACITY SUMMARY ===", "", ""});
                while (summaryRs.next()) {
                    String type = summaryRs.getString("slot_type");
                    int total = summaryRs.getInt("total");
                    int available = summaryRs.getInt("available");
                    model.addRow(new Object[]{
                        type + " Slots",
                        available + "/" + total + " available",
                        available == 0 ? "🚫 FULL" : "AVAILABLE"
                    });
                }
                model.addRow(new Object[]{"", "", ""}); 
                
                model.addRow(new Object[]{"=== ALL SLOTS ===", "", ""});
                
                
                String sql = "SELECT slot_number, slot_type, status FROM parking_slots ORDER BY slot_number";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getString("slot_number"),
                        rs.getString("slot_type"),
                        rs.getString("status")
                    });
                }
                
                
                String totalSql = "SELECT COUNT(*) as total FROM parking_slots";
                ResultSet totalRs = conn.createStatement().executeQuery(totalSql);
                if (totalRs.next()) {
                    model.addRow(new Object[]{"", "", ""});
                    model.addRow(new Object[]{"TOTAL SLOTS", totalRs.getInt("total"), ""});
                }
                
                conn.close();
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });
        
        
        refreshBtn.doClick();
        
        return panel;
    }
    
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton generateBtn = new JButton("Generate Report");
        panel.add(generateBtn, BorderLayout.NORTH);
        
        JTextArea reportArea = new JTextArea(15, 60);
        reportArea.setEditable(false);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        
        generateBtn.addActionListener(e -> {
            reportArea.setText("=== PARKING REPORT ===\n\n");
            
            try {
                Connection conn = getConnection();
                if (conn == null) return;
                
                Statement stmt = conn.createStatement();
                
                
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM vehicles");
                if (rs.next()) {
                    reportArea.append("Total Vehicles Parked: " + rs.getInt(1) + "\n");
                }
                
                
                rs = stmt.executeQuery("SELECT SUM(amount_paid) FROM vehicles");
                if (rs.next()) {
                    reportArea.append("Total Revenue: Rs." + rs.getDouble(1) + "\n");
                }
                
               
                rs = stmt.executeQuery("SELECT COUNT(*) FROM parking_slots WHERE status = 'Available'");
                if (rs.next()) {
                    reportArea.append("Available Slots: " + rs.getInt(1) + "\n");
                }
                
                
                rs = stmt.executeQuery("SELECT COUNT(*) FROM parking_slots WHERE status = 'Occupied'");
                if (rs.next()) {
                    reportArea.append("Occupied Slots: " + rs.getInt(1) + "\n");
                }
                
                conn.close();
                
            } catch (Exception ex) {
                reportArea.append("Error: " + ex.getMessage());
            }
        });
        
       
        generateBtn.doClick();
        
        return panel;
    }
    
    
    private int getRate(String vehicleType) {
        switch(vehicleType) {
            case "Car": return 200;
            case "Bike": return 100;
            case "Truck": return 300;
            default: return 0;
        }
    }
}