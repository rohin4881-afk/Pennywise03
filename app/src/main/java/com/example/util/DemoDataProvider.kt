package com.example.util

import com.example.data.BudgetEntity
import com.example.data.PaymentMethod
import com.example.data.SavingsGoalEntity
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import java.util.Calendar

object DemoDataProvider {

    fun generateDemoTransactions(): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        fun timeOffsetDays(daysAgo: Int, hour: Int = 14): Long {
            return Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 30)
            }.timeInMillis
        }

        return listOf(
            TransactionEntity(
                amount = 95000.0,
                type = TransactionType.INCOME.name,
                category = "Salary",
                date = timeOffsetDays(11, 9),
                paymentMethod = PaymentMethod.NET_BANKING.displayName,
                notes = "Monthly tech salary credit",
                isDemo = true
            ),
            TransactionEntity(
                amount = 28000.0,
                type = TransactionType.INCOME.name,
                category = "Freelance Income",
                date = timeOffsetDays(4, 16),
                paymentMethod = PaymentMethod.NET_BANKING.displayName,
                notes = "Fintech app dashboard design project",
                isDemo = true
            ),
            TransactionEntity(
                amount = 22000.0,
                type = TransactionType.EXPENSE.name,
                category = "Home",
                date = timeOffsetDays(10, 11),
                paymentMethod = PaymentMethod.NET_BANKING.displayName,
                notes = "Apartment rent payment",
                isDemo = true
            ),
            TransactionEntity(
                amount = 4850.0,
                type = TransactionType.EXPENSE.name,
                category = "Shopping",
                date = timeOffsetDays(7, 18),
                paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                notes = "DMart monthly household groceries",
                isDemo = true
            ),
            TransactionEntity(
                amount = 1820.0,
                type = TransactionType.EXPENSE.name,
                category = "Bills",
                date = timeOffsetDays(8, 10),
                paymentMethod = PaymentMethod.UPI.displayName,
                notes = "BESCOM electricity bill",
                isDemo = true
            ),
            TransactionEntity(
                amount = 999.0,
                type = TransactionType.EXPENSE.name,
                category = "Bills",
                date = timeOffsetDays(6, 12),
                paymentMethod = PaymentMethod.UPI.displayName,
                notes = "ACT Fibernet broadband bill",
                isDemo = true
            ),
            TransactionEntity(
                amount = 1250.0,
                type = TransactionType.EXPENSE.name,
                category = "Food",
                date = timeOffsetDays(5, 20),
                paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                notes = "Dinner with friends at Truffles",
                isDemo = true
            ),
            TransactionEntity(
                amount = 480.0,
                type = TransactionType.EXPENSE.name,
                category = "Food",
                date = timeOffsetDays(2, 13),
                paymentMethod = PaymentMethod.UPI.displayName,
                notes = "Swiggy lunch bowl",
                isDemo = true
            ),
            TransactionEntity(
                amount = 320.0,
                type = TransactionType.EXPENSE.name,
                category = "Food",
                date = timeOffsetDays(0, 12),
                paymentMethod = PaymentMethod.UPI.displayName,
                notes = "South Indian breakfast & filter coffee",
                isDemo = true
            ),
            TransactionEntity(
                amount = 1500.0,
                type = TransactionType.EXPENSE.name,
                category = "Transport",
                date = timeOffsetDays(3, 17),
                paymentMethod = PaymentMethod.DEBIT_CARD.displayName,
                notes = "Petrol refill at Indian Oil",
                isDemo = true
            ),
            TransactionEntity(
                amount = 500.0,
                type = TransactionType.EXPENSE.name,
                category = "Transport",
                date = timeOffsetDays(1, 9),
                paymentMethod = PaymentMethod.UPI.displayName,
                notes = "Metro smart card recharge",
                isDemo = true
            ),
            TransactionEntity(
                amount = 640.0,
                type = TransactionType.EXPENSE.name,
                category = "Health",
                date = timeOffsetDays(2, 19),
                paymentMethod = PaymentMethod.UPI.displayName,
                notes = "Apollo Pharmacy vitamins & medicine",
                isDemo = true
            ),
            TransactionEntity(
                amount = 850.0,
                type = TransactionType.EXPENSE.name,
                category = "Entertainment",
                date = timeOffsetDays(1, 21),
                paymentMethod = PaymentMethod.UPI.displayName,
                notes = "BookMyShow IMAX movie tickets",
                isDemo = true
            ),
            TransactionEntity(
                amount = 3200.0,
                type = TransactionType.EXPENSE.name,
                category = "Education",
                date = timeOffsetDays(9, 15),
                paymentMethod = PaymentMethod.CREDIT_CARD.displayName,
                notes = "Cloud architecture course certificate",
                isDemo = true
            )
        )
    }

    fun generateDemoBudgets(currentMonthYear: String): List<BudgetEntity> {
        return listOf(
            BudgetEntity(
                monthYear = currentMonthYear,
                category = "TOTAL",
                allocatedAmount = 65000.0,
                isDemo = true
            ),
            BudgetEntity(
                monthYear = currentMonthYear,
                category = "Food",
                allocatedAmount = 14000.0,
                isDemo = true
            ),
            BudgetEntity(
                monthYear = currentMonthYear,
                category = "Home",
                allocatedAmount = 25000.0,
                isDemo = true
            ),
            BudgetEntity(
                monthYear = currentMonthYear,
                category = "Shopping",
                allocatedAmount = 8000.0,
                isDemo = true
            ),
            BudgetEntity(
                monthYear = currentMonthYear,
                category = "Transport",
                allocatedAmount = 5000.0,
                isDemo = true
            ),
            BudgetEntity(
                monthYear = currentMonthYear,
                category = "Bills",
                allocatedAmount = 6000.0,
                isDemo = true
            ),
            BudgetEntity(
                monthYear = currentMonthYear,
                category = "Entertainment",
                allocatedAmount = 3500.0,
                isDemo = true
            )
        )
    }

    fun generateDemoSavingsGoals(): List<SavingsGoalEntity> {
        val targetDateYear = Calendar.getInstance().apply { add(Calendar.MONTH, 6) }.timeInMillis
        return listOf(
            SavingsGoalEntity(
                name = "Emergency Reserve Fund",
                targetAmount = 150000.0,
                currentAmount = 90000.0,
                targetDate = targetDateYear,
                category = "Safety",
                isDemo = true
            ),
            SavingsGoalEntity(
                name = "Diwali Festival & Gifts",
                targetAmount = 30000.0,
                currentAmount = 21500.0,
                targetDate = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.timeInMillis,
                category = "Celebration",
                isDemo = true
            ),
            SavingsGoalEntity(
                name = "MacBook Pro M-Series",
                targetAmount = 135000.0,
                currentAmount = 52000.0,
                targetDate = Calendar.getInstance().apply { add(Calendar.MONTH, 4) }.timeInMillis,
                category = "Tech Gear",
                isDemo = true
            )
        )
    }
}
