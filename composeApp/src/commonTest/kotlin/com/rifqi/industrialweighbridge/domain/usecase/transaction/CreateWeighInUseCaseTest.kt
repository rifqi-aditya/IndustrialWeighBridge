package com.rifqi.industrialweighbridge.domain.usecase.transaction

import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/** Fake repository for testing */
class FakeTransactionRepository : TransactionRepository {
    var createWeighInCalled = false
    var lastTicket: String? = null
    var lastVehicleId: Long? = null
    var lastDriverId: Long? = null
    var lastProductId: Long? = null
    var lastWeight: Double? = null
    var lastIsManual: Boolean? = null

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
    ) {
        if (shouldThrowError) {
            throw RuntimeException("Test error")
        }
        createWeighInCalled = true
        lastTicket = ticket
        lastVehicleId = vehicleId
        lastDriverId = driverId
        lastProductId = productId
        lastWeight = weight
        lastIsManual = isManual
    }

    override suspend fun updateWeighOut(ticket: String, exitWeight: Double, netWeight: Double) {}

    override suspend fun deleteTransaction(ticket: String) {}

    fun reset() {
        createWeighInCalled = false
        lastTicket = null
        lastVehicleId = null
        lastDriverId = null
        lastProductId = null
        lastWeight = null
        lastIsManual = null
        shouldThrowError = false
    }
}

class CreateWeighInUseCaseTest {

    private lateinit var repository: FakeTransactionRepository
    private lateinit var useCase: CreateWeighInUseCase

    @BeforeTest
    fun setup() {
        repository = FakeTransactionRepository()
        useCase = CreateWeighInUseCase(repository)
        com.rifqi.industrialweighbridge.domain.utils.TicketGenerator.resetCounter()
    }

    @Test
    fun `invoke with valid data creates transaction`() = runTest {
        val result =
                useCase(
                        vehicleId = 1L,
                        driverId = 1L,
                        productId = 1L,
                        weight = 1000.0,
                        isManual = true
                )

        assertTrue(result.isSuccess, "Should succeed with valid data")
        assertTrue(repository.createWeighInCalled, "Repository should be called")
        assertEquals(1L, repository.lastVehicleId)
        assertEquals(1L, repository.lastDriverId)
        assertEquals(1L, repository.lastProductId)
        assertEquals(1000.0, repository.lastWeight)
        assertEquals(true, repository.lastIsManual)
    }

    @Test
    fun `invoke returns ticket number on success`() = runTest {
        val result =
                useCase(
                        vehicleId = 1L,
                        driverId = 1L,
                        productId = 1L,
                        weight = 1000.0,
                        isManual = true
                )

        assertTrue(result.isSuccess)
        val ticket = result.getOrNull()
        assertTrue(ticket?.startsWith("WB-") == true, "Ticket should start with WB-")
    }

    @Test
    fun `invoke fails when vehicleId is zero`() = runTest {
        val result =
                useCase(
                        vehicleId = 0L,
                        driverId = 1L,
                        productId = 1L,
                        weight = 1000.0,
                        isManual = true
                )

        assertTrue(result.isFailure, "Should fail when vehicleId is 0")
        assertTrue(result.exceptionOrNull()?.message?.contains("kendaraan") == true)
    }

    @Test
    fun `invoke fails when driverId is zero`() = runTest {
        val result =
                useCase(
                        vehicleId = 1L,
                        driverId = 0L,
                        productId = 1L,
                        weight = 1000.0,
                        isManual = true
                )

        assertTrue(result.isFailure, "Should fail when driverId is 0")
        assertTrue(result.exceptionOrNull()?.message?.contains("driver") == true)
    }

    @Test
    fun `invoke fails when productId is zero`() = runTest {
        val result =
                useCase(
                        vehicleId = 1L,
                        driverId = 1L,
                        productId = 0L,
                        weight = 1000.0,
                        isManual = true
                )

        assertTrue(result.isFailure, "Should fail when productId is 0")
        assertTrue(result.exceptionOrNull()?.message?.contains("produk") == true)
    }

    @Test
    fun `invoke fails when weight is below minimum`() = runTest {
        val result =
                useCase(
                        vehicleId = 1L,
                        driverId = 1L,
                        productId = 1L,
                        weight = 10.0, // Below minimum 50 kg
                        isManual = true
                )

        assertTrue(result.isFailure, "Should fail when weight is below minimum")
        assertTrue(result.exceptionOrNull()?.message?.contains("minimal") == true)
    }

    @Test
    fun `invoke succeeds with weight exactly at minimum`() = runTest {
        val result =
                useCase(
                        vehicleId = 1L,
                        driverId = 1L,
                        productId = 1L,
                        weight = 50.0, // Exactly at minimum
                        isManual = true
                )

        assertTrue(result.isSuccess, "Should succeed with weight at minimum threshold")
    }

    @Test
    fun `invoke returns failure when repository throws exception`() = runTest {
        repository.shouldThrowError = true

        val result =
                useCase(
                        vehicleId = 1L,
                        driverId = 1L,
                        productId = 1L,
                        weight = 1000.0,
                        isManual = true
                )

        assertTrue(result.isFailure, "Should fail when repository throws")
    }
}
