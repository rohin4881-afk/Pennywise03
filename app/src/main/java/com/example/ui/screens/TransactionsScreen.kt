package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.Categories
import com.example.data.PaymentMethod
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.TransactionSortOption
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.TransactionItemRow
import com.example.util.DateUtils
import com.example.util.IndianCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    searchQuery: String,
    filterType: String?,
    filterCategory: String?,
    filterPaymentMethod: String?,
    sortOption: TransactionSortOption,
    onSearchChange: (String) -> Unit,
    onFilterTypeChange: (String?) -> Unit,
    onFilterCategoryChange: (String?) -> Unit,
    onFilterMethodChange: (String?) -> Unit,
    onSortChange: (TransactionSortOption) -> Unit,
    onAddTransaction: (Double, TransactionType, String, Long, String, String) -> Unit,
    onUpdateTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    showAddDialogInitially: Boolean = false,
    onDismissAddDialogInitial: () -> Unit = {},
    onOpenAddExpenseScreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAddEditDialog by remember { mutableStateOf(showAddDialogInitially) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(showAddDialogInitially) {
        if (showAddDialogInitially) {
            showAddEditDialog = true
            editingTransaction = null
            onDismissAddDialogInitial()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("transactions_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (onOpenAddExpenseScreen != null) {
                        onOpenAddExpenseScreen()
                    } else {
                        editingTransaction = null
                        showAddEditDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Filter trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search transactions, notes...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_transactions_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .testTag("open_filters_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Quick Type Filter Chips (All, Expense, Income)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == "ALL" || filterType == null,
                    onClick = { onFilterTypeChange("ALL") },
                    label = { Text("All (${transactions.size})") },
                    modifier = Modifier.testTag("filter_all_chip")
                )
                FilterChip(
                    selected = filterType == "EXPENSE",
                    onClick = { onFilterTypeChange("EXPENSE") },
                    label = { Text("Expenses") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_expense_chip")
                )
                FilterChip(
                    selected = filterType == "INCOME",
                    onClick = { onFilterTypeChange("INCOME") },
                    label = { Text("Income") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_income_chip")
                )

                if (filterCategory != "All" && filterCategory != null) {
                    AssistChip(
                        onClick = { onFilterCategoryChange("All") },
                        label = { Text("Category: $filterCategory") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        }
                    )
                }

                if (filterPaymentMethod != "All" && filterPaymentMethod != null) {
                    AssistChip(
                        onClick = { onFilterMethodChange("All") },
                        label = { Text("Method: $filterPaymentMethod") },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            // Transaction List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your filters or search query, or add a new transaction.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            onClick = {
                                editingTransaction = tx
                                showAddEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        TransactionAddEditDialog(
            initialTransaction = editingTransaction,
            onDismiss = {
                showAddEditDialog = false
                editingTransaction = null
            },
            onSave = { amount, type, category, date, method, notes ->
                if (editingTransaction != null) {
                    onUpdateTransaction(
                        editingTransaction!!.copy(
                            amount = amount,
                            type = type.name,
                            category = category,
                            date = date,
                            paymentMethod = method,
                            notes = notes
                        )
                    )
                } else {
                    onAddTransaction(amount, type, category, date, method, notes)
                }
                showAddEditDialog = false
                editingTransaction = null
            },
            onDelete = { tx ->
                transactionToDelete = tx
                showAddEditDialog = false
            }
        )
    }

    // Filter Bottom Sheet / Dialog
    if (showFilterSheet) {
        TransactionFilterDialog(
            currentCategory = filterCategory ?: "All",
            currentMethod = filterPaymentMethod ?: "All",
            currentSort = sortOption,
            onApply = { category, method, sort ->
                onFilterCategoryChange(category)
                onFilterMethodChange(method)
                onSortChange(sort)
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    // Delete Confirmation
    if (transactionToDelete != null) {
        ConfirmDialog(
            title = "Delete Transaction?",
            message = "Are you sure you want to delete this ${transactionToDelete!!.category} record of ${IndianCurrencyFormatter.formatRupees(transactionToDelete!!.amount)}? This action cannot be undone.",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteTransaction(transactionToDelete!!.id)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionAddEditDialog(
    initialTransaction: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (amount: Double, type: TransactionType, category: String, date: Long, method: String, notes: String) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    var amountText by remember {
        mutableStateOf(if (initialTransaction != null) String.format(java.util.Locale.US, "%.2f", initialTransaction.amount) else "")
    }
    var type by remember {
        mutableStateOf(
            if (initialTransaction?.type == TransactionType.INCOME.name) TransactionType.INCOME else TransactionType.EXPENSE
        )
    }
    var category by remember {
        mutableStateOf(
            initialTransaction?.category ?: (if (type == TransactionType.EXPENSE) Categories.expenseCategories.first() else Categories.incomeCategories.first())
        )
    }
    var paymentMethod by remember {
        mutableStateOf(initialTransaction?.paymentMethod ?: PaymentMethod.UPI.displayName)
    }
    var notes by remember {
        mutableStateOf(initialTransaction?.notes ?: "")
    }
    val date by remember {
        mutableStateOf(initialTransaction?.date ?: System.currentTimeMillis())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val categoryList = if (type == TransactionType.EXPENSE) Categories.expenseCategories else Categories.incomeCategories

    // Ensure category remains valid when type toggles
    LaunchedEffect(type) {
        if (!categoryList.contains(category)) {
            category = categoryList.first()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTransaction != null) "Edit Transaction" else "Add Transaction",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Switcher Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val isExpense = type == TransactionType.EXPENSE
                    Button(
                        onClick = { type = TransactionType.EXPENSE },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpense) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isExpense) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("type_expense_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Expense")
                    }
                    Button(
                        onClick = { type = TransactionType.INCOME },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isExpense) Color(0xFF2E7D32) else Color.Transparent,
                            contentColor = if (!isExpense) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("type_income_btn"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Income")
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (₹ INR)") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input"),
                    isError = errorMessage != null
                )

                // Category Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("transaction_category_select")
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categoryList.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Payment Method Dropdown
                var methodExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("transaction_method_select")
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        PaymentMethod.entries.forEach { pm ->
                            DropdownMenuItem(
                                text = { Text(pm.displayName) },
                                onClick = {
                                    paymentMethod = pm.displayName
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Swiggy dinner, Metro recharge") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_notes_input"),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    val amountVal = amountText.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0.0) {
                        errorMessage = "Please enter a valid positive amount in ₹."
                        return@Button
                    }
                    if (amountVal > 100_000_000.0) {
                        errorMessage = "Amount exceeds practical maximum limit."
                        return@Button
                    }
                    isSubmitting = true
                    onSave(amountVal, type, category, date, paymentMethod, notes)
                },
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text(if (initialTransaction != null) "Update" else "Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialTransaction != null) {
                    TextButton(
                        onClick = { onDelete(initialTransaction) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("delete_transaction_button")
                    ) {
                        Text("Delete")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun TransactionFilterDialog(
    currentCategory: String,
    currentMethod: String,
    currentSort: TransactionSortOption,
    onApply: (category: String, method: String, sort: TransactionSortOption) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentCategory) }
    var selectedMethod by remember { mutableStateOf(currentMethod) }
    var selectedSort by remember { mutableStateOf(currentSort) }

    val allCategories = listOf("All") + Categories.getAll()
    val allMethods = listOf("All") + PaymentMethod.entries.map { it.displayName }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter & Sort Transactions", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sort Options
                Column {
                    Text("Sort By", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedSort == TransactionSortOption.DATE_DESC,
                            onClick = { selectedSort = TransactionSortOption.DATE_DESC },
                            label = { Text("Newest") }
                        )
                        FilterChip(
                            selected = selectedSort == TransactionSortOption.DATE_ASC,
                            onClick = { selectedSort = TransactionSortOption.DATE_ASC },
                            label = { Text("Oldest") }
                        )
                        FilterChip(
                            selected = selectedSort == TransactionSortOption.AMOUNT_DESC,
                            onClick = { selectedSort = TransactionSortOption.AMOUNT_DESC },
                            label = { Text("Amount High-Low") }
                        )
                        FilterChip(
                            selected = selectedSort == TransactionSortOption.AMOUNT_ASC,
                            onClick = { selectedSort = TransactionSortOption.AMOUNT_ASC },
                            label = { Text("Amount Low-High") }
                        )
                    }
                }

                // Category Filter
                Column {
                    Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allCategories.take(6).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                // Payment Method Filter
                Column {
                    Text("Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allMethods.forEach { method ->
                            FilterChip(
                                selected = selectedMethod == method,
                                onClick = { selectedMethod = method },
                                label = { Text(method) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(selectedCategory, selectedMethod, selectedSort) },
                modifier = Modifier.testTag("apply_filters_button")
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onApply("All", "All", TransactionSortOption.DATE_DESC)
                }
            ) {
                Text("Reset")
            }
        }
    )
}
