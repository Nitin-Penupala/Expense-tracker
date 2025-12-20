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
        try (java.sql.Connection conn = com.billtracker.dao.DBConnection.getConnection()) {
            if (conn != null)
                System.out.println("Database connected successfully!");
        } catch (Exception e) {
            System.out.println("Failed to connect to DB: " + e.getMessage());
        }

        while (true) {
            printMainMenu();
            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    addUser();
                    break;
                case 2:
                    addExpense(null);
                    break;
                case 3:
                    expenseManager.showBalances();
                    break;
                case 4:
                    handleGroupsMenu();
                    break;
                case 5:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Add User (Global)");
        System.out.println("2. Add Expense (Global)");
        System.out.println("3. Show Balances (Global)");
        System.out.println("4. Groups Menu");
        System.out.println("5. Exit");
        System.out.print("Enter choice: ");
    }

    private static void handleGroupsMenu() {
        while (true) {
            System.out.println("\n--- Groups Menu ---");
            System.out.println("1. Create Group");
            System.out.println("2. Add User to Group");
            System.out.println("3. Enter Group (Manage Expenses)");
            System.out.println("4. Back to Main Menu");
            System.out.print("Enter choice: ");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    createGroup();
                    break;
                case 2:
                    addUserToGroup();
                    break;
                case 3:
                    enterGroup();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void createGroup() {
        System.out.print("Enter Group ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter Group Name: ");
        String name = scanner.nextLine();
        expenseManager.createGroup(id, name);
    }

    private static void addUserToGroup() {
        System.out.print("Enter Group ID: ");
        String groupId = scanner.nextLine();
        System.out.print("Enter User ID to add: ");
        String userId = scanner.nextLine();
        expenseManager.addUserToGroup(groupId, userId);
    }

    private static void enterGroup() {
        System.out.print("Enter Group ID: ");
        String groupId = scanner.nextLine();
        Group group = expenseManager.getGroup(groupId);
        if (group == null) {
            System.out.println("Group not found.");
            return;
        }

        System.out.println("--- Entered Group: " + group.getName() + " ---");
        while (true) {
            System.out.println("\n--- Group: " + group.getName() + " ---");
            System.out.println("1. Add Expense (Group)");
            System.out.println("2. Show Balances (Group)");
            System.out.println("3. Back to Groups Menu");
            System.out.print("Enter choice: ");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    addExpense(groupId);
                    break;
                case 2:
                    expenseManager.showBalances(groupId);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void addUser() {
        System.out.print("Enter User ID: ");
        String id = scanner.nextLine();
        if (expenseManager.getUser(id) != null) {
            System.out.println("User exists.");
            return;
        }
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        expenseManager.addUser(new User(id, name, "", ""));
        System.out.println("User added.");
    }

    private static void addExpense(String groupId) {
        System.out.print("Enter User ID who paid: ");
        String paidById = scanner.nextLine();
        User paidBy = expenseManager.getUser(paidById);
        if (paidBy == null) {
            System.out.println("User not found.");
            return;
        }
        System.out.print("Enter Description: ");
        String desc = scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = Double.parseDouble(scanner.nextLine());

        System.out.println("Type: 1.EQUAL, 2.EXACT, 3.PERCENT");
        int type = Integer.parseInt(scanner.nextLine());

        System.out.print("Num users in split: ");
        int num = Integer.parseInt(scanner.nextLine());
        List<Split> splits = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            System.out.print("User ID " + (i + 1) + ": ");
            String uid = scanner.nextLine();
            User u = expenseManager.getUser(uid);
            if (u == null) {
                System.out.println("User not found.");
                return;
            }

            if (type == 1)
                splits.add(new EqualSplit(u));
            else if (type == 2) {
                System.out.print("Amount owed: ");
                splits.add(new ExactSplit(u, Double.parseDouble(scanner.nextLine())));
            } else if (type == 3) {
                System.out.print("Percent: ");
                splits.add(new PercentSplit(u, Double.parseDouble(scanner.nextLine())));
            }
        }

        expenseManager.addExpense(
                type == 1 ? ExpenseType.EQUAL : (type == 2 ? ExpenseType.EXACT : ExpenseType.PERCENT),
                amount, paidById, splits, desc, groupId);
    }
}
