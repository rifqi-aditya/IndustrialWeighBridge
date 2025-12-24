package com.rifqi.industrialweighbridge.engine

import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/** Fake TransactionRepository untuk testing. Menyimpan data transaksi di memory. */
class FakeTransactionRepository : TransactionRepository {

        val createdTransactions = mutableListOf<CreatedTransaction>()
        val completedTransactions = mutableListOf<CompletedTransactionData>()

        data class CreatedTransaction(
                val ticket: String,
                val vehicleId: Long,
                val driverId: Long,
                val productId: Long,
                val partnerId: Long?,
                val weight: Double,
                val isManual: Boolean,
                val transactionType: TransactionType
        )

        data class CompletedTransactionData(
                val ticket: String,
                val exitWeight: Double,
                val netWeight: Double
        )

        override fun getAllTransactions(): Flow<List<SelectAllTransactions>> = flowOf(emptyList())

        override fun getOpenTransactions(): Flow<List<SelectOpenTransactions>> = flowOf(emptyList())

        override suspend fun createWeighIn(
                ticket: String,
                vehicleId: Long,
                driverId: Long,
                productId: Long,
                partnerId: Long?,
                weight: Double,
                isManual: Boolean,
                transactionType: TransactionType
        ) {
                createdTransactions.add(
                        CreatedTransaction(
                                ticket,
                                vehicleId,
                                driverId,
                                productId,
                                partnerId,
                                weight,
                                isManual,
                                transactionType
                        )
                )
        }

        override suspend fun updateWeighOut(ticket: String, exitWeight: Double, netWeight: Double) {
                completedTransactions.add(CompletedTransactionData(ticket, exitWeight, netWeight))
        }

        override suspend fun deleteTransaction(ticket: String) {
                // Not needed for these tests
        }
}

/** Unit tests untuk WeighingEngine. Memverifikasi alur penimbangan */
class WeighingEngineTest {

        private fun createEngine(
                repository: TransactionRepository = FakeTransactionRepository(),
                config: StabilityConfig =
                        StabilityConfig(
                                windowSize = 3,
                                toleranceKg = 1.0,
                                minimumWeightKg = 10.0 // Lower for testing
                        )
        ): WeighingEngine {
                return WeighingEngine(repository, config)
        }

        // =========================================
        // Test: State Machine - Initial State
        // =========================================

        @Test
        fun `initial state should be Idle`() {
                val engine = createEngine()
                assertIs<WeighingState.Idle>(engine.state.value)
        }

        // =========================================
        // Test: startWeighIn
        // =========================================

        @Test
        fun `startWeighIn should transition to WeighingIn state`() {
                val engine = createEngine()

                val result =
                        engine.startWeighIn(
                                WeighInRequest(
                                        vehicleId = 1,
                                        driverId = 1,
                                        productId = 1,
                                        partnerId = null,
                                        transactionType = TransactionType.INBOUND,
                                        isManualMode = true
                                )
                        )

                assertIs<WeighingResult.Success<Unit>>(result)
                assertIs<WeighingState.WeighingIn>(engine.state.value)
        }

        @Test
        fun `startWeighIn should store correct data in state`() {
                val engine = createEngine()

                engine.startWeighIn(
                        WeighInRequest(
                                vehicleId = 100,
                                driverId = 200,
                                productId = 300,
                                partnerId = null,
                                transactionType = TransactionType.OUTBOUND,
                                isManualMode = true
                        )
                )

                val state = engine.state.value
                assertIs<WeighingState.WeighingIn>(state)
                assertEquals(100L, state.selectedVehicleId)
                assertEquals(200L, state.selectedDriverId)
                assertEquals(300L, state.selectedProductId)
                assertEquals(TransactionType.OUTBOUND, state.transactionType)
                assertTrue(state.isManualMode)
        }

