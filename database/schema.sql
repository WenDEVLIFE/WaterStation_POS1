-- =============================================
-- Water Station POS with Inventory System
-- Final Project Schema
-- Database: MySQL (XAMPP)
-- =============================================

-- CREATE DATABASE IF NOT EXISTS water_station_db;
-- USE water_station_db;

-- 1. DROP TABLES (for clean re-run)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS inventory_logs;
DROP TABLE IF EXISTS sales_details;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. CREATE TABLES

-- Categories Table
CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB;

-- Products Table
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    low_stock_threshold INT DEFAULT 5,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Customers Table
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Users Table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'Cashier', 'Manager') DEFAULT 'Cashier',
    full_name VARCHAR(150),
    last_login DATETIME
) ENGINE=InnoDB;

-- Sales Table
CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NULL,
    user_id INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payment_method ENUM('Cash', 'G-Cash', 'Credit Card') DEFAULT 'Cash',
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- Sales Details Table
CREATE TABLE sales_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) AS (quantity * unit_price) STORED,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

-- Inventory Logs Table
CREATE TABLE inventory_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    change_amount INT NOT NULL,
    reason VARCHAR(255),
    log_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. INDEXING
CREATE INDEX idx_product_name ON products(name);
CREATE INDEX idx_sale_date ON sales(sale_date);

-- 4. TRIGGERS

-- Trigger: Auto-deduct stock after sale
DELIMITER //
CREATE TRIGGER trg_after_sale_detail_insert
AFTER INSERT ON sales_details
FOR EACH ROW
BEGIN
    UPDATE products 
    SET stock_quantity = stock_quantity - NEW.quantity
    WHERE id = NEW.product_id;
    
    INSERT INTO inventory_logs (product_id, change_amount, reason)
    VALUES (NEW.product_id, -NEW.quantity, CONCAT('Sale #', NEW.sale_id));
END //
DELIMITER ;

-- Trigger: Prevent negative stock
DELIMITER //
CREATE TRIGGER trg_before_sale_detail_insert
BEFORE INSERT ON sales_details
FOR EACH ROW
BEGIN
    DECLARE available_stock INT;
    SELECT stock_quantity INTO available_stock FROM products WHERE id = NEW.product_id;
    
    IF available_stock < NEW.quantity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient stock for this product.';
    END IF;
END //
DELIMITER ;

-- 5. STORED PROCEDURES

