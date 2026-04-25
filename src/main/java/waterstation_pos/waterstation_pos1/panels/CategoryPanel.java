package waterstation_pos.waterstation_pos1.panels;

import waterstation_pos.waterstation_pos1.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CategoryPanel extends JPanel {
    private JTextField txtName;
    private JTextArea txtDescription;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedId = -1;

    public CategoryPanel() {
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- Form Panel (North) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Category Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        txtName = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(scrollDesc, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // --- Table Panel (Center) ---
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent direct cell editing
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedId = (int) tableModel.getValueAt(row, 0);
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                Object desc = tableModel.getValueAt(row, 2);
                txtDescription.setText(desc != null ? desc.toString() : "");
            }
        });

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Categories List"));
        scrollTable.getViewport().setBackground(Color.WHITE);
        add(scrollTable, BorderLayout.CENTER);

        // --- Event Listeners ---
        btnAdd.addActionListener(e -> addCategory());
        btnUpdate.addActionListener(e -> updateCategory());
        btnDelete.addActionListener(e -> deleteCategory());
        btnClear.addActionListener(e -> clearForm());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM categories ORDER BY id");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading categories: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCategory() {
        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO categories (name, description) VALUES (?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Category added successfully.");
            clearForm();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding category: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCategory() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        String description = txtDescription.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE categories SET name=?, description=? WHERE id=?")) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setInt(3, selectedId);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Category updated successfully.");
            clearForm();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating category: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCategory() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this category?\nThis will also delete associated products.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
                stmt.setInt(1, selectedId);
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Category deleted successfully.");
                clearForm();
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting category: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtDescription.setText("");
        table.clearSelection();
    }
}
