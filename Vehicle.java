package models;

import java.time.LocalDateTime;

public class Vehicle {
    private int vehicleId;
    private String plateNumber;
    private String vehicleType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private int slotId;
    private double amountPaid;
    
    
    public Vehicle() {}
    
    public Vehicle(int vehicleId, String plateNumber, String vehicleType, 
                   LocalDateTime entryTime, LocalDateTime exitTime, 
                   int slotId, double amountPaid) {
        this.vehicleId = vehicleId;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.slotId = slotId;
        this.amountPaid = amountPaid;
    }
    
   
    public int getVehicleId() { return vehicleId; }
    public void setVehicleId(int vehicleId) { this.vehicleId = vehicleId; }
    
    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }
    
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    
    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    
    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }
    
    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }
}

