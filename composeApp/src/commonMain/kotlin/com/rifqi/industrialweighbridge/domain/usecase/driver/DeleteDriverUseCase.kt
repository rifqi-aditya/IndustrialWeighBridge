package com.rifqi.industrialweighbridge.domain.usecase.driver

import com.rifqi.industrialweighbridge.domain.repository.DriverRepository

class DeleteDriverUseCase(private val repository: DriverRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteDriver(id)
    }
}
