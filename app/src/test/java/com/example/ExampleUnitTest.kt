package com.example

import com.example.util.DateUtils
import com.example.util.IndianCurrencyFormatter
import com.example.util.SecurityUtils
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testIndianCurrencyFormatting() {
        // Standard Lakh formatting
        val formattedLakh = IndianCurrencyFormatter.formatRupees(125000.0)
        assertEquals("₹1,25,000.00", formattedLakh)

        // Decimals handling
        val formattedDecimals = IndianCurrencyFormatter.formatRupees(5400.5)
        assertEquals("₹5,400.50", formattedDecimals)

        // Crore formatting
        val formattedCrore = IndianCurrencyFormatter.formatRupees(10000000.0)
        assertEquals("₹1,00,00,000.00", formattedCrore)

        // Compact formatting
        val compactLakh = IndianCurrencyFormatter.formatCompact(125000.0)
        assertTrue(compactLakh.contains("1.25 L"))
    }

    @Test
    fun testCsvFormulaInjectionSanitization() {
        // Equals formula trigger
        val sanitizedFormula = SecurityUtils.sanitizeForCsv("=SUM(A1:A10)")
        assertEquals("\"'=SUM(A1:A10)\"", sanitizedFormula)

        // Plus formula trigger
        val sanitizedPlus = SecurityUtils.sanitizeForCsv("+12345")
        assertEquals("\"'+12345\"", sanitizedPlus)

        // At symbol trigger
        val sanitizedAt = SecurityUtils.sanitizeForCsv("@someCommand")
        assertEquals("\"'@someCommand\"", sanitizedAt)

        // Standard string with internal quotes
        val regularText = SecurityUtils.sanitizeForCsv("Dinner with \"Truffles\"")
        assertEquals("\"Dinner with \"\"Truffles\"\"\"", regularText)
    }

    @Test
    fun testJsonBackupValidation() {
        val emptyResult = SecurityUtils.validateBackupJson("")
        assertFalse(emptyResult.isValid)

        val invalidAppResult = SecurityUtils.validateBackupJson("{\"app\": \"OTHER\"}")
        assertFalse(invalidAppResult.isValid)

        val validJson = """
            {
              "app": "PENNYWISE",
              "schemaVersion": 1,
              "exportedAt": 1728000000000,
              "transactions": [
                {
                  "amount": 450.0,
                  "type": "EXPENSE",
                  "category": "Food",
                  "date": 1728000000000,
                  "paymentMethod": "UPI",
                  "notes": "Lunch"
                }
              ],
              "budgets": [
                {
                  "monthYear": "2026-10",
                  "category": "Food",
                  "allocatedAmount": 10000.0
                }
              ],
              "savingsGoals": [
                {
                  "name": "Trip",
                  "targetAmount": 20000.0,
                  "currentAmount": 5000.0
                }
              ]
            }
        """.trimIndent()

        val validResult = SecurityUtils.validateBackupJson(validJson)
        assertTrue(validResult.errorMessage ?: "Expected valid", validResult.isValid)
        assertEquals(1, validResult.transactionsCount)
        assertEquals(1, validResult.budgetsCount)
        assertEquals(1, validResult.savingsGoalsCount)
    }

    @Test
    fun testDateUtilsRemainingDays() {
        val remainingDays = DateUtils.getRemainingDaysInMonth(2026, 9) // October 2026
        assertTrue(remainingDays in 1..31)
    }
}
