package com.rifqi.industrialweighbridge.domain.usecase.driver

import com.rifqi.industrialweighbridge.db.Driver
import com.rifqi.industrialweighbridge.domain.repository.DriverRepository
import kotlinx.coroutines.flow.Flow

class GetAllDriversUseCase(private val repository: DriverRepository) {
    operator fun invoke(): Flow<List<Driver>> = repository.getAllDrivers()
}
