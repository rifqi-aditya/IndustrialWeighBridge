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
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SqlDelightVehicleRepositoryTest {

    private lateinit var database: WeighbridgeDatabase
    private lateinit var repository: SqlDelightVehicleRepository

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

        repository = SqlDelightVehicleRepository(database)
    }

    // ==========================================
    // TEST CASES: getAllVehicles
    // ==========================================

    @Test
    fun `test getAllVehicles returns empty list initially`() = runTest {
        // Test untuk memastikan kalau belum isi apa-apa, list-nya kosong
        val vehicles = repository.getAllVehicles().first()
        assertTrue(vehicles.isEmpty(), "Database awal harusnya kosong")
    }

    // ==========================================
    // TEST CASES: addVehicle - Complete Data
    // ==========================================

    @Test
    fun `test addVehicle inserts data correctly with all fields`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        val platNomor = "B 1234 ABC"
        val deskripsi = "Truk Merah"
        val beratKosong = 5000.0

        // --- 2. WHEN (Aksi dilakukan) ---
        repository.addVehicle(platNomor, deskripsi, beratKosong)

        // --- 3. THEN (Verifikasi Hasil) ---
        val vehicles = repository.getAllVehicles().first()

        // Cek 1: Jumlah data harus 1
        assertEquals(1, vehicles.size, "Seharusnya ada 1 kendaraan")

        // Cek 2: Datanya harus sesuai dengan yang diinput
        val savedVehicle = vehicles[0]
        assertEquals(platNomor, savedVehicle.plate_number)
        assertEquals(deskripsi, savedVehicle.description)
        assertEquals(beratKosong, savedVehicle.tare_weight)
        assertNotNull(savedVehicle.id, "ID kendaraan seharusnya tidak null")
    }

    // ==========================================
    // TEST CASES: addVehicle - Nullable Fields
    // ==========================================

    @Test
    fun `test addVehicle inserts data correctly without description`() = runTest {
        // --- 1. GIVEN ---
        val platNomor = "D 5678 XYZ"
        val beratKosong = 4500.0

        // --- 2. WHEN ---
        repository.addVehicle(platNomor, null, beratKosong)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(1, vehicles.size, "Seharusnya ada 1 kendaraan")

        val savedVehicle = vehicles[0]
        assertEquals(platNomor, savedVehicle.plate_number)
        assertNull(savedVehicle.description, "Deskripsi seharusnya null")
        assertEquals(beratKosong, savedVehicle.tare_weight)
    }

    @Test
    fun `test addVehicle inserts data correctly without tare weight`() = runTest {
        // --- 1. GIVEN ---
        val platNomor = "F 9999 ZZZ"
        val deskripsi = "Pickup Hitam"

        // --- 2. WHEN ---
        repository.addVehicle(platNomor, deskripsi, null)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(1, vehicles.size, "Seharusnya ada 1 kendaraan")

        val savedVehicle = vehicles[0]
        assertEquals(platNomor, savedVehicle.plate_number)
        assertEquals(deskripsi, savedVehicle.description)
        assertNull(savedVehicle.tare_weight, "Berat kosong seharusnya null")
    }

    @Test
    fun `test addVehicle inserts data with only plate number`() = runTest {
        // --- 1. GIVEN ---
        val platNomor = "H 1111 AAA"

        // --- 2. WHEN ---
        repository.addVehicle(platNomor, null, null)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(1, vehicles.size, "Seharusnya ada 1 kendaraan")

        val savedVehicle = vehicles[0]
        assertEquals(platNomor, savedVehicle.plate_number)
        assertNull(savedVehicle.description, "Deskripsi seharusnya null")
        assertNull(savedVehicle.tare_weight, "Berat kosong seharusnya null")
    }

    // ==========================================
    // TEST CASES: Multiple Vehicles
    // ==========================================

    @Test
    fun `test addVehicle inserts multiple vehicles correctly`() = runTest {
        // --- 1. GIVEN ---
        val vehicle1 = Triple("B 1234 ABC", "Truk Merah", 5000.0)
        val vehicle2 = Triple("D 5678 DEF", "Truk Biru", 4500.0)
        val vehicle3 = Triple("F 9012 GHI", "Pickup Putih", 3000.0)

        // --- 2. WHEN ---
        repository.addVehicle(vehicle1.first, vehicle1.second, vehicle1.third)
        repository.addVehicle(vehicle2.first, vehicle2.second, vehicle2.third)
        repository.addVehicle(vehicle3.first, vehicle3.second, vehicle3.third)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(3, vehicles.size, "Seharusnya ada 3 kendaraan")

        val plateNumbers = vehicles.map { it.plate_number }
        assertTrue(vehicle1.first in plateNumbers, "Kendaraan 1 harus ada dalam list")
        assertTrue(vehicle2.first in plateNumbers, "Kendaraan 2 harus ada dalam list")
        assertTrue(vehicle3.first in plateNumbers, "Kendaraan 3 harus ada dalam list")
    }

    // ==========================================
    // TEST CASES: Unique ID & Data Integrity
    // ==========================================

    @Test
    fun `test vehicle has unique auto-generated id`() = runTest {
        // --- 1. GIVEN & WHEN ---
        repository.addVehicle("B 1111 AAA", "Truk A", 5000.0)
        repository.addVehicle("B 2222 BBB", "Truk B", 4500.0)
        repository.addVehicle("B 3333 CCC", "Truk C", 4000.0)

        // --- 2. THEN ---
        val vehicles = repository.getAllVehicles().first()

        // Cek: ID harus unik
        val ids = vehicles.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "Semua ID harus unik")
    }

    @Test
    fun `test vehicles with different plate numbers are stored separately`() = runTest {
        // --- 1. GIVEN ---
        val plate1 = "B 1234 ABC"
        val plate2 = "B 1234 DEF"
        val plate3 = "D 1234 ABC"

        // --- 2. WHEN ---
        repository.addVehicle(plate1, "Truk 1", 5000.0)
        repository.addVehicle(plate2, "Truk 2", 4500.0)
        repository.addVehicle(plate3, "Truk 3", 4000.0)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(3, vehicles.size, "Seharusnya ada 3 kendaraan dengan plat berbeda")

        val plateNumbers = vehicles.map { it.plate_number }
        assertTrue(plate1 in plateNumbers)
        assertTrue(plate2 in plateNumbers)
        assertTrue(plate3 in plateNumbers)
    }

    // ==========================================
    // TEST CASES: Edge Cases & Tare Weight
    // ==========================================

    @Test
    fun `test addVehicle with zero tare weight`() = runTest {
        // --- 1. GIVEN ---
        val platNomor = "Z 0000 ZZZ"
        val deskripsi = "Motor Roda Tiga"
        val beratKosong = 0.0

        // --- 2. WHEN ---
        repository.addVehicle(platNomor, deskripsi, beratKosong)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(1, vehicles.size)
        assertEquals(0.0, vehicles[0].tare_weight, "Berat kosong 0 harus tersimpan dengan benar")
    }

    @Test
    fun `test addVehicle with large tare weight`() = runTest {
        // --- 1. GIVEN ---
        val platNomor = "B 9999 MAX"
        val deskripsi = "Trailer Besar"
        val beratKosong = 50000.0 // 50 ton

        // --- 2. WHEN ---
        repository.addVehicle(platNomor, deskripsi, beratKosong)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(1, vehicles.size)
        assertEquals(
                beratKosong,
                vehicles[0].tare_weight,
                "Berat kosong besar harus tersimpan dengan benar"
        )
    }

    @Test
    fun `test addVehicle with decimal tare weight precision`() = runTest {
        // --- 1. GIVEN ---
        val platNomor = "B 1234 DEC"
        val deskripsi = "Truk Presisi"
        val beratKosong = 4567.89 // Dengan desimal

        // --- 2. WHEN ---
        repository.addVehicle(platNomor, deskripsi, beratKosong)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(1, vehicles.size)
        assertEquals(beratKosong, vehicles[0].tare_weight, "Presisi desimal harus terjaga")
    }

    // ==========================================
    // TEST CASES: Special Characters
    // ==========================================

    @Test
    fun `test addVehicle with special characters in description`() = runTest {
        // --- 1. GIVEN ---
        val platNomor = "B 1234 SPL"
        val deskripsi = "Truk 'Container' - 20ft (Blue & Red)"
        val beratKosong = 5500.0

        // --- 2. WHEN ---
        repository.addVehicle(platNomor, deskripsi, beratKosong)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(1, vehicles.size)
        assertEquals(
                deskripsi,
                vehicles[0].description,
                "Karakter spesial harus tersimpan dengan benar"
        )
    }

    @Test
    fun `test addVehicle with long description`() = runTest {
        // --- 1. GIVEN ---
        val platNomor = "B 1234 LNG"
        val deskripsi =
                "Truk kontainer besar warna biru dengan logo perusahaan XYZ Corporation di sisi kiri dan kanan, " +
                        "dilengkapi dengan GPS tracker dan sensor berat otomatis, " +
                        "biasa digunakan untuk pengangkutan barang antar pulau."
        val beratKosong = 8000.0

        // --- 2. WHEN ---
        repository.addVehicle(platNomor, deskripsi, beratKosong)

        // --- 3. THEN ---
        val vehicles = repository.getAllVehicles().first()

        assertEquals(1, vehicles.size)
        assertEquals(
                deskripsi,
                vehicles[0].description,
                "Deskripsi panjang harus tersimpan lengkap"
        )
    }
}
