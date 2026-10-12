package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.screens.*
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.PennywiseTheme
import com.example.util.AuthUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import androidx.credentials.CredentialManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PennywiseApp(
    viewModel: PennywiseViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val isPinEnabled by viewModel.isAppLocked.collectAsStateWithLifecycle()

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var hasSkippedAuth by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    PennywiseTheme(darkTheme = isDarkTheme) {
        if (isPinEnabled && !isUnlocked) {
            PinUnlockScreen(
                onUnlock = { pin -> viewModel.unlockWithPin(pin) }
            )
        } else if (currentUser == null && !hasSkippedAuth) {
            // First-time or unauthenticated Google Sign-In gate with reCAPTCHA verification
            SignInScreen(
                onAuthSuccess = {
                    currentUser = Firebase.auth.currentUser
                    hasSkippedAuth = true
                },
                onSkipSignIn = {
                    hasSkippedAuth = true
                }
            )
        } else {
            MainAppScaffold(
                viewModel = viewModel,
                currentUser = currentUser,
                onShowSignIn = { hasSkippedAuth = false },
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: PennywiseViewModel,
    currentUser: FirebaseUser?,
    onShowSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showSignInModal by remember { mutableStateOf(false) }
    var showAddExpenseScreen by remember { mutableStateOf(false) }

    // Silent Auto-Sign-In attempt on launch
    LaunchedEffect(Unit) {
        if (currentUser == null) {
            val credentialManager = CredentialManager.create(context)
            AuthUtils.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = coroutineScope
            )
        }
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val currentMonthBudgets by viewModel.currentMonthBudgets.collectAsStateWithLifecycle()
    val allSavingsGoals by viewModel.allSavingsGoals.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()
    val filterCategory by viewModel.filterCategory.collectAsStateWithLifecycle()
    val filterMethod by viewModel.filterPaymentMethod.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    var showQuickAddDialog by remember { mutableStateOf(false) }
    var transactionForEditModal by remember { mutableStateOf<TransactionEntity?>(null) }

    // Handle back button when not on Dashboard
    if (currentTab != AppTab.DASHBOARD) {
        BackHandler {
            viewModel.selectTab(AppTab.DASHBOARD)
        }
    }

    // Adaptive Window: check width to switch between Bottom Navigation (Mobile) and Navigation Rail (Tablet/Desktop)
    val configuration = LocalConfiguration.current
    val isExpandedOrMedium = configuration.screenWidthDp >= 600

    val navItems = listOf(
        Triple(AppTab.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
        Triple(AppTab.TRANSACTIONS, Icons.Default.ReceiptLong, "Transactions"),
        Triple(AppTab.BUDGETS, Icons.Default.PieChart, "Budgets"),
        Triple(AppTab.SAVINGS, Icons.Default.Savings, "Savings"),
        Triple(AppTab.REPORTS, Icons.Default.BarChart, "Reports"),
        Triple(AppTab.SETTINGS, Icons.Default.Settings, "Settings")
    )

    fun shareExportedCsv(csvContent: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Pennywise Transactions Export.csv")
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
        val chooser = Intent.createChooser(intent, "Share Transactions CSV")
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Pennywise CSV", csvContent))
            Toast.makeText(context, "Copied CSV to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.pennywise_logo),
                            contentDescription = "Pennywise Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Column {
                            Text(
                                text = "PENNYWISE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = currentTab.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    val isPinEnabled by viewModel.isAppLocked.collectAsStateWithLifecycle()
                    if (isPinEnabled) {
                        IconButton(
                            onClick = { viewModel.lockApp() },
                            modifier = Modifier.testTag("lock_app_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock App",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("quick_theme_toggle_btn")
                    ) {
                        val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    IconButton(
                        onClick = {
                            if (currentUser == null) {
                                showSignInModal = true
                            } else {
                                viewModel.selectTab(AppTab.SETTINGS)
                            }
                        },
                        modifier = Modifier.testTag("topbar_google_account_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = if (currentUser != null) "Google Account (${currentUser.displayName ?: "User"})" else "Sign In with Google",
                            tint = if (currentUser != null) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            if (!isExpandedOrMedium) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    navItems.forEach { (tab, icon, label) ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ForestGreen,
                                selectedTextColor = ForestGreen,
                                indicatorColor = LimeAccent
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Adaptive Side Rail for Tablet/Desktop
            if (isExpandedOrMedium) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        FloatingActionButton(
                            onClick = { showAddExpenseScreen = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .testTag("rail_fab_add")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Expense")
                        }
                    }
                ) {
                    navItems.forEach { (tab, icon, label) ->
                        NavigationRailItem(
                            selected = currentTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = ForestGreen,
                                selectedTextColor = ForestGreen,
                                indicatorColor = LimeAccent
                            )
                        )
                    }
                }
            }

            // Tab Screen Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (currentTab) {
                    AppTab.DASHBOARD -> {
                        DashboardScreen(
                            year = selectedYear,
                            month = selectedMonth,
                            metrics = metrics,
                            recentTransactions = allTransactions,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onResetMonth = { viewModel.resetToCurrentMonth() },
                            onNavigateTab = { tab -> viewModel.selectTab(tab) },
                            onOpenAddTransaction = { showAddExpenseScreen = true },
                            onTransactionClick = { tx -> transactionForEditModal = tx }
                        )
                    }

                    AppTab.TRANSACTIONS -> {
                        TransactionsScreen(
                            transactions = filteredTransactions,
                            searchQuery = searchQuery,
                            filterType = filterType,
                            filterCategory = filterCategory,
                            filterPaymentMethod = filterMethod,
                            sortOption = sortOption,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onFilterTypeChange = { viewModel.setFilterType(it) },
                            onFilterCategoryChange = { viewModel.setFilterCategory(it) },
                            onFilterMethodChange = { viewModel.setFilterPaymentMethod(it) },
                            onSortChange = { viewModel.setSortOption(it) },
                            onOpenAddExpenseScreen = { showAddExpenseScreen = true },
                            onAddTransaction = { amount, type, category, date, method, notes ->
                                viewModel.addTransaction(amount, type, category, date, method, notes)
                            },
                            onUpdateTransaction = { tx -> viewModel.updateTransaction(tx) },
                            onDeleteTransaction = { id -> viewModel.deleteTransaction(id) }
                        )
                    }

                    AppTab.BUDGETS -> {
                        BudgetsScreen(
                            year = selectedYear,
                            month = selectedMonth,
                            budgets = currentMonthBudgets,
                            categorySpends = metrics.categorySpends,
                            monthlyTotalSpend = metrics.monthlySpend,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onResetMonth = { viewModel.resetToCurrentMonth() },
                            onUpsertBudget = { cat, amt -> viewModel.upsertBudget(cat, amt) },
                            onDeleteBudget = { id -> viewModel.deleteBudget(id) }
                        )
                    }

                    AppTab.SAVINGS -> {
                        SavingsGoalsScreen(
                            goals = allSavingsGoals,
                            onAddGoal = { name, target, initial, targetDate, category ->
                                viewModel.addSavingsGoal(name, target, initial, targetDate, category)
                            },
                            onUpdateGoal = { goal -> viewModel.updateSavingsGoal(goal) },
                            onDeleteGoal = { id -> viewModel.deleteSavingsGoal(id) },
                            onContribute = { goal, amt -> viewModel.contributeToGoal(goal, amt) },
                            onWithdraw = { goal, amt, callback -> viewModel.withdrawFromGoal(goal, amt, callback) }
                        )
                    }

                    AppTab.REPORTS -> {
                        ReportsScreen(
                            year = selectedYear,
                            month = selectedMonth,
                            transactions = allTransactions,
                            budgets = currentMonthBudgets,
                            onPreviousMonth = { viewModel.previousMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onResetMonth = { viewModel.resetToCurrentMonth() },
                            onExportCsv = { txList ->
                                val csv = viewModel.repository.exportTransactionsToCsv(txList)
                                shareExportedCsv(csv)
                            }
                        )
                    }

                    AppTab.SETTINGS -> {
                        val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
                        val isPin by viewModel.isAppLocked.collectAsStateWithLifecycle()

                        SettingsScreen(
                            isDarkTheme = isDark,
                            isPinEnabled = isPin,
                            auditLogs = auditLogs,
                            currentUser = currentUser,
                            onSignInWithGoogle = { showSignInModal = true },
                            onSignOutGoogle = {
                                AuthUtils.signOut(
                                    context = context,
                                    credentialManager = CredentialManager.create(context),
                                    onSignOutComplete = {},
                                    scope = coroutineScope
                                )
                            },
                            onToggleTheme = { viewModel.toggleTheme() },
                            onSetPin = { pin -> viewModel.setAppPin(pin) },
                            onRemovePin = { viewModel.removeAppPin() },
                            onExportCsv = {
                                val csv = viewModel.repository.exportTransactionsToCsv(allTransactions)
                                shareExportedCsv(csv)
                            },
                            onGenerateJsonBackup = { callback ->
                                coroutineScope.launch {
                                    val json = viewModel.repository.exportFullBackupJson()
                                    callback(json)
                                }
                            },
                            onRestoreJsonBackup = { json, replace, callback ->
                                coroutineScope.launch {
                                    val result = viewModel.repository.restoreBackupJson(json, replace)
                                    callback(result)
                                }
                            },
                            onLoadDemoData = { viewModel.loadDemoData() },
                            onRemoveDemoData = { viewModel.removeDemoData() },
                            onClearAllData = { viewModel.clearAllData() }
                        )
                    }
                }
            }
        }
    }

    // Full-screen Add Expense Form
    if (showAddExpenseScreen) {
        AddExpenseScreen(
            onNavigateBack = { showAddExpenseScreen = false },
            onSaveExpense = { amount, category, date, method, notes ->
                viewModel.addTransaction(
                    amount = amount,
                    type = TransactionType.EXPENSE,
                    category = category,
                    date = date,
                    paymentMethod = method,
                    notes = notes
                )
                showAddExpenseScreen = false
            }
        )
    }

    // Google Sign-In & reCAPTCHA Modal
    if (showSignInModal) {
        SignInScreen(
            onAuthSuccess = { showSignInModal = false },
            onSkipSignIn = { showSignInModal = false }
        )
    }

    // Quick Add Transaction Modal (Triggered by Dashboard or Rail)
    if (showQuickAddDialog) {
        TransactionAddEditDialog(
            initialTransaction = null,
            onDismiss = { showQuickAddDialog = false },
            onSave = { amount, type, category, date, method, notes ->
                viewModel.addTransaction(amount, type, category, date, method, notes)
                showQuickAddDialog = false
            },
            onDelete = {}
        )
    }

    // Edit Transaction Modal (Triggered when tapping a transaction row on Dashboard)
    if (transactionForEditModal != null) {
        TransactionAddEditDialog(
            initialTransaction = transactionForEditModal,
            onDismiss = { transactionForEditModal = null },
            onSave = { amount, type, category, date, method, notes ->
                viewModel.updateTransaction(
                    transactionForEditModal!!.copy(
                        amount = amount,
                        type = type.name,
                        category = category,
                        date = date,
                        paymentMethod = method,
                        notes = notes
                    )
                )
                transactionForEditModal = null
            },
            onDelete = { tx ->
                viewModel.deleteTransaction(tx.id)
                transactionForEditModal = null
            }
        )
    }
}
