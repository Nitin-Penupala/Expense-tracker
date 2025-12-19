package com.billtracker.dao;

import com.billtracker.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    public void addExpense(Expense expense) throws SQLException {
        String insertExpense = "INSERT INTO expenses (id, amount, paid_by, type, description) VALUES (?, ?, ?, ?, ?)";
        String insertSplit = "INSERT INTO splits (expense_id, user_id, amount, percent) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement expenseStmt = conn.prepareStatement(insertExpense)) {
                expenseStmt.setString(1, expense.getId());
                expenseStmt.setDouble(2, expense.getAmount());
                expenseStmt.setString(3, expense.getPaidBy().getId());
                expenseStmt.setString(4, expense.getExpenseType().name());
                expenseStmt.setString(5, expense.getDescription());
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

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // For simplicity, not implementing getAllExpenses in this turn unless needed
    // for balance calculation logic persistence.
    // However, if we restart app, we lose balances unless we re-calculate from DB.
    // Let's assume we load all Expenses on startup or calculate balances on easier
    // way.
    // For this task, persisting is the key.
}
