package com.rifqi.industrialweighbridge.domain.repository

import com.rifqi.industrialweighbridge.db.Driver
import kotlinx.coroutines.flow.Flow

interface DriverRepository {
    fun getAllDrivers(): Flow<List<Driver>>
    suspend fun addDriver(name: String, licenseNo: String?)
    suspend fun deleteDriver(id: Long)
}