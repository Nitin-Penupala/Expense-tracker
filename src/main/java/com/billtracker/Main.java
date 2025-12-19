package com.billtracker;

import com.billtracker.model.*;
import com.billtracker.service.ExpenseManager;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ExpenseManager expenseManager = new ExpenseManager();

        // 1. Add Users
        User u1 = new User("u1", "Alice", "alice@test.com", "1234");
        User u2 = new User("u2", "Bob", "bob@test.com", "5678");
        User u3 = new User("u3", "Charlie", "charlie@test.com", "9101");
        User u4 = new User("u4", "David", "david@test.com", "1121");

        expenseManager.addUser(u1);
        expenseManager.addUser(u2);
        expenseManager.addUser(u3);
        expenseManager.addUser(u4);

        System.out.println("Initialized Users: Alice, Bob, Charlie, David");
        expenseManager.showBalances(); // Should be empty

        // 2. EQUAL Split
        // Alice pays 1000 for everyone (250 each)
        System.out.println("\n--- Expense 1: Alice pays 1000 for everyone (Equal) ---");
        List<Split> splits1 = new ArrayList<>();
        splits1.add(new EqualSplit(u1));
        splits1.add(new EqualSplit(u2));
        splits1.add(new EqualSplit(u3));
        splits1.add(new EqualSplit(u4));

        expenseManager.addExpense(ExpenseType.EQUAL, 1000, "u1", splits1, "Lunch");
        expenseManager.showBalances();
        // Expected:
        // Bob owes Alice 250
        // Charlie owes Alice 250
        // David owes Alice 250

        // 3. EXACT Split
        // Bob pays 1250: Alice owes 370, Charlie owes 880
        System.out.println("\n--- Expense 2: Bob pays 1250 for Alice (370) and Charlie (880) (Exact) ---");
        List<Split> splits2 = new ArrayList<>();
        splits2.add(new ExactSplit(u1, 370));
        splits2.add(new ExactSplit(u3, 880));

        expenseManager.addExpense(ExpenseType.EXACT, 1250, "u2", splits2, "Shopping");
        expenseManager.showBalances();
        // Previous State: B->A:250, C->A:250, D->A:250
        // New Transaction: A->B:370, C->B:880
        // Net:
        // A and B: B owes A 250, A owes B 370 => A owes B 120
        // C and B: C owes B 880
        // C and A: C owes A 250
        // D and A: D owes A 250
        // Result:
        // A owes B: 120
        // C owes B: 880
        // C owes A: 250
        // D owes A: 250

        // 4. PERCENT Split
        // Charlie pays 1200: Alice 40%, Bob 20%, Charlie 20%, David 20%
        // Alice: 480, Bob: 240, Charlie: 240, David: 240
        System.out.println("\n--- Expense 3: Charlie pays 1200 (Percent: A:40, B:20, C:20, D:20) ---");
        List<Split> splits3 = new ArrayList<>();
        splits3.add(new PercentSplit(u1, 40));
        splits3.add(new PercentSplit(u2, 20));
        splits3.add(new PercentSplit(u3, 20));
        splits3.add(new PercentSplit(u4, 20));

        expenseManager.addExpense(ExpenseType.PERCENT, 1200, "u3", splits3, "Party");
        expenseManager.showBalances();
        // New Transaction (Paid by C):
        // A owes C: 480
        // B owes C: 240
        // D owes C: 240

        // Cumulative with Previous:
        // 1. A owes B: 120
        // 2. C owes B: 880
        // 3. C owes A: 250
        // 4. D owes A: 250

        // Updates:
        // A and C:
        // Old: C owes A 250
        // New: A owes C 480
        // Net: A owes C (480 - 250) = 230

        // B and C:
        // Old: C owes B 880
        // New: B owes C 240
        // Net: C owes B (880 - 240) = 640

        // D and C:
        // New: D owes C 240

        // D and A:
        // Old: D owes A 250
        // No change between D and A directly.

        // A and B:
        // Old: A owes B 120
        // No change

        // Final Expected:
        // A owes B: 120
        // A owes C: 230
        // C owes B: 640
        // D owes A: 250
        // D owes C: 240
    }
}
