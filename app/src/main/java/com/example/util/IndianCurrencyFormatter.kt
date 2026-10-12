package com.example.util

import java.util.Locale
import kotlin.math.abs

object IndianCurrencyFormatter {

    /**
     * Formats a double amount into Indian Rupee format, e.g.:
     * 125000.0 -> ₹1,25,000.00
     * 5400.5 -> ₹5,400.50
     * 10000000.0 -> ₹1,00,00,000.00
     */
    fun formatRupees(amount: Double, includeDecimals: Boolean = true): String {
        val isNegative = amount < 0
        val positiveAmount = abs(amount)

        val integerPart = positiveAmount.toLong()
        val decimalPart = if (includeDecimals) {
            String.format(Locale.US, "%.2f", positiveAmount - integerPart).substring(1) // e.g. ".50"
        } else {
            ""
        }

        val formattedInt = formatIndianInteger(integerPart)
        val prefix = if (isNegative) "-₹" else "₹"
        return "$prefix$formattedInt$decimalPart"
    }

    private fun formatIndianInteger(value: Long): String {
        val s = value.toString()
        if (s.length <= 3) return s

        val lastThree = s.takeLast(3)
        var remaining = s.dropLast(3)
        val parts = mutableListOf<String>()

        while (remaining.length > 2) {
            parts.add(0, remaining.takeLast(2))
            remaining = remaining.dropLast(2)
        }
        if (remaining.isNotEmpty()) {
            parts.add(0, remaining)
        }

        return parts.joinToString(",") + ",$lastThree"
    }

    /**
     * Compact format for charts or badges, e.g. ₹1.25 L, ₹2.5 Cr, ₹5.2K
     */
    fun formatCompact(amount: Double): String {
        val isNegative = amount < 0
        val absVal = abs(amount)
        val prefix = if (isNegative) "-₹" else "₹"

        return when {
            absVal >= 10_000_000 -> String.format(Locale.US, "%s%.2f Cr", prefix, absVal / 10_000_000)
            absVal >= 100_000 -> String.format(Locale.US, "%s%.2f L", prefix, absVal / 100_000)
            absVal >= 1_000 -> String.format(Locale.US, "%s%.1f K", prefix, absVal / 1_000)
            else -> formatRupees(amount, includeDecimals = false)
        }
    }
}
