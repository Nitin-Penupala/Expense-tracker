package com.billtracker;

import java.sql.*;

public class SetupDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "Nitin@0705";

        System.out.println("Connecting to database at " + url + " with user " + user + "...");

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected to MySQL Server successfully!");
            Statement stmt = conn.createStatement();

            System.out.println("Creating database 'bill_tracker' if it doesn't exist...");
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS bill_tracker");

            System.out.println("Switching to 'bill_tracker'...");
            stmt.executeUpdate("USE bill_tracker");

            System.out.println("Creating table 'users'...");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100), " +
                    "phone VARCHAR(20))");

            System.out.println("Creating table 'expenses'...");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS expenses (" +
                    "id VARCHAR(50) PRIMARY KEY, " +
                    "amount DECIMAL(10, 2) NOT NULL, " +
                    "paid_by VARCHAR(50) NOT NULL, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "description VARCHAR(255), " +
                    "FOREIGN KEY (paid_by) REFERENCES users(id))");

            System.out.println("Creating table 'splits'...");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS splits (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "expense_id VARCHAR(50) NOT NULL, " +
                    "user_id VARCHAR(50) NOT NULL, " +
                    "amount DECIMAL(10, 2) NOT NULL, " +
                    "percent DECIMAL(5, 2), " +
                    "FOREIGN KEY (expense_id) REFERENCES expenses(id), " +
                    "FOREIGN KEY (user_id) REFERENCES users(id))");

            System.out.println("Creating table 'balance_sheet'...");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS balance_sheet (" +
                    "user_who_owes VARCHAR(50), " +
                    "user_who_is_owed VARCHAR(50), " +
                    "amount DECIMAL(10, 2), " +
                    "PRIMARY KEY (user_who_owes, user_who_is_owed), " +
                    "FOREIGN KEY (user_who_owes) REFERENCES users(id), " +
                    "FOREIGN KEY (user_who_is_owed) REFERENCES users(id))");

            System.out.println("Database setup completed successfully!");

        } catch (SQLException e) {
            System.out.println("Error during database setup:");
            e.printStackTrace();
        }
    }
}
