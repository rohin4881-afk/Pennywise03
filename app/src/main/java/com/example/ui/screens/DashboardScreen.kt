package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.TransactionEntity
import com.example.ui.AppTab
import com.example.ui.DashboardMetrics
import com.example.ui.components.*
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.util.IndianCurrencyFormatter

@Composable
fun DashboardScreen(
    year: Int,
    month: Int,
    metrics: DashboardMetrics,
    recentTransactions: List<TransactionEntity>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onResetMonth: () -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    onOpenAddTransaction: () -> Unit,
    onTransactionClick: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
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

        // Daily Safe To Spend Hero Card
        item {
            DailySafeToSpendCard(
                metrics = metrics,
                onSetBudgetClick = { onNavigateTab(AppTab.BUDGETS) }
            )
        }

        // Metric Stat Grid (Today's spend, Monthly spend, Monthly Income, Remaining budget)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Today's Spend",
                    amount = metrics.todaySpend,
                    icon = Icons.Default.Today,
                    accentColor = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f),
                    testTag = "today_spend_card"
                )
                MetricStatCard(
                    title = "Monthly Spend",
                    amount = metrics.monthlySpend,
                    icon = Icons.Default.TrendingDown,
                    accentColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    testTag = "monthly_spend_card"
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Monthly Income",
                    amount = metrics.monthlyIncome,
                    icon = Icons.Default.TrendingUp,
                    accentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    testTag = "monthly_income_card"
                )
                val budgetAccent = if (metrics.isBudgetExceeded) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
                MetricStatCard(
                    title = "Remaining Budget",
                    amount = metrics.remainingBudget,
                    subtitle = if (metrics.hasBudget) {
                        "Allocated: ${IndianCurrencyFormatter.formatCompact(metrics.totalBudgetAllocated)}"
                    } else {
                        "No budget set"
                    },
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = budgetAccent,
                    modifier = Modifier.weight(1f),
                    testTag = "remaining_budget_card"
                )
            }
        }

        // Quick Actions Row
        item {
            QuickActionsBar(
                onAddTransaction = onOpenAddTransaction,
                onGoToBudgets = { onNavigateTab(AppTab.BUDGETS) },
                onGoToSavings = { onNavigateTab(AppTab.SAVINGS) },
                onGoToReports = { onNavigateTab(AppTab.REPORTS) }
            )
        }

        // Visual Banner Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pennywise_hero),
                    contentDescription = "Pennywise Financial Planning",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Spending Breakdown Card
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
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { onNavigateTab(AppTab.REPORTS) }) {
                            Text("Full Report")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryDonutChart(categorySpends = metrics.categorySpends)
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { onNavigateTab(AppTab.TRANSACTIONS) },
                    modifier = Modifier.testTag("view_all_transactions_btn")
                ) {
                    Text("View All")
                }
            }
        }

        if (recentTransactions.isEmpty()) {
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add your first expense or load demo data from Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentTransactions.take(5)) { tx ->
                TransactionItemRow(
                    transaction = tx,
                    onClick = { onTransactionClick(tx) }
                )
            }
        }
    }
}

@Composable
fun DailySafeToSpendCard(
    metrics: DashboardMetrics,
    onSetBudgetClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("daily_safe_to_spend_card"),
        color = ForestGreen,
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = LimeAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "DAILY SAFE TO SPEND",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = LimeAccent,
                        letterSpacing = 1.sp
                    )
                }

                if (!metrics.hasBudget) {
                    TextButton(
                        onClick = onSetBudgetClick,
                        colors = ButtonDefaults.textButtonColors(contentColor = LimeAccent)
                    ) {
                        Text("Set Budget", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (metrics.hasBudget && !metrics.isBudgetExceeded) {
                Text(
                    text = IndianCurrencyFormatter.formatRupees(metrics.dailySafeToSpend, includeDecimals = true),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            } else if (metrics.isBudgetExceeded) {
                Text(
                    text = "₹0.00",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF8A80)
                )
            } else {
                Text(
                    text = "--",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = metrics.safeToSpendMessage,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun QuickActionsBar(
    onAddTransaction: () -> Unit,
    onGoToBudgets: () -> Unit,
    onGoToSavings: () -> Unit,
    onGoToReports: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.AddCircle,
                label = "Add Entry",
                tint = MaterialTheme.colorScheme.primary,
                onClick = onAddTransaction,
                tag = "quick_add_entry"
            )
            QuickActionButton(
                icon = Icons.Default.PieChart,
                label = "Budgets",
                tint = Color(0xFF00796B),
                onClick = onGoToBudgets,
                tag = "quick_budgets"
            )
            QuickActionButton(
                icon = Icons.Default.Savings,
                label = "Goals",
                tint = Color(0xFFC2185B),
                onClick = onGoToSavings,
                tag = "quick_goals"
            )
            QuickActionButton(
                icon = Icons.Default.BarChart,
                label = "Reports",
                tint = Color(0xFF512DA8),
                onClick = onGoToReports,
                tag = "quick_reports"
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
