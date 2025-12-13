package com.rifqi.industrialweighbridge.domain.usecase.product

import com.rifqi.industrialweighbridge.domain.repository.ProductRepository

class AddProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(name: String, code: String?) {
        repository.addProduct(name, code)
    }
}
