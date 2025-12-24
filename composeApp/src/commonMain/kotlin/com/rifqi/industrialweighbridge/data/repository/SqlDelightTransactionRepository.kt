package com.rifqi.industrialweighbridge.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.db.WeighbridgeDatabase
import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import com.rifqi.industrialweighbridge.domain.utils.DateTimeUtils
import com.rifqi.industrialweighbridge.engine.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class SqlDelightTransactionRepository(db: WeighbridgeDatabase) : TransactionRepository {

    private val queries = db.weighbridgeQueries

    override fun getAllTransactions(): Flow<List<SelectAllTransactions>> {
        return queries.selectAllTransactions().asFlow().mapToList(Dispatchers.IO)
    }

    override fun getOpenTransactions(): Flow<List<SelectOpenTransactions>> {
        return queries.selectOpenTransactions().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun createWeighIn(
            ticket: String,
            vehicleId: Long,
            driverId: Long,
            productId: Long,
            partnerId: Long?,
            weight: Double,
            isManual: Boolean,
            transactionType: TransactionType
    ) {
        val currentTimestamp = DateTimeUtils.nowIsoString()

        queries.insertWeighIn(
                ticket_number = ticket,
                vehicle_id = vehicleId,
                driver_id = driverId,
                product_id = productId,
                partner_id = partnerId,
                user_id = null, // TODO: Set when authentication is implemented
                weigh_in_timestamp = currentTimestamp,
                weigh_in_weight = weight,
                is_manual = if (isManual) 1L else 0L,
                transaction_type = transactionType
        )
    }

    override suspend fun updateWeighOut(ticket: String, exitWeight: Double, netWeight: Double) {
        val currentTimestamp = DateTimeUtils.nowIsoString()

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
