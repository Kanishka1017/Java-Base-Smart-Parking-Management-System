package dao;

import models.ParkingSlot;
import database.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSlotDAO {
    
    
    public List<ParkingSlot> getAllSlots() {
        List<ParkingSlot> slots = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM parking_slots ORDER BY slot_number";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                slots.add(new ParkingSlot(
                    rs.getInt("slot_id"),
                    rs.getString("slot_number"),
                    rs.getString("slot_type"),
                    rs.getString("status"),
                    rs.getInt("floor")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slots;
    }
    
   
    public List<String> getAvailableSlots(String vehicleType) {
        List<String> slots = new ArrayList<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT slot_number FROM parking_slots WHERE slot_type = ? AND status = 'Available'";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, vehicleType);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                slots.add(rs.getString("slot_number"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slots;
    }
    
    
    public boolean updateSlotStatus(String slotNumber, String status) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE parking_slots SET status = ? WHERE slot_number = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setString(2, slotNumber);
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
	


