package models;

public class ParkingSlot {
    private int slotId;
    private String slotNumber;
    private String slotType; 
    private String status;   
    private int floor;
    
    // Constructors
    public ParkingSlot() {}
    
    public ParkingSlot(int slotId, String slotNumber, String slotType, String status, int floor) {
        this.slotId = slotId;
        this.slotNumber = slotNumber;
        this.slotType = slotType;
        this.status = status;
        this.floor = floor;
    }
    
   
    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }
    
    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }
    
    public String getSlotType() { return slotType; }
    public void setSlotType(String slotType) { this.slotType = slotType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }
    
    
    public boolean isAvailable() {
        return "Available".equals(status);
    }
}
