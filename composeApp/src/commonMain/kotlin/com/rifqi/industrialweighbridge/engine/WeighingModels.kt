package com.rifqi.industrialweighbridge.engine

/** Result of a weighing engine operation. */
sealed class WeighingResult<out T> {
        data class Success<T>(val data: T) : WeighingResult<T>()
        data class Failure(val error: String, val errorType: ErrorType) : WeighingResult<Nothing>()
}

/** Data required to start a weigh-in operation. */
data class WeighInRequest(
        val vehicleId: Long,
        val driverId: Long,
        val productId: Long,
        val partnerId: Long?,
        val transactionType: TransactionType,
        val isManualMode: Boolean = false
)

/** Data required to start a weigh-out operation. */
data class WeighOutRequest(
        val ticketNumber: String,
        val firstWeight: Double,
        val transactionType: TransactionType,
        val vehicleId: Long,
        val driverId: Long,
        val productId: Long,
        val partnerId: Long?,
        val isManualMode: Boolean = false
)

/** Data for a completed transaction. */
data class CompletedTransaction(
        val ticketNumber: String,
        val vehicleId: Long,
        val driverId: Long,
        val productId: Long,
        val partnerId: Long?,
        val grossWeight: Double,
        val tareWeight: Double,
        val netWeight: Double,
        val transactionType: TransactionType,
        val isManualEntry: Boolean
)
