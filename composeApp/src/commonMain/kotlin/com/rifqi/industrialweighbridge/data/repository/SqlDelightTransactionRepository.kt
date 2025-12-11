package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SqlDelightTransactionRepository(
    db: WeighbridgeDatabase
) : TransactionRepository {

    private val queries = db.weighbridgeQueries

    override fun getAllTransactions(): Flow<List<SelectAllTransactions>> {
        return queries.selectAllTransactions()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override fun getOpenTransactions(): Flow<List<SelectOpenTransactions>> {
        return queries.selectOpenTransactions()
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    override suspend fun createWeighIn(
        ticket: String,
        vehicleId: Long,
        driverId: Long,
        productId: Long,
        weight: Double,
        isManual: Boolean
    ) {
        // Timestamp kita hardcode dulu string-nya untuk tes
        // Nanti di Phase Logic kita ganti pakai Clock.System.now()
        val currentTimestamp = "2023-10-27T10:00:00"

        queries.insertWeighIn(
            ticket_number = ticket,
            vehicle_id = vehicleId,
            driver_id = driverId,
            product_id = productId,
            user_id = null, // Nanti diisi ID user login
            weigh_in_timestamp = currentTimestamp,
            weigh_in_weight = weight,
            is_manual = if (isManual) 1L else 0L
        )
    }

    override suspend fun updateWeighOut(
        ticket: String,
        exitWeight: Double,
        netWeight: Double
    ) {
        val currentTimestamp = "2023-10-27T12:00:00"

        queries.updateWeighOut(
            weigh_out_timestamp = currentTimestamp,
            weigh_out_weight = exitWeight,
            net_weight = netWeight,
            ticket_number = ticket
        )
    }

    override suspend fun deleteTransaction(ticket: String) {
        queries.deleteTransaction(ticket)
    }
}