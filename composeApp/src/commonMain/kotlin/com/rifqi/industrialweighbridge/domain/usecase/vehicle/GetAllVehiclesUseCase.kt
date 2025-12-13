package com.rifqi.industrialweighbridge.domain.usecase.vehicle

import com.rifqi.industrialweighbridge.db.Vehicle
import com.rifqi.industrialweighbridge.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow

class GetAllVehiclesUseCase(private val repository: VehicleRepository) {
    operator fun invoke(): Flow<List<Vehicle>> = repository.getAllVehicles()
}
