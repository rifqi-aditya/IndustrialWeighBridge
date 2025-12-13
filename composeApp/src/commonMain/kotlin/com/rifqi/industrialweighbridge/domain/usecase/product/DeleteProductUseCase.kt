package com.rifqi.industrialweighbridge.domain.usecase.product

import com.rifqi.industrialweighbridge.domain.repository.ProductRepository

class DeleteProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteProduct(id)
    }
}
