package com.billtracker.service;

import com.billtracker.model.*;

import java.util.List;

public class ExpenseService {

    public static Expense createExpense(ExpenseType expenseType, double amount, User paidBy, List<Split> splits, String description) {
        switch (expenseType) {
            case EXACT:
                validateExactSplit(splits, amount);
                return new Expense("EXP" + System.nanoTime(), amount, paidBy, splits, expenseType, description);
            case PERCENT:
                for (Split split : splits) {
                    PercentSplit percentSplit = (PercentSplit) split;
                    split.setAmount((amount * percentSplit.getPercent()) / 100.0);
                }
                validatePercentSplit(splits);
                return new Expense("EXP" + System.nanoTime(), amount, paidBy, splits, expenseType, description);
            case EQUAL:
                int totalSplits = splits.size();
                double splitAmount = ((double) Math.round(amount * 100 / totalSplits)) / 100.0;
                for (Split split : splits) {
                    split.setAmount(splitAmount);
                }
                // Handle remainder by adding to clean split (usually 1st person gets remainder or similar)
                // For simplicity, we just keep as is, but could be adjusted.
                // Let's ensure sum matches total exactly by adjusting first person 
                // However, for this simplified version, let's just leave it near equal.
                // Actually, to be robust:
                 double currentSum = splitAmount * totalSplits;
                 if (currentSum != amount) {
                     splits.get(0).setAmount(splits.get(0).getAmount() + (amount - currentSum));
                 }
                return new Expense("EXP" + System.nanoTime(), amount, paidBy, splits, expenseType, description);
            default:
                return null;
        }
    }

    private static void validateExactSplit(List<Split> splits, double totalAmount) {
        double sum = 0;
        for (Split split : splits) {
            if (!(split instanceof ExactSplit)) {
                // Technically shouldn't happen if caller provides correct type
                continue;
            }
            sum += split.getAmount();
        }

        if (sum != totalAmount) {
             // In real app, throw exception. 
             // System.out.println("Error: Exact splits do not sum to total amount.");
             throw new IllegalArgumentException("Exact splits sum " + sum + " does not match total " + totalAmount);
        }
    }

    private static void validatePercentSplit(List<Split> splits) {
        double totalPercent = 0;
        for (Split split : splits) {
            if (!(split instanceof PercentSplit)) {
                continue;
            }
            totalPercent += ((PercentSplit) split).getPercent();
        }

        if (totalPercent != 100) {
            throw new IllegalArgumentException("Percent splits sum " + totalPercent + " does not match 100%");
        }
    }
}
