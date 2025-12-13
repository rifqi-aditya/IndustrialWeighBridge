package com.rifqi.industrialweighbridge.domain.usecase.vehicle

import com.rifqi.industrialweighbridge.domain.repository.VehicleRepository

class DeleteVehicleUseCase(private val repository: VehicleRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteVehicle(id)
    }
}
