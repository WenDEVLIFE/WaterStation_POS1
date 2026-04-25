package waterstation_pos.waterstation_pos1;

import waterstation_pos.waterstation_pos1.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginForm extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginForm() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Water Station POS - Secure Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // --- Left Panel (Branding) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(41, 128, 185)); // Deep Blue
        leftPanel.setPreferredSize(new Dimension(300, 450));
        leftPanel.setLayout(new GridBagLayout());
        
        JLabel lblLogo = new JLabel("💧", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.PLAIN, 80));
        lblLogo.setForeground(Color.WHITE);
        
        JLabel lblTitle = new JLabel("Water Station", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblSubtitle = new JLabel("Point of Sale System", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitle.setForeground(new Color(200, 230, 255));

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0; gbcLeft.gridy = 0;
        leftPanel.add(lblLogo, gbcLeft);
        gbcLeft.gridy = 1; gbcLeft.insets = new Insets(20, 0, 5, 0);
        leftPanel.add(lblTitle, gbcLeft);
        gbcLeft.gridy = 2; gbcLeft.insets = new Insets(0, 0, 0, 0);
        leftPanel.add(lblSubtitle, gbcLeft);

        add(leftPanel, BorderLayout.WEST);

        // --- Right Panel (Login Form) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.insets = new Insets(10, 10, 10, 10);
        gbcRight.fill = GridBagConstraints.HORIZONTAL;
        gbcRight.weightx = 1.0;

        // Login Header
        JLabel lblLoginHeader = new JLabel("Welcome Back");
        lblLoginHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLoginHeader.setForeground(new Color(50, 50, 50));
        gbcRight.gridx = 0; gbcRight.gridy = 0; gbcRight.gridwidth = 2;
        gbcRight.insets = new Insets(20, 30, 30, 30);
        rightPanel.add(lblLoginHeader, gbcRight);

        // Username Label & Field
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUser.setForeground(Color.GRAY);
        gbcRight.gridy = 1; gbcRight.gridwidth = 2;
        gbcRight.insets = new Insets(0, 30, 5, 30);
        rightPanel.add(lblUser, gbcRight);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtUsername.setPreferredSize(new Dimension(250, 35));
        gbcRight.gridy = 2;
        gbcRight.insets = new Insets(0, 30, 20, 30);
        rightPanel.add(txtUsername, gbcRight);

        // Password Label & Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPass.setForeground(Color.GRAY);
        gbcRight.gridy = 3;
        gbcRight.insets = new Insets(0, 30, 5, 30);
        rightPanel.add(lblPass, gbcRight);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPassword.setPreferredSize(new Dimension(250, 35));
        gbcRight.gridy = 4;
        gbcRight.insets = new Insets(0, 30, 30, 30);
        rightPanel.add(txtPassword, gbcRight);

        // Login Button
        btnLogin = new JButton("LOGIN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(250, 45));
        
        // Add hover effect
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.setBackground(new Color(41, 128, 185));
            }
        });

        btnLogin.addActionListener(e -> attemptLogin());
        
        gbcRight.gridy = 5;
        gbcRight.insets = new Insets(0, 30, 20, 30);
        rightPanel.add(btnLogin, gbcRight);

        add(rightPanel, BorderLayout.CENTER);
        
        // Allow pressing Enter to login
        getRootPane().setDefaultButton(btnLogin);
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
            
            pst.setString(1, username);
            pst.setString(2, password);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String role = rs.getString("role");
                    String fullName = rs.getString("full_name");
                    JOptionPane.showMessageDialog(this, "Welcome " + fullName + "!\nRole: " + role, "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                    
                    this.dispose();
                    new MainFrame(userId, role).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
