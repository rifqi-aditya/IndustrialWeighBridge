package com.rifqi.industrialweighbridge.domain.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DateTimeUtilsTest {

    @Test
    fun `nowIsoString returns ISO 8601 format`() {
        val timestamp = DateTimeUtils.nowIsoString()

        // Format: YYYY-MM-DDTHH:mm:ss
        assertTrue(
                timestamp.matches(Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")),
                "Timestamp should match ISO 8601 format: $timestamp"
        )
    }

    @Test
    fun `nowIsoString contains current date`() {
        val timestamp = DateTimeUtils.nowIsoString()
        val now = java.time.LocalDateTime.now()

        val expectedDatePart = "%04d-%02d-%02d".format(now.year, now.monthValue, now.dayOfMonth)

        assertTrue(
                timestamp.startsWith(expectedDatePart),
                "Timestamp should start with current date: $timestamp"
        )
    }

    @Test
    fun `formatForDisplay formats timestamp correctly`() {
        val input = "2023-12-14T10:30:45"
        val expected = "14/12/2023 10:30"

        val result = DateTimeUtils.formatForDisplay(input)

        assertEquals(expected, result, "Should format to DD/MM/YYYY HH:mm")
    }

    @Test
    fun `formatForDisplay returns dash for null input`() {
        val result = DateTimeUtils.formatForDisplay(null)

        assertEquals("-", result, "Should return dash for null input")
    }

    @Test
    fun `formatForDisplay returns dash for blank input`() {
        val result = DateTimeUtils.formatForDisplay("")

        assertEquals("-", result, "Should return dash for blank input")
    }

    @Test
    fun `formatForDisplay returns original for invalid format`() {
        val input = "invalid-timestamp"

        val result = DateTimeUtils.formatForDisplay(input)

        assertEquals(input, result, "Should return original for invalid format")
    }

    @Test
    fun `formatForDisplay handles timestamp without T separator`() {
        val input = "2023-12-14 10:30:45" // Invalid - no T

        val result = DateTimeUtils.formatForDisplay(input)

        assertEquals(input, result, "Should return original when T separator is missing")
    }
}
