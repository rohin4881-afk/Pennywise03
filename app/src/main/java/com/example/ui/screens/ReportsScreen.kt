package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BudgetEntity
import com.example.data.TransactionEntity
import com.example.data.TransactionType
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.ui.theme.WarningOrange
import com.example.util.DateUtils
import com.example.util.IndianCurrencyFormatter

@Composable
fun ReportsScreen(
    year: Int,
    month: Int,
    transactions: List<TransactionEntity>,
    budgets: List<BudgetEntity>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
    onExportCsv: (List<TransactionEntity>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Current month window
    val startOfMonth = DateUtils.getStartOfMonth(year, month)
    val endOfMonth = DateUtils.getEndOfMonth(year, month)

    // Previous month window for comparison
    val prevMonthCal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, year)
        set(java.util.Calendar.MONTH, month)
        add(java.util.Calendar.MONTH, -1)
    }
    val startOfPrevMonth = DateUtils.getStartOfMonth(prevMonthCal.get(java.util.Calendar.YEAR), prevMonthCal.get(java.util.Calendar.MONTH))
    val endOfPrevMonth = DateUtils.getEndOfMonth(prevMonthCal.get(java.util.Calendar.YEAR), prevMonthCal.get(java.util.Calendar.MONTH))

    val currentMonthTxs = transactions.filter { it.date in startOfMonth..endOfMonth }
    val prevMonthTxs = transactions.filter { it.date in startOfPrevMonth..endOfPrevMonth }

    val currentExpense = currentMonthTxs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
    val currentIncome = currentMonthTxs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
    val netSavings = currentIncome - currentExpense
    val savingsRate = if (currentIncome > 0) ((netSavings / currentIncome) * 100).toInt() else 0

    val prevExpense = prevMonthTxs.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
    val prevIncome = prevMonthTxs.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }

    // Category spends
    val categorySpends = currentMonthTxs.filter { it.type == TransactionType.EXPENSE.name }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    // Payment method breakdown
    val methodSpends = currentMonthTxs.filter { it.type == TransactionType.EXPENSE.name }
        .groupBy { it.paymentMethod }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    // Total budget
    val totalBudget = budgets.firstOrNull { it.category == "TOTAL" }?.allocatedAmount ?: 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector
        item {
            MonthSelectorHeader(
                year = year,
                month = month,
                onPrevious = onPreviousMonth,
                onNext = onNextMonth,
                onReset = onResetMonth
            )
        }

        // Net Cashflow Summary
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("cashflow_summary_card"),
                color = ForestGreen,
                contentColor = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "MONTHLY NET CASHFLOW",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LimeAccent
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = IndianCurrencyFormatter.formatRupees(netSavings),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (netSavings >= 0) LimeAccent else Color(0xFFFF8A80)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (currentIncome > 0) "Savings Rate: $savingsRate% of total monthly income" else "Income has not yet been recorded for this month.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Inflow", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Text(IndianCurrencyFormatter.formatRupees(currentIncome), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Outflow", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Text(IndianCurrencyFormatter.formatRupees(currentExpense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Month-over-Month Comparison
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comparison with Previous Month",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (prevMonthTxs.isEmpty()) {
                        Text(
                            text = "No stored transactions found for the previous month (${DateUtils.formatMonthYear(prevMonthCal.get(java.util.Calendar.YEAR), prevMonthCal.get(java.util.Calendar.MONTH))}). Comparison trends will populate as you track multiple months.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val spendDiff = currentExpense - prevExpense
                        val spendPercentChange = if (prevExpense > 0) ((spendDiff / prevExpense) * 100) else 0.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Previous Month Spend", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(IndianCurrencyFormatter.formatRupees(prevExpense), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Difference", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val diffColor = if (spendDiff <= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                val diffPrefix = if (spendDiff > 0) "+" else ""
                                Text(
                                    text = "$diffPrefix${String.format(java.util.Locale.US, "%.1f", spendPercentChange)}% (${IndianCurrencyFormatter.formatRupees(spendDiff)})",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = diffColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Breakdown Chart Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    CategoryDonutChart(categorySpends = categorySpends)
                }
            }
        }

        // Payment Method Breakdown Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spending by Payment Method",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (methodSpends.isEmpty()) {
                        Text(
                            text = "No expenses recorded this month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val total = currentExpense.coerceAtLeast(0.01)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            methodSpends.entries.sortedByDescending { it.value }.forEach { entry ->
                                val pct = (entry.value / total * 100).toInt()
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(entry.key, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        Text("${IndianCurrencyFormatter.formatRupees(entry.value, false)} ($pct%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (entry.value / total).toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Export Actions
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Export",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Export transactions with CSV formula sanitization and RFC-4180 compatibility.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onExportCsv(currentMonthTxs) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_month_csv_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Month CSV")
                        }

                        OutlinedButton(
                            onClick = { onExportCsv(transactions) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_all_csv_btn")
                        ) {
                            Text("Export All CSV")
                        }
                    }
                }
            }
        }
    }
}
