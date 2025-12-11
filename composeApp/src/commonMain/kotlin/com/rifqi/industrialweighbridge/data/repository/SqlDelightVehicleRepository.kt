package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rifqi.industrialweighbridge.db.Vehicle
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.domain.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SqlDelightVehicleRepository(
    db: WeighbridgeDatabase
) : VehicleRepository {

    private val queries = db.weighbridgeQueries

    override fun getAllVehicles(): Flow<List<Vehicle>> {
        return queries.selectAllVehicles()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun addVehicle(plateNumber: String, desc: String?, tare: Double?) {
        queries.insertVehicle(plateNumber, desc, tare)
    }

    override suspend fun deleteVehicle(id: Long) {
        // Implementasi delete jika query sudah ada di .sq
        // queries.deleteVehicle(id)
    }
}