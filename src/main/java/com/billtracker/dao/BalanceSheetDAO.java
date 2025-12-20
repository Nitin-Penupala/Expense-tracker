package com.billtracker.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class BalanceSheetDAO {

    public double getBalance(Connection conn, String debtor, String creditor, String groupId) throws SQLException {
        String sql = "SELECT amount FROM balance_sheet WHERE user_who_owes = ? AND user_who_is_owed = ? AND group_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, debtor);
            pstmt.setString(2, creditor);
            pstmt.setString(3, groupId == null ? "GLOBAL" : groupId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("amount");
                }
            }
        }
        return 0.0;
    }

    public void setBalance(Connection conn, String debtor, String creditor, String groupId, double amount)
            throws SQLException {
        String sql = "INSERT INTO balance_sheet (user_who_owes, user_who_is_owed, group_id, amount) VALUES (?, ?, ?, ?) "
                +
                "ON DUPLICATE KEY UPDATE amount = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String gid = groupId == null ? "GLOBAL" : groupId;
            pstmt.setString(1, debtor);
            pstmt.setString(2, creditor);
            pstmt.setString(3, gid);
            pstmt.setDouble(4, amount);
            pstmt.setDouble(5, amount);
            pstmt.executeUpdate();
        }
    }

    public Map<String, Map<String, Double>> getBalances(String groupId) throws SQLException {
        Map<String, Map<String, Double>> balanceSheet = new HashMap<>();
        String sql;
        boolean isAggregate = "GLOBAL_AGGREGATE".equals(groupId);

        if (isAggregate) {
            sql = "SELECT user_who_owes, user_who_is_owed, SUM(amount) as amount FROM balance_sheet GROUP BY user_who_owes, user_who_is_owed";
        } else {
            sql = "SELECT user_who_owes, user_who_is_owed, amount FROM balance_sheet WHERE group_id = ?";
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (!isAggregate) {
                pstmt.setString(1, groupId == null ? "GLOBAL" : groupId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String debtor = rs.getString("user_who_owes");
                    String creditor = rs.getString("user_who_is_owed");
                    double amount = rs.getDouble("amount");

                    balanceSheet.putIfAbsent(debtor, new HashMap<>());
                    balanceSheet.get(debtor).put(creditor, amount);
                }
            }
        }
        return balanceSheet;
    }
}
