package com.rifqi.industrialweighbridge.engine

/**
 * Represents the current state of the weighing system.
 *
 * State machine transitions : Idle -> WeighingIn (when starting weigh-in) WeighingIn -> Idle (when
 * weigh-in captured successfully, transaction created) Idle -> WeighingOut (when selecting active
 * transaction for weigh-out) WeighingOut -> Completed (when weigh-out captured and transaction
 * closed) Completed -> Idle (after displaying results/printing ticket)
 *
 * Any state -> Error (on failure) Error -> Idle (after error acknowledged)
 */
sealed class WeighingState {

        /** System is idle, waiting for user action. No active weighing operation in progress. */
        data object Idle : WeighingState()

        /**
         * System is in weigh-in mode (first weighing). Waiting for weight to stabilize and be
         * captured.
         */
        data class WeighingIn(
                val selectedVehicleId: Long,
                val selectedDriverId: Long,
                val selectedProductId: Long,
                val selectedPartnerId: Long?,
                val transactionType: TransactionType,
                val currentWeight: Double = 0.0,
                val isStable: Boolean = false,
                val isManualMode: Boolean = false
        ) : WeighingState()

        /**
         * System is in weigh-out mode (second weighing). Linked to an existing open transaction.
         */
        data class WeighingOut(
                val ticketNumber: String,
                val firstWeight: Double,
                val transactionType: TransactionType,
                val currentWeight: Double = 0.0,
                val isStable: Boolean = false,
                val isManualMode: Boolean = false,
                val vehicleId: Long,
                val driverId: Long,
                val productId: Long,
                val partnerId: Long?
        ) : WeighingState()

        /** Transaction completed successfully. Contains all final data for display/printing. */
        data class Completed(
                val ticketNumber: String,
                val grossWeight: Double,
                val tareWeight: Double,
                val netWeight: Double,
                val transactionType: TransactionType,
                val completedAtMillis: Long = System.currentTimeMillis()
        ) : WeighingState()

        /** An error occurred during weighing process. */
        data class Error(
                val message: String,
                val errorType: ErrorType,
                val previousState: WeighingState? = null
        ) : WeighingState()
}

/** Types of errors that can occur during weighing. */
enum class ErrorType {
        /** Device connection lost or unavailable */
        DEVICE_DISCONNECTED,

        /** Weight reading is unstable */
        UNSTABLE_WEIGHT,

        /** Invalid or corrupted weight data */
        INVALID_DATA,

        /** Business rule violation */
        BUSINESS_RULE_VIOLATION,

        /** Generic/unknown error */
        UNKNOWN
}
