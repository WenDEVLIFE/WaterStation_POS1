package waterstation_pos.waterstation_pos1.panels;

import waterstation_pos.waterstation_pos1.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InventoryPanel extends JPanel {
    private JTable productsTable, logsTable;
    private DefaultTableModel productsTableModel, logsTableModel;
    private JButton btnRestock, btnRefresh;
    private int selectedProductId = -1;
    private String selectedProductName = "";

    public InventoryPanel() {
        initComponents();
        loadProducts();
        loadLogs();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- Top Panel (Actions) ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBackground(Color.WHITE);
        btnRestock = new JButton("Restock Selected Product");
        btnRefresh = new JButton("Refresh Data");

        actionPanel.add(btnRestock);
        actionPanel.add(btnRefresh);
        add(actionPanel, BorderLayout.NORTH);

        // --- Center Panel (Split Pane for Products & Logs) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // Products Table
        productsTableModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Current Stock", "Threshold"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productsTable = new JTable(productsTableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && productsTable.getSelectedRow() != -1) {
                int row = productsTable.getSelectedRow();
                selectedProductId = (int) productsTableModel.getValueAt(row, 0);
                selectedProductName = productsTableModel.getValueAt(row, 1).toString();
            }
        });

        JScrollPane scrollProducts = new JScrollPane(productsTable);
        scrollProducts.setBorder(BorderFactory.createTitledBorder("Current Inventory Status"));
        scrollProducts.getViewport().setBackground(Color.WHITE);

        // Logs Table
        logsTableModel = new DefaultTableModel(new String[]{"Log ID", "Product", "Change Amount", "Reason", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        logsTable = new JTable(logsTableModel);
        JScrollPane scrollLogs = new JScrollPane(logsTable);
        scrollLogs.setBorder(BorderFactory.createTitledBorder("Inventory Logs"));
        scrollLogs.getViewport().setBackground(Color.WHITE);

        splitPane.setTopComponent(scrollProducts);
        splitPane.setBottomComponent(scrollLogs);
        
        add(splitPane, BorderLayout.CENTER);

        // --- Event Listeners ---
        btnRefresh.addActionListener(e -> {
            loadProducts();
            loadLogs();
        });

        btnRestock.addActionListener(e -> handleRestock());
    }

    private void loadProducts() {
        productsTableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, stock_quantity, low_stock_threshold FROM products ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                productsTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("low_stock_threshold")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading inventory products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadLogs() {
        logsTableModel.setRowCount(0);
        String query = "SELECT l.id, p.name, l.change_amount, l.reason, l.log_date " +
                       "FROM inventory_logs l JOIN products p ON l.product_id = p.id ORDER BY l.log_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                logsTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("change_amount"),
                        rs.getString("reason"),
                        rs.getTimestamp("log_date")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading inventory logs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRestock() {
        if (selectedProductId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product from the inventory list to restock.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Prompt for Quantity
        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity to restock for: " + selectedProductName, "Restock Item", JOptionPane.QUESTION_MESSAGE);
        if (qtyStr == null || qtyStr.trim().isEmpty()) return;

        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) throw new NumberFormatException("Quantity must be positive.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive number for quantity.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Prompt for Reason
        String reason = JOptionPane.showInputDialog(this, "Enter reason for restock (e.g., 'Supplier Delivery'):", "Restock Reason", JOptionPane.QUESTION_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) {
            reason = "Manual Restock";
        }

        // Execute Stored Procedure
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL sp_restock_product(?, ?, ?)}")) {
            
            stmt.setInt(1, selectedProductId);
            stmt.setInt(2, quantity);
            stmt.setString(3, reason);
            
            stmt.execute();
            
            JOptionPane.showMessageDialog(this, "Product restocked successfully.");
            loadProducts();
            loadLogs();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error restocking product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
