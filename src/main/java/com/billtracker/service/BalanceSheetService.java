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

    public void updateBalances(Connection conn, Expense expense) throws SQLException {
        User paidBy = expense.getPaidBy();

        for (Split split : expense.getSplits()) {
            User paidTo = split.getUser();
            double amount = split.getAmount();

            if (paidBy.getId().equals(paidTo.getId())) {
                continue;
            }

            addDebt(conn, paidTo.getId(), paidBy.getId(), amount);
        }
    }

    private void addDebt(Connection conn, String userWhoOwesId, String userWhoIsOwedId, double amount)
            throws SQLException {
        double reverseDebt = balanceSheetDAO.getBalance(conn, userWhoIsOwedId, userWhoOwesId);

        if (reverseDebt > 0) {
            if (reverseDebt >= amount) {
                double newReverseDebt = reverseDebt - amount;
                balanceSheetDAO.setBalance(conn, userWhoIsOwedId, userWhoOwesId, newReverseDebt);
                return;
            } else {
                balanceSheetDAO.setBalance(conn, userWhoIsOwedId, userWhoOwesId, 0.0);
                amount = amount - reverseDebt;
            }
        }

        double currentDebt = balanceSheetDAO.getBalance(conn, userWhoOwesId, userWhoIsOwedId);
        balanceSheetDAO.setBalance(conn, userWhoOwesId, userWhoIsOwedId, currentDebt + amount);
    }

    public void showBalanceSheet(List<User> users) {
        try {
            Map<String, Map<String, Double>> balanceSheet = balanceSheetDAO.getAllBalances();

            System.out.println("---------------------------------------");
            System.out.println("Balance Sheet (DB):");
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
