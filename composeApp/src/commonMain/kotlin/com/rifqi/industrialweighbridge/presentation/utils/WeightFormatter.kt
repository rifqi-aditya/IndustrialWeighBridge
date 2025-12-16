package com.rifqi.industrialweighbridge.presentation.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * Utility object for formatting weight values in Indonesian format.
 *
 * Indonesian format:
 * - Thousand separator: titik (.)
 * - Decimal separator: koma (,)
 * - Example: 1.234,56 kg
 */
object WeightFormatter {

    // Indonesian locale symbols
    private val indonesianSymbols =
            DecimalFormatSymbols().apply {
                groupingSeparator = '.'
                decimalSeparator = ','
            }

    // Formatter with 2 decimal places and thousand separators
    private val weightFormat = DecimalFormat("#,##0.00", indonesianSymbols)

    // Formatter without decimal places (for integer display)
    private val integerFormat = DecimalFormat("#,##0", indonesianSymbols)

    /**
     * Formats a weight value with 2 decimal places and "kg" suffix. Example: 1234.5 -> "1.234,50
     * kg"
     */
    fun formatWeight(value: Double?): String {
        if (value == null) return "-"
        return "${weightFormat.format(value)} kg"
    }

    /**
     * Formats a weight value with 2 decimal places without suffix. Example: 1234.5 -> "1.234,50"
     */
    fun formatNumber(value: Double?): String {
        if (value == null) return ""
        return weightFormat.format(value)
    }

    /** Formats an integer value with thousand separators. Example: 1234 -> "1.234" */
    fun formatInteger(value: Long): String {
        return integerFormat.format(value)
    }

    /** Formats a Double as integer with thousand separators. Example: 1234.56 -> "1.235" */
    fun formatInteger(value: Double): String {
        return integerFormat.format(value)
    }

    /** Parses a formatted string back to Double. Handles both formats: "1.234,56" and "1234.56" */
    fun parseWeight(formattedValue: String): Double? {
        if (formattedValue.isBlank()) return null

        // Remove "kg" suffix if present
        val cleaned = formattedValue.replace("kg", "", ignoreCase = true).replace(" ", "").trim()

        // Try Indonesian format first (1.234,56)
        return try {
            // Replace Indonesian format to standard
            val standardFormat =
                    cleaned.replace(".", "") // Remove thousand separator
                            .replace(",", ".") // Convert decimal separator
            standardFormat.toDouble()
        } catch (e: Exception) {
            // Try standard format (1234.56)
            try {
                cleaned.toDouble()
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Formats input as user types (for TextField). Only formats the integer part with thousand
     * separators. Example: "1234" -> "1.234" or "1234,5" -> "1.234,5"
     */
    fun formatInputAsYouType(input: String): String {
        if (input.isBlank()) return ""

        // Allow only digits, comma, and one decimal separator
        val filtered = input.filter { it.isDigit() || it == ',' }

        // Split by comma (decimal separator)
        val parts = filtered.split(",")

        if (parts.isEmpty()) return ""

        // Format integer part with thousand separators
        val integerPart = parts[0].filter { it.isDigit() }
        if (integerPart.isEmpty()) return if (parts.size > 1) ",${parts[1].take(2)}" else ""

        val formattedInteger =
                try {
                    integerFormat.format(integerPart.toLong())
                } catch (e: Exception) {
                    integerPart
                }

        // Add decimal part if exists (max 2 digits)
        return if (parts.size > 1) {
            val decimalPart = parts[1].take(2)
            "$formattedInteger,$decimalPart"
        } else {
            formattedInteger
        }
    }
}
