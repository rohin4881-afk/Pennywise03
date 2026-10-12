package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Categories
import com.example.data.PaymentMethod
import com.example.ui.components.getCategoryStyle
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.util.DateUtils
import com.example.util.IndianCurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Material 3 Compose screen providing a comprehensive form to record new expenses.
 * Contains dedicated fields for Amount, Category, Date, and Notes, along with Payment Method
 * and quick-entry assistance for optimal user experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onSaveExpense: (amount: Double, category: String, date: Long, paymentMethod: String, notes: String) -> Unit,
    modifier: Modifier = Modifier,
    initialAmount: Double? = null,
    initialCategory: String? = null,
    initialNotes: String? = null,
    initialDate: Long? = null,
    initialPaymentMethod: String? = null
) {
    // Intercept back button to return safely
    BackHandler {
        onNavigateBack()
    }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Form state variables
    var amountText by remember {
        mutableStateOf(if (initialAmount != null && initialAmount > 0) String.format(Locale.US, "%.2f", initialAmount) else "")
    }
    var selectedCategory by remember {
        mutableStateOf(initialCategory ?: Categories.expenseCategories.first())
    }
    var customCategoryName by remember { mutableStateOf("") }
    var isCustomCategoryMode by remember { mutableStateOf(false) }

    var selectedDateMillis by remember {
        mutableStateOf(initialDate ?: System.currentTimeMillis())
    }
    var selectedPaymentMethod by remember {
        mutableStateOf(initialPaymentMethod ?: PaymentMethod.UPI.displayName)
    }
    var notesText by remember {
        mutableStateOf(initialNotes ?: "")
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Quick note suggestion chips
    val quickNotes = listOf(
        "Groceries",
        "Lunch / Dinner",
        "Coffee & Snacks",
        "Cab / Fuel",
        "Mobile / Wifi",
        "Medicines",
        "Shopping",
        "Subscription"
    )

    // Quick amount additions (+₹100, +₹200, +₹500, +₹1,000, +₹2,000)
    val quickAmounts = listOf(100, 200, 500, 1000, 2000)

    val fullDateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US) }

    fun addQuickAmount(delta: Int) {
        val current = amountText.toDoubleOrNull() ?: 0.0
        val updated = current + delta
        amountText = if (updated % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", updated)
        } else {
            String.format(Locale.US, "%.2f", updated)
        }
        errorMessage = null
    }

    fun handleSave() {
        val parsedAmount = amountText.toDoubleOrNull()
        if (parsedAmount == null || parsedAmount <= 0.0) {
            errorMessage = "Please enter a valid expense amount greater than ₹0."
            return
        }
        if (parsedAmount > 100_000_000.0) {
            errorMessage = "Amount exceeds the maximum permissible limit."
            return
        }

        val finalCategory = if (isCustomCategoryMode && customCategoryName.isNotBlank()) {
            customCategoryName.trim()
        } else {
            selectedCategory
        }

        errorMessage = null
        onSaveExpense(
            parsedAmount,
            finalCategory,
            selectedDateMillis,
            selectedPaymentMethod,
            notesText.trim()
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("add_expense_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Add Expense",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Record outflow & track budget",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_expense_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            amountText = ""
                            selectedCategory = Categories.expenseCategories.first()
                            selectedDateMillis = System.currentTimeMillis()
                            selectedPaymentMethod = PaymentMethod.UPI.displayName
                            notesText = ""
                            customCategoryName = ""
                            isCustomCategoryMode = false
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("add_expense_reset_button")
                    ) {
                        Text("Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("cancel_expense_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { handleSave() },
                        modifier = Modifier
                            .weight(2f)
                            .height(52.dp)
                            .testTag("save_expense_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Expense",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Error Alert Banner
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 1. AMOUNT FIELD (Hero Section)
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_amount_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AMOUNT SPENT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "EXPENSE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Large Numeric Amount Input
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = {
                            amountText = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                text = "0.00",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        },
                        leadingIcon = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "₹",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            if (amountText.isNotEmpty()) {
                                IconButton(
                                    onClick = { amountText = "" },
                                    modifier = Modifier.testTag("clear_amount_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear amount",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input"),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorMessage != null
                    )

                    // Quick increment / preset chips
                    Column {
                        Text(
                            text = "Quick Add",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickAmounts.forEach { amt ->
                                SuggestionChip(
                                    onClick = { addQuickAmount(amt) },
                                    label = {
                                        Text("+₹$amt", fontWeight = FontWeight.SemiBold)
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("quick_amount_add_$amt")
                                )
                            }
                        }
                    }
                }
            }

            // 2. CATEGORY SELECTOR FIELD
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_category_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = if (isCustomCategoryMode) "Custom" else selectedCategory,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Grid-style category chips with icons and colors
                    val categories = Categories.expenseCategories
                    val chunkedCategories = remember { categories.chunked(2) }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunkedCategories.forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCategories.forEach { categoryName ->
                                    val isSelected = !isCustomCategoryMode && selectedCategory == categoryName
                                    val (icon, bgCol, tintCol) = getCategoryStyle(categoryName)

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedCategory = categoryName
                                                isCustomCategoryMode = false
                                            }
                                            .testTag("category_chip_${categoryName.lowercase()}"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        },
                                        border = if (isSelected) {
                                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                        } else {
                                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(bgCol),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = categoryName,
                                                    tint = tintCol,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Text(
                                                text = categoryName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Fill uneven odd slot if any
                                if (rowCategories.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Optional Custom Category toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                isCustomCategoryMode = !isCustomCategoryMode
                            },
                            modifier = Modifier.testTag("toggle_custom_category_button")
                        ) {
                            Icon(
                                imageVector = if (isCustomCategoryMode) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isCustomCategoryMode) "Use Standard Category" else "Enter Custom Category")
                        }
                    }

                    AnimatedVisibility(visible = isCustomCategoryMode) {
                        OutlinedTextField(
                            value = customCategoryName,
                            onValueChange = { customCategoryName = it },
                            placeholder = { Text("e.g. Pet Care, Software, Gym") },
                            label = { Text("Custom Category Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_category_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 3. DATE SELECTOR FIELD
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_date_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DATE OF EXPENSE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    // Selected Date Display & Tap to pick
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePickerDialog = true }
                            .testTag("expense_date_picker_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Calendar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fullDateFormatter.format(Date(selectedDateMillis)),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = DateUtils.formatShortDate(selectedDateMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Change Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Date Chips: Today, Yesterday
                    val cal = Calendar.getInstance()
                    val todayMillis = remember {
                        cal.timeInMillis
                    }
                    val yesterdayMillis = remember {
                        val yesterdayCal = Calendar.getInstance()
                        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
                        yesterdayCal.timeInMillis
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = DateUtils.formatIso(selectedDateMillis) == DateUtils.formatIso(todayMillis),
                            onClick = { selectedDateMillis = System.currentTimeMillis() },
                            label = { Text("Today") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("date_chip_today")
                        )

                        FilterChip(
                            selected = DateUtils.formatIso(selectedDateMillis) == DateUtils.formatIso(yesterdayMillis),
                            onClick = { selectedDateMillis = yesterdayMillis },
                            label = { Text("Yesterday") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("date_chip_yesterday")
                        )

                        FilterChip(
                            selected = showDatePickerDialog,
                            onClick = { showDatePickerDialog = true },
                            label = { Text("Pick Date...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            modifier = Modifier.testTag("date_chip_pick_custom")
                        )
                    }
                }
            }

            // 4. PAYMENT METHOD FIELD
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_payment_method_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PAYMENT METHOD",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethod.entries.forEach { method ->
                            val isSelected = selectedPaymentMethod == method.displayName
                            val icon = when (method) {
                                PaymentMethod.UPI -> Icons.Default.QrCodeScanner
                                PaymentMethod.CASH -> Icons.Default.Payments
                                PaymentMethod.CREDIT_CARD -> Icons.Default.CreditCard
                                PaymentMethod.DEBIT_CARD -> Icons.Default.AccountBalanceWallet
                                PaymentMethod.NET_BANKING -> Icons.Default.AccountBalance
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPaymentMethod = method.displayName },
                                label = { Text(method.displayName) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.testTag("payment_method_${method.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 5. NOTES & REMARKS FIELD
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_notes_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NOTES & REMARKS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "${notesText.length}/150",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = {
                            if (it.length <= 150) {
                                notesText = it
                            }
                        },
                        placeholder = {
                            Text("e.g. Swiggy team lunch, Auto to office, Blinkit grocery")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (notesText.isNotEmpty()) {
                                IconButton(onClick = { notesText = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear notes"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )

                    // Quick suggestion tags
                    Column {
                        Text(
                            text = "Suggestions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickNotes.forEach { suggestion ->
                                AssistChip(
                                    onClick = {
                                        notesText = if (notesText.isBlank()) {
                                            suggestion
                                        } else {
                                            "$notesText, $suggestion"
                                        }
                                    },
                                    label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("quick_note_chip_${suggestion.lowercase().replace(" ", "_").replace("/", "_")}")
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("confirm_date_picker_button")
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    modifier = Modifier.testTag("dismiss_date_picker_button")
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }
}
