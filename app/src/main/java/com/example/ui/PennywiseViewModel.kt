package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AuditLogEntity
import com.example.data.BudgetEntity
import com.example.data.PennywiseRepository
import com.example.data.SavingsGoalEntity
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    TRANSACTIONS("Transactions"),
    BUDGETS("Budgets"),
    SAVINGS("Savings"),
    REPORTS("Reports"),
    SETTINGS("Settings")
}

enum class TransactionSortOption {
    DATE_DESC,
    DATE_ASC,
    AMOUNT_DESC,
    AMOUNT_ASC
}

data class DashboardMetrics(
    val todaySpend: Double = 0.0,
    val monthlySpend: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val totalBudgetAllocated: Double = 0.0,
    val remainingBudget: Double = 0.0,
    val hasBudget: Boolean = false,
    val isBudgetExceeded: Boolean = false,
    val dailySafeToSpend: Double = 0.0,
    val remainingDays: Int = 0,
    val safeToSpendMessage: String = "",
    val categorySpends: Map<String, Double> = emptyMap()
)

class PennywiseViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("pennywise_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getInstance(application)
    val repository = PennywiseRepository(database.pennywiseDao())

    // Month Selector: Year and 0-indexed month
    private val calendar = Calendar.getInstance()
    private val _selectedYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow(calendar.get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // Navigation
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Preferences
    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isAppLocked = MutableStateFlow(prefs.getBoolean("is_pin_enabled", false))
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _isUnlocked = MutableStateFlow(!prefs.getBoolean("is_pin_enabled", false))
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _savedPin = MutableStateFlow(prefs.getString("app_pin", "") ?: "")
    val savedPin: StateFlow<String> = _savedPin.asStateFlow()

    // Transaction Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow<String?>("ALL") // ALL, EXPENSE, INCOME
    val filterType: StateFlow<String?> = _filterType.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>("All")
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    private val _filterPaymentMethod = MutableStateFlow<String?>("All")
    val filterPaymentMethod: StateFlow<String?> = _filterPaymentMethod.asStateFlow()

    private val _sortOption = MutableStateFlow(TransactionSortOption.DATE_DESC)
    val sortOption: StateFlow<TransactionSortOption> = _sortOption.asStateFlow()

    // Reactive Data Sources
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month String representation "yyyy-MM"
    val selectedMonthYearString = combine(_selectedYear, _selectedMonth) { year, month ->
        String.format(java.util.Locale.US, "%04d-%02d", year, month + 1)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Filtered Transactions for Transactions screen
    private val filterTextAndType = combine(_searchQuery, _filterType, _filterCategory) { q, t, c ->
        Triple(q, t, c)
    }
    private val filterMethodAndSort = combine(_filterPaymentMethod, _sortOption) { m, s ->
        Pair(m, s)
    }

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        filterTextAndType,
        filterMethodAndSort
    ) { txs, (query, type, category), (method, sort) ->
        var list = txs

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.notes.lowercase().contains(q) ||
                it.category.lowercase().contains(q) ||
                it.amount.toString().contains(q)
            }
        }

        if (type != null && type != "ALL") {
            list = list.filter { it.type == type }
        }

        if (category != null && category != "All") {
            list = list.filter { it.category == category }
        }

        if (method != null && method != "All") {
            list = list.filter { it.paymentMethod == method }
        }

        when (sort) {
            TransactionSortOption.DATE_DESC -> list.sortedWith(compareByDescending<TransactionEntity> { it.date }.thenByDescending { it.id })
            TransactionSortOption.DATE_ASC -> list.sortedWith(compareBy<TransactionEntity> { it.date }.thenBy { it.id })
            TransactionSortOption.AMOUNT_DESC -> list.sortedByDescending { it.amount }
            TransactionSortOption.AMOUNT_ASC -> list.sortedBy { it.amount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Month Budgets
    val currentMonthBudgets: StateFlow<List<BudgetEntity>> = combine(
        selectedMonthYearString,
        database.pennywiseDao().getAllBudgets()
    ) { monthYear, allBudgets ->
        allBudgets.filter { it.monthYear == monthYear }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(
        allTransactions,
        currentMonthBudgets,
        _selectedYear,
        _selectedMonth
    ) { txs, budgets, year, month ->
        val startOfMonth = DateUtils.getStartOfMonth(year, month)
        val endOfMonth = DateUtils.getEndOfMonth(year, month)
        val startOfToday = DateUtils.getStartOfToday()
        val endOfToday = DateUtils.getEndOfToday()

        var todaySpend = 0.0
        var monthlySpend = 0.0
        var monthlyIncome = 0.0
        val categorySpends = mutableMapOf<String, Double>()

        for (tx in txs) {
            if (tx.date in startOfMonth..endOfMonth) {
                if (tx.type == TransactionType.EXPENSE.name) {
                    monthlySpend += tx.amount
                    categorySpends[tx.category] = (categorySpends[tx.category] ?: 0.0) + tx.amount
                    if (tx.date in startOfToday..endOfToday) {
                        todaySpend += tx.amount
                    }
                } else if (tx.type == TransactionType.INCOME.name) {
                    monthlyIncome += tx.amount
                }
            }
        }

        val totalBudgetObj = budgets.firstOrNull { it.category == "TOTAL" }
        val totalBudgetAllocated = totalBudgetObj?.allocatedAmount ?: 0.0
        val hasBudget = totalBudgetObj != null && totalBudgetAllocated > 0.0
        val remainingBudget = if (hasBudget) totalBudgetAllocated - monthlySpend else 0.0
        val isExceeded = hasBudget && remainingBudget < 0.0

        val remainingDays = DateUtils.getRemainingDaysInMonth(year, month)

        val (dailySafe, message) = when {
            !hasBudget -> {
                0.0 to "No monthly budget set. Create one in Budgets to calculate daily safe limit."
            }
            isExceeded -> {
                0.0 to "Monthly budget exceeded by ₹${String.format(java.util.Locale.US, "%.0f", -remainingBudget)}. Safe spend is ₹0."
            }
            remainingDays <= 0 -> {
                0.0 to "This billing period has ended."
            }
            else -> {
                val safe = remainingBudget / remainingDays
                safe to "Based on ₹${String.format(java.util.Locale.US, "%.0f", remainingBudget)} remaining over $remainingDays days."
            }
        }

        DashboardMetrics(
            todaySpend = todaySpend,
            monthlySpend = monthlySpend,
            monthlyIncome = monthlyIncome,
            totalBudgetAllocated = totalBudgetAllocated,
            remainingBudget = remainingBudget,
            hasBudget = hasBudget,
            isBudgetExceeded = isExceeded,
            dailySafeToSpend = dailySafe,
            remainingDays = remainingDays,
            safeToSpendMessage = message,
            categorySpends = categorySpends
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun nextMonth() {
        if (_selectedMonth.value == 11) {
            _selectedMonth.value = 0
            _selectedYear.value += 1
        } else {
            _selectedMonth.value += 1
        }
    }

    fun previousMonth() {
        if (_selectedMonth.value == 0) {
            _selectedMonth.value = 11
            _selectedYear.value -= 1
        } else {
            _selectedMonth.value -= 1
        }
    }

    fun resetToCurrentMonth() {
        val now = Calendar.getInstance()
        _selectedYear.value = now.get(Calendar.YEAR)
        _selectedMonth.value = now.get(Calendar.MONTH)
    }

    fun toggleTheme() {
        val next = !_isDarkTheme.value
        _isDarkTheme.value = next
        prefs.edit().putBoolean("is_dark_theme", next).apply()
    }

    fun setAppPin(pin: String) {
        if (pin.length == 4) {
            prefs.edit().putString("app_pin", pin).putBoolean("is_pin_enabled", true).apply()
            _savedPin.value = pin
            _isAppLocked.value = true
            _isUnlocked.value = true
        }
    }

    fun removeAppPin() {
        prefs.edit().remove("app_pin").putBoolean("is_pin_enabled", false).apply()
        _savedPin.value = ""
        _isAppLocked.value = false
        _isUnlocked.value = true
    }

    fun unlockWithPin(entered: String): Boolean {
        return if (entered == _savedPin.value) {
            _isUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockApp() {
        if (_isAppLocked.value) {
            _isUnlocked.value = false
        }
    }

    // Filter controls
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: String?) {
        _filterType.value = type
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
    }

    fun setFilterPaymentMethod(method: String?) {
        _filterPaymentMethod.value = method
    }

    fun setSortOption(sort: TransactionSortOption) {
        _sortOption.value = sort
    }

    // CRUD operations
    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        date: Long,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    amount = amount,
                    type = type.name,
                    category = category,
                    date = date,
                    paymentMethod = paymentMethod,
                    notes = notes.trim()
                )
            )
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun upsertBudget(category: String, amount: Double) {
        viewModelScope.launch {
            val monthYear = selectedMonthYearString.value
            val existing = currentMonthBudgets.value.firstOrNull { it.category == category }
            if (existing != null) {
                repository.upsertBudget(existing.copy(allocatedAmount = amount))
            } else {
                repository.upsertBudget(
                    BudgetEntity(
                        monthYear = monthYear,
                        category = category,
                        allocatedAmount = amount
                    )
                )
            }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
        }
    }

    fun addSavingsGoal(name: String, targetAmount: Double, initialAmount: Double, targetDate: Long?, category: String) {
        viewModelScope.launch {
            repository.addSavingsGoal(
                SavingsGoalEntity(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    currentAmount = initialAmount,
                    targetDate = targetDate,
                    category = category
                )
            )
        }
    }

    fun updateSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.updateSavingsGoal(goal)
        }
    }

    fun deleteSavingsGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
        }
    }

    fun contributeToGoal(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            repository.contributeToGoal(goal, amount)
        }
    }

    fun withdrawFromGoal(goal: SavingsGoalEntity, amount: Double, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.withdrawFromGoal(goal, amount)
            onResult(success)
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            repository.loadDemoData(selectedMonthYearString.value)
        }
    }

    fun removeDemoData() {
        viewModelScope.launch {
            repository.removeDemoData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}
