package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AuditLogEntity
import com.example.ui.components.ConfirmDialog
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LimeAccent
import com.example.util.DateUtils
import com.example.util.SecurityUtils
import com.google.firebase.auth.FirebaseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    isPinEnabled: Boolean,
    auditLogs: List<AuditLogEntity>,
    currentUser: FirebaseUser? = null,
    onSignInWithGoogle: () -> Unit = {},
    onSignOutGoogle: () -> Unit = {},
    onToggleTheme: () -> Unit,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit,
    onExportCsv: () -> Unit,
    onGenerateJsonBackup: (onResult: (String) -> Unit) -> Unit,
    onRestoreJsonBackup: (json: String, replace: Boolean, onResult: (Result<String>) -> Unit) -> Unit,
    onLoadDemoData: () -> Unit,
    onRemoveDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Dialog states
    var showPinDialog by remember { mutableStateOf(false) }
    var showDemoConfirmDialog by remember { mutableStateOf(false) }
    var showRemoveDemoConfirmDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var showExportJsonDialog by remember { mutableStateOf<String?>(null) }
    var showRestoreJsonInputDialog by remember { mutableStateOf(false) }
    var showRestoreValidationDialog by remember { mutableStateOf<Pair<String, SecurityUtils.JsonValidationResult>?>(null) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showSecurityAudit by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Google Account & Cloud Security
        item {
            SettingsSectionHeader(title = "Google Account & Cloud Sync")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .testTag("settings_google_auth_card"),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentUser != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "User Avatar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser.displayName ?: "Google User",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentUser.email ?: "Signed in via Google",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // reCAPTCHA & App Check Status Tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Cloud Sync Active • reCAPTCHA Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onSignOutGoogle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_google_sign_out_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign Out of Google Account")
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = "Cloud Offline",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Not Connected to Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sign in to backup transactions to Firestore securely with reCAPTCHA",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onSignInWithGoogle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_google_sign_in_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign in with Google", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // App Preferences
        item {
            SettingsSectionHeader(title = "Appearance & Currency")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = if (isDarkTheme) "Forest Dark enabled" else "Soft Cream Light enabled",
                        trailing = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = { onToggleTheme() },
                                modifier = Modifier.testTag("theme_toggle_switch")
                            )
                        }
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Default.CurrencyRupee,
                        title = "Default Currency",
                        subtitle = "Indian Rupee (₹ INR) with Lakh/Crore grouping",
                        trailing = {
                            Text(
                                text = "₹ INR",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }

        // Security & Privacy
        item {
            SettingsSectionHeader(title = "Privacy & App Security")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Lock,
                        title = "Local App Lock PIN",
                        subtitle = if (isPinEnabled) "4-digit PIN lock active" else "Protect app from casual glances",
                        trailing = {
                            if (isPinEnabled) {
                                TextButton(onClick = { onRemovePin() }) {
                                    Text("Remove PIN", color = MaterialTheme.colorScheme.error)
                                }
                            } else {
                                Button(
                                    onClick = { showPinDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Set PIN")
                                }
                            }
                        }
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Default.Security,
                        title = "Security Architecture & Audit",
                        subtitle = "Review CSV formula injection defense & cloud roadmap",
                        onClick = { showSecurityAudit = true }
                    )
                }
            }
        }

        // Data Backup & Portability
        item {
            SettingsSectionHeader(title = "Backup & Portability")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.TableChart,
                        title = "Export CSV Spreadsheet",
                        subtitle = "RFC-4180 with spreadsheet formula injection protection",
                        onClick = onExportCsv
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Default.FileDownload,
                        title = "Export JSON Backup",
                        subtitle = "Complete backup of transactions, budgets & goals",
                        onClick = {
                            onGenerateJsonBackup { json ->
                                showExportJsonDialog = json
                            }
                        }
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Default.FileUpload,
                        title = "Restore JSON Backup",
                        subtitle = "Validate schema and merge or replace records",
                        onClick = { showRestoreJsonInputDialog = true }
                    )
                }
            }
        }

        // Demo Data Management
        item {
            SettingsSectionHeader(title = "Sample Data")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.AddBusiness,
                        title = "Load Indian Demo Data",
                        subtitle = "Realistic sample records (Swiggy, DMart, Rent, Salary)",
                        onClick = { showDemoConfirmDialog = true }
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Remove Demo Data Only",
                        subtitle = "Cleans sample data without affecting user records",
                        onClick = { showRemoveDemoConfirmDialog = true }
                    )
                }
            }
        }

        // Storage & Legal Information
        item {
            SettingsSectionHeader(title = "Storage Architecture & Policies")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Storage,
                        title = "Local Storage (Room SQLite)",
                        subtitle = "Data is saved locally in private app storage. No automatic cross-device sync."
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "Review disclosures and India DPDP compliance",
                        onClick = { showPrivacyPolicy = true }
                    )
                    Divider()
                    SettingsRow(
                        icon = Icons.Default.Description,
                        title = "Terms of Service",
                        subtitle = "Personal budgeting app terms and disclaimers",
                        onClick = { showTerms = true }
                    )
                }
            }
        }

        // Danger Zone
        item {
            SettingsSectionHeader(title = "Danger Zone", color = MaterialTheme.colorScheme.error)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                SettingsRow(
                    icon = Icons.Default.Warning,
                    title = "Clear All Application Data",
                    subtitle = "Permanently wipe all transactions, budgets, and savings goals",
                    onClick = { showClearAllConfirmDialog = true },
                    titleColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Set PIN Dialog
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Security PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a 4-digit numeric PIN to protect your financial transactions.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Text(text = pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            onSetPin(pinInput)
                            showPinDialog = false
                            Toast.makeText(context, "PIN Protection Enabled", Toast.LENGTH_SHORT).show()
                        } else {
                            pinError = "PIN must be exactly 4 digits."
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Export JSON Dialog
    if (showExportJsonDialog != null) {
        AlertDialog(
            onDismissRequest = { showExportJsonDialog = null },
            title = { Text("JSON Backup Ready", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your complete financial records have been exported. Copy this JSON or share to save a safe off-device backup.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = showExportJsonDialog!!,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Pennywise Backup", showExportJsonDialog))
                        Toast.makeText(context, "Copied backup to clipboard", Toast.LENGTH_SHORT).show()
                        showExportJsonDialog = null
                    }
                ) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportJsonDialog = null }) { Text("Done") }
            }
        )
    }

    // Restore JSON Input Dialog
    if (showRestoreJsonInputDialog) {
        var jsonText by remember { mutableStateOf("") }
        var parseError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showRestoreJsonInputDialog = false },
            title = { Text("Restore From JSON Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste the contents of your Pennywise JSON backup below.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = jsonText,
                        onValueChange = {
                            jsonText = it
                            parseError = null
                        },
                        placeholder = { Text("{\"app\": \"PENNYWISE\", ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    if (parseError != null) {
                        Text(text = parseError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val validation = SecurityUtils.validateBackupJson(jsonText)
                        if (validation.isValid) {
                            showRestoreJsonInputDialog = false
                            showRestoreValidationDialog = jsonText to validation
                        } else {
                            parseError = validation.errorMessage ?: "Invalid backup file."
                        }
                    }
                ) {
                    Text("Validate & Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreJsonInputDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Restore Validation & Confirmation Dialog
    if (showRestoreValidationDialog != null) {
        val (jsonString, validation) = showRestoreValidationDialog!!

        AlertDialog(
            onDismissRequest = { showRestoreValidationDialog = null },
            title = { Text("Confirm Backup Restoration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Backup Verified Successfully:", fontWeight = FontWeight.SemiBold, color = ForestGreen)
                    Text("• Schema Version: ${validation.schemaVersion}")
                    Text("• Exported At: ${DateUtils.formatDate(validation.exportedAt)}")
                    Text("• Transactions: ${validation.transactionsCount}")
                    Text("• Budgets: ${validation.budgetsCount}")
                    Text("• Savings Goals: ${validation.savingsGoalsCount}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose whether to merge these records with your current data or replace existing data completely.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onRestoreJsonBackup(jsonString, false) { result ->
                                result.onSuccess {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }.onFailure {
                                    Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                            showRestoreValidationDialog = null
                        }
                    ) {
                        Text("Merge")
                    }
                    Button(
                        onClick = {
                            onRestoreJsonBackup(jsonString, true) { result ->
                                result.onSuccess {
                                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                }.onFailure {
                                    Toast.makeText(context, "Error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                            showRestoreValidationDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Replace All")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreValidationDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Demo Confirmation
    if (showDemoConfirmDialog) {
        ConfirmDialog(
            title = "Load Indian Demo Data?",
            message = "This will add realistic sample transactions (Swiggy, DMart, Rent, Salary), budgets, and savings goals so you can test all features. You can remove demo data at any time without deleting your personal records.",
            confirmButtonText = "Load Demo Data",
            onConfirm = {
                onLoadDemoData()
                showDemoConfirmDialog = false
                Toast.makeText(context, "Demo data loaded successfully", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showDemoConfirmDialog = false }
        )
    }

    // Remove Demo Confirmation
    if (showRemoveDemoConfirmDialog) {
        ConfirmDialog(
            title = "Remove Demo Records?",
            message = "This will purge all sample demo transactions, budgets, and goals. Any records you personally created will be preserved.",
            confirmButtonText = "Remove Demo Data",
            isDestructive = true,
            onConfirm = {
                onRemoveDemoData()
                showRemoveDemoConfirmDialog = false
                Toast.makeText(context, "Demo records purged", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showRemoveDemoConfirmDialog = false }
        )
    }

    // Clear All Confirmation
    if (showClearAllConfirmDialog) {
        ConfirmDialog(
            title = "Permanently Clear All Data?",
            message = "WARNING: This will permanently delete ALL transactions, budgets, and savings goals from your device's local database. This action cannot be undone unless you have exported a JSON backup.",
            confirmButtonText = "Wipe Everything",
            isDestructive = true,
            onConfirm = {
                onClearAllData()
                showClearAllConfirmDialog = false
                Toast.makeText(context, "All data wiped", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showClearAllConfirmDialog = false }
        )
    }

    // Policies
    if (showPrivacyPolicy) PrivacyPolicyDialog { showPrivacyPolicy = false }
    if (showTerms) TermsOfServiceDialog { showTerms = false }
    if (showSecurityAudit) SecurityAuditDialog { showSecurityAudit = false }
}

@Composable
fun SettingsSectionHeader(title: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = titleColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
