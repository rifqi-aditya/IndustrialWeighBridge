package com.rifqi.industrialweighbridge.domain.repository

import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.engine.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    // Mengambil semua history transaksi (Lengkap dengan nama supir & plat nomor)
    fun getAllTransactions(): Flow<List<SelectAllTransactions>>

    // Mengambil transaksi yang statusnya masih OPEN (Kendaraan masih di dalam)
    fun getOpenTransactions(): Flow<List<SelectOpenTransactions>>

    // Proses Weigh In (Masuk)
    suspend fun createWeighIn(
            ticket: String,
            vehicleId: Long,
            driverId: Long,
            productId: Long,
            partnerId: Long?,
            weight: Double,
            isManual: Boolean,
            transactionType: TransactionType,
            poDoNumber: String? = null
    )

    // Proses Weigh Out (Keluar)
    suspend fun updateWeighOut(ticket: String, exitWeight: Double, netWeight: Double)

    // Hapus Transaksi (Optional/Admin Only)
    suspend fun deleteTransaction(ticket: String)
}
