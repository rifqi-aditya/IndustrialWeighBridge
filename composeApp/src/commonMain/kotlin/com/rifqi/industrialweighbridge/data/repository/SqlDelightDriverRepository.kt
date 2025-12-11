package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rifqi.industrialweighbridge.db.Driver
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.domain.repository.DriverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SqlDelightDriverRepository(
    db: WeighbridgeDatabase
) : DriverRepository {

    private val queries = db.weighbridgeQueries

    override fun getAllDrivers(): Flow<List<Driver>> {
        return queries.selectAllDrivers()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun addDriver(name: String, licenseNo: String?) {
        queries.insertDriver(name, licenseNo)
    }

    override suspend fun deleteDriver(id: Long) {
        // Implementasi delete nanti
    }
}