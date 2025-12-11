package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rifqi.industrialweighbridge.db.TransactionStatus
import com.rifqi.industrialweighbridge.db.User
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.db.WeighingTransaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        // 1. Buat Driver di Memori
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

        // 2. Buat Skema
        WeighbridgeDatabase.Schema.create(driver)

        // 3. Inisialisasi Database dengan ADAPTER
        database =
                WeighbridgeDatabase(
                        driver = driver,

                        // Memberi tahu cara konversi Enum UserRole <-> Text
                        UserAdapter = User.Adapter(roleAdapter = EnumColumnAdapter()),

                        // Memberi tahu cara konversi Enum TransactionStatus <-> Text
                        WeighingTransactionAdapter =
                                WeighingTransaction.Adapter(statusAdapter = EnumColumnAdapter())
                )

        // Inisialisasi semua repository yang dibutuhkan
        transactionRepository = SqlDelightTransactionRepository(database)
        vehicleRepository = SqlDelightVehicleRepository(database)
        driverRepository = SqlDelightDriverRepository(database)
        productRepository = SqlDelightProductRepository(database)
    }

    // Helper function untuk setup data master (Vehicle, Driver, Product)
    // Dipanggil di test yang membutuhkan foreign key
    private suspend fun setupMasterData() {
        // Insert Vehicle
        vehicleRepository.addVehicle("B 1234 ABC", "Truk Merah", 5000.0)
        val vehicles = vehicleRepository.getAllVehicles().first()
        testVehicleId = vehicles[0].id

        // Insert Driver
        driverRepository.addDriver("Budi Setiawan", "SIM-123456")
        val drivers = driverRepository.getAllDrivers().first()
        testDriverId = drivers[0].id

        // Insert Product
        productRepository.addProduct("Pasir Silika", "PRD-001")
        val products = productRepository.getAllProducts().first()
        testProductId = products[0].id
    }

    @Test
    fun `test getAllTransactions returns empty list initially`() = runTest {
        // Test untuk memastikan kalau belum isi apa-apa, list-nya kosong
        val transactions = transactionRepository.getAllTransactions().first()
        assertTrue(transactions.isEmpty(), "Database awal harusnya kosong")
    }

    @Test
    fun `test getOpenTransactions returns empty list initially`() = runTest {
        // Test untuk memastikan open transactions awal kosong
        val openTransactions = transactionRepository.getOpenTransactions().first()
        assertTrue(openTransactions.isEmpty(), "Open transactions awal harusnya kosong")
    }

    @Test
    fun `test createWeighIn inserts transaction correctly`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        setupMasterData()

        val ticketNumber = "TKT-001"
        val weighInWeight = 15000.0
        val isManual = false

        // --- 2. WHEN (Aksi dilakukan) ---
        transactionRepository.createWeighIn(
                ticket = ticketNumber,
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = weighInWeight,
                isManual = isManual
        )

        // --- 3. THEN (Verifikasi Hasil) ---
        val allTransactions = transactionRepository.getAllTransactions().first()

        // Cek 1: Jumlah data harus 1
        assertEquals(1, allTransactions.size, "Seharusnya ada 1 transaksi")

        // Cek 2: Datanya harus sesuai dengan yang diinput
        val savedTx = allTransactions[0]
        assertEquals(ticketNumber, savedTx.ticket_number)
        assertEquals(weighInWeight, savedTx.weigh_in_weight)
        assertEquals(TransactionStatus.OPEN, savedTx.status)
    }

    @Test
    fun `test createWeighIn creates OPEN transaction`() = runTest {
        // --- 1. GIVEN ---
        setupMasterData()

        // --- 2. WHEN ---
        transactionRepository.createWeighIn(
                ticket = "TKT-002",
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 12000.0,
                isManual = true
        )

        // --- 3. THEN ---
        val openTransactions = transactionRepository.getOpenTransactions().first()

        assertEquals(1, openTransactions.size, "Seharusnya ada 1 transaksi OPEN")
        assertEquals("TKT-002", openTransactions[0].ticket_number)
    }

    @Test
    fun `test createWeighIn with manual flag`() = runTest {
        // --- 1. GIVEN ---
        setupMasterData()

        // --- 2. WHEN ---
        // Insert transaksi manual
        transactionRepository.createWeighIn(
                ticket = "TKT-MANUAL",
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 10000.0,
                isManual = true
        )

        // --- 3. THEN ---
        val transactions = transactionRepository.getAllTransactions().first()

        assertEquals(1, transactions.size)
        assertEquals(1L, transactions[0].is_manual, "is_manual harus 1 (true)")
    }

    @Test
    fun `test updateWeighOut updates transaction correctly`() = runTest {
        // --- 1. GIVEN ---
        setupMasterData()

        val ticketNumber = "TKT-003"
        val weighInWeight = 15000.0
        val weighOutWeight = 5000.0
        val netWeight = weighInWeight - weighOutWeight // 10000.0

        // Create weigh in first
        transactionRepository.createWeighIn(
                ticket = ticketNumber,
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = weighInWeight,
                isManual = false
        )

        // --- 2. WHEN ---
        transactionRepository.updateWeighOut(
                ticket = ticketNumber,
                exitWeight = weighOutWeight,
                netWeight = netWeight
        )

        // --- 3. THEN ---
        val transactions = transactionRepository.getAllTransactions().first()

        assertEquals(1, transactions.size)

        val updatedTx = transactions[0]
        assertEquals(weighOutWeight, updatedTx.weigh_out_weight)
        assertEquals(netWeight, updatedTx.net_weight)
        assertEquals(TransactionStatus.CLOSED, updatedTx.status)
    }

    @Test
    fun `test updateWeighOut removes transaction from open list`() = runTest {
        // --- 1. GIVEN ---
        setupMasterData()

        val ticketNumber = "TKT-004"

        // Create weigh in
        transactionRepository.createWeighIn(
                ticket = ticketNumber,
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 15000.0,
                isManual = false
        )

        // Verify it's in open transactions
        val openBefore = transactionRepository.getOpenTransactions().first()
        assertEquals(1, openBefore.size, "Transaksi harus ada di open list")

        // --- 2. WHEN ---
        transactionRepository.updateWeighOut(
                ticket = ticketNumber,
                exitWeight = 5000.0,
                netWeight = 10000.0
        )

        // --- 3. THEN ---
        val openAfter = transactionRepository.getOpenTransactions().first()
        assertTrue(openAfter.isEmpty(), "Setelah weigh out, tidak ada transaksi OPEN")
    }

    @Test
    fun `test deleteTransaction removes transaction`() = runTest {
        // --- 1. GIVEN ---
        setupMasterData()

        val ticketNumber = "TKT-DELETE"

        transactionRepository.createWeighIn(
                ticket = ticketNumber,
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 15000.0,
                isManual = false
        )

        // Verify transaction exists
        val transactionsBefore = transactionRepository.getAllTransactions().first()
        assertEquals(1, transactionsBefore.size)

        // --- 2. WHEN ---
        transactionRepository.deleteTransaction(ticketNumber)

        // --- 3. THEN ---
        val transactionsAfter = transactionRepository.getAllTransactions().first()
        assertTrue(transactionsAfter.isEmpty(), "Transaksi harus sudah terhapus")
    }

    @Test
    fun `test multiple transactions with different tickets`() = runTest {
        // --- 1. GIVEN ---
        setupMasterData()

        // --- 2. WHEN ---
        transactionRepository.createWeighIn(
                ticket = "TKT-A",
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 10000.0,
                isManual = false
        )

        transactionRepository.createWeighIn(
                ticket = "TKT-B",
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 12000.0,
                isManual = true
        )

        transactionRepository.createWeighIn(
                ticket = "TKT-C",
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 14000.0,
                isManual = false
        )

        // --- 3. THEN ---
        val transactions = transactionRepository.getAllTransactions().first()

        assertEquals(3, transactions.size, "Seharusnya ada 3 transaksi")

        val tickets = transactions.map { it.ticket_number }
        assertTrue("TKT-A" in tickets)
        assertTrue("TKT-B" in tickets)
        assertTrue("TKT-C" in tickets)
    }

    @Test
    fun `test open transactions only returns OPEN status`() = runTest {
        // --- 1. GIVEN ---
        setupMasterData()

        // Create 3 transactions
        transactionRepository.createWeighIn(
                ticket = "TKT-OPEN-1",
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 10000.0,
                isManual = false
        )

        transactionRepository.createWeighIn(
                ticket = "TKT-COMPLETED",
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 12000.0,
                isManual = false
        )

        transactionRepository.createWeighIn(
                ticket = "TKT-OPEN-2",
                vehicleId = testVehicleId,
                driverId = testDriverId,
                productId = testProductId,
                weight = 14000.0,
                isManual = false
        )

        // Complete one transaction
        transactionRepository.updateWeighOut(
                ticket = "TKT-COMPLETED",
                exitWeight = 5000.0,
                netWeight = 7000.0
        )

        // --- 2. WHEN ---
        val openTransactions = transactionRepository.getOpenTransactions().first()

        // --- 3. THEN ---
        assertEquals(2, openTransactions.size, "Seharusnya ada 2 transaksi OPEN")

        val openTickets = openTransactions.map { it.ticket_number }
        assertTrue("TKT-OPEN-1" in openTickets, "TKT-OPEN-1 harus ada")
        assertTrue("TKT-OPEN-2" in openTickets, "TKT-OPEN-2 harus ada")
        assertTrue("TKT-COMPLETED" !in openTickets, "TKT-COMPLETED tidak boleh ada di open list")
    }
}
