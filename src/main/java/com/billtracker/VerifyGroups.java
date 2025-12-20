package com.billtracker;

import com.billtracker.model.*;
import com.billtracker.service.ExpenseManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VerifyGroups {
    public static void main(String[] args) {
        System.out.println("Starting Groups Verification...");

        String suffix = UUID.randomUUID().toString().substring(0, 5);
        String groupId = "Group_" + suffix;
        String userAId = "Alice_" + suffix;
        String userBId = "Bob_" + suffix;

        ExpenseManager manager = new ExpenseManager();

        manager.addUser(new User(userAId, "Alice", "", ""));
        manager.addUser(new User(userBId, "Bob", "", ""));

        manager.createGroup(groupId, "Vacation " + suffix);
        manager.addUserToGroup(groupId, userAId);
        manager.addUserToGroup(groupId, userBId);

        List<Split> splits = new ArrayList<>();
        splits.add(new EqualSplit(manager.getUser(userAId)));
        splits.add(new EqualSplit(manager.getUser(userBId)));

        System.out.println("Adding Group Expense...");
        manager.addExpense(ExpenseType.EQUAL, 100.0, userAId, splits, "Dinner", groupId);

        System.out.println("\n--- Checking Group Balance ---");
        manager.showBalances(groupId);

        System.out.println("\n--- Checking Global Balance ---");
        manager.showBalances();

        System.out.println("\nAdding Global Expense...");
        manager.addExpense(ExpenseType.EQUAL, 20.0, userBId, splits, "Snacks");

        System.out.println("\n--- Checking Global Balance (Aggregate) ---");
        manager.showBalances();

        System.out.println("\n--- Checking Group Balance (Should be unchanged) ---");
        manager.showBalances(groupId);
    }
}
