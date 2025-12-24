package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rifqi.industrialweighbridge.db.Partner
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

    // ==========================================
    // TEST CASES: updateVehicle
    // ==========================================

    @Test
    fun `test updateVehicle updates plate number correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("B 1234 OLD", "Truk Lama", 5000.0)
        val vehicles = repository.getAllVehicles().first()
        val vehicleId = vehicles[0].id

        // --- 2. WHEN ---
        repository.updateVehicle(vehicleId, "B 5678 NEW", "Truk Lama", 5000.0)

        // --- 3. THEN ---
        val updatedVehicles = repository.getAllVehicles().first()
        assertEquals(1, updatedVehicles.size, "Jumlah kendaraan harus tetap 1")
        assertEquals("B 5678 NEW", updatedVehicles[0].plate_number, "Plat nomor harus terupdate")
    }

    @Test
    fun `test updateVehicle updates description correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("B 1234 ABC", "Deskripsi Lama", 5000.0)
        val vehicles = repository.getAllVehicles().first()
        val vehicleId = vehicles[0].id

        // --- 2. WHEN ---
        repository.updateVehicle(vehicleId, "B 1234 ABC", "Deskripsi Baru", 5000.0)

        // --- 3. THEN ---
        val updatedVehicles = repository.getAllVehicles().first()
        assertEquals("Deskripsi Baru", updatedVehicles[0].description, "Deskripsi harus terupdate")
    }

    @Test
    fun `test updateVehicle updates tare weight correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("B 1234 ABC", "Truk", 5000.0)
        val vehicles = repository.getAllVehicles().first()
        val vehicleId = vehicles[0].id

        // --- 2. WHEN ---
        repository.updateVehicle(vehicleId, "B 1234 ABC", "Truk", 6000.0)

        // --- 3. THEN ---
        val updatedVehicles = repository.getAllVehicles().first()
        assertEquals(6000.0, updatedVehicles[0].tare_weight, "Tare weight harus terupdate")
    }

    @Test
    fun `test updateVehicle updates all fields`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("OLD-PLATE", "Old Desc", 1000.0)
        val vehicles = repository.getAllVehicles().first()
        val vehicleId = vehicles[0].id

        // --- 2. WHEN ---
        repository.updateVehicle(vehicleId, "NEW-PLATE", "New Desc", 2000.0)

        // --- 3. THEN ---
        val updated = repository.getAllVehicles().first()[0]
        assertEquals("NEW-PLATE", updated.plate_number)
        assertEquals("New Desc", updated.description)
        assertEquals(2000.0, updated.tare_weight)
    }

    @Test
    fun `test updateVehicle can set nullable fields to null`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("B 1234 ABC", "Has Desc", 5000.0)
        val vehicles = repository.getAllVehicles().first()
        val vehicleId = vehicles[0].id

        // --- 2. WHEN ---
        repository.updateVehicle(vehicleId, "B 1234 ABC", null, null)

        // --- 3. THEN ---
        val updated = repository.getAllVehicles().first()[0]
        assertNull(updated.description, "Description harus bisa diset ke null")
        assertNull(updated.tare_weight, "Tare weight harus bisa diset ke null")
    }

    @Test
    fun `test updateVehicle only affects specified vehicle`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("B 1111 AAA", "Truk A", 5000.0)
        repository.addVehicle("B 2222 BBB", "Truk B", 4000.0)
        val vehicles = repository.getAllVehicles().first()
        val vehicleAId = vehicles.find { it.plate_number == "B 1111 AAA" }!!.id

        // --- 2. WHEN ---
        repository.updateVehicle(vehicleAId, "B 1111 NEW", "Truk A Updated", 5500.0)

        // --- 3. THEN ---
        val updatedVehicles = repository.getAllVehicles().first()
        val vehicleA = updatedVehicles.find { it.id == vehicleAId }!!
        val vehicleB = updatedVehicles.find { it.plate_number == "B 2222 BBB" }!!

        assertEquals("B 1111 NEW", vehicleA.plate_number, "Vehicle A harus terupdate")
        assertEquals("B 2222 BBB", vehicleB.plate_number, "Vehicle B harus tetap sama")
        assertEquals("Truk B", vehicleB.description, "Vehicle B description harus tetap sama")
    }

    // ==========================================
    // TEST CASES: deleteVehicle
    // ==========================================

    @Test
    fun `test deleteVehicle removes vehicle correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("B DELETE", "Akan Dihapus", 5000.0)
        val vehicles = repository.getAllVehicles().first()
        assertEquals(1, vehicles.size)
        val vehicleId = vehicles[0].id

        // --- 2. WHEN ---
        repository.deleteVehicle(vehicleId)

        // --- 3. THEN ---
        val remaining = repository.getAllVehicles().first()
        assertTrue(remaining.isEmpty(), "Database harus kosong setelah delete")
    }

    @Test
    fun `test deleteVehicle only removes specified vehicle`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("B KEEP 1", "Keep 1", 5000.0)
        repository.addVehicle("B DELETE", "Delete", 4000.0)
        repository.addVehicle("B KEEP 2", "Keep 2", 3000.0)
        val vehicles = repository.getAllVehicles().first()
        assertEquals(3, vehicles.size)
        val deleteId = vehicles.find { it.plate_number == "B DELETE" }!!.id

        // --- 2. WHEN ---
        repository.deleteVehicle(deleteId)

        // --- 3. THEN ---
        val remaining = repository.getAllVehicles().first()
        assertEquals(2, remaining.size, "Harus tersisa 2 kendaraan")

        val plates = remaining.map { it.plate_number }
        assertTrue("B KEEP 1" in plates, "Vehicle Keep 1 harus tetap ada")
        assertTrue("B KEEP 2" in plates, "Vehicle Keep 2 harus tetap ada")
        assertTrue("B DELETE" !in plates, "Vehicle Delete harus sudah terhapus")
    }

    @Test
    fun `test deleteVehicle with non-existent id does not affect data`() = runTest {
        // --- 1. GIVEN ---
        repository.addVehicle("B TETAP", "Tetap Ada", 5000.0)
        val vehicles = repository.getAllVehicles().first()
        assertEquals(1, vehicles.size)

        // --- 2. WHEN ---
        repository.deleteVehicle(99999L) // ID yang tidak ada

        // --- 3. THEN ---
        val remaining = repository.getAllVehicles().first()
        assertEquals(1, remaining.size, "Data harus tetap ada")
    }
}
