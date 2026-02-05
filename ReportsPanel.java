package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ReportsPanel extends JPanel {
    private JTextArea reportArea;
    
    public ReportsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.add(new JLabel("Report Type:"));
        
        String[] reports = {"Daily", "Weekly", "Monthly"};
        JComboBox<String> reportCombo = new JComboBox<>(reports);
        controlPanel.add(reportCombo);
        
        JButton generateBtn = new JButton("Generate Report");
        controlPanel.add(generateBtn);
        
       
        reportArea = new JTextArea(15, 50);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        
       
        generateBtn.addActionListener(e -> {
            String type = (String) reportCombo.getSelectedItem();
            generateReport(type);
        });
        
        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
       
        generateReport("Daily");
    }
    
    private void generateReport(String type) {
        reportArea.setText("=== PARKING REPORT ===\n");
        reportArea.append("Report Type: " + type + "\n");
        reportArea.append("Date: " + new java.util.Date() + "\n");
        reportArea.append("========================\n\n");
        
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/smart_parking_db", "root", "");
            
            
            String sql = "SELECT COUNT(*) as total FROM vehicles";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                reportArea.append("Total Vehicles Parked: " + rs.getInt("total") + "\n");
            }
            
           
            sql = "SELECT SUM(amount_paid) as revenue FROM vehicles";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                double revenue = rs.getDouble("revenue");
                reportArea.append("Total Revenue: Rs." + (revenue > 0 ? revenue : 0) + "\n");
            }
            
           
            sql = "SELECT COUNT(*) as available FROM parking_slots WHERE status = 'Available'";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                reportArea.append("Available Slots: " + rs.getInt("available") + "\n");
            }
            
            conn.close();
            
        } catch (Exception e) {
            reportArea.append("\nError generating report: " + e.getMessage());
        }
    }
}