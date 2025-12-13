package com.rifqi.industrialweighbridge.domain.usecase.vehicle

import com.rifqi.industrialweighbridge.domain.repository.VehicleRepository

class UpdateVehicleUseCase(private val repository: VehicleRepository) {
    suspend operator fun invoke(id: Long, plateNumber: String, desc: String?, tare: Double?) {
        repository.updateVehicle(id, plateNumber, desc, tare)
    }
}
