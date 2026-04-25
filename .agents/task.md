# 📋 Project Task List: Water Station POS with Inventory System

## Phase 1: Database Architecture & Documentation (40%)
- [x] **Database Planning**
    - [x] Define Purpose, Scope, and Assumptions.
    - [x] Define User Roles & Permissions (Admin, Cashier).
- [x] **Data Requirements**
    - [x] List entities: Users, Products, Categories, Inventory, Sales, SalesDetails, Customers, Payments.
- [x] **Database Design**
    - [x] Create Conceptual Design (ER Diagram).
    - [x] Perform Normalization (UNF → 1NF → 2NF → 3NF).
    - [x] Identify Functional, Partial, and Transitive Dependencies.
- [x] **Physical Design**
    - [x] Define Table Structures, Data Types, and Constraints (PK, FK, NOT NULL, CHECK).
    - [x] Plan Indexing strategy.

## Phase 2: MySQL Implementation (Backend)
- [x] **Schema Creation**
    - [x] Run SQL scripts for all tables and constraints.
- [x] **Advanced Features**
    - [x] Implement `process_sale` Stored Procedure (with parameters).
    - [x] Implement `restock` Stored Procedure.
    - [x] Implement `auto_deduct_stock` Trigger.
    - [x] Implement `prevent_negative_stock` Trigger.
- [ ] **Security (DCL)**
    - [ ] Create DB users and assign `GRANT` privileges.
- [x] **Seed Data (Requirement Checklist)**
    - [x] Insert 5+ Categories.
    - [x] Insert 20+ Products.
    - [x] Insert 10+ Customers.
    - [x] Insert 20+ Initial Sales Records for reporting.

## Phase 3: Java Swing UI Implementation (40%)
- [x] **Project Setup**
    - [x] Configure `pom.xml` with dependencies (MySQL Connector, FlatLaf, JFreeChart).
    - [x] Create `DatabaseConnection` utility class.
- [x] **Base UI Framework**
    - [x] Setup Main Frame with Sidebar/Navigation.
    - [x] Integrate FlatLaf for modern aesthetics.
- [x] **CRUD Modules**
    - [x] Product Management (Add, Update, Delete, View).
    - [x] Category Management.
    - [x] Customer Records.
    - [x] Inventory Tracking Module.
- [x] **Sales Processing System**
    - [x] Create Transaction UI (Multi-product cart).
    - [x] Implement Automated Total Computation.
    - [x] Integrate TCL (Transaction Control) in Java logic.

## Phase 4: Business Intelligence & Reporting (10%)
- [x] **Analytical Dashboard**
    - [x] Monthly Sales Trend Chart (Line/Bar).
    - [x] Product Category Performance (Pie).
- [x] **Reporting**
    - [x] Daily Sales Report.
    - [x] Low Stock Warning Report.

## Phase 5: Finalization & Presentation (10%)
- [ ] **Validation & Error Handling**
    - [ ] Test all forms for user input validation.
    - [ ] Verify immediate UIreflection of DB changes.
- [ ] **Documentation Cleanup**
    - [ ] Generate final PDF Documentation.
    - [ ] Export final `.sql` script.
- [ ] **Defense Prep**
    - [ ] Prepare live transaction demonstration.
    - [ ] Prepare explanation for Normalization and SP logic.
