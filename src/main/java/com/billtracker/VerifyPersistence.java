package com.billtracker;

import com.billtracker.model.*;
import com.billtracker.service.ExpenseManager;
import com.billtracker.dao.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VerifyPersistence {
    public static void main(String[] args) {
        System.out.println("Starting DB Persistence Verification...");

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 5);
        String userAId = "UserA_" + uniqueSuffix;
        String userBId = "UserB_" + uniqueSuffix;
        
        try {
             ExpenseManager manager = new ExpenseManager();
             
             User userA = new User(userAId, "TestUserA", "a@test.com", "123");
             User userB = new User(userBId, "TestUserB", "b@test.com", "456");
             manager.addUser(userA);
             manager.addUser(userB);
             
             List<Split> splits = new ArrayList<>();
             splits.add(new EqualSplit(userA));
             splits.add(new EqualSplit(userB));
             
             System.out.println("Adding expense...");
             manager.addExpense(ExpenseType.EQUAL, 100.0, userAId, splits, "Test Expense " + uniqueSuffix);
             
             System.out.println("Verifying Database Content directly...");

                  
                 String checkBalance = "SELECT amount FROM balance_sheet WHERE user_who_owes='" + userBId + "' AND user_who_is_owed='" + userAId + "'";
                 try (ResultSet rs = stmt.executeQuery(checkBa

                         if (Math.abs(amount - 50.0) < 0.01) {
                             System.out.println("SUCCESS: DB check passed. " + userBId + " owes " + userAId + ": " + amount);
                         } else {
                             System.out.println("FAILURE: DB check failed. Expected 50.0, got " + amount);
                         }
                     } else {
                         System.out.println("FAILURE: No record found in balance_sheet table for these users.");
                     }
                 }
             }
             
             System.out.println("Verifying via ShowBalances (output should match):");
             manager.showBalances();
             
        } catch (Exception e) {
            e

            
