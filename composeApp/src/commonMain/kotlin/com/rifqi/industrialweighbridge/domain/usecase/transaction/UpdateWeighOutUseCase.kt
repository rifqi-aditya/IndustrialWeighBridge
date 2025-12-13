package com.rifqi.industrialweighbridge.domain.usecase.transaction

import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository

/**
 * Use case for completing a Weigh-Out transaction.
 *
 * Calculates net weight = exit weight - entry weight
 */
class UpdateWeighOutUseCase(private val repository: TransactionRepository) {
    companion object {
        const val MIN_WEIGHT_KG = 50.0
    }

    /**
     * Completes a weigh-out transaction.
     *
     * @param ticketNumber The ticket to complete
     * @param entryWeight The original weigh-in weight
     * @param exitWeight The weigh-out weight
     * @return Result with net weight on success, or error message on failure
     */
    suspend operator fun invoke(
            ticketNumber: String,
            entryWeight: Double,
            exitWeight: Double
    ): Result<Double> {
        // Validation
        if (ticketNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Ticket number tidak valid"))
        }

        if (exitWeight < MIN_WEIGHT_KG) {
            return Result.failure(IllegalArgumentException("Berat minimal $MIN_WEIGHT_KG kg"))
        }

        // Calculate net weight (absolute difference)
        // In weighbridge:
        // - If entry > exit: Vehicle brought goods (entry is gross, exit is tare)
        // - If exit > entry: Vehicle took goods (entry is tare, exit is gross)
        val netWeight = kotlin.math.abs(exitWeight - entryWeight)

        return try {
            repository.updateWeighOut(
                    ticket = ticketNumber,
                    exitWeight = exitWeight,
                    netWeight = netWeight
            )

            Result.success(netWeight)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