        @Test
        fun `startWeighIn should fail when in WeighingOut state`() {
                val engine = createEngine()

                // First, transition to WeighingOut
                engine.startWeighOut(
                        WeighOutRequest(
                                ticketNumber = "TEST-001",
                                firstWeight = 1000.0,
                                transactionType = TransactionType.INBOUND,
                                vehicleId = 1,
                                driverId = 1,
                                productId = 1,
                                partnerId = null,
                                isManualMode = true
                        )
                )

                // Now try startWeighIn - should fail
                val result =
                        engine.startWeighIn(
                                WeighInRequest(
                                        vehicleId = 1,
                                        driverId = 1,
                                        productId = 1,
                                        partnerId = null,
                                        transactionType = TransactionType.INBOUND,
                                        isManualMode = true
                                )
                        )

                assertIs<WeighingResult.Failure>(result)
        }

        // =========================================
        // Test: captureWeighIn (Manual Mode)
        // =========================================

        @Test
        fun `captureWeighIn should create transaction and return to Idle`() = runTest {
                val repository = FakeTransactionRepository()
                val engine = createEngine(repository)

                // Set manual mode and weight
                engine.setManualMode(true)
                engine.setManualWeight(500.0)

                // Start weigh-in
                engine.startWeighIn(
                        WeighInRequest(
                                vehicleId = 1,
                                driverId = 2,
                                productId = 3,
                                partnerId = null,
                                transactionType = TransactionType.INBOUND,
                                isManualMode = true
                        )
                )

                // Capture
                val result = engine.captureWeighIn()

                assertIs<WeighingResult.Success<String>>(result)
                assertTrue(result.data.isNotBlank()) // Ticket number should be generated
                assertIs<WeighingState.Idle>(engine.state.value)

                // Verify transaction was saved
                assertEquals(1, repository.createdTransactions.size)
                val txn = repository.createdTransactions[0]
                assertEquals(1L, txn.vehicleId)
                assertEquals(2L, txn.driverId)
                assertEquals(3L, txn.productId)
                assertEquals(500.0, txn.weight)
                assertTrue(txn.isManual)
        }

        @Test
        fun `captureWeighIn should fail if weight below minimum`() = runTest {
                val engine = createEngine()

                engine.setManualMode(true)
                engine.setManualWeight(5.0) // Below minimum of 10 kg

                engine.startWeighIn(
                        WeighInRequest(
                                vehicleId = 1,
                                driverId = 1,
                                productId = 1,
                                partnerId = null,
                                transactionType = TransactionType.INBOUND,
                                isManualMode = true
                        )
                )

                val result = engine.captureWeighIn()

                assertIs<WeighingResult.Failure>(result)
                assertEquals(ErrorType.BUSINESS_RULE_VIOLATION, result.errorType)
        }

        @Test
        fun `captureWeighIn should fail if not in WeighingIn state`() = runTest {
                val engine = createEngine()

                // Don't call startWeighIn, try to capture directly
                val result = engine.captureWeighIn()

                assertIs<WeighingResult.Failure>(result)
        }

        // =========================================
        // Test: captureWeighOut - Gross/Tare/Net Calculation
        // =========================================

        @Test
        fun `captureWeighOut INBOUND should calculate Gross=First Tare=Second`() = runTest {
                val repository = FakeTransactionRepository()
                val engine = createEngine(repository)

                engine.setManualMode(true)
                engine.setManualWeight(300.0) // Second weight

                // Start weigh-out with first weight = 1000 (Gross for INBOUND)
                engine.startWeighOut(
                        WeighOutRequest(
                                ticketNumber = "TEST-001",
                                firstWeight = 1000.0, // This is Gross for INBOUND
                                transactionType = TransactionType.INBOUND,
                                vehicleId = 1,
                                driverId = 1,
                                productId = 1,
                                partnerId = null,
                                isManualMode = true
                        )
                )

                val result = engine.captureWeighOut()

                assertIs<WeighingResult.Success<CompletedTransaction>>(result)
                val txn = result.data

                // INBOUND: First = Gross (1000), Second = Tare (300)
                assertEquals(1000.0, txn.grossWeight)
                assertEquals(300.0, txn.tareWeight)
                assertEquals(700.0, txn.netWeight) // 1000 - 300 = 700
        }

