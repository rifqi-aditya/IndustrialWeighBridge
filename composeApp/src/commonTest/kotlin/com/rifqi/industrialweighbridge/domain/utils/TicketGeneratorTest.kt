package com.rifqi.industrialweighbridge.domain.utils

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketGeneratorTest {

    @BeforeTest
    fun setup() {
        // Reset counter before each test
        TicketGenerator.resetCounter()
    }

    @Test
    fun `generate returns ticket with correct format`() {
        val ticket = TicketGenerator.generate()

        // Format: WB-YYYYMMDD-XXXX
        assertTrue(ticket.startsWith("WB-"), "Ticket should start with 'WB-'")
        assertTrue(
                ticket.matches(Regex("WB-\\d{8}-\\d{4}")),
                "Ticket format should be WB-YYYYMMDD-XXXX"
        )
    }

    @Test
    fun `generate increments counter for same day`() {
        val ticket1 = TicketGenerator.generate()
        val ticket2 = TicketGenerator.generate()
        val ticket3 = TicketGenerator.generate()

        // Extract counter part (last 4 digits)
        val counter1 = ticket1.takeLast(4).toInt()
        val counter2 = ticket2.takeLast(4).toInt()
        val counter3 = ticket3.takeLast(4).toInt()

        assertEquals(1, counter1, "First ticket should have counter 0001")
        assertEquals(2, counter2, "Second ticket should have counter 0002")
        assertEquals(3, counter3, "Third ticket should have counter 0003")
    }

    @Test
    fun `generate uses current date in ticket`() {
        val ticket = TicketGenerator.generate()

        // Get current date parts
        val now = java.time.LocalDateTime.now()
        val expectedDatePart = "%04d%02d%02d".format(now.year, now.monthValue, now.dayOfMonth)

        // Extract date part from ticket (positions 3-10)
        val datePart = ticket.substring(3, 11)

        assertEquals(expectedDatePart, datePart, "Ticket should contain current date")
    }

    @Test
    fun `resetCounter resets the daily counter`() {
        // Generate some tickets
        TicketGenerator.generate()
        TicketGenerator.generate()
        TicketGenerator.generate()

        // Reset
        TicketGenerator.resetCounter()

        // Generate new ticket
        val ticket = TicketGenerator.generate()
        val counter = ticket.takeLast(4).toInt()

        assertEquals(1, counter, "Counter should reset to 0001 after resetCounter()")
    }

    @Test
    fun `generate pads counter with leading zeros`() {
        val ticket = TicketGenerator.generate()

        // Counter should be 4 digits with leading zeros
        val counterPart = ticket.takeLast(4)
        assertEquals("0001", counterPart, "Counter should be padded with leading zeros")
    }
}
