package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class PaymentMethod(val displayName: String) {
    UPI("UPI"),
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    NET_BANKING("Net Banking")
}

object Categories {
    val expenseCategories = listOf(
        "Food",
        "Transport",
        "Shopping",
        "Bills",
        "Health",
        "Education",
        "Entertainment",
        "Home",
        "Travel",
        "Other"
    )

    val incomeCategories = listOf(
        "Salary",
        "Freelance Income",
        "Business",
        "Investments",
        "Other"
    )

    fun getAll(): List<String> = (expenseCategories + incomeCategories).distinct()
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // EXPENSE or INCOME
    val category: String,
    val date: Long,
    val paymentMethod: String,
    val notes: String = "",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthYear: String, // Format "yyyy-MM", e.g., "2026-10"
    val category: String,  // "TOTAL" or specific category name
    val allocatedAmount: Double,
    val isDemo: Boolean = false
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long? = null,
    val category: String = "General",
    val isDemo: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
