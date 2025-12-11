package com.rifqi.industrialweighbridge.domain.repository

import com.rifqi.industrialweighbridge.db.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun addProduct(name: String, code: String?)
    suspend fun deleteProduct(id: Long)
}