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

class SqlDelightProductRepositoryTest {

    private lateinit var database: WeighbridgeDatabase
    private lateinit var repository: SqlDelightProductRepository

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

        repository = SqlDelightProductRepository(database)
    }

    @Test
    fun `test getAllProducts returns empty list initially`() = runTest {
        // Test untuk memastikan kalau belum isi apa-apa, list-nya kosong
        val products = repository.getAllProducts().first()
        assertTrue(products.isEmpty(), "Database awal harusnya kosong")
    }

    @Test
    fun `test addProduct inserts data correctly with product code`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        val namaProduk = "Pasir Silika"
        val kodeProduk = "PRD-001"

        // --- 2. WHEN (Aksi dilakukan) ---
        // Panggil fungsi repository
        repository.addProduct(namaProduk, kodeProduk)

        // --- 3. THEN (Verifikasi Hasil) ---
        // Ambil data dari database (menggunakan Flow.first() untuk ambil data terbaru)
        val products = repository.getAllProducts().first()

        // Cek 1: Jumlah data harus 1
        assertEquals(1, products.size, "Seharusnya ada 1 produk")

        // Cek 2: Datanya harus sesuai dengan yang diinput
        val savedProduct = products[0]
        assertEquals(namaProduk, savedProduct.name)
        assertEquals(kodeProduk, savedProduct.code)
        assertNotNull(savedProduct.id, "ID produk seharusnya tidak null")
    }

    @Test
    fun `test addProduct inserts data correctly without product code`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        val namaProduk = "Batu Kerikil"

        // --- 2. WHEN (Aksi dilakukan) ---
        // Panggil fungsi repository dengan code = null
        repository.addProduct(namaProduk, null)

        // --- 3. THEN (Verifikasi Hasil) ---
        val products = repository.getAllProducts().first()

        // Cek 1: Jumlah data harus 1
        assertEquals(1, products.size, "Seharusnya ada 1 produk")

        // Cek 2: Datanya harus sesuai dengan yang diinput
        val savedProduct = products[0]
        assertEquals(namaProduk, savedProduct.name)
        assertNull(savedProduct.code, "Kode produk seharusnya null")
    }

    @Test
    fun `test addProduct inserts multiple products correctly`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        val product1Name = "Pasir Silika"
        val product1Code = "PRD-001"
        val product2Name = "Batu Kerikil"
        val product2Code = "PRD-002"
        val product3Name = "Semen Portland"

        // --- 2. WHEN (Aksi dilakukan) ---
        // Insert beberapa produk
        repository.addProduct(product1Name, product1Code)
        repository.addProduct(product2Name, product2Code)
        repository.addProduct(product3Name, null)

        // --- 3. THEN (Verifikasi Hasil) ---
        val products = repository.getAllProducts().first()

        // Cek: Jumlah data harus 3
        assertEquals(3, products.size, "Seharusnya ada 3 produk")

        // Verifikasi data masing-masing produk
        val productNames = products.map { it.name }
        assertTrue(product1Name in productNames, "Produk 1 harus ada dalam list")
        assertTrue(product2Name in productNames, "Produk 2 harus ada dalam list")
        assertTrue(product3Name in productNames, "Produk 3 harus ada dalam list")
    }

    @Test
    fun `test product has unique auto-generated id`() = runTest {
        // --- 1. GIVEN & WHEN ---
        repository.addProduct("Produk A", "CODE-A")
        repository.addProduct("Produk B", "CODE-B")

        // --- 2. THEN ---
        val products = repository.getAllProducts().first()

        // Cek: ID harus unik
        val ids = products.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "Semua ID harus unik")
    }

    @Test
    fun `test products with same name but different codes are allowed`() = runTest {
        // --- 1. GIVEN (Kondisi Awal) ---
        val sameName = "Pasir"
        val code1 = "PASIR-HALUS"
        val code2 = "PASIR-KASAR"

        // --- 2. WHEN (Aksi dilakukan) ---
        repository.addProduct(sameName, code1)
        repository.addProduct(sameName, code2)

        // --- 3. THEN (Verifikasi Hasil) ---
        val products = repository.getAllProducts().first()

        assertEquals(2, products.size, "Seharusnya ada 2 produk dengan nama yang sama")

        val codes = products.map { it.code }
        assertTrue(code1 in codes, "Kode 1 harus ada")
        assertTrue(code2 in codes, "Kode 2 harus ada")
    }
}
