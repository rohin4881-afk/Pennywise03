package com.example.data

import com.example.util.DemoDataProvider
import com.example.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PennywiseRepository(private val dao: PennywiseDao) {

    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = dao.getAllSavingsGoals()
    val recentAuditLogs: Flow<List<AuditLogEntity>> = dao.getRecentAuditLogs()

    fun getTransactionsForRange(start: Long, end: Long): Flow<List<TransactionEntity>> {
        return dao.getTransactionsBetween(start, end)
    }

    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> {
        return dao.getBudgetsForMonth(monthYear)
    }

    suspend fun addTransaction(tx: TransactionEntity): Long = withContext(Dispatchers.IO) {
        dao.insertTransaction(tx)
    }

    suspend fun updateTransaction(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.updateTransaction(tx)
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransactionById(id)
    }

    suspend fun upsertBudget(budget: BudgetEntity): Long = withContext(Dispatchers.IO) {
        dao.insertBudget(budget)
    }

    suspend fun deleteBudget(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteBudgetById(id)
    }

    suspend fun addSavingsGoal(goal: SavingsGoalEntity): Long = withContext(Dispatchers.IO) {
        dao.insertSavingsGoal(goal)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        dao.updateSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteSavingsGoalById(id)
    }

    suspend fun contributeToGoal(goal: SavingsGoalEntity, amount: Double) = withContext(Dispatchers.IO) {
        if (amount > 0) {
            val updated = goal.copy(currentAmount = goal.currentAmount + amount)
            dao.updateSavingsGoal(updated)
            dao.insertAuditLog(
                AuditLogEntity(
                    action = "GOAL_CONTRIBUTION",
                    details = "Contributed ₹$amount to '${goal.name}'"
                )
            )
        }
    }

    suspend fun withdrawFromGoal(goal: SavingsGoalEntity, amount: Double): Boolean = withContext(Dispatchers.IO) {
        if (amount > 0 && goal.currentAmount >= amount) {
            val updated = goal.copy(currentAmount = goal.currentAmount - amount)
            dao.updateSavingsGoal(updated)
            dao.insertAuditLog(
                AuditLogEntity(
                    action = "GOAL_WITHDRAWAL",
                    details = "Withdrew ₹$amount from '${goal.name}'"
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun countAllRecords(): Int = withContext(Dispatchers.IO) {
        val txs = dao.getAllTransactionsSnapshot().size
        val bgs = dao.getAllBudgetsSnapshot().size
        val gls = dao.getAllSavingsGoalsSnapshot().size
        txs + bgs + gls
    }

    suspend fun loadDemoData(monthYear: String) = withContext(Dispatchers.IO) {
        val txs = DemoDataProvider.generateDemoTransactions()
        val budgets = DemoDataProvider.generateDemoBudgets(monthYear)
        val goals = DemoDataProvider.generateDemoSavingsGoals()

        dao.insertTransactions(txs)
        dao.insertBudgets(budgets)
        dao.insertSavingsGoals(goals)
        dao.insertAuditLog(
            AuditLogEntity(
                action = "DEMO_DATA_LOADED",
                details = "Loaded ${txs.size} transactions, ${budgets.size} budgets, ${goals.size} goals."
            )
        )
    }

    suspend fun removeDemoData() = withContext(Dispatchers.IO) {
        dao.deleteDemoTransactions()
        dao.deleteDemoBudgets()
        dao.deleteDemoSavingsGoals()
        dao.insertAuditLog(
            AuditLogEntity(
                action = "DEMO_DATA_REMOVED",
                details = "Purged all fictional demo records without altering user data."
            )
        )
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearAllTransactions()
        dao.clearAllBudgets()
        dao.clearAllSavingsGoals()
        dao.insertAuditLog(
            AuditLogEntity(
                action = "ALL_DATA_CLEARED",
                details = "Purged all local transactions, budgets, and savings goals."
            )
        )
    }

    fun exportTransactionsToCsv(transactions: List<TransactionEntity>): String {
        val sb = StringBuilder()
        // CSV Header
        sb.append("ID,Date,Type,Category,Amount,Payment Method,Notes\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (tx in transactions) {
            val id = tx.id.toString()
            val date = dateFormat.format(Date(tx.date))
            val type = tx.type
            val category = SecurityUtils.sanitizeForCsv(tx.category)
            val amount = String.format(Locale.US, "%.2f", tx.amount)
            val method = SecurityUtils.sanitizeForCsv(tx.paymentMethod)
            val notes = SecurityUtils.sanitizeForCsv(tx.notes)

            sb.append("$id,\"$date\",$type,$category,$amount,$method,$notes\n")
        }
        return sb.toString()
    }

    suspend fun exportFullBackupJson(): String = withContext(Dispatchers.IO) {
        val txs = dao.getAllTransactionsSnapshot()
        val budgets = dao.getAllBudgetsSnapshot()
        val goals = dao.getAllSavingsGoalsSnapshot()

        val root = JSONObject()
        root.put("app", "PENNYWISE")
        root.put("schemaVersion", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val txArray = JSONArray()
        for (tx in txs) {
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("amount", tx.amount)
            obj.put("type", tx.type)
            obj.put("category", tx.category)
            obj.put("date", tx.date)
            obj.put("paymentMethod", tx.paymentMethod)
            obj.put("notes", tx.notes)
            obj.put("isDemo", tx.isDemo)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val bgArray = JSONArray()
        for (b in budgets) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("monthYear", b.monthYear)
            obj.put("category", b.category)
            obj.put("allocatedAmount", b.allocatedAmount)
            obj.put("isDemo", b.isDemo)
            bgArray.put(obj)
        }
        root.put("budgets", bgArray)

        val glArray = JSONArray()
        for (g in goals) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("name", g.name)
            obj.put("targetAmount", g.targetAmount)
            obj.put("currentAmount", g.currentAmount)
            if (g.targetDate != null) {
                obj.put("targetDate", g.targetDate)
            }
            obj.put("category", g.category)
            obj.put("isDemo", g.isDemo)
            glArray.put(obj)
        }
        root.put("savingsGoals", glArray)

        root.toString(2)
    }

    suspend fun restoreBackupJson(jsonStr: String, replaceExisting: Boolean): Result<String> = withContext(Dispatchers.IO) {
        val validation = SecurityUtils.validateBackupJson(jsonStr)
        if (!validation.isValid) {
            return@withContext Result.failure(Exception(validation.errorMessage ?: "Invalid backup file"))
        }

        try {
            val root = JSONObject(jsonStr)
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            val bgArray = root.optJSONArray("budgets") ?: JSONArray()
            val glArray = root.optJSONArray("savingsGoals") ?: JSONArray()

            val txList = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                txList.add(
                    TransactionEntity(
                        id = if (replaceExisting) obj.optLong("id", 0) else 0,
                        amount = obj.getDouble("amount"),
                        type = obj.getString("type"),
                        category = obj.getString("category"),
                        date = obj.getLong("date"),
                        paymentMethod = obj.getString("paymentMethod"),
                        notes = obj.optString("notes", ""),
                        isDemo = obj.optBoolean("isDemo", false)
                    )
                )
            }

            val bgList = mutableListOf<BudgetEntity>()
            for (i in 0 until bgArray.length()) {
                val obj = bgArray.getJSONObject(i)
                bgList.add(
                    BudgetEntity(
                        id = if (replaceExisting) obj.optLong("id", 0) else 0,
                        monthYear = obj.getString("monthYear"),
                        category = obj.getString("category"),
                        allocatedAmount = obj.getDouble("allocatedAmount"),
                        isDemo = obj.optBoolean("isDemo", false)
                    )
                )
            }

            val glList = mutableListOf<SavingsGoalEntity>()
            for (i in 0 until glArray.length()) {
                val obj = glArray.getJSONObject(i)
                glList.add(
                    SavingsGoalEntity(
                        id = if (replaceExisting) obj.optLong("id", 0) else 0,
                        name = obj.getString("name"),
                        targetAmount = obj.getDouble("targetAmount"),
                        currentAmount = obj.getDouble("currentAmount"),
                        targetDate = if (obj.has("targetDate")) obj.getLong("targetDate") else null,
                        category = obj.optString("category", "General"),
                        isDemo = obj.optBoolean("isDemo", false)
                    )
                )
            }

            if (replaceExisting) {
                dao.clearAllTransactions()
                dao.clearAllBudgets()
                dao.clearAllSavingsGoals()
            }

            dao.insertTransactions(txList)
            dao.insertBudgets(bgList)
            dao.insertSavingsGoals(glList)

            dao.insertAuditLog(
                AuditLogEntity(
                    action = if (replaceExisting) "BACKUP_RESTORE_REPLACE" else "BACKUP_RESTORE_MERGE",
                    details = "Restored ${txList.size} transactions, ${bgList.size} budgets, ${glList.size} goals."
                )
            )

            Result.success("Successfully restored ${txList.size} transactions, ${bgList.size} budgets, and ${glList.size} savings goals.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
