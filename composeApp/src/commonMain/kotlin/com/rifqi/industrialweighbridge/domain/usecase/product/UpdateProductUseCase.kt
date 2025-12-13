package com.rifqi.industrialweighbridge.domain.usecase.product

import com.rifqi.industrialweighbridge.domain.repository.ProductRepository

class UpdateProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(id: Long, name: String, code: String?) {
        repository.updateProduct(id, name, code)
    }
}
