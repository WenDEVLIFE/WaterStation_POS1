package waterstation_pos.waterstation_pos1.panels;

import waterstation_pos.waterstation_pos1.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class SalesPanel extends JPanel {
    private int userId;
    
    // UI Components
    private JTable productsTable, cartTable;
    private DefaultTableModel productsTableModel, cartTableModel;
    private JSpinner spinQuantity;
    private JButton btnAddToCart, btnRemoveFromCart, btnProcessSale, btnRefreshProducts;
    private JLabel lblTotalAmount;
    private JComboBox<CustomerItem> cbCustomer;
    private JComboBox<String> cbPaymentMethod;
    
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public SalesPanel(int userId) {
        this.userId = userId;
        initComponents();
        loadProducts();
        loadCustomers();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        // ================= LEFT: PRODUCTS LIST =================
        JPanel productsPanel = new JPanel(new BorderLayout());
        productsPanel.setBorder(BorderFactory.createTitledBorder("Available Products"));
        productsPanel.setBackground(Color.WHITE);

        productsTableModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productsTable = new JTable(productsTableModel);
        productsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        productsPanel.add(new JScrollPane(productsTable), BorderLayout.CENTER);

        JPanel addActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addActionPanel.setBackground(Color.WHITE);
        addActionPanel.add(new JLabel("Qty:"));
        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        addActionPanel.add(spinQuantity);
        
        btnAddToCart = new JButton("Add to Cart");
        btnRefreshProducts = new JButton("Refresh");
        addActionPanel.add(btnAddToCart);
        addActionPanel.add(btnRefreshProducts);

        productsPanel.add(addActionPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(productsPanel);

        // ================= RIGHT: CART & CHECKOUT =================
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        cartPanel.setBackground(Color.WHITE);

        cartTableModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Qty", "Unit Price", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        cartTable = new JTable(cartTableModel);
        cartPanel.add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel checkoutPanel = new JPanel(new GridBagLayout());
        checkoutPanel.setBackground(Color.WHITE);
        checkoutPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Total Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        lblTotalAmount = new JLabel("Total: ₱ 0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalAmount.setForeground(new Color(46, 204, 113)); // Green
        checkoutPanel.add(lblTotalAmount, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        checkoutPanel.add(new JLabel("Customer:"), gbc);
        cbCustomer = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 1;
        checkoutPanel.add(cbCustomer, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        checkoutPanel.add(new JLabel("Payment:"), gbc);
        cbPaymentMethod = new JComboBox<>(new String[]{"Cash", "G-Cash", "Credit Card"});
        gbc.gridx = 1; gbc.gridy = 2;
        checkoutPanel.add(cbPaymentMethod, gbc);

        // Buttons
        JPanel cartActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cartActions.setBackground(Color.WHITE);
        btnRemoveFromCart = new JButton("Remove Selected");
        btnProcessSale = new JButton("Process Sale");
        btnProcessSale.setBackground(new Color(52, 152, 219));
        btnProcessSale.setForeground(Color.WHITE);
        btnProcessSale.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        cartActions.add(btnRemoveFromCart);
        cartActions.add(btnProcessSale);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        checkoutPanel.add(cartActions, gbc);

        cartPanel.add(checkoutPanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(cartPanel);

        add(splitPane, BorderLayout.CENTER);

        // ================= EVENT LISTENERS =================
        btnRefreshProducts.addActionListener(e -> loadProducts());
        btnAddToCart.addActionListener(e -> addToCart());
        btnRemoveFromCart.addActionListener(e -> removeFromCart());
        btnProcessSale.addActionListener(e -> processSale());
    }

    private void loadProducts() {
        productsTableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, price, stock_quantity FROM products WHERE stock_quantity > 0 ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                productsTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock_quantity")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomers() {
        cbCustomer.removeAllItems();
        cbCustomer.addItem(new CustomerItem(0, "Walk-in Customer")); // 0 acts as NULL
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, first_name, last_name FROM customers ORDER BY first_name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                cbCustomer.addItem(new CustomerItem(rs.getInt("id"), fullName));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToCart() {
        int selectedRow = productsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to add to cart.");
            return;
        }

        int productId = (int) productsTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productsTableModel.getValueAt(selectedRow, 1);
        BigDecimal price = (BigDecimal) productsTableModel.getValueAt(selectedRow, 2);
        int availableStock = (int) productsTableModel.getValueAt(selectedRow, 3);
        int qtyToAdd = (int) spinQuantity.getValue();

        // Check if already in cart to calculate total requested quantity
        int currentCartQty = 0;
        int existingCartRow = -1;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            if ((int) cartTableModel.getValueAt(i, 0) == productId) {
                currentCartQty = (int) cartTableModel.getValueAt(i, 2);
                existingCartRow = i;
                break;
            }
        }

        if (currentCartQty + qtyToAdd > availableStock) {
            JOptionPane.showMessageDialog(this, "Not enough stock available. Remaining: " + (availableStock - currentCartQty));
            return;
        }

        BigDecimal subtotal = price.multiply(new BigDecimal(qtyToAdd));

        if (existingCartRow != -1) {
            int newQty = currentCartQty + qtyToAdd;
            BigDecimal newSubtotal = price.multiply(new BigDecimal(newQty));
            cartTableModel.setValueAt(newQty, existingCartRow, 2);
            cartTableModel.setValueAt(newSubtotal, existingCartRow, 4);
        } else {
            cartTableModel.addRow(new Object[]{productId, productName, qtyToAdd, price, subtotal});
        }

        updateTotal();
        spinQuantity.setValue(1); // Reset spinner
    }

    private void removeFromCart() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item in the cart to remove.");
            return;
        }
        cartTableModel.removeRow(selectedRow);
        updateTotal();
    }

    private void updateTotal() {
        totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < cartTableModel.getRowCount(); i++) {
            BigDecimal subtotal = (BigDecimal) cartTableModel.getValueAt(i, 4);
            totalAmount = totalAmount.add(subtotal);
        }
        lblTotalAmount.setText("Total: ₱ " + String.format("%.2f", totalAmount));
    }

    private void processSale() {
        if (cartTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        CustomerItem customer = (CustomerItem) cbCustomer.getSelectedItem();
        String paymentMethod = (String) cbPaymentMethod.getSelectedItem();
        Integer customerId = (customer.id == 0) ? null : customer.id;

        int confirm = JOptionPane.showConfirmDialog(this, "Process sale for ₱" + String.format("%.2f", totalAmount) + "?", "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            
            // Integrate TCL (Transaction Control)
            conn.setAutoCommit(false);

            // 1. Insert Sale
            String insertSaleQuery = "INSERT INTO sales (customer_id, user_id, total_amount, payment_method) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstSale = conn.prepareStatement(insertSaleQuery, Statement.RETURN_GENERATED_KEYS)) {
                if (customerId == null) pstSale.setNull(1, java.sql.Types.INTEGER);
                else pstSale.setInt(1, customerId);
                
                pstSale.setInt(2, userId);
                pstSale.setBigDecimal(3, totalAmount);
                pstSale.setString(4, paymentMethod);
                pstSale.executeUpdate();

                // Get Sale ID
                ResultSet rsKeys = pstSale.getGeneratedKeys();
                int saleId = -1;
                if (rsKeys.next()) saleId = rsKeys.getInt(1);
                
                if (saleId == -1) throw new Exception("Failed to generate Sale ID.");

                // 2. Insert Sale Details
                String insertDetailQuery = "INSERT INTO sales_details (sale_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstDetail = conn.prepareStatement(insertDetailQuery)) {
                    for (int i = 0; i < cartTableModel.getRowCount(); i++) {
                        int productId = (int) cartTableModel.getValueAt(i, 0);
                        int qty = (int) cartTableModel.getValueAt(i, 2);
                        BigDecimal price = (BigDecimal) cartTableModel.getValueAt(i, 3);
                        
                        pstDetail.setInt(1, saleId);
                        pstDetail.setInt(2, productId);
                        pstDetail.setInt(3, qty);
                        pstDetail.setBigDecimal(4, price);
                        pstDetail.addBatch();
                    }
                    pstDetail.executeBatch();
                }
            }

            // Commit Transaction
            conn.commit();
            JOptionPane.showMessageDialog(this, "Sale Processed Successfully!");
            
            // Reset UI
            cartTableModel.setRowCount(0);
            updateTotal();
            loadProducts();
            cbCustomer.setSelectedIndex(0);

        } catch (Exception e) {
            e.printStackTrace();
            // Rollback on error
            if (conn != null) {
                try {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Transaction Failed. Rolled back.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception re) {
                    re.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception ce) {
                    ce.printStackTrace();
                }
            }
        }
    }

    // Helper Class
    class CustomerItem {
        int id;
        String name;
        public CustomerItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        @Override
        public String toString() { return name; }
    }
}
