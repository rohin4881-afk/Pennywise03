package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.PennywiseRepository
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: PennywiseRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PennywiseRepository(db.pennywiseDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Pennywise", appName)
    }

    @Test
    fun `test transaction insertion and retrieval`() = runBlocking {
        val tx = TransactionEntity(
            amount = 1250.0,
            type = TransactionType.EXPENSE.name,
            category = "Food",
            date = System.currentTimeMillis(),
            paymentMethod = "UPI",
            notes = "Dinner"
        )
        val id = repository.addTransaction(tx)
        assertNotNull(id)

        val txs = db.pennywiseDao().getAllTransactionsSnapshot()
        assertEquals(1, txs.size)
        assertEquals(1250.0, txs[0].amount, 0.001)
    }

    @Test
    fun `test separate income and expense calculation`() = runBlocking {
        repository.addTransaction(
            TransactionEntity(
                amount = 50000.0,
                type = TransactionType.INCOME.name,
                category = "Salary",
                date = System.currentTimeMillis(),
                paymentMethod = "Bank Transfer"
            )
        )
        repository.addTransaction(
            TransactionEntity(
                amount = 4500.0,
                type = TransactionType.EXPENSE.name,
                category = "Groceries",
                date = System.currentTimeMillis(),
                paymentMethod = "UPI"
            )
        )

        val all = db.pennywiseDao().getAllTransactionsSnapshot()
        val totalIncome = all.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val totalExpense = all.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }

        assertEquals(50000.0, totalIncome, 0.001)
        assertEquals(4500.0, totalExpense, 0.001)
        assertEquals(45500.0, totalIncome - totalExpense, 0.001)
    }

    @Test
    fun `test savings goal contributions and withdrawals`() = runBlocking {
        val goal = com.example.data.SavingsGoalEntity(
            id = 1,
            name = "Emergency Fund",
            targetAmount = 100000.0,
            currentAmount = 20000.0
        )
        db.pennywiseDao().insertSavingsGoal(goal)

        repository.contributeToGoal(goal, 5000.0)
        var updated = db.pennywiseDao().getAllSavingsGoalsSnapshot().first()
        assertEquals(25000.0, updated.currentAmount, 0.001)

        val withdrawSuccess = repository.withdrawFromGoal(updated, 3000.0)
        org.junit.Assert.assertTrue(withdrawSuccess)
        updated = db.pennywiseDao().getAllSavingsGoalsSnapshot().first()
        assertEquals(22000.0, updated.currentAmount, 0.001)

        // Excessive withdrawal rejection
        val excessiveWithdraw = repository.withdrawFromGoal(updated, 50000.0)
        org.junit.Assert.assertFalse(excessiveWithdraw)
    }

    @Test
    fun `test demo data isolation and removal`() = runBlocking {
        repository.loadDemoData("2026-10")
        val withDemo = db.pennywiseDao().getAllTransactionsSnapshot()
        org.junit.Assert.assertTrue(withDemo.isNotEmpty())
        org.junit.Assert.assertTrue(withDemo.all { it.isDemo })

        // Add real user transaction
        repository.addTransaction(
            TransactionEntity(
                amount = 999.0,
                type = TransactionType.EXPENSE.name,
                category = "Books",
                date = System.currentTimeMillis(),
                paymentMethod = "UPI",
                isDemo = false
            )
        )

        // Remove demo data
        repository.removeDemoData()
        val remaining = db.pennywiseDao().getAllTransactionsSnapshot()
        assertEquals(1, remaining.size)
        assertEquals(999.0, remaining[0].amount, 0.001)
        org.junit.Assert.assertFalse(remaining[0].isDemo)
    }
}
