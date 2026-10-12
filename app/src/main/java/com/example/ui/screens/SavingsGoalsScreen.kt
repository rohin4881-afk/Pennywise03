package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.SavingsGoalEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.util.DateUtils
import com.example.util.IndianCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalsScreen(
    goals: List<SavingsGoalEntity>,
    onAddGoal: (name: String, target: Double, initial: Double, targetDate: Long?, category: String) -> Unit,
    onUpdateGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onContribute: (SavingsGoalEntity, Double) -> Unit,
    onWithdraw: (SavingsGoalEntity, Double, (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    // Contribute / Withdraw state
    var goalForContribution by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var goalForWithdrawal by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    val totalSaved = goals.sumOf { it.currentAmount }
    val totalTarget = goals.sumOf { it.targetAmount }
    val overallPercent = if (totalTarget > 0) (totalSaved / totalTarget).toFloat() else 0f

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("savings_goals_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingGoal = null
                    showCreateDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_goal")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
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
            // Summary Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("savings_summary_card"),
                    color = ForestGreen,
                    contentColor = Color.White
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SAVINGS RESERVE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LimeAccent
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = LimeAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${goals.size} Active Goals",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LimeAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = IndianCurrencyFormatter.formatRupees(totalSaved),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Target across all goals: ${IndianCurrencyFormatter.formatRupees(totalTarget, false)} (${(overallPercent * 100).toInt()}% reached)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { overallPercent.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = LimeAccent,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Disclaimer Banner
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Savings contributions are stored in dedicated goals and do not automatically count as spending transactions unless you explicitly log them as expenses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // List of Goals
            if (goals.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No savings goals yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start an Emergency Fund, Diwali fund, or tech gadget target.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Create Goal")
                            }
                        }
                    }
                }
            } else {
                items(goals, key = { it.id }) { goal ->
                    SavingsGoalCard(
                        goal = goal,
                        onContribute = { goalForContribution = goal },
                        onWithdraw = { goalForWithdrawal = goal },
                        onEdit = {
                            editingGoal = goal
                            showCreateDialog = true
                        },
                        onDelete = { goalToDelete = goal }
                    )
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showCreateDialog) {
        SavingsGoalAddEditDialog(
            initialGoal = editingGoal,
            onDismiss = {
                showCreateDialog = false
                editingGoal = null
            },
            onSave = { name, target, initial, targetDate, category ->
                if (editingGoal != null) {
                    onUpdateGoal(
                        editingGoal!!.copy(
                            name = name,
                            targetAmount = target,
                            currentAmount = initial,
                            targetDate = targetDate,
                            category = category
                        )
                    )
                } else {
                    onAddGoal(name, target, initial, targetDate, category)
                }
                showCreateDialog = false
                editingGoal = null
            }
        )
    }

    // Contribution Dialog
    if (goalForContribution != null) {
        ContributeGoalDialog(
            goal = goalForContribution!!,
            onDismiss = { goalForContribution = null },
            onConfirm = { amount ->
                onContribute(goalForContribution!!, amount)
                goalForContribution = null
            }
        )
    }

    // Withdrawal Dialog
    if (goalForWithdrawal != null) {
        WithdrawGoalDialog(
            goal = goalForWithdrawal!!,
            onDismiss = { goalForWithdrawal = null },
            onConfirm = { amount ->
                onWithdraw(goalForWithdrawal!!, amount) { success ->
                    if (success) {
                        goalForWithdrawal = null
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (goalToDelete != null) {
        ConfirmDialog(
            title = "Delete Savings Goal?",
            message = "Are you sure you want to delete '${goalToDelete!!.name}'? This will remove the tracked goal record.",
            confirmButtonText = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteGoal(goalToDelete!!.id)
                goalToDelete = null
            },
            onDismiss = { goalToDelete = null }
        )
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    onContribute: () -> Unit,
    onWithdraw: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val isCompleted = goal.currentAmount >= goal.targetAmount

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .testTag("savings_goal_card_${goal.id}"),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Savings,
                            contentDescription = null,
                            tint = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = goal.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = IndianCurrencyFormatter.formatRupees(goal.currentAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = IndianCurrencyFormatter.formatRupees(goal.targetAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isCompleted) Color(0xFF2E7D32) else ForestGreen,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCompleted) "Target Achieved! 🎉" else "${IndianCurrencyFormatter.formatRupees(remaining, false)} remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                if (goal.targetDate != null) {
                    Text(
                        text = "Target: ${DateUtils.formatDate(goal.targetDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onContribute,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ForestGreen.copy(alpha = 0.12f),
                        contentColor = ForestGreen
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Funds")
                }

                OutlinedButton(
                    onClick = onWithdraw,
                    modifier = Modifier.weight(1f),
                    enabled = goal.currentAmount > 0
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Withdraw")
                }
            }
        }
    }
}

@Composable
fun SavingsGoalAddEditDialog(
    initialGoal: SavingsGoalEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, target: Double, initial: Double, targetDate: Long?, category: String) -> Unit
) {
    var name by remember { mutableStateOf(initialGoal?.name ?: "") }
    var targetText by remember {
        mutableStateOf(if (initialGoal != null) String.format(java.util.Locale.US, "%.0f", initialGoal.targetAmount) else "")
    }
    var currentText by remember {
        mutableStateOf(if (initialGoal != null) String.format(java.util.Locale.US, "%.0f", initialGoal.currentAmount) else "0")
    }
    var category by remember { mutableStateOf(initialGoal?.category ?: "Emergency Fund") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Emergency Fund", "Celebration & Gifts", "Electronics & Tech", "Travel & Vacation", "Home Upgrade", "Investment")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal != null) "Edit Savings Goal" else "New Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g. New Laptop, Goa Trip") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentText,
                    onValueChange = { currentText = it },
                    label = { Text("Current Saved Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category / Purpose") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                    if (name.isBlank()) {
                        errorMessage = "Please enter a goal name."
                        return@Button
                    }
                    val targetVal = targetText.toDoubleOrNull()
                    if (targetVal == null || targetVal <= 0.0) {
                        errorMessage = "Target amount must be a positive number."
                        return@Button
                    }
                    val currentVal = currentText.toDoubleOrNull() ?: 0.0
                    if (currentVal < 0) {
                        errorMessage = "Saved amount cannot be negative."
                        return@Button
                    }
                    onSave(name.trim(), targetVal, currentVal, null, category.trim())
                }
            ) {
                Text(if (initialGoal != null) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ContributeGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Funds to '${goal.name}'", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Current saved: ${IndianCurrencyFormatter.formatRupees(goal.currentAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = null
                    },
                    label = { Text("Contribution Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        error = "Enter a valid positive amount."
                        return@Button
                    }
                    onConfirm(amount)
                }
            ) {
                Text("Add Funds")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun WithdrawGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Withdraw Funds from '${goal.name}'", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Available to withdraw: ${IndianCurrencyFormatter.formatRupees(goal.currentAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        error = null
                    },
                    label = { Text("Withdrawal Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        error = "Enter a valid positive amount."
                        return@Button
                    }
                    if (amount > goal.currentAmount) {
                        error = "Cannot withdraw more than current balance (${IndianCurrencyFormatter.formatRupees(goal.currentAmount)})."
                        return@Button
                    }
                    onConfirm(amount)
                }
            ) {
                Text("Withdraw")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
