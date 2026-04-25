package waterstation_pos.waterstation_pos1.panels;

import waterstation_pos.waterstation_pos1.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerPanel extends JPanel {
    private JTextField txtFirstName, txtLastName, txtPhone, txtAddress;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private int selectedId = -1;

    public CustomerPanel() {
        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- Form Panel (North) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        txtFirstName = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(txtFirstName, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Last Name:"), gbc);
        txtLastName = new JTextField(15);
        gbc.gridx = 3; gbc.gridy = 0;
        formPanel.add(txtLastName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Phone:"), gbc);
        txtPhone = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(txtPhone, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Address:"), gbc);
        txtAddress = new JTextField(15);
        gbc.gridx = 3; gbc.gridy = 1;
        formPanel.add(txtAddress, gbc);

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

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // --- Table Panel (Center) ---
        tableModel = new DefaultTableModel(new String[]{"ID", "First Name", "Last Name", "Phone", "Address"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedId = (int) tableModel.getValueAt(row, 0);
                txtFirstName.setText(tableModel.getValueAt(row, 1).toString());
                txtLastName.setText(tableModel.getValueAt(row, 2).toString());
                txtPhone.setText(tableModel.getValueAt(row, 3) != null ? tableModel.getValueAt(row, 3).toString() : "");
                txtAddress.setText(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "");
            }
        });

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Customers List"));
        scrollTable.getViewport().setBackground(Color.WHITE);
        add(scrollTable, BorderLayout.CENTER);

        // --- Event Listeners ---
        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearForm());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM customers ORDER BY id");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("phone"),
                        rs.getString("address")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addCustomer() {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name and Last Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO customers (first_name, last_name, phone, address) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phone.isEmpty() ? null : phone);
            stmt.setString(4, address.isEmpty() ? null : address);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Customer added successfully.");
            clearForm();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First Name and Last Name are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE customers SET first_name=?, last_name=?, phone=?, address=? WHERE id=?")) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, phone.isEmpty() ? null : phone);
            stmt.setString(4, address.isEmpty() ? null : address);
            stmt.setInt(5, selectedId);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Customer updated successfully.");
            clearForm();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM customers WHERE id=?")) {
                stmt.setInt(1, selectedId);
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Customer deleted successfully.");
                clearForm();
                loadData();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        selectedId = -1;
        txtFirstName.setText("");
        txtLastName.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
        table.clearSelection();
    }
}
