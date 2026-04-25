package waterstation_pos.waterstation_pos1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import waterstation_pos.waterstation_pos1.panels.*;

public class MainFrame extends JFrame {
    
    private String role;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public MainFrame(String role) {
        this.role = role;
        initComponents();
    }

    private void initComponents() {
        setTitle("Water Station POS - Main Dashboard (" + role + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        sidebar.setBackground(new Color(41, 128, 185)); // A nice modern blue

        JLabel lblTitle = new JLabel("Navigation", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebar.add(lblTitle);

        String[] navItems = {"Dashboard", "Sales (POS)", "Inventory", "Products", "Categories", "Customers"};
        for (String item : navItems) {
            JButton btn = createNavButton(item);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        sidebar.add(Box.createVerticalGlue());
        
        JButton btnLogout = createNavButton("Logout");
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginForm().setVisible(true);
        });
        sidebar.add(btnLogout);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        add(sidebar, BorderLayout.WEST);

        // Main Content Area
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);

        // Real CRUD Panels
        contentPanel.add(createDummyPanel("Dashboard Area"), "Dashboard");
        contentPanel.add(createDummyPanel("Sales (POS) Area"), "Sales (POS)");
        contentPanel.add(new InventoryPanel(), "Inventory");
        contentPanel.add(new ProductPanel(), "Products");
        contentPanel.add(new CategoryPanel(), "Categories");
        contentPanel.add(new CustomerPanel(), "Customers");

        add(contentPanel, BorderLayout.CENTER);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(52, 152, 219)); // Lighter blue
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!text.equals("Logout")) {
                    cardLayout.show(contentPanel, text);
                }
            }
        });
        
        return btn;
    }

    private JPanel createDummyPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(Color.DARK_GRAY);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
