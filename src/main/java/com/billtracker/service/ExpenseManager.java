package com.billtracker.service;

import com.billtracker.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseManager {
    private List<User> users;
    private Map<String, Group> groups;
    private List<Expense> expenses;
    private BalanceSheetService balanceSheetService;

    public ExpenseManager() {
        this.users = new ArrayList<>();
        this.groups = new HashMap<>();
        this.expenses = new ArrayList<>();
        this.balanceSheetService = new BalanceSheetService();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void addGroup(Group group) {
        groups.put(group.getId(), group);
    }

    public void addExpense(ExpenseType expenseType, double amount, String paidByUserId, List<Split> splits, String description) {
        User paidBy = getUser(paidByUserId);
        Expense expense = ExpenseService.createExpense(expenseType, amount, paidBy, splits, description);
        expenses.add(expense);
        balanceSheetService.updateBalances(expense);
    }

    public User getUser(String id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public void showBalances() {
        balanceSheetService.showBalanceSheet(users);
    }
}
