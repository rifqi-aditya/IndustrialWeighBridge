package com.rifqi.industrialweighbridge.domain.usecase.product

import com.rifqi.industrialweighbridge.db.Product
import com.rifqi.industrialweighbridge.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow

class GetAllProductsUseCase(private val repository: ProductRepository) {
    operator fun invoke(): Flow<List<Product>> = repository.getAllProducts()
}
