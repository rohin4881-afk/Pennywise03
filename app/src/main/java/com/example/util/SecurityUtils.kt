package com.example.util

import org.json.JSONObject
import org.json.JSONArray

object SecurityUtils {

    /**
     * Defense against Spreadsheet Formula Injection (CSV injection).
     * If a cell begins with '=', '+', '-', '@', '\t', '\r', Excel or Google Sheets
     * may execute it as a formula. We prepend a single quote (') to force text interpretation.
     * We also properly escape quotes and wrap in quotes if commas or quotes are present.
     */
    fun sanitizeForCsv(input: String): String {
        var text = input.trim()
        if (text.isEmpty()) return "\"\""

        val formulaTriggers = listOf('=', '+', '-', '@', '\t', '\r')
        if (formulaTriggers.any { text.startsWith(it) }) {
            text = "'$text"
        }

        // Escape internal double quotes with double double-quotes
        val escaped = text.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    data class JsonValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val schemaVersion: Int = 1,
        val exportedAt: Long = 0L,
        val transactionsCount: Int = 0,
        val budgetsCount: Int = 0,
        val savingsGoalsCount: Int = 0
    )

    /**
     * Validates JSON backup payload for schema integrity, valid fields,
     * positive numeric amounts, and reasonable bounds before any data changes.
     */
    fun validateBackupJson(jsonStr: String): JsonValidationResult {
        if (jsonStr.isBlank()) {
            return JsonValidationResult(isValid = false, errorMessage = "Backup file is empty.")
        }
        if (jsonStr.length > 20 * 1024 * 1024) { // 20 MB size limit
            return JsonValidationResult(isValid = false, errorMessage = "File exceeds maximum size limit (20MB).")
        }

        return try {
            val root = JSONObject(jsonStr)
            val app = root.optString("app", "")
            if (app != "PENNYWISE") {
                return JsonValidationResult(
                    isValid = false,
                    errorMessage = "Invalid backup format: missing PENNYWISE signature."
                )
            }

            val schemaVersion = root.optInt("schemaVersion", 1)
            val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())

            val transactionsArray = root.optJSONArray("transactions") ?: JSONArray()
            val budgetsArray = root.optJSONArray("budgets") ?: JSONArray()
            val goalsArray = root.optJSONArray("savingsGoals") ?: JSONArray()

            // Validate transactions
            for (i in 0 until transactionsArray.length()) {
                val tObj = transactionsArray.getJSONObject(i)
                val amount = tObj.optDouble("amount", -1.0)
                if (amount <= 0 || amount.isNaN() || amount.isInfinite()) {
                    return JsonValidationResult(
                        isValid = false,
                        errorMessage = "Transaction #$i contains an invalid amount: $amount"
                    )
                }
                val type = tObj.optString("type")
                if (type != "EXPENSE" && type != "INCOME") {
                    return JsonValidationResult(
                        isValid = false,
                        errorMessage = "Transaction #$i has unknown type: $type"
                    )
                }
            }

            // Validate budgets
            for (i in 0 until budgetsArray.length()) {
                val bObj = budgetsArray.getJSONObject(i)
                val allocated = bObj.optDouble("allocatedAmount", -1.0)
                if (allocated <= 0 || allocated.isNaN()) {
                    return JsonValidationResult(
                        isValid = false,
                        errorMessage = "Budget #$i contains an invalid allocated amount: $allocated"
                    )
                }
            }

            // Validate savings goals
            for (i in 0 until goalsArray.length()) {
                val gObj = goalsArray.getJSONObject(i)
                val target = gObj.optDouble("targetAmount", -1.0)
                val current = gObj.optDouble("currentAmount", -1.0)
                if (target <= 0 || current < 0) {
                    return JsonValidationResult(
                        isValid = false,
                        errorMessage = "Savings Goal #$i contains invalid target or current amounts."
                    )
                }
            }

            JsonValidationResult(
                isValid = true,
                schemaVersion = schemaVersion,
                exportedAt = exportedAt,
                transactionsCount = transactionsArray.length(),
                budgetsCount = budgetsArray.length(),
                savingsGoalsCount = goalsArray.length()
            )
        } catch (e: Exception) {
            JsonValidationResult(
                isValid = false,
                errorMessage = "Malformed JSON syntax: ${e.localizedMessage ?: "Parse error"}"
            )
        }
    }
}
