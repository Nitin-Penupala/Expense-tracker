package com.billtracker.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BalanceSheetDAO {

    public double getBalance(Connection conn, String debtor, String creditor) throws SQLException {
        String sql = "SELECT amount FROM balance_sheet WHERE user_who_owes = ? AND user_who_is_owed = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, debtor);
            pstmt.setString(2, creditor);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("amount");
                }
            }
        }
        return 0.0;
    }

    public void setBalance(Connection conn, String debtor, String creditor, double amount) throws SQLException {
        String sql = "INSERT INTO balance_sheet (user_who_owes, user_who_is_owed, amount) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE amount = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, debtor);
            pstmt.setString(2, creditor);
            pstmt.setDouble(3, amount);
            pstmt.setDouble(4, amount);
            pstmt.executeUpdate();
        }
    }

    public Map<String, Map<String, Double>> getAllBalances() throws SQLException {
        Map<String, Map<String, Double>> balanceSheet = new HashMap<>();
        String sql = "SELECT * FROM balance_sheet";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String debtor = rs.getString("user_who_owes");
                String creditor = rs.getString("user_who_is_owed");
                double amount = rs.getDouble("amount");

                balanceSheet.putIfAbsent(debtor, new HashMap<>());
                balanceSheet.get(debtor).put(creditor, amount);
            }
        }
        return balanceSheet;
    }
}
