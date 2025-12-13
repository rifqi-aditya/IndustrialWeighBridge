package com.rifqi.industrialweighbridge.domain.utils

import java.time.LocalDateTime

/**
 * Utility object for generating ticket numbers. Format: WB-YYYYMMDD-XXXX (e.g., WB-20231214-0001)
 */
object TicketGenerator {

    private var dailyCounter: Long = 0
    private var lastDate: String = ""

    /** Generates a unique ticket number. Format: WB-YYYYMMDD-XXXX */
    fun generate(): String {
        val now = LocalDateTime.now()

        val currentDate = "%04d%02d%02d".format(now.year, now.monthValue, now.dayOfMonth)

        // Reset counter if date changed
        if (currentDate != lastDate) {
            dailyCounter = 0
            lastDate = currentDate
        }

        dailyCounter++

        return "WB-$currentDate-%04d".format(dailyCounter)
    }

    /** Resets the counter (for testing purposes) */
    fun resetCounter() {
        dailyCounter = 0
        lastDate = ""
    }
}
