package com.billtracker.service;

import com.billtracker.dao.ExpenseDAO;
import com.billtracker.dao.UserDAO;
import com.billtracker.model.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseManager {
    // private List<User> users; // Removed in favor of DAO
    // private List<Expense> expenses; // Removed in favor of DAO
    private UserDAO userDAO;
    private ExpenseDAO expenseDAO;
    private BalanceSheetService balanceSheetService;

    public ExpenseManager() {
        this.userDAO = new UserDAO();
        this.expenseDAO = new ExpenseDAO();
        this.balanceSheetService = new BalanceSheetService();
        // Ideally we should load existing expenses to rebuild balance sheet on startup
        // But for now we start fresh or need a way to load.
    }

    public void addUser(User user) {
        try {
            userDAO.addUser(user);
        } catch (SQLException e) {
            System.out.println("Error adding user to DB: " + e.getMessage());
        }
    }

    public void addExpense(ExpenseType expenseType, double amount, String paidByUserId, List<Split> splits,
            String description) {
        User paidBy = getUser(paidByUserId);
        if (paidBy == null)
            return;

        Expense expense = ExpenseService.createExpense(expenseType, amount, paidBy, splits, description);
        try {
            expenseDAO.addExpense(expense);
            balanceSheetService.updateBalances(expense);
        } catch (SQLException e) {
            System.out.println("Error adding expense to DB: " + e.getMessage());
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
        // Balances are still in memory in BalanceSheetService.
        // If app restarts, they are lost unless we replay expenses.
        // For 'Integrate MySQL', persisting data is step 1.
        // Replaying is step 2 (advanced).
        // To support current session, we pass current users list if needed, or fetch
        // all.
        try {
            List<User> allUsers = userDAO.getAllUsers();
            balanceSheetService.showBalanceSheet(allUsers);
        } catch (SQLException e) {
            System.out.println("Error fetching users for report: " + e.getMessage());
        }
    }
}
