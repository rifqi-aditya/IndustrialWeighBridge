package com.rifqi.industrialweighbridge.domain.usecase.driver

import com.rifqi.industrialweighbridge.domain.repository.DriverRepository

class UpdateDriverUseCase(private val repository: DriverRepository) {
    suspend operator fun invoke(id: Long, name: String, licenseNo: String?) {
        repository.updateDriver(id, name, licenseNo)
    }
}
