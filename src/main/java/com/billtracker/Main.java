package com.billtracker;

import com.billtracker.model.*;
import com.billtracker.service.ExpenseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static ExpenseManager expenseManager = new ExpenseManager();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to Bill Tracker!");

        // Check Database Connection
        try (java.sql.Connection conn = com.billtracker.dao.DBConnection.getConnection()) {
            if (conn != null) {
                System.out.println("Database connected successfully!");
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
            System.out.println("Please ensure the database 'bill_tracker' exists and credentials are correct.");
            // Optional: return; // to stop if DB is critical
        }

        while (true) {
            printMenu();
            int choice = 0;
            try {
                String line = scanner.nextLine();
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    addUser();
                    break;
                case 2:
                    addExpense();
                    break;
                case 3:
                    expenseManager.showBalances();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. Add User");
        System.out.println("2. Add Expense");
        System.out.println("3. Show Balances");
        System.out.println("4. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void addUser() {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine();

        if (expenseManager.getUser(id) != null) {
            System.out.println("User with this ID already exists.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Phone: ");
        String phone = scanner.nextLine();

        User user = new User(id, name, email, phone);
        expenseManager.addUser(user);
        System.out.println("User added successfully.");
    }

    private static void addExpense() {
        System.out.print("Enter User ID who paid: ");
        String paidById = scanner.nextLine();
        User paidBy = expenseManager.getUser(paidById);
        if (paidBy == null) {
            System.out.println("User not found.");
            return;
        }

        System.out.print("Enter Description: ");
        String description = scanner.nextLine();

        System.out.print("Enter Total Amount: ");
        double amount = 0;
        try {
            amount = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        System.out.println("Select Expense Type (1: EQUAL, 2: EXACT, 3: PERCENT): ");
        int typeChoice = 0;
        try {
            typeChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid type.");
            return;
        }

        List<Split> splits = new ArrayList<>();
        System.out.print("Enter number of users involved in split: ");
        int numUsers = 0;
        try {
            numUsers = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return;
        }

        for (int i = 0; i < numUsers; i++) {
            System.out.print("Enter User ID for user " + (i + 1) + ": ");
            String userId = scanner.nextLine();
            User user = expenseManager.getUser(userId);
            if (user == null) {
                System.out.println("User not found: " + userId);
                // In a real app we might retry or abort. Here we just abort expense creation.
                return;
            }

            if (typeChoice == 1) { // EQUAL
                splits.add(new EqualSplit(user));
            } else if (typeChoice == 2) { // EXACT
                System.out.print("Enter amount owed by " + user.getName() + ": ");
                double share = Double.parseDouble(scanner.nextLine());
                splits.add(new ExactSplit(user, share));
            } else if (typeChoice == 3) { // PERCENT
                System.out.print("Enter percentage owed by " + user.getName() + ": ");
                double percent = Double.parseDouble(scanner.nextLine());
                splits.add(new PercentSplit(user, percent));
            } else {
                System.out.println("Invalid expense type.");
                return;
            }
        }

        ExpenseType type = null;
        switch (typeChoice) {
            case 1:
                type = ExpenseType.EQUAL;
                break;
            case 2:
                type = ExpenseType.EXACT;
                break;
            case 3:
                type = ExpenseType.PERCENT;
                break;
        }

        try {
            expenseManager.addExpense(type, amount, paidById, splits, description);
            System.out.println("Expense added successfully.");
        } catch (Exception e) {
            System.out.println("Error adding expense: " + e.getMessage());
        }
    }
}
