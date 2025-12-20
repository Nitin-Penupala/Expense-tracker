package com.billtracker.service;

import com.billtracker.dao.BalanceSheetDAO;
import com.billtracker.model.Expense;
import com.billtracker.model.Split;
import com.billtracker.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;

public class BalanceSheetService {
    private BalanceSheetDAO balanceSheetDAO;

    public BalanceSheetService() {
        this.balanceSheetDAO = new BalanceSheetDAO();
    }

    public void updateBalances(Connection conn, Expense expense, String groupId) throws SQLException {
        User paidBy = expense.getPaidBy();

        for (Split split : expense.getSplits()) {
            User paidTo = split.getUser();
            double amount = split.getAmount();

            if (paidBy.getId().equals(paidTo.getId())) {
                continue;
            }

            addDebt(conn, paidTo.getId(), paidBy.getId(), groupId, amount);
        }
    }

    private void addDebt(Connection conn, String userWhoOwesId, String userWhoIsOwedId, String groupId, double amount)
            throws SQLException {
        double reverseDebt = balanceSheetDAO.getBalance(conn, userWhoIsOwedId, userWhoOwesId, groupId);

        if (reverseDebt > 0) {
            if (reverseDebt >= amount) {
                double newReverseDebt = reverseDebt - amount;
                balanceSheetDAO.setBalance(conn, userWhoIsOwedId, userWhoOwesId, groupId, newReverseDebt);
                return;
            } else {
                balanceSheetDAO.setBalance(conn, userWhoIsOwedId, userWhoOwesId, groupId, 0.0);
                amount = amount - reverseDebt;
            }
        }

        double currentDebt = balanceSheetDAO.getBalance(conn, userWhoOwesId, userWhoIsOwedId, groupId);
        balanceSheetDAO.setBalance(conn, userWhoOwesId, userWhoIsOwedId, groupId, currentDebt + amount);
    }

    public void showBalanceSheet(List<User> users, String groupId) {
        try {
            Map<String, Map<String, Double>> balanceSheet = balanceSheetDAO.getBalances(groupId);

            System.out.println("---------------------------------------");
            if ("GLOBAL_AGGREGATE".equals(groupId)) {
                System.out.println("Global Balance Sheet:");
            } else if (groupId == null) {
                System.out.println("Balance Sheet (Non-Group):");
            } else {
                System.out.println("Balance Sheet (Group " + groupId + "):");
            }

            boolean isEmpty = true;

            for (String userOwesId : balanceSheet.keySet()) {
                for (String userOwedId : balanceSheet.get(userOwesId).keySet()) {
                    double amount = balanceSheet.get(userOwesId).get(userOwedId);
                    if (amount > 0.01) {
                        String userOwesName = findUserName(users, userOwesId);
                        String userOwedName = findUserName(users, userOwedId);

                        System.out
                                .println(userOwesName + " owes " + userOwedName + ": " + String.format("%.2f", amount));
                        isEmpty = false;
                    }
                }
            }

            if (isEmpty) {
                System.out.println("No dues.");
            }
            System.out.println("---------------------------------------");
        } catch (SQLException e) {
            System.out.println("Error reading balances: " + e.getMessage());
        }
    }

    private String findUserName(List<User> users, String id) {
        for (User u : users) {
            if (u.getId().equals(id))
                return u.getName();
        }
        return id;
    }
}
