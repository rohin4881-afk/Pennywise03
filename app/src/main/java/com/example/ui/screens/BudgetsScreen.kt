package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.BudgetEntity
import com.example.data.Categories
import com.example.ui.components.CategoryIconBadge
import com.example.ui.components.ConfirmDialog
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.WarningOrange
import com.example.util.IndianCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    year: Int,
    month: Int,
    budgets: List<BudgetEntity>,
    categorySpends: Map<String, Double>,
    monthlyTotalSpend: Double,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
    onUpsertBudget: (category: String, amount: Double) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var selectedBudgetForEdit by remember { mutableStateOf<BudgetEntity?>(null) }
    var budgetToDelete by remember { mutableStateOf<BudgetEntity?>(null) }
    var initialCategoryForAdd by remember { mutableStateOf("TOTAL") }

    val totalBudget = budgets.firstOrNull { it.category == "TOTAL" }
    val categoryBudgets = budgets.filter { it.category != "TOTAL" }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("budgets_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedBudgetForEdit = null
                    initialCategoryForAdd = "Food"
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_budget")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Header
            item {
                MonthSelectorHeader(
                    year = year,
                    month = month,
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth,
                    onReset = onResetMonth
                )
            }

            // Overall Monthly Budget Hero Card
            item {
                val allocated = totalBudget?.allocatedAmount ?: 0.0
                val remaining = allocated - monthlyTotalSpend
                val percent = if (allocated > 0) (monthlyTotalSpend / allocated).coerceIn(0.0, 2.0).toFloat() else 0f
                val isExceeded = allocated > 0 && remaining < 0

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("total_budget_card"),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Overall Monthly Budget",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (allocated > 0) "Total allocation for this month" else "No total budget configured",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    selectedBudgetForEdit = totalBudget
                                    initialCategoryForAdd = "TOTAL"
                                    showAddEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("edit_total_budget_btn")
                            ) {
                                Text(if (totalBudget != null) "Edit" else "Set Budget")
                            }
                        }

                        if (allocated > 0) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Spent",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = IndianCurrencyFormatter.formatRupees(monthlyTotalSpend),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isExceeded) "Exceeded By" else "Remaining",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = IndianCurrencyFormatter.formatRupees(if (isExceeded) -remaining else remaining),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExceeded) MaterialTheme.colorScheme.error else ForestGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Progress Bar
                            val progressColor = when {
                                percent >= 1f -> MaterialTheme.colorScheme.error
                                percent >= 0.75f -> WarningOrange
                                else -> ForestGreen
                            }
                            LinearProgressIndicator(
                                progress = { percent.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = progressColor,
                                trackColor = progressColor.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(percent * 100).toInt()}% utilized",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = progressColor
                                )
                                Text(
                                    text = "Target: ${IndianCurrencyFormatter.formatRupees(allocated)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Category Budgets Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budgets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${categoryBudgets.size} configured",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (categoryBudgets.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No category budgets yet",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Create specific limits for Food, Shopping, Transport, etc.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            FilledTonalButton(
                                onClick = {
                                    selectedBudgetForEdit = null
                                    initialCategoryForAdd = "Food"
                                    showAddEditDialog = true
                                }
                            ) {
                                Text("Add Category Budget")
                            }
                        }
                    }
                }
            } else {
                items(categoryBudgets, key = { it.id }) { item ->
                    val spent = categorySpends[item.category] ?: 0.0
                    val remaining = item.allocatedAmount - spent
                    val percent = if (item.allocatedAmount > 0) (spent / item.allocatedAmount).toFloat() else 0f
                    val isExceeded = remaining < 0

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                selectedBudgetForEdit = item
                                initialCategoryForAdd = item.category
                                showAddEditDialog = true
                            }
                            .testTag("budget_item_${item.id}"),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryIconBadge(category = item.category)
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${IndianCurrencyFormatter.formatRupees(spent, false)} spent of ${IndianCurrencyFormatter.formatRupees(item.allocatedAmount, false)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isExceeded) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else ForestGreen.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = if (isExceeded) "Exceeded" else "${(percent * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExceeded) MaterialTheme.colorScheme.error else ForestGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val barColor = when {
                                isExceeded -> MaterialTheme.colorScheme.error
                                percent >= 0.8f -> WarningOrange
                                else -> ForestGreen
                            }

                            LinearProgressIndicator(
                                progress = { percent.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = barColor.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isExceeded) "Over by ${IndianCurrencyFormatter.formatRupees(-remaining, false)}" else "${IndianCurrencyFormatter.formatRupees(remaining, false)} remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap to edit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Budget Dialog
    if (showAddEditDialog) {
        BudgetAddEditDialog(
            initialBudget = selectedBudgetForEdit,
            initialCategory = initialCategoryForAdd,
            onDismiss = {
                showAddEditDialog = false
                selectedBudgetForEdit = null
            },
            onSave = { category, amount ->
                onUpsertBudget(category, amount)
                showAddEditDialog = false
                selectedBudgetForEdit = null
            },
            onDelete = { budget ->
                budgetToDelete = budget
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation
    if (budgetToDelete != null) {
        ConfirmDialog(
            title = "Delete Budget?",
            message = "Are you sure you want to remove the budget for '${budgetToDelete!!.category}'? Spending will still be tracked.",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteBudget(budgetToDelete!!.id)
                budgetToDelete = null
            },
            onDismiss = { budgetToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetAddEditDialog(
    initialBudget: BudgetEntity?,
    initialCategory: String,
    onDismiss: () -> Unit,
    onSave: (category: String, amount: Double) -> Unit,
    onDelete: (BudgetEntity) -> Unit
) {
    var category by remember { mutableStateOf(initialBudget?.category ?: initialCategory) }
    var amountText by remember {
        mutableStateOf(if (initialBudget != null) String.format(java.util.Locale.US, "%.0f", initialBudget.allocatedAmount) else "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val allOptions = listOf("TOTAL") + Categories.expenseCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialBudget != null) "Edit Budget" else "Create Budget",
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
                // Category Selector
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (category == "TOTAL") "Overall Monthly Budget (Total)" else category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Budget Type / Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("budget_category_select")
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        allOptions.forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(if (opt == "TOTAL") "Overall Monthly Budget (Total)" else opt) },
                                onClick = {
                                    category = opt
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Allocated Amount (₹ INR)") },
                    placeholder = { Text("e.g. 25000") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_amount_input"),
                    isError = errorMessage != null
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
                onClick = {
                    val amountVal = amountText.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0.0) {
                        errorMessage = "Please enter a valid positive budget amount."
                        return@Button
                    }
                    onSave(category, amountVal)
                },
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text(if (initialBudget != null) "Update" else "Save")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (initialBudget != null && initialBudget.category != "TOTAL") {
                    TextButton(
                        onClick = { onDelete(initialBudget) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
