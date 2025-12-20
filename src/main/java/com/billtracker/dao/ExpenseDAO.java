package com.billtracker.dao;

import com.billtracker.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    public void addExpense(Connection conn, Expense expense, String groupId) throws SQLException {
        String insertExpense = "INSERT INTO expenses (id, amount, paid_by, type, description, group_id) VALUES (?, ?, ?, ?, ?, ?)";
        String insertSplit = "INSERT INTO splits (expense_id, user_id, amount, percent) VALUES (?, ?, ?, ?)";

        try (PreparedStatement expenseStmt = conn.prepareStatement(insertExpense)) {
            expenseStmt.setString(1, expense.getId());
            expenseStmt.setDouble(2, expense.getAmount());
            expenseStmt.setString(3, expense.getPaidBy().getId());
            expenseStmt.setString(4, expense.getExpenseType().name());
            expenseStmt.setString(5, expense.getDescription());
            if (groupId == null || groupId.isEmpty()) {
                expenseStmt.setNull(6, Types.VARCHAR);
            } else {
                expenseStmt.setString(6, groupId);
            }
            expenseStmt.executeUpdate();
        }

        try (PreparedStatement splitStmt = conn.prepareStatement(insertSplit)) {
            for (Split split : expense.getSplits()) {
                splitStmt.setString(1, expense.getId());
                splitStmt.setString(2, split.getUser().getId());
                splitStmt.setDouble(3, split.getAmount());
                if (split instanceof PercentSplit) {
                    splitStmt.setDouble(4, ((PercentSplit) split).getPercent());
                } else {
                    splitStmt.setNull(4, Types.DECIMAL);
                }
                splitStmt.addBatch();
            }
            splitStmt.executeBatch();
        }
    }

    public List<Expense> getAllExpenses() throws SQLException {
        List<Expense> expenses = new ArrayList<>();
        String selectExpenses = "SELECT * FROM expenses";
        String selectSplits = "SELECT * FROM splits WHERE expense_id = ?";

        UserDAO userDAO = new UserDAO();

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(selectExpenses)) {

            while (rs.next()) {
                String id = rs.getString("id");
                double amount = rs.getDouble("amount");
                String paidById = rs.getString("paid_by");
                String typeStr = rs.getString("type");
                String description = rs.getString("description");

                User paidBy = userDAO.getUser(paidById);
                ExpenseType type = ExpenseType.valueOf(typeStr);

                List<Split> splits = new ArrayList<>();
                try (PreparedStatement splitStmt = conn.prepareStatement(selectSplits)) {
                    splitStmt.setString(1, id);
                    try (ResultSet splitRs = splitStmt.executeQuery()) {
                        while (splitRs.next()) {
                            String userId = splitRs.getString("user_id");
                            User user = userDAO.getUser(userId);
                            double splitAmount = splitRs.getDouble("amount");

                            Split split;
                            if (type == ExpenseType.PERCENT) {
                                double percent = splitRs.getDouble("percent");
                                split = new PercentSplit(user, percent);
                                split.setAmount(splitAmount);
                            } else if (type == ExpenseType.EXACT) {
                                split = new ExactSplit(user, splitAmount);
                            } else {
                                split = new EqualSplit(user);
                                split.setAmount(splitAmount);
                            }
                            splits.add(split);
                        }
                    }
                }

                Expense expense = new Expense(id, amount, paidBy, splits, type, description);
                expenses.add(expense);
            }
        }
        return expenses;
    }
}
