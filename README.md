Expense Sharing Application (Backend)

A simplified expense sharing backend system inspired by Splitwise.
This project focuses on group-based expense management, multiple split strategies, and net balance tracking between users.

📌 Features

👤 User management

👥 Group creation and member management

💰 Add shared expenses within a group

🔀 Multiple expense split types:

Equal split

Exact amount split

Percentage split

📊 Automatic balance calculation

🔁 Simplified balances (net amount owed between users)

🤝 Settlement support

🛠 Tech Stack

Language: Java

Build Tool: Maven

Architecture: Layered (Model → Service → Manager)

Execution: Console-based (entry point via Main.java)

📂 Project Structure
BILL TRACKER/
├── pom.xml
└── src/main/java/com/billtracker
    ├── Main.java
    ├── model/
    │   ├── User.java
    │   ├── Group.java
    │   ├── Expense.java
    │   ├── ExpenseType.java
    │   ├── Split.java
    │   ├── EqualSplit.java
    │   ├── ExactSplit.java
    │   └── PercentSplit.java
    └── service/
        ├── ExpenseManager.java
        ├── ExpenseService.java
        └── BalanceSheetService.java

🧩 Core Components
🔹 Models

User – Represents an individual user.

Group – Represents a collection of users sharing expenses.

Expense – Represents a financial transaction within a group.

Split – Abstract representation of how an expense is divided.

EqualSplit / ExactSplit / PercentSplit – Concrete split strategies.

ExpenseType – Enum defining the split type.

🔹 Services

ExpenseService

Validates and processes expenses.

Applies split logic based on expense type.

ExpenseManager

Acts as the orchestrator for creating users, groups, and expenses.

BalanceSheetService

Maintains who owes whom and how much.

Ensures balances are stored in a simplified (net) form.

🔄 Supported Expense Splits
1️⃣ Equal Split

Expense is divided equally among all participants.

2️⃣ Exact Amount Split

Each user pays a fixed amount.

Validation ensures total equals expense amount.

3️⃣ Percentage Split

Expense is divided based on user-defined percentages.

Percentages must sum to 100%.

📊 Balance Tracking Logic

The system tracks balances as:

User A → User B : Amount


If two users owe each other, balances are netted to avoid redundancy.

Users can view:

How much they owe others

How much others owe them

▶️ How to Run
Prerequisites

Java 8 or above

Maven installed

Steps
mvn clean install


Run the application by executing:

Main.java


(Mock users, groups, and expenses are created via code.)
