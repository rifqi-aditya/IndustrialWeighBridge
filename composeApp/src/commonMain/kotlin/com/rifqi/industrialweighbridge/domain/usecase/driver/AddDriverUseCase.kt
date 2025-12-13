package com.rifqi.industrialweighbridge.domain.usecase.driver

import com.rifqi.industrialweighbridge.domain.repository.DriverRepository

class AddDriverUseCase(private val repository: DriverRepository) {
    suspend operator fun invoke(name: String, licenseNo: String?) {
        repository.addDriver(name, licenseNo)
    }
}