        @Test
        fun `captureWeighOut OUTBOUND should calculate Tare=First Gross=Second`() = runTest {
                val repository = FakeTransactionRepository()
                val engine = createEngine(repository)

                engine.setManualMode(true)
                engine.setManualWeight(1200.0) // Second weight (Gross for OUTBOUND)

                // Start weigh-out with first weight = 400 (Tare for OUTBOUND)
                engine.startWeighOut(
                        WeighOutRequest(
                                ticketNumber = "TEST-002",
                                firstWeight = 400.0, // This is Tare for OUTBOUND
                                transactionType = TransactionType.OUTBOUND,
                                vehicleId = 1,
                                driverId = 1,
                                productId = 1,
                                partnerId = null,
                                isManualMode = true
                        )
                )

                val result = engine.captureWeighOut()

                assertIs<WeighingResult.Success<CompletedTransaction>>(result)
                val txn = result.data

                // OUTBOUND: First = Tare (400), Second = Gross (1200)
                assertEquals(1200.0, txn.grossWeight)
                assertEquals(400.0, txn.tareWeight)
                assertEquals(800.0, txn.netWeight) // 1200 - 400 = 800
        }

        @Test
        fun `captureWeighOut should transition to Completed state`() = runTest {
                val engine = createEngine()

                engine.setManualMode(true)
                engine.setManualWeight(500.0)

                engine.startWeighOut(
                        WeighOutRequest(
                                ticketNumber = "TEST-003",
                                firstWeight = 1000.0,
                                transactionType = TransactionType.INBOUND,
                                vehicleId = 1,
                                driverId = 1,
                                productId = 1,
                                partnerId = null,
                                isManualMode = true
                        )
                )

                engine.captureWeighOut()

                val state = engine.state.value
                assertIs<WeighingState.Completed>(state)
                assertEquals("TEST-003", state.ticketNumber)
                assertEquals(1000.0, state.grossWeight)
                assertEquals(500.0, state.tareWeight)
                assertEquals(500.0, state.netWeight)
        }

        // =========================================
        // Test: cancelOperation
        // =========================================

        @Test
        fun `cancelOperation should return to Idle from WeighingIn`() {
                val engine = createEngine()

                engine.startWeighIn(
                        WeighInRequest(
                                vehicleId = 1,
                                driverId = 1,
                                productId = 1,
                                partnerId = null,
                                transactionType = TransactionType.INBOUND,
                                isManualMode = true
                        )
                )

                assertIs<WeighingState.WeighingIn>(engine.state.value)

                engine.cancelOperation()

                assertIs<WeighingState.Idle>(engine.state.value)
        }

        @Test
        fun `cancelOperation should return to Idle from WeighingOut`() {
                val engine = createEngine()

                engine.startWeighOut(
                        WeighOutRequest(
                                ticketNumber = "TEST-001",
                                firstWeight = 1000.0,
                                transactionType = TransactionType.INBOUND,
                                vehicleId = 1,
                                driverId = 1,
                                productId = 1,
                                partnerId = null,
                                isManualMode = true
                        )
                )

                assertIs<WeighingState.WeighingOut>(engine.state.value)

                engine.cancelOperation()

                assertIs<WeighingState.Idle>(engine.state.value)
        }

        // =========================================
        // Test: Manual Mode
        // =========================================

        @Test
        fun `setManualWeight should update currentWeight in manual mode`() {
                val engine = createEngine()

                engine.setManualMode(true)
                engine.setManualWeight(1234.5)

                assertEquals(1234.5, engine.currentWeight.value)
        }

        @Test
        fun `setManualWeight should set isStable to true`() {
                val engine = createEngine()

                engine.setManualMode(true)
                engine.setManualWeight(100.0)

                assertTrue(engine.isStable.value)
        }

        @Test
        fun `setManualWeight should not update if not in manual mode`() {
                val engine = createEngine()

                // Default is not manual mode in engine (though UI starts with manual)
                engine.setManualMode(false)
                engine.setManualWeight(999.0)

                assertEquals(0.0, engine.currentWeight.value) // Should remain 0
        }
}
