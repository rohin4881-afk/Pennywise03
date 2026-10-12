package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pennywise Financial Privacy Disclosures",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1. Local-First Storage:\nAll transaction records, monthly budgets, savings goals, and preferences are stored exclusively on your device inside an offline SQLite Room Database. Pennywise does not operate unauthenticated third-party tracker servers.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "2. No Financial Account Access:\nPennywise never asks for or stores bank credentials, net banking passwords, debit/credit card CVVs, or OTPs. All data entries are self-recorded by you.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "3. Exports & Backups:\nWhen you generate a CSV or JSON backup, the file remains solely within your device's private storage or standard system sharing sheet. You have full ownership of your backup files.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "4. Data Deletion Rights:\nYou have absolute control over your financial data. You can delete individual transactions, purge sample demo data, or execute a permanent wipe of all local database tables at any time via Settings.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "5. India Digital Personal Data Protection (DPDP):\nIn compliance with ethical privacy principles, no unnecessary telemetries or behavioral tracking pixels are integrated.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun TermsOfServiceDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pennywise Personal Finance App Terms",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1. Personal Use Only:\nPennywise is designed as an informational expense tracking and personal budgeting utility. It does not provide certified financial, tax, or investment advice.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "2. Calculation Estimates:\nThe 'Daily Safe to Spend' and savings metrics are mathematical estimates derived from your entered budget targets and calendar days. Actual spending decisions remain solely the responsibility of the user.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "3. Device Responsibility:\nBecause data is stored locally on the device, clearing application cache/storage or uninstalling the app without an exported JSON backup will result in permanent data loss.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Understood") }
        }
    )
}

@Composable
fun SecurityAuditDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Security Architecture & Cloud Roadmap", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Current Implemented Protections:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• CSV Formula Injection Defense: Prepends single quotes (') to any text field beginning with '=', '+', '-', '@', '\\t', or '\\r' to prevent arbitrary spreadsheet script execution when opening exports in Microsoft Excel or Google Sheets.\n" +
                            "• JSON Backup Validation: Strict schema verification, file size caps (20MB), positive amount assertion, and non-silent merge/replace confirmations.\n" +
                            "• Zero Network Leaks: Operates completely offline without background ad-trackers.\n" +
                            "• Local PIN Protection: Built-in 4-digit privacy PIN gate for casual screen peeking.",
                    style = MaterialTheme.typography.bodySmall
                )
                Divider()
                Text(
                    text = "Cloud Account Migration Roadmap (If Enabled):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "If cross-device cloud synchronization is provisioned in the future, the following enterprise security controls are required:\n" +
                            "1. Google Identity Services / Jetpack Credential Manager OAuth 2.0 with cryptographic nonce verification.\n" +
                            "2. Google reCAPTCHA Enterprise server-side token validation on auth & recovery actions.\n" +
                            "3. Database Row-Level Security (RLS) guaranteeing strict per-user UID isolation.\n" +
                            "4. End-to-end transport encryption (TLS 1.3) and AES-256 database encryption at rest.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}
