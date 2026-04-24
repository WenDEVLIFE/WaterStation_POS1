# 📋 Project Task List: Water Station POS with Inventory System

## Phase 1: Database Architecture & Documentation (40%)
- [ ] **Database Planning**
    - [ ] Define Purpose, Scope, and Assumptions.
    - [ ] Define User Roles & Permissions (Admin, Cashier).
- [ ] **Data Requirements**
    - [ ] List entities: Users, Products, Categories, Inventory, Sales, SalesDetails, Customers, Payments.
- [ ] **Database Design**
    - [ ] Create Conceptual Design (ER Diagram).
    - [ ] Perform Normalization (UNF → 1NF → 2NF → 3NF).
    - [ ] Identify Functional, Partial, and Transitive Dependencies.
- [ ] **Physical Design**
    - [ ] Define Table Structures, Data Types, and Constraints (PK, FK, NOT NULL, CHECK).
    - [ ] Plan Indexing strategy.

## Phase 2: MySQL Implementation (Backend)
- [ ] **Schema Creation**
    - [ ] Run SQL scripts for all tables and constraints.
- [ ] **Advanced Features**
    - [ ] Implement `process_sale` Stored Procedure (with parameters).
    - [ ] Implement `restock` Stored Procedure.
    - [ ] Implement `auto_deduct_stock` Trigger.
    - [ ] Implement `prevent_negative_stock` Trigger.
- [ ] **Security (DCL)**
    - [ ] Create DB users and assign `GRANT` privileges.
- [ ] **Seed Data (Requirement Checklist)**
    - [ ] Insert 5+ Categories.
    - [ ] Insert 20+ Products.
    - [ ] Insert 10+ Customers.
    - [ ] Insert 20+ Initial Sales Records for reporting.

## Phase 3: Java Swing UI Implementation (40%)
- [ ] **Project Setup**
    - [ ] Configure `pom.xml` with dependencies (MySQL Connector, FlatLaf, JFreeChart).
    - [ ] Create `DatabaseConnection` utility class.
- [ ] **Base UI Framework**
    - [ ] Setup Main Frame with Sidebar/Navigation.
    - [ ] Integrate FlatLaf for modern aesthetics.
- [ ] **CRUD Modules**
    - [ ] Product Management (Add, Update, Delete, View).
    - [ ] Category Management.
    - [ ] Customer Records.
    - [ ] Inventory Tracking Module.
- [ ] **Sales Processing System**
    - [ ] Create Transaction UI (Multi-product cart).
    - [ ] Implement Automated Total Computation.
    - [ ] Integrate TCL (Transaction Control) in Java logic.

## Phase 4: Business Intelligence & Reporting (10%)
- [ ] **Analytical Dashboard**
    - [ ] Monthly Sales Trend Chart (Line/Bar).
    - [ ] Product Category Performance (Pie).
- [ ] **Reporting**
    - [ ] Daily Sales Report.
    - [ ] Low Stock Warning Report.

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
