package com.billtracker.service;

import com.billtracker.dao.ExpenseDAO;
import com.billtracker.dao.GroupDAO;
import com.billtracker.dao.UserDAO;
import com.billtracker.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ExpenseManager {
    private UserDAO userDAO;
    private ExpenseDAO expenseDAO;
    private GroupDAO groupDAO;
    private BalanceSheetService balanceSheetService;

    public ExpenseManager() {
        this.userDAO = new UserDAO();
        this.expenseDAO = new ExpenseDAO();
        this.groupDAO = new GroupDAO();
        this.balanceSheetService = new BalanceSheetService();
    }

    public void addUser(User user) {
        try {
            userDAO.addUser(user);
        } catch (SQLException e) {
            System.out.println("Error adding user to DB: " + e.getMessage());
        }
    }

    public void createGroup(String id, String name) {
        try {
            groupDAO.createGroup(new Group(id, name));
            System.out.println("Group created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating group: " + e.getMessage());
        }
    }

    public void addUserToGroup(String groupId, String userId) {
        try {
            groupDAO.addGroupMember(groupId, userId);
            System.out.println("User added to group successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding user to group: " + e.getMessage());
        }
    }

    public Group getGroup(String groupId) {
        try {
            return groupDAO.getGroup(groupId);
        } catch (SQLException e) {
            System.out.println("Error fetching group: " + e.getMessage());
            return null;
        }
    }

    public void addExpense(ExpenseType expenseType, double amount, String paidByUserId, List<Split> splits,
            String description) {
        addExpense(expenseType, amount, paidByUserId, splits, description, null);
    }

    public void addExpense(ExpenseType expenseType, double amount, String paidByUserId, List<Split> splits,
            String description, String groupId) {
        User paidBy = getUser(paidByUserId);
        if (paidBy == null)
            return;

        Expense expense = ExpenseService.createExpense(expenseType, amount, paidBy, splits, description);

        Connection conn = null;
        try {
            conn = com.billtracker.dao.DBConnection.getConnection();
            conn.setAutoCommit(false);

            expenseDAO.addExpense(conn, expense, groupId);
            balanceSheetService.updateBalances(conn, expense, groupId);

            conn.commit();
            System.out.println("Expense added and balances updated successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding expense/updating balance: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public User getUser(String id) {
        try {
            return userDAO.getUser(id);
        } catch (SQLException e) {
            System.out.println("Error fetching user: " + e.getMessage());
            return null;
        }
    }

    public void showBalances() {
        showBalances("GLOBAL_AGGREGATE");
    }

    public void showBalances(String groupId) {
        try {
            List<User> allUsers = userDAO.getAllUsers();
            balanceSheetService.showBalanceSheet(allUsers, groupId);
        } catch (SQLException e) {
            System.out.println("Error fetching users for report: " + e.getMessage());
        }
    }
}
