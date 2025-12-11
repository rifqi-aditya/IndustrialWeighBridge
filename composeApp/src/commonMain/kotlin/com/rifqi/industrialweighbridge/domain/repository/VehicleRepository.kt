package com.rifqi.industrialweighbridge.domain.repository

import com.rifqi.industrialweighbridge.db.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun getAllVehicles(): Flow<List<Vehicle>>
    suspend fun addVehicle(plateNumber: String, desc: String?, tare: Double?)
    suspend fun deleteVehicle(id: Long)
}