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

class SqlDelightProductRepositoryTest {

    private lateinit var database: WeighbridgeDatabase
    private lateinit var repository: SqlDelightProductRepository

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

    // ==========================================
    // TEST CASES: updateProduct
    // ==========================================

    @Test
    fun `test updateProduct updates name correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addProduct("Produk Lama", "PRD-001")
        val products = repository.getAllProducts().first()
        val productId = products[0].id

        // --- 2. WHEN ---
        repository.updateProduct(productId, "Produk Baru", "PRD-001")

        // --- 3. THEN ---
        val updatedProducts = repository.getAllProducts().first()
        assertEquals(1, updatedProducts.size, "Jumlah produk harus tetap 1")
        assertEquals("Produk Baru", updatedProducts[0].name, "Nama harus terupdate")
        assertEquals("PRD-001", updatedProducts[0].code, "Code harus tetap sama")
    }

    @Test
    fun `test updateProduct updates code correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addProduct("Pasir", "OLD-CODE")
        val products = repository.getAllProducts().first()
        val productId = products[0].id

        // --- 2. WHEN ---
        repository.updateProduct(productId, "Pasir", "NEW-CODE")

        // --- 3. THEN ---
        val updatedProducts = repository.getAllProducts().first()
        assertEquals("Pasir", updatedProducts[0].name, "Nama harus tetap sama")
        assertEquals("NEW-CODE", updatedProducts[0].code, "Code harus terupdate")
    }

    @Test
    fun `test updateProduct updates both name and code`() = runTest {
        // --- 1. GIVEN ---
        repository.addProduct("Nama Lama", "CODE-LAMA")
        val products = repository.getAllProducts().first()
        val productId = products[0].id

        // --- 2. WHEN ---
        repository.updateProduct(productId, "Nama Baru", "CODE-BARU")

        // --- 3. THEN ---
        val updatedProducts = repository.getAllProducts().first()
        assertEquals("Nama Baru", updatedProducts[0].name)
        assertEquals("CODE-BARU", updatedProducts[0].code)
    }

    @Test
    fun `test updateProduct can set code to null`() = runTest {
        // --- 1. GIVEN ---
        repository.addProduct("Produk", "PRD-123")
        val products = repository.getAllProducts().first()
        val productId = products[0].id

        // --- 2. WHEN ---
        repository.updateProduct(productId, "Produk", null)

        // --- 3. THEN ---
        val updatedProducts = repository.getAllProducts().first()
        assertNull(updatedProducts[0].code, "Code harus bisa diset ke null")
    }

    @Test
    fun `test updateProduct only affects specified product`() = runTest {
        // --- 1. GIVEN ---
        repository.addProduct("Produk A", "CODE-A")
        repository.addProduct("Produk B", "CODE-B")
        val products = repository.getAllProducts().first()
        val productAId = products.find { it.name == "Produk A" }!!.id

        // --- 2. WHEN ---
        repository.updateProduct(productAId, "Produk A Updated", "CODE-A-NEW")

        // --- 3. THEN ---
        val updatedProducts = repository.getAllProducts().first()
        val productA = updatedProducts.find { it.id == productAId }!!
        val productB = updatedProducts.find { it.name == "Produk B" }!!

        assertEquals("Produk A Updated", productA.name, "Produk A harus terupdate")
        assertEquals("Produk B", productB.name, "Produk B harus tetap sama")
        assertEquals("CODE-B", productB.code, "Produk B code harus tetap sama")
    }

    // ==========================================
    // TEST CASES: deleteProduct
    // ==========================================

    @Test
    fun `test deleteProduct removes product correctly`() = runTest {
        // --- 1. GIVEN ---
        repository.addProduct("Produk Akan Dihapus", "PRD-DEL")
        val products = repository.getAllProducts().first()
        assertEquals(1, products.size)
        val productId = products[0].id

        // --- 2. WHEN ---
        repository.deleteProduct(productId)

        // --- 3. THEN ---
        val remainingProducts = repository.getAllProducts().first()
        assertTrue(remainingProducts.isEmpty(), "Database harus kosong setelah delete")
    }

    @Test
    fun `test deleteProduct only removes specified product`() = runTest {
        // --- 1. GIVEN ---
        repository.addProduct("Produk Keep 1", "PRD-1")
        repository.addProduct("Produk Delete", "PRD-DEL")
        repository.addProduct("Produk Keep 2", "PRD-2")
        val products = repository.getAllProducts().first()
        assertEquals(3, products.size)
        val deleteId = products.find { it.name == "Produk Delete" }!!.id

        // --- 2. WHEN ---
        repository.deleteProduct(deleteId)

        // --- 3. THEN ---
        val remainingProducts = repository.getAllProducts().first()
        assertEquals(2, remainingProducts.size, "Harus tersisa 2 produk")

        val names = remainingProducts.map { it.name }
        assertTrue("Produk Keep 1" in names, "Produk Keep 1 harus tetap ada")
        assertTrue("Produk Keep 2" in names, "Produk Keep 2 harus tetap ada")
        assertTrue("Produk Delete" !in names, "Produk Delete harus sudah terhapus")
    }

    @Test
    fun `test deleteProduct with non-existent id does not affect data`() = runTest {
        // --- 1. GIVEN ---
        repository.addProduct("Produk Tetap", "PRD-1")
        val products = repository.getAllProducts().first()
        assertEquals(1, products.size)

        // --- 2. WHEN ---
        repository.deleteProduct(99999L) // ID yang tidak ada

        // --- 3. THEN ---
        val remainingProducts = repository.getAllProducts().first()
        assertEquals(1, remainingProducts.size, "Data harus tetap ada")
    }
}
