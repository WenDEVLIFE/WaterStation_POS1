package waterstation_pos.waterstation_pos1.panels;

import waterstation_pos.waterstation_pos1.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProductPanel extends JPanel {
    private JTextField txtName, txtPrice, txtStock, txtThreshold;
    private JTextArea txtDescription;
    private JComboBox<CategoryItem> cbCategory;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedId = -1;

    public ProductPanel() {
        initComponents();
        loadCategories();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- Form Panel (North) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Product Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        txtName = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(txtName, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Category:"), gbc);
        cbCategory = new JComboBox<>();
        gbc.gridx = 3; gbc.gridy = 0;
        formPanel.add(cbCategory, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Price:"), gbc);
        txtPrice = new JTextField(10);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtPrice, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Stock:"), gbc);
        txtStock = new JTextField(10);
        gbc.gridx = 3; gbc.gridy = 1;
        formPanel.add(txtStock, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Low Stock Threshold:"), gbc);
        txtThreshold = new JTextField(10);
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(txtThreshold, gbc);

        gbc.gridx = 2; gbc.gridy = 2;
        formPanel.add(new JLabel("Description:"), gbc);
        txtDescription = new JTextArea(2, 15);
        txtDescription.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        gbc.gridx = 3; gbc.gridy = 2;
        formPanel.add(scrollDesc, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // --- Table Panel (Center) ---
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Category", "Price", "Stock", "Threshold", "CatID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        // Hide Category ID column
        table.removeColumn(table.getColumnModel().getColumn(6));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedId = (int) tableModel.getValueAt(row, 0);
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtPrice.setText(tableModel.getValueAt(row, 3).toString());
                txtStock.setText(tableModel.getValueAt(row, 4).toString());
                txtThreshold.setText(tableModel.getValueAt(row, 5).toString());
                
                int catId = (int) tableModel.getValueAt(row, 6);
                for (int i = 0; i < cbCategory.getItemCount(); i++) {
                    if (cbCategory.getItemAt(i).id == catId) {
                        cbCategory.setSelectedIndex(i);
                        break;
                    }
                }
                
                // Description requires another query or can be omitted from basic view, let's query it.
                loadDescription(selectedId);
            }
        });

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Products List"));
        scrollTable.getViewport().setBackground(Color.WHITE);
        add(scrollTable, BorderLayout.CENTER);

        // --- Event Listeners ---
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> clearForm());
    }

    private void loadCategories() {
        cbCategory.removeAllItems();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM categories ORDER BY name");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                cbCategory.addItem(new CategoryItem(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String query = "SELECT p.id, p.name, c.name as category_name, p.price, p.stock_quantity, p.low_stock_threshold, p.category_id " +
                       "FROM products p JOIN categories c ON p.category_id = c.id ORDER BY p.name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category_name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("low_stock_threshold"),
                        rs.getInt("category_id")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDescription(int productId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT description FROM products WHERE id=?")) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                txtDescription.setText(rs.getString("description"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addProduct() {
        if (cbCategory.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        CategoryItem category = (CategoryItem) cbCategory.getSelectedItem();
        String desc = txtDescription.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String stockStr = txtStock.getText().trim();
        String threshStr = txtThreshold.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Price, and Stock are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO products (category_id, name, description, price, stock_quantity, low_stock_threshold) VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, category.id);
            stmt.setString(2, name);
            stmt.setString(3, desc.isEmpty() ? null : desc);
            stmt.setBigDecimal(4, new java.math.BigDecimal(priceStr));
            stmt.setInt(5, Integer.parseInt(stockStr));
            stmt.setInt(6, threshStr.isEmpty() ? 5 : Integer.parseInt(threshStr));
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Product added successfully.");
            clearForm();
            loadData();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price, Stock, and Threshold must be numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduct() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        CategoryItem category = (CategoryItem) cbCategory.getSelectedItem();
        String desc = txtDescription.getText().trim();
        String priceStr = txtPrice.getText().trim();
        String stockStr = txtStock.getText().trim();
        String threshStr = txtThreshold.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Price, and Stock are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE products SET category_id=?, name=?, description=?, price=?, stock_quantity=?, low_stock_threshold=? WHERE id=?")) {
            stmt.setInt(1, category.id);
            stmt.setString(2, name);
            stmt.setString(3, desc.isEmpty() ? null : desc);
            stmt.setBigDecimal(4, new java.math.BigDecimal(priceStr));
            stmt.setInt(5, Integer.parseInt(stockStr));
            stmt.setInt(6, threshStr.isEmpty() ? 5 : Integer.parseInt(threshStr));
            stmt.setInt(7, selectedId);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Product updated successfully.");
            clearForm();
            loadData();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Price, Stock, and Threshold must be numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this product?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id=?")) {
                stmt.setInt(1, selectedId);
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Product deleted successfully.");
                clearForm();
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting product: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtStock.setText("");
        txtThreshold.setText("");
        if (cbCategory.getItemCount() > 0) cbCategory.setSelectedIndex(0);
        table.clearSelection();
    }

    // Helper class for ComboBox
    class CategoryItem {
        int id;
        String name;

        public CategoryItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
