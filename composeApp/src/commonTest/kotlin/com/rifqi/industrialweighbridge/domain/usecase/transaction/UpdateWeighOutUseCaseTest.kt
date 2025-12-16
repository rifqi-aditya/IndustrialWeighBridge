package com.rifqi.industrialweighbridge.domain.usecase.transaction

import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/** Fake repository for testing UpdateWeighOutUseCase */
class FakeTransactionRepositoryForWeighOut : TransactionRepository {
    var updateWeighOutCalled = false
    var lastTicket: String? = null
    var lastExitWeight: Double? = null
    var lastNetWeight: Double? = null

    var shouldThrowError = false

    override fun getAllTransactions() =
            flowOf(emptyList<com.rifqi.industrialweighbridge.db.SelectAllTransactions>())

    override fun getOpenTransactions() =
            flowOf(emptyList<com.rifqi.industrialweighbridge.db.SelectOpenTransactions>())

    override suspend fun createWeighIn(
            ticket: String,
            vehicleId: Long,
            driverId: Long,
            productId: Long,
            weight: Double,
            isManual: Boolean
    ) {}

    override suspend fun updateWeighOut(ticket: String, exitWeight: Double, netWeight: Double) {
        if (shouldThrowError) {
            throw RuntimeException("Test error")
        }
        updateWeighOutCalled = true
        lastTicket = ticket
        lastExitWeight = exitWeight
        lastNetWeight = netWeight
    }

    override suspend fun deleteTransaction(ticket: String) {}

    fun reset() {
        updateWeighOutCalled = false
        lastTicket = null
        lastExitWeight = null
        lastNetWeight = null
        shouldThrowError = false
    }
}

class UpdateWeighOutUseCaseTest {

    private lateinit var repository: FakeTransactionRepositoryForWeighOut
    private lateinit var useCase: UpdateWeighOutUseCase

    @BeforeTest
    fun setup() {
        repository = FakeTransactionRepositoryForWeighOut()
        useCase = UpdateWeighOutUseCase(repository)
    }

    @Test
    fun `invoke with valid data updates transaction`() = runTest {
        val result =
                useCase(
                        ticketNumber = "WB-20231214-0001",
                        entryWeight = 5000.0,
                        exitWeight = 3000.0
                )

        assertTrue(result.isSuccess, "Should succeed with valid data")
        assertTrue(repository.updateWeighOutCalled, "Repository should be called")
        assertEquals("WB-20231214-0001", repository.lastTicket)
        assertEquals(3000.0, repository.lastExitWeight)
    }

    @Test
    fun `invoke calculates correct netto when entry greater than exit`() = runTest {
        // Entry (gross) = 5000, Exit (tare) = 3000 => Netto = 2000
        val result =
                useCase(
                        ticketNumber = "WB-20231214-0001",
                        entryWeight = 5000.0,
                        exitWeight = 3000.0
                )

        assertTrue(result.isSuccess)
        assertEquals(2000.0, result.getOrNull(), "Netto should be abs(exit - entry)")
        assertEquals(2000.0, repository.lastNetWeight)
    }

    @Test
    fun `invoke calculates correct netto when exit greater than entry`() = runTest {
        // Entry (tare) = 2000, Exit (gross) = 5000 => Netto = 3000
        val result =
                useCase(
                        ticketNumber = "WB-20231214-0001",
                        entryWeight = 2000.0,
                        exitWeight = 5000.0
                )

        assertTrue(result.isSuccess)
        assertEquals(3000.0, result.getOrNull(), "Netto should be abs(exit - entry)")
        assertEquals(3000.0, repository.lastNetWeight)
    }

    @Test
    fun `invoke fails when ticket is blank`() = runTest {
        val result = useCase(ticketNumber = "", entryWeight = 5000.0, exitWeight = 3000.0)

        assertTrue(result.isFailure, "Should fail when ticket is blank")
        assertTrue(result.exceptionOrNull()?.message?.contains("Ticket") == true)
    }

    @Test
    fun `invoke fails when exit weight is below minimum`() = runTest {
        val result =
                useCase(
                        ticketNumber = "WB-20231214-0001",
                        entryWeight = 5000.0,
                        exitWeight = 10.0 // Below minimum 50 kg
                )

        assertTrue(result.isFailure, "Should fail when exit weight is below minimum")
        assertTrue(result.exceptionOrNull()?.message?.contains("minimal") == true)
    }

    @Test
    fun `invoke succeeds with exit weight exactly at minimum`() = runTest {
        val result =
                useCase(
                        ticketNumber = "WB-20231214-0001",
                        entryWeight = 5000.0,
                        exitWeight = 50.0 // Exactly at minimum
                )

        assertTrue(result.isSuccess, "Should succeed with exit weight at minimum")
    }

    @Test
    fun `invoke returns failure when repository throws exception`() = runTest {
        repository.shouldThrowError = true

        val result =
                useCase(
                        ticketNumber = "WB-20231214-0001",
                        entryWeight = 5000.0,
                        exitWeight = 3000.0
                )

        assertTrue(result.isFailure, "Should fail when repository throws")
    }

    @Test
    fun `invoke returns netto on success`() = runTest {
        val result =
                useCase(
                        ticketNumber = "WB-20231214-0001",
                        entryWeight = 10000.0,
                        exitWeight = 7500.0
                )

        assertTrue(result.isSuccess)
        assertEquals(2500.0, result.getOrNull(), "Should return calculated netto")
    }
}
