package waterstation_pos.waterstation_pos1.panels;

import waterstation_pos.waterstation_pos1.util.DatabaseConnection;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardPanel extends JPanel {
    private JTable dailySalesTable;
    private DefaultTableModel dailySalesTableModel;
    
    private JTable lowStockTable;
    private DefaultTableModel lowStockTableModel;

    private JPanel chartContainer;

    public DashboardPanel() {
        initComponents();
        refreshDashboard();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- Top Half: Analytical Dashboard (Visuals) ---
        chartContainer = new JPanel(new GridLayout(1, 2, 10, 10));
        chartContainer.setBackground(Color.WHITE);
        chartContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(chartContainer, BorderLayout.CENTER);

        // --- Bottom Half: Reporting (Tabbed Data) ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(getWidth(), 300));
        
        // Tab 1: Daily Sales Report
        dailySalesTableModel = new DefaultTableModel(new String[]{"Sale ID", "Customer", "Cashier", "Payment Method", "Total Amount", "Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        dailySalesTable = new JTable(dailySalesTableModel);
        JScrollPane scrollDailySales = new JScrollPane(dailySalesTable);
        scrollDailySales.getViewport().setBackground(Color.WHITE);
        tabbedPane.addTab("Daily Sales Report", scrollDailySales);

        // Tab 2: Low Stock Warning
        lowStockTableModel = new DefaultTableModel(new String[]{"Product ID", "Product Name", "Current Stock", "Threshold"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        lowStockTable = new JTable(lowStockTableModel);
        JScrollPane scrollLowStock = new JScrollPane(lowStockTable);
        scrollLowStock.getViewport().setBackground(Color.WHITE);
        tabbedPane.addTab("Low Stock Warning", scrollLowStock);

        add(tabbedPane, BorderLayout.SOUTH);

        // Refresh Button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.WHITE);
        JButton btnRefresh = new JButton("Refresh Dashboard");
        btnRefresh.addActionListener(e -> refreshDashboard());
        topPanel.add(btnRefresh);
        add(topPanel, BorderLayout.NORTH);
    }

    private void refreshDashboard() {
        // Load Charts
        chartContainer.removeAll();
        chartContainer.add(createMonthlySalesChart());
        chartContainer.add(createCategoryPerformanceChart());
        chartContainer.revalidate();
        chartContainer.repaint();

        // Load Tables
        loadDailySales();
        loadLowStock();
    }

    private ChartPanel createMonthlySalesChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        String query = "SELECT MONTHNAME(sale_date) as month_name, SUM(total_amount) as total FROM sales GROUP BY MONTH(sale_date), month_name ORDER BY MONTH(sale_date)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                dataset.addValue(rs.getDouble("total"), "Sales", rs.getString("month_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Monthly Sales Trend",
                "Month",
                "Revenue (₱)",
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                false, true, false
        );
        barChart.setBackgroundPaint(Color.WHITE);
        
        return new ChartPanel(barChart);
    }

    private ChartPanel createCategoryPerformanceChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        String query = "SELECT c.name, SUM(sd.subtotal) as total " +
                       "FROM sales_details sd " +
                       "JOIN products p ON sd.product_id = p.id " +
                       "JOIN categories c ON p.category_id = c.id " +
                       "GROUP BY c.id, c.name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                dataset.setValue(rs.getString("name"), rs.getDouble("total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Revenue by Product Category",
                dataset,
                true, true, false
        );
        pieChart.setBackgroundPaint(Color.WHITE);
        
        return new ChartPanel(pieChart);
    }

    private void loadDailySales() {
        dailySalesTableModel.setRowCount(0);
        String query = "SELECT s.id, CONCAT(c.first_name, ' ', c.last_name) as customer_name, u.full_name as cashier_name, s.payment_method, s.total_amount, TIME(s.sale_date) as sale_time " +
                       "FROM sales s " +
                       "LEFT JOIN customers c ON s.customer_id = c.id " +
                       "JOIN users u ON s.user_id = u.id " +
                       "WHERE DATE(s.sale_date) = CURRENT_DATE " +
                       "ORDER BY s.sale_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String customer = rs.getString("customer_name") != null ? rs.getString("customer_name") : "Walk-in";
                dailySalesTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        customer,
                        rs.getString("cashier_name"),
                        rs.getString("payment_method"),
                        rs.getBigDecimal("total_amount"),
                        rs.getTime("sale_time")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLowStock() {
        lowStockTableModel.setRowCount(0);
        String query = "SELECT id, name, stock_quantity, low_stock_threshold FROM products WHERE stock_quantity <= low_stock_threshold ORDER BY stock_quantity ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                lowStockTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("stock_quantity"),
                        rs.getInt("low_stock_threshold")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
