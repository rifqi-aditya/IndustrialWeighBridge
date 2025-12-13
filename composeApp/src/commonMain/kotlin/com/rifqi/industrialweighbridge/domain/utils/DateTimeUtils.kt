package com.rifqi.industrialweighbridge.domain.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Utility object for date/time operations. */
object DateTimeUtils {

    private val isoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    /** Returns current ISO 8601 timestamp string. Format: 2023-12-14T10:30:45 */
    fun nowIsoString(): String {
        return LocalDateTime.now().format(isoFormatter)
    }

    /** Formats timestamp string for display. Input: 2023-12-14T10:30:45 Output: 14/12/2023 10:30 */
    fun formatForDisplay(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return "-"

        return try {
            // Parse ISO format
            val parts = isoTimestamp.split("T")
            if (parts.size != 2) return isoTimestamp

            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")

            if (dateParts.size != 3 || timeParts.size < 2) return isoTimestamp

            "${dateParts[2]}/${dateParts[1]}/${dateParts[0]} ${timeParts[0]}:${timeParts[1]}"
        } catch (e: Exception) {
            isoTimestamp
        }
    }
}