-- Procedure: Process Sale (Demonstrating TCL)
DELIMITER //
CREATE PROCEDURE sp_process_sale(
    IN p_customer_id INT,
    IN p_user_id INT,
    IN p_payment_method VARCHAR(50),
    OUT p_sale_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Transaction failed. Sale rolled back.';
    END;

    START TRANSACTION;
        INSERT INTO sales (customer_id, user_id, total_amount, payment_method)
        VALUES (p_customer_id, p_user_id, 0.00, p_payment_method);
        
        SET p_sale_id = LAST_INSERT_ID();
    COMMIT;
END //
DELIMITER ;

-- Procedure: Restock Product
DELIMITER //
CREATE PROCEDURE sp_restock_product(
    IN p_product_id INT,
    IN p_quantity INT,
    IN p_reason VARCHAR(255)
)
BEGIN
    START TRANSACTION;
        UPDATE products SET stock_quantity = stock_quantity + p_quantity WHERE id = p_product_id;
        INSERT INTO inventory_logs (product_id, change_amount, reason) VALUES (p_product_id, p_quantity, p_reason);
    COMMIT;
END //
DELIMITER ;

-- 6. SEED DATA

-- Categories (5+)
INSERT INTO categories (name, description) VALUES 
('Refills', 'Water refill services for various gallon sizes'),
('Bottled Water', 'Pre-filled bottled water products'),
('Dispensers', 'Water dispensing equipment'),
('Accessories', 'Caps, seals, and other parts'),
('Services', 'Cleaning and maintenance services');

-- Products (20+)
INSERT INTO products (category_id, name, price, stock_quantity) VALUES 
(1, '5-Gallon Slim Refill', 25.00, 100),
(1, '5-Gallon Round Refill', 25.00, 100),
(1, 'Alkaline 5-Gallon Refill', 50.00, 50),
(1, 'Mineral 5-Gallon Refill', 35.00, 75),
(2, '500ml Bottled Water', 10.00, 200),
(2, '1L Bottled Water', 18.00, 150),
(2, '6L Bottled Water', 65.00, 40),
(2, '10L Bottled Water', 110.00, 30),
(3, 'Desktop Dispenser', 450.00, 10),
(3, 'Standing Dispenser', 1200.00, 5),
(3, 'Manual Hand Pump', 150.00, 25),
(3, 'Electric Rechargeable Pump', 350.00, 15),
(4, 'Non-Spill Cap (Blue)', 5.00, 500),
(4, 'Non-Spill Cap (White)', 5.00, 500),
(4, 'Gallon Seal (Plastic)', 1.00, 1000),
(4, 'Gallon Handle', 45.00, 50),
(5, 'Gallon Cleaning Service', 15.00, 999),
(1, '2.5 Gallon Slim Refill', 15.00, 60),
(1, 'Distilled 5-Gallon Refill', 45.00, 45),
(2, '350ml Bottled Water (Box 24)', 220.00, 20);

-- Customers (10+)
INSERT INTO customers (first_name, last_name, phone, address) VALUES 
('Juan', 'Dela Cruz', '09171234567', 'Quezon City'),
('Maria', 'Santos', '09187654321', 'Manila'),
('Pedro', 'Penduko', '09192223334', 'Makati'),
('Elena', 'Reyes', '09205556667', 'Pasig'),
('Jose', 'Rizal', '09219998887', 'Calamba'),
('Andres', 'Bonifacio', '09224445556', 'Tondo'),
('Gabriela', 'Silang', '09231112223', 'Ilocos'),
('Liza', 'Soberano', '09248887776', 'Quezon City'),
('Daniel', 'Padilla', '09253334442', 'North Park'),
('Catriona', 'Gray', '09267778889', 'Bicol');

-- Users
INSERT INTO users (username, password, role, full_name) VALUES 
('admin', 'admin123', 'Admin', 'System Administrator'),
('cashier1', 'cashier123', 'Cashier', 'Jane Doe');

-- Initial Sales (20+ Records - For Reporting)
-- Note: Manually inserting into sales and sales_details to simulate history
-- We will use a script-friendly approach here.
INSERT INTO sales (customer_id, user_id, total_amount, sale_date) VALUES 
(1, 2, 75.00, '2024-04-01 10:00:00'),
(2, 2, 25.00, '2024-04-02 11:30:00'),
(3, 2, 150.00, '2024-04-03 14:20:00'),
(4, 2, 50.00, '2024-04-04 09:15:00'),
(1, 2, 100.00, '2024-04-05 16:45:00'),
(5, 2, 450.00, '2024-04-06 10:10:00'),
(6, 2, 35.00, '2024-04-07 12:00:00'),
(7, 2, 1200.00, '2024-04-08 15:30:00'),
(8, 2, 20.00, '2024-04-09 17:00:00'),
(9, 2, 65.00, '2024-04-10 08:45:00'),
(10, 2, 18.00, '2024-04-11 11:15:00'),
(1, 2, 25.00, '2024-04-12 13:20:00'),
(2, 2, 50.00, '2024-04-13 14:50:00'),
(3, 2, 350.00, '2024-04-14 10:30:00'),
(4, 2, 10.00, '2024-04-15 16:00:00'),
(5, 2, 25.00, '2024-04-16 09:00:00'),
(6, 2, 110.00, '2024-04-17 11:45:00'),
(7, 2, 25.00, '2024-04-18 13:10:00'),
(8, 2, 45.00, '2024-04-19 15:25:00'),
(9, 2, 220.00, '2024-04-20 17:40:00');

-- Link details (Simplified for demo data)
INSERT INTO sales_details (sale_id, product_id, quantity, unit_price) VALUES 
(1, 1, 3, 25.00),
(2, 2, 1, 25.00),
(3, 11, 1, 150.00),
(4, 1, 2, 25.00),
(5, 1, 4, 25.00),
(6, 9, 1, 450.00),
(7, 4, 1, 35.00),
(8, 10, 1, 1200.00),
(9, 13, 4, 5.00),
(10, 7, 1, 65.00),
(11, 6, 1, 18.00),
(12, 2, 1, 25.00),
(13, 3, 1, 50.00),
(14, 12, 1, 350.00),
(15, 5, 1, 10.00),
(16, 1, 1, 25.00),
(17, 8, 1, 110.00),
(18, 2, 1, 25.00),
(19, 16, 1, 45.00),
(20, 20, 1, 220.00);

-- Update sales totals based on details (Manual sync for seed data)
UPDATE sales s SET total_amount = (SELECT SUM(subtotal) FROM sales_details WHERE sale_id = s.id);
