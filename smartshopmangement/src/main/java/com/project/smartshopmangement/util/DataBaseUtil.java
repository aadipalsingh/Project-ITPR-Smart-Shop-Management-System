package com.project.smartshopmangement.util;

import java.sql.*;

public class DataBaseUtil {

    // 1. Database Configuration
    private static final String URL = "jdbc:mysql://localhost:3306/smartshop_management?createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASSWORD = "0011"; 

    private static Connection con = null;

    // 2. Method to Establish Connection
    public static Connection establishConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            
            // Only create tables if the connection is successful
            createTables();
            
        } catch (SQLException e) {
            System.err.println("\n❌ CONNECTION ERROR: Could not connect to MySQL.");
            System.err.println("   -> Check if MySQL Server is running.");
            System.err.println("   -> Check if password '" + PASSWORD + "' is correct for this computer.");
            System.err.println("   -> Error Details: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver Error: MySQL Connector JAR is missing.");
        }
        return con;
    }

    // 3. Method to Close Connection
    public static void closeConnection() {
        try {
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4. Private Method to Create All Tables
    private static void createTables() throws SQLException {
        if (con == null) return; // Safety check
        
        Statement stmt = con.createStatement();

        // --- A. MASTER DATA TABLES ---
        String t1 = "CREATE TABLE IF NOT EXISTS users (user_id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) UNIQUE, password VARCHAR(50), role VARCHAR(20))";
        String t2 = "CREATE TABLE IF NOT EXISTS categories (category_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50), description VARCHAR(100))";
        String t3 = "CREATE TABLE IF NOT EXISTS brands (brand_id INT AUTO_INCREMENT PRIMARY KEY, brand_name VARCHAR(50))";
        String t4 = "CREATE TABLE IF NOT EXISTS taxes (tax_id INT AUTO_INCREMENT PRIMARY KEY, tax_name VARCHAR(20), rate DOUBLE)";

        // --- B. INVENTORY & CUSTOMER ---
        String t5 = "CREATE TABLE IF NOT EXISTS products (product_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), price DOUBLE, quantity INT, category_id INT, brand_id INT, tax_id INT, FOREIGN KEY (category_id) REFERENCES categories(category_id), FOREIGN KEY (brand_id) REFERENCES brands(brand_id), FOREIGN KEY (tax_id) REFERENCES taxes(tax_id))";
        String t6 = "CREATE TABLE IF NOT EXISTS customers (customer_id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), phone VARCHAR(15) UNIQUE, email VARCHAR(100), password VARCHAR(50))";

        // --- C. TRANSACTIONS ---
        String t7 = "CREATE TABLE IF NOT EXISTS sales (sale_id INT AUTO_INCREMENT PRIMARY KEY, sale_date DATETIME DEFAULT CURRENT_TIMESTAMP, total_amount DOUBLE, customer_id INT, FOREIGN KEY (customer_id) REFERENCES customers(customer_id))";
        String t8 = "CREATE TABLE IF NOT EXISTS cart_items (id INT AUTO_INCREMENT PRIMARY KEY, sale_id INT, product_id INT, qty INT, price_at_sale DOUBLE, FOREIGN KEY (sale_id) REFERENCES sales(sale_id), FOREIGN KEY (product_id) REFERENCES products(product_id))";

        // --- D. DEFAULT DATA ---
        // Using 'INSERT IGNORE' to prevent errors if data already exists
        String adminData = "INSERT IGNORE INTO users (user_id, username, password, role) VALUES (1, 'admin', 'admin123', 'ADMIN')";
        
        // Execute Batch
        stmt.addBatch(t1); stmt.addBatch(t2); stmt.addBatch(t3); stmt.addBatch(t4);
        stmt.addBatch(t5); stmt.addBatch(t6); stmt.addBatch(t7); stmt.addBatch(t8);
        stmt.addBatch(adminData); // Ensure admin exists

        stmt.executeBatch();
        
        // Call helper to populate dummy data if empty
        populateDummyData(con);
    }

    // --- NEW METHOD TO INSERT SAMPLE DATA AUTOMATICALLY ---
    private static void populateDummyData(Connection c) throws SQLException {
        Statement stmt = c.createStatement();
        
        // Check if Categories exist. If count is 0, the DB is empty/new.
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM categories");
        rs.next();
        
        if(rs.getInt(1) == 0) {
            System.out.println("⚠️ Database empty. Inserting sample data...");
            
            // 1. Categories
            stmt.addBatch("INSERT INTO categories (category_id, name) VALUES (1, 'Electronics'), (2, 'Clothing'), (3, 'Grocery')");
            
            // 2. Brands
            stmt.addBatch("INSERT INTO brands (brand_id, brand_name) VALUES (1, 'Samsung'), (2, 'Nike'), (3, 'Nestle'), (4, 'Generic')");
            
            // 3. Taxes
            stmt.addBatch("INSERT INTO taxes (tax_id, tax_name, rate) VALUES (1, 'GST 18%', 18.0), (2, 'GST 5%', 5.0), (3, 'No Tax', 0.0)");
            
            // 4. Sample Products
            stmt.addBatch("INSERT INTO products (name, price, quantity, category_id, brand_id, tax_id) VALUES ('Smart TV', 25000, 10, 1, 1, 1)");
            stmt.addBatch("INSERT INTO products (name, price, quantity, category_id, brand_id, tax_id) VALUES ('Running Shoes', 3500, 20, 2, 2, 1)");
            stmt.addBatch("INSERT INTO products (name, price, quantity, category_id, brand_id, tax_id) VALUES ('Maggie Noodles', 120, 50, 3, 3, 2)");
            
            // 5. Sample Customer
            stmt.addBatch("INSERT INTO customers (name, phone, email, password) VALUES ('Demo User', '9999999999', 'demo@test.com', 'pass123')");

            stmt.executeBatch();
            System.out.println("✅ Sample Data Inserted Successfully!");
        } // Closing 'if'
    } // Closing 'populateDummyData'
} // Closing 'DataBaseUtil' Class
