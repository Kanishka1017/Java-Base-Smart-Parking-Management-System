package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import dao.ParkingSlotDAO;
import models.ParkingSlot;

public class SlotManagementPanel extends JPanel {
    private JTable slotTable;
    private DefaultTableModel tableModel;
    
    public SlotManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
       
        String[] columns = {"Slot #", "Type", "Status", "Floor"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        slotTable = new JTable(tableModel);
        slotTable.setRowHeight(30);
        
      
        slotTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
           
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column == 2) {
                    String status = (String) table.getValueAt(row, 2);
                    if ("Available".equals(status)) {
                        c.setBackground(Color.GREEN);
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(Color.RED);
                        c.setForeground(Color.WHITE);
                    }
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(slotTable);
        add(scrollPane, BorderLayout.CENTER);
        
      
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadSlots());
        
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        
       
        loadSlots();
    }
    
    private void loadSlots() {
        tableModel.setRowCount(0);
        
        ParkingSlotDAO dao = new ParkingSlotDAO();
        for (ParkingSlot slot : dao.getAllSlots()) {
            Object[] row = {
                slot.getSlotNumber(),
                slot.getSlotType(),
                slot.getStatus(),
                slot.getFloor()
            };
            tableModel.addRow(row);
        }
    }
}
