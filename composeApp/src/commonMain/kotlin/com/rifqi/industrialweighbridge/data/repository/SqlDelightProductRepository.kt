package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rifqi.industrialweighbridge.db.Product
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SqlDelightProductRepository(
    db: WeighbridgeDatabase
) : ProductRepository {

    private val queries = db.weighbridgeQueries

    override fun getAllProducts(): Flow<List<Product>> {
        return queries.selectAllProducts()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun addProduct(name: String, code: String?) {
        queries.insertProduct(name, code)
    }

    override suspend fun deleteProduct(id: Long) {
        // Implementasi delete nanti
    }
}