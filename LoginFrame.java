package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginFrame() {
        setTitle("Smart Parking System - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
       
        if (!testDatabaseConnection()) {
            showDatabaseError();
            return;
        }
        
       
        setupDatabase();
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel title = new JLabel("PARKING SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(title, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        panel.add(usernameField, gbc);
        
        
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);
        
        
        gbc.gridy = 3; gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        JButton loginBtn = new JButton("Login");
        JButton exitBtn = new JButton("Exit");
        
        loginBtn.addActionListener(e -> login());
        exitBtn.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(loginBtn);
        buttonPanel.add(exitBtn);
        panel.add(buttonPanel, gbc);
        
        
        gbc.gridy = 4;
        JLabel testLabel = new JLabel("Test: admin / admin123", SwingConstants.CENTER);
        testLabel.setForeground(Color.GRAY);
        panel.add(testLabel, gbc);
        
        add(panel);
        setVisible(true);
        
        
        usernameField.requestFocus();
    }
    
    private boolean testDatabaseConnection() {
        try {
            
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/smart_parking_db", "root", "");
            System.out.println("Database connection successful!");
            conn.close();
            return true;
        } catch (Exception e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
    
    private void setupDatabase() {
        try {
            
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/smart_parking_db", "root", "");
            
            Statement stmt = conn.createStatement();
            
           
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50), " +
                        "password VARCHAR(50), " +
                        "role VARCHAR(20))");
            
           
            stmt.execute("CREATE TABLE IF NOT EXISTS parking_slots (" +
                        "slot_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "slot_number VARCHAR(10), " +
                        "slot_type VARCHAR(10), " +
                        "status VARCHAR(20) DEFAULT 'Available')");
            
           
            stmt.execute("CREATE TABLE IF NOT EXISTS vehicles (" +
                        "vehicle_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "plate_number VARCHAR(20), " +
                        "vehicle_type VARCHAR(10), " +
                        "entry_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "exit_time DATETIME, " +
                        "slot_id INT, " +
                        "amount_paid DECIMAL(10,2) DEFAULT 0)");
            
           
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            rs.next();
            if (rs.getInt(1) == 0) {
              
                stmt.execute("INSERT INTO users (username, password, role) VALUES " +
                            "('admin', 'admin123', 'admin'), " +
                            "('staff', 'staff123', 'staff')");
                System.out.println("Default users added");
            }
            
           
            rs = stmt.executeQuery("SELECT COUNT(*) FROM parking_slots");
            rs.next();
            if (rs.getInt(1) == 0) {
              
                stmt.execute("INSERT INTO parking_slots (slot_number, slot_type) VALUES " +
                            "('C-1', 'Car'), ('C-2', 'Car'), ('C-3', 'Car'), ('C-4', 'Car'), ('C-5', 'Car'), " +
                            "('B-1', 'Bike'), ('B-2', 'Bike'), ('B-3', 'Bike'), " +
                            "('T-1', 'Truck'), ('T-2', 'Truck')");
                System.out.println("Created 10 parking slots (5 Cars, 3 Bikes, 2 Trucks)");
            }
            
            conn.close();
            System.out.println("Database setup complete!");
            
        } catch (Exception e) {
            System.out.println("Setup error: " + e.getMessage());
        }
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password!");
            return;
        }
        
        try {
           
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/smart_parking_db", "root", "");
            
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
               
                System.out.println("Login successful for user: " + username);
                
               
                new DashboardFrame(username);
                dispose(); 
                
            } else {
                
                JOptionPane.showMessageDialog(this, 
                    "Wrong username or password!\n\n" +
                    "Try:\nUsername: admin\nPassword: admin123");
           }
            
            conn.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Login error: " + e.getMessage() + 
                "\n\nMake sure database is running!");
        }
    }
    
    private void showDatabaseError() {
        JOptionPane.showMessageDialog(null,
            "DATABASE CONNECTION FAILED!\n\n" +
            "Please ensure:\n" +
            "1. MySQL server is running (XAMPP/WAMP)\n" +
            "2. Create database: CREATE DATABASE smart_parking_db;\n" +
            "3. Check username/password in code\n\n" +
            "Click OK to exit.",
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame();
        });
    }
}