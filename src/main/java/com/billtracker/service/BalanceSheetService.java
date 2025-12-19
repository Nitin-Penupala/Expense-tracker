package com.billtracker.service;

import com.billtracker.model.Expense;
import com.billtracker.model.Split;
import com.billtracker.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceSheetService {
    // Map<UserWhoOwes, Map<UserWhoIsOwed, Amount>>
    private Map<String, Map<String, Double>> balanceSheet;

    public BalanceSheetService() {
        this.balanceSheet = new HashMap<>();
    }

    public void updateBalances(Expense expense) {
        User paidBy = expense.getPaidBy();
        
        for (Split split : expense.getSplits()) {
            User paidTo = split.getUser();
            double amount = split.getAmount();

            if (paidBy.getId().equals(paidTo.getId())) {
                continue; // Cannot owe yourself
            }

            // paidTo owes paidBy 'amount'
            addDebt(paidTo.getId(), paidBy.getId(), amount);
        }
    }

    private void addDebt(String userWhoOwesId, String userWhoIsOwedId, double amount) {
        // Update UserWhoOwes -> UserWhoIsOwed
        balanceSheet.putIfAbsent(userWhoOwesId, new HashMap<>());
        Map<String, Double> owesMap = balanceSheet.get(userWhoOwesId);
        owesMap.put(userWhoIsOwedId, owesMap.getOrDefault(userWhoIsOwedId, 0.0) + amount);

        // Also update reverse? Usually we might simplify here instantly or keeping raw.
        // Let's just keep raw debt here: A owes B 50.
        // If B owes A 20 later, we will have A->B:50, B->A:20. 
        // We can simplify on view or on add. Let's simplify on add.
        simplifyDebt(userWhoOwesId, userWhoIsOwedId);
    }
    
    private void simplifyDebt(String userA, String userB) {
        // check if B owes A
        double aOwesB = getAmountOwed(userA, userB);
        double bOwesA = getAmountOwed(userB, userA);
        
        if (aOwesB > bOwesA) {
            double diff = aOwesB - bOwesA;
            setAmountOwed(userA, userB, diff);
            setAmountOwed(userB, userA, 0);
        } else {
            double diff = bOwesA - aOwesB;
            setAmountOwed(userB, userA, diff);
            setAmountOwed(userA, userB, 0);
        }
    }
    
    private double getAmountOwed(String fromUser, String toUser) {
        if (!balanceSheet.containsKey(fromUser)) return 0.0;
        return balanceSheet.get(fromUser).getOrDefault(toUser, 0.0);
    }
    
    private void setAmountOwed(String fromUser, String toUser, double amount) {
        balanceSheet.putIfAbsent(fromUser, new HashMap<>());
        balanceSheet.get(fromUser).put(toUser, amount);
    }

    public void showBalanceSheet(List<User> users) {
        System.out.println("---------------------------------------");
        System.out.println("Balance Sheet:");
        boolean isEmpty = true;
        
        for (String userOwesId : balanceSheet.keySet()) {
            for (String userOwedId : balanceSheet.get(userOwesId).keySet()) {
                double amount = balanceSheet.get(userOwesId).get(userOwedId);
                if (amount > 0) {
                    // Start finding names (inefficient for simplified demo, assuming ids are mapped or stored)
                    // For finding names, I'll just print IDs or assuming caller passed populated users list to find
                     String userOwesName = findUserName(users, userOwesId);
                     String userOwedName = findUserName(users, userOwedId);
                    
                    System.out.println(userOwesName + " owes " + userOwedName + ": " + String.format("%.2f", amount));
                    isEmpty = false;
                }
            }
        }
        
        if (isEmpty) {
            System.out.println("No dues.");
        }
        System.out.println("---------------------------------------");
    }
    
    private String findUserName(List<User> users, String id) {
        // naive linear search
        for(User u: users) {
            if(u.getId().equals(id)) return u.getName();
        }
        return id;
    }
}
