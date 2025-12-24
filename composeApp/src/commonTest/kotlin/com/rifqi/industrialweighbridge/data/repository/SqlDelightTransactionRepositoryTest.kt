package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rifqi.industrialweighbridge.db.Partner
import com.rifqi.industrialweighbridge.db.TransactionStatus
import com.rifqi.industrialweighbridge.db.User
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.db.WeighingTransaction
import com.rifqi.industrialweighbridge.engine.TransactionType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SqlDelightTransactionRepositoryTest {

        private lateinit var database: WeighbridgeDatabase
        private lateinit var transactionRepository: SqlDelightTransactionRepository
        private lateinit var vehicleRepository: SqlDelightVehicleRepository
        private lateinit var driverRepository: SqlDelightDriverRepository
        private lateinit var productRepository: SqlDelightProductRepository

        // Data dummy untuk testing
        private var testVehicleId: Long = 0
        private var testDriverId: Long = 0
        private var testProductId: Long = 0

        @BeforeTest
        fun setup() {
                val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
                WeighbridgeDatabase.Schema.create(driver)

                database =
                        WeighbridgeDatabase(
                                driver = driver,
                                UserAdapter = User.Adapter(roleAdapter = EnumColumnAdapter()),
                                WeighingTransactionAdapter =
                                        WeighingTransaction.Adapter(
                                                statusAdapter = EnumColumnAdapter(),
                                                transaction_typeAdapter = EnumColumnAdapter()
                                        ),
                                PartnerAdapter = Partner.Adapter(typeAdapter = EnumColumnAdapter())
                        )

                transactionRepository = SqlDelightTransactionRepository(database)
                vehicleRepository = SqlDelightVehicleRepository(database)
                driverRepository = SqlDelightDriverRepository(database)
                productRepository = SqlDelightProductRepository(database)
        }

        private suspend fun setupMasterData() {
                vehicleRepository.addVehicle("B 1234 ABC", "Truk Merah", 5000.0)
                val vehicles = vehicleRepository.getAllVehicles().first()
                testVehicleId = vehicles[0].id

                driverRepository.addDriver("Budi Setiawan", "SIM-123456")
                val drivers = driverRepository.getAllDrivers().first()
                testDriverId = drivers[0].id

                productRepository.addProduct("Pasir Silika", "PRD-001")
                val products = productRepository.getAllProducts().first()
                testProductId = products[0].id
        }

        @Test
        fun `test getAllTransactions returns empty list initially`() = runTest {
                val transactions = transactionRepository.getAllTransactions().first()
                assertTrue(transactions.isEmpty(), "Database awal harusnya kosong")
        }

        @Test
        fun `test getOpenTransactions returns empty list initially`() = runTest {
                val openTransactions = transactionRepository.getOpenTransactions().first()
                assertTrue(openTransactions.isEmpty(), "Open transactions awal harusnya kosong")
        }

        @Test
        fun `test createWeighIn inserts transaction correctly`() = runTest {
                setupMasterData()

                val ticketNumber = "TKT-001"
                val weighInWeight = 15000.0
                val isManual = false

                transactionRepository.createWeighIn(
                        ticket = ticketNumber,
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = weighInWeight,
                        isManual = isManual,
                        transactionType = TransactionType.INBOUND
                )

                val allTransactions = transactionRepository.getAllTransactions().first()

                assertEquals(1, allTransactions.size, "Seharusnya ada 1 transaksi")

                val savedTx = allTransactions[0]
                assertEquals(ticketNumber, savedTx.ticket_number)
                assertEquals(weighInWeight, savedTx.weigh_in_weight)
                assertEquals(TransactionStatus.OPEN, savedTx.status)
        }

        @Test
        fun `test createWeighIn creates OPEN transaction`() = runTest {
                setupMasterData()

                transactionRepository.createWeighIn(
                        ticket = "TKT-002",
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 12000.0,
                        isManual = true,
                        transactionType = TransactionType.INBOUND
                )

                val openTransactions = transactionRepository.getOpenTransactions().first()

                assertEquals(1, openTransactions.size, "Seharusnya ada 1 transaksi OPEN")
                assertEquals("TKT-002", openTransactions[0].ticket_number)
        }

        @Test
        fun `test createWeighIn with manual flag`() = runTest {
                setupMasterData()

                transactionRepository.createWeighIn(
                        ticket = "TKT-MANUAL",
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 10000.0,
                        isManual = true,
                        transactionType = TransactionType.OUTBOUND
                )

                val transactions = transactionRepository.getAllTransactions().first()

                assertEquals(1, transactions.size)
                assertEquals(1L, transactions[0].is_manual, "is_manual harus 1 (true)")
        }

        @Test
        fun `test updateWeighOut updates transaction correctly`() = runTest {
                setupMasterData()

                val ticketNumber = "TKT-003"
                val weighInWeight = 15000.0
                val weighOutWeight = 5000.0
                val netWeight = weighInWeight - weighOutWeight

                transactionRepository.createWeighIn(
                        ticket = ticketNumber,
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = weighInWeight,
                        isManual = false,
                        transactionType = TransactionType.INBOUND
                )

                transactionRepository.updateWeighOut(
                        ticket = ticketNumber,
                        exitWeight = weighOutWeight,
                        netWeight = netWeight
                )

                val transactions = transactionRepository.getAllTransactions().first()

                assertEquals(1, transactions.size)

                val updatedTx = transactions[0]
                assertEquals(weighOutWeight, updatedTx.weigh_out_weight)
                assertEquals(netWeight, updatedTx.net_weight)
                assertEquals(TransactionStatus.CLOSED, updatedTx.status)
        }

        @Test
        fun `test updateWeighOut removes transaction from open list`() = runTest {
                setupMasterData()

                val ticketNumber = "TKT-004"

                transactionRepository.createWeighIn(
                        ticket = ticketNumber,
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 15000.0,
                        isManual = false,
                        transactionType = TransactionType.INBOUND
                )

                val openBefore = transactionRepository.getOpenTransactions().first()
                assertEquals(1, openBefore.size, "Transaksi harus ada di open list")

                transactionRepository.updateWeighOut(
                        ticket = ticketNumber,
                        exitWeight = 5000.0,
                        netWeight = 10000.0
                )

                val openAfter = transactionRepository.getOpenTransactions().first()
                assertTrue(openAfter.isEmpty(), "Setelah weigh out, tidak ada transaksi OPEN")
        }

        @Test
        fun `test deleteTransaction removes transaction`() = runTest {
                setupMasterData()

                val ticketNumber = "TKT-DELETE"

                transactionRepository.createWeighIn(
                        ticket = ticketNumber,
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 15000.0,
                        isManual = false,
                        transactionType = TransactionType.INBOUND
                )

                val transactionsBefore = transactionRepository.getAllTransactions().first()
                assertEquals(1, transactionsBefore.size)

                transactionRepository.deleteTransaction(ticketNumber)

                val transactionsAfter = transactionRepository.getAllTransactions().first()
                assertTrue(transactionsAfter.isEmpty(), "Transaksi harus sudah terhapus")
        }

        @Test
        fun `test multiple transactions with different tickets`() = runTest {
                setupMasterData()

                transactionRepository.createWeighIn(
                        ticket = "TKT-A",
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 10000.0,
                        isManual = false,
                        transactionType = TransactionType.INBOUND
                )

                transactionRepository.createWeighIn(
                        ticket = "TKT-B",
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 12000.0,
                        isManual = true,
                        transactionType = TransactionType.OUTBOUND
                )

                transactionRepository.createWeighIn(
                        ticket = "TKT-C",
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 14000.0,
                        isManual = false,
                        transactionType = TransactionType.INBOUND
                )

                val transactions = transactionRepository.getAllTransactions().first()

                assertEquals(3, transactions.size, "Seharusnya ada 3 transaksi")

                val tickets = transactions.map { it.ticket_number }
                assertTrue("TKT-A" in tickets)
                assertTrue("TKT-B" in tickets)
                assertTrue("TKT-C" in tickets)
        }

        @Test
        fun `test open transactions only returns OPEN status`() = runTest {
                setupMasterData()

                transactionRepository.createWeighIn(
                        ticket = "TKT-OPEN-1",
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 10000.0,
                        isManual = false,
                        transactionType = TransactionType.INBOUND
                )

                transactionRepository.createWeighIn(
                        ticket = "TKT-COMPLETED",
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 12000.0,
                        isManual = false,
                        transactionType = TransactionType.INBOUND
                )

                transactionRepository.createWeighIn(
                        ticket = "TKT-OPEN-2",
                        vehicleId = testVehicleId,
                        driverId = testDriverId,
                        productId = testProductId,
                        partnerId = null,
                        weight = 14000.0,
                        isManual = false,
                        transactionType = TransactionType.OUTBOUND
                )

                transactionRepository.updateWeighOut(
                        ticket = "TKT-COMPLETED",
                        exitWeight = 5000.0,
                        netWeight = 7000.0
                )

                val openTransactions = transactionRepository.getOpenTransactions().first()

                assertEquals(2, openTransactions.size, "Seharusnya ada 2 transaksi OPEN")

                val openTickets = openTransactions.map { it.ticket_number }
                assertTrue("TKT-OPEN-1" in openTickets, "TKT-OPEN-1 harus ada")
                assertTrue("TKT-OPEN-2" in openTickets, "TKT-OPEN-2 harus ada")
                assertTrue(
                        "TKT-COMPLETED" !in openTickets,
                        "TKT-COMPLETED tidak boleh ada di open list"
                )
        }
}
