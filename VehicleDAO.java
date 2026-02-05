package dao;

import models.Vehicle;
import database.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class VehicleDAO {
    
    
    public boolean addVehicle(Vehicle vehicle) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO vehicles (plate_number, vehicle_type, slot_id) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, vehicle.getPlateNumber());
            pstmt.setString(2, vehicle.getVehicleType());
            pstmt.setInt(3, vehicle.getSlotId());
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
   
    public Vehicle findActiveVehicle(String plateNumber) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM vehicles WHERE plate_number = ? AND exit_time IS NULL";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, plateNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Vehicle vehicle = new Vehicle();
                vehicle.setVehicleId(rs.getInt("vehicle_id"));
                vehicle.setPlateNumber(rs.getString("plate_number"));
                vehicle.setVehicleType(rs.getString("vehicle_type"));
                vehicle.setSlotId(rs.getInt("slot_id"));
                vehicle.setEntryTime(rs.getTimestamp("entry_time").toLocalDateTime());
                return vehicle;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
   
    public boolean processExit(String plateNumber, double amount) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE vehicles SET exit_time = NOW(), amount_paid = ? WHERE plate_number = ? AND exit_time IS NULL";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, amount);
            pstmt.setString(2, plateNumber);
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}