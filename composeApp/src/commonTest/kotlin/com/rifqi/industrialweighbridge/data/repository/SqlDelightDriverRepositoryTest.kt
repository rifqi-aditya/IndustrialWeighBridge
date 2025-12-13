package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rifqi.industrialweighbridge.db.User
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.db.WeighingTransaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SqlDelightDriverRepositoryTest {

    private lateinit var database: WeighbridgeDatabase
    private lateinit var repository: SqlDelightDriverRepository

    // @BeforeTest artinya: Jalankan fungsi ini SEBELUM setiap @Test dimulai.
    // Ini memastikan setiap test mulai dengan database yang bersih (kosong).
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

        repository = SqlDelightDriverRepository(database)
    }

    @Test
    fun `test getAllDrivers returns empty list initially`() = runTest {
        // Test untuk memastikan kalau belum isi apa-apa, list-nya kosong
        val drivers = repository.getAllDrivers().first()
        assertTrue(drivers.isEmpty(), "Database awal harusnya kosong")
    }

    @Test
    fun `test addDriver inserts data correctly with license number`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        val namaDriver = "Budi Setiawan"
        val nomorSIM = "SIM-123456789"

        // --- 2. WHEN (Aksi dilakukan) ---
        // Panggil fungsi repository
        repository.addDriver(namaDriver, nomorSIM)

        // --- 3. THEN (Verifikasi Hasil) ---
        // Ambil data dari database (menggunakan Flow.first() untuk ambil data terbaru)
        val drivers = repository.getAllDrivers().first()

        // Cek 1: Jumlah data harus 1
        assertEquals(1, drivers.size, "Seharusnya ada 1 driver")

        // Cek 2: Datanya harus sesuai dengan yang diinput
        val savedDriver = drivers[0]
        assertEquals(namaDriver, savedDriver.name)
        assertEquals(nomorSIM, savedDriver.license_no)
        assertNotNull(savedDriver.id, "ID driver seharusnya tidak null")
    }

    @Test
    fun `test addDriver inserts data correctly without license number`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        val namaDriver = "Ahmad Yani"

        // --- 2. WHEN (Aksi dilakukan) ---
        // Panggil fungsi repository dengan licenseNo = null
        repository.addDriver(namaDriver, null)

        // --- 3. THEN (Verifikasi Hasil) ---
        val drivers = repository.getAllDrivers().first()

        // Cek 1: Jumlah data harus 1
        assertEquals(1, drivers.size, "Seharusnya ada 1 driver")

        // Cek 2: Datanya harus sesuai dengan yang diinput
        val savedDriver = drivers[0]
        assertEquals(namaDriver, savedDriver.name)
        assertEquals(null, savedDriver.license_no, "License number seharusnya null")
    }

    @Test
    fun `test addDriver inserts multiple drivers correctly`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        val driver1Name = "Budi Setiawan"
        val driver1License = "SIM-001"
        val driver2Name = "Ahmad Yani"
        val driver2License = "SIM-002"
        val driver3Name = "Siti Rahayu"

        // --- 2. WHEN (Aksi dilakukan) ---
        // Insert beberapa driver
        repository.addDriver(driver1Name, driver1License)
        repository.addDriver(driver2Name, driver2License)
        repository.addDriver(driver3Name, null)

        // --- 3. THEN (Verifikasi Hasil) ---
        val drivers = repository.getAllDrivers().first()

        // Cek: Jumlah data harus 3
        assertEquals(3, drivers.size, "Seharusnya ada 3 driver")

        // Verifikasi data masing-masing driver
        val driverNames = drivers.map { it.name }
        assertTrue(driver1Name in driverNames, "Driver 1 harus ada dalam list")
        assertTrue(driver2Name in driverNames, "Driver 2 harus ada dalam list")
        assertTrue(driver3Name in driverNames, "Driver 3 harus ada dalam list")
    }

    @Test
    fun `test driver has unique auto-generated id`() = runTest {
        // --- 1. GIVEN & WHEN ---
        repository.addDriver("Driver A", "SIM-A")
        repository.addDriver("Driver B", "SIM-B")

        // --- 2. THEN ---
        val drivers = repository.getAllDrivers().first()

        // Cek: ID harus unik
        val ids = drivers.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "Semua ID harus unik")
    }

    // ==========================================
    // TEST CASES: updateDriver
    // ==========================================

    @Test
    fun `test updateDriver updates name correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addDriver("Budi Lama", "SIM-001")
        val drivers = repository.getAllDrivers().first()
        val driverId = drivers[0].id

        // --- 2. WHEN ---
        repository.updateDriver(driverId, "Budi Baru", "SIM-001")

        // --- 3. THEN ---
        val updatedDrivers = repository.getAllDrivers().first()
        assertEquals(1, updatedDrivers.size, "Jumlah driver harus tetap 1")
        assertEquals("Budi Baru", updatedDrivers[0].name, "Nama harus terupdate")
        assertEquals("SIM-001", updatedDrivers[0].license_no, "License harus tetap sama")
    }

    @Test
    fun `test updateDriver updates license number correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addDriver("Ahmad", "SIM-OLD")
        val drivers = repository.getAllDrivers().first()
        val driverId = drivers[0].id

        // --- 2. WHEN ---
        repository.updateDriver(driverId, "Ahmad", "SIM-NEW")

        // --- 3. THEN ---
        val updatedDrivers = repository.getAllDrivers().first()
        assertEquals("Ahmad", updatedDrivers[0].name, "Nama harus tetap sama")
        assertEquals("SIM-NEW", updatedDrivers[0].license_no, "License harus terupdate")
    }

    @Test
    fun `test updateDriver updates both name and license`() = runTest {
        // --- 1. GIVEN ---
        repository.addDriver("Nama Lama", "SIM-LAMA")
        val drivers = repository.getAllDrivers().first()
        val driverId = drivers[0].id

        // --- 2. WHEN ---
        repository.updateDriver(driverId, "Nama Baru", "SIM-BARU")

        // --- 3. THEN ---
        val updatedDrivers = repository.getAllDrivers().first()
        assertEquals("Nama Baru", updatedDrivers[0].name)
        assertEquals("SIM-BARU", updatedDrivers[0].license_no)
    }

    @Test
    fun `test updateDriver can set license to null`() = runTest {
        // --- 1. GIVEN ---
        repository.addDriver("Driver", "SIM-123")
        val drivers = repository.getAllDrivers().first()
        val driverId = drivers[0].id

        // --- 2. WHEN ---
        repository.updateDriver(driverId, "Driver", null)

        // --- 3. THEN ---
        val updatedDrivers = repository.getAllDrivers().first()
        assertEquals(null, updatedDrivers[0].license_no, "License harus bisa diset ke null")
    }

    @Test
    fun `test updateDriver only affects specified driver`() = runTest {
        // --- 1. GIVEN ---
        repository.addDriver("Driver A", "SIM-A")
        repository.addDriver("Driver B", "SIM-B")
        val drivers = repository.getAllDrivers().first()
        val driverAId = drivers.find { it.name == "Driver A" }!!.id

        // --- 2. WHEN ---
        repository.updateDriver(driverAId, "Driver A Updated", "SIM-A-NEW")

        // --- 3. THEN ---
        val updatedDrivers = repository.getAllDrivers().first()
        val driverA = updatedDrivers.find { it.id == driverAId }!!
        val driverB = updatedDrivers.find { it.name == "Driver B" }!!

        assertEquals("Driver A Updated", driverA.name, "Driver A harus terupdate")
        assertEquals("Driver B", driverB.name, "Driver B harus tetap sama")
        assertEquals("SIM-B", driverB.license_no, "Driver B license harus tetap sama")
    }

    // ==========================================
    // TEST CASES: deleteDriver
    // ==========================================

    @Test
    fun `test deleteDriver removes driver correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addDriver("Driver Akan Dihapus", "SIM-DEL")
        val drivers = repository.getAllDrivers().first()
        assertEquals(1, drivers.size)
        val driverId = drivers[0].id

        // --- 2. WHEN ---
        repository.deleteDriver(driverId)

        // --- 3. THEN ---
        val remainingDrivers = repository.getAllDrivers().first()
        assertTrue(remainingDrivers.isEmpty(), "Database harus kosong setelah delete")
    }

    @Test
    fun `test deleteDriver only removes specified driver`() = runTest {
        // --- 1. GIVEN ---
        repository.addDriver("Driver Keep 1", "SIM-1")
        repository.addDriver("Driver Delete", "SIM-DEL")
        repository.addDriver("Driver Keep 2", "SIM-2")
        val drivers = repository.getAllDrivers().first()
        assertEquals(3, drivers.size)
        val deleteId = drivers.find { it.name == "Driver Delete" }!!.id

        // --- 2. WHEN ---
        repository.deleteDriver(deleteId)

        // --- 3. THEN ---
        val remainingDrivers = repository.getAllDrivers().first()
        assertEquals(2, remainingDrivers.size, "Harus tersisa 2 driver")

        val names = remainingDrivers.map { it.name }
        assertTrue("Driver Keep 1" in names, "Driver Keep 1 harus tetap ada")
        assertTrue("Driver Keep 2" in names, "Driver Keep 2 harus tetap ada")
        assertTrue("Driver Delete" !in names, "Driver Delete harus sudah terhapus")
    }

    @Test
    fun `test deleteDriver with non-existent id does not affect data`() = runTest {
        // --- 1. GIVEN ---
        repository.addDriver("Driver Tetap", "SIM-1")
        val drivers = repository.getAllDrivers().first()
        assertEquals(1, drivers.size)

        // --- 2. WHEN ---
        repository.deleteDriver(99999L) // ID yang tidak ada

        // --- 3. THEN ---
        val remainingDrivers = repository.getAllDrivers().first()
        assertEquals(1, remainingDrivers.size, "Data harus tetap ada")
    }
}
