package com.rifqi.industrialweighbridge.domain.usecase.vehicle

import com.rifqi.industrialweighbridge.domain.repository.VehicleRepository
import kotlin.jvm.Throws

class AddVehicleUseCase(private val repository: VehicleRepository) {
    @Throws(Exception::class)
    suspend operator fun invoke(plateNumber: String, desc: String?, tare: Double?) {

        if (plateNumber == "") {
            throw Exception("Plat nomor wajib diisi")
        }

        val cleanPlate = plateNumber.trim().uppercase()

        repository.addVehicle(cleanPlate, desc, tare)
    }
}
