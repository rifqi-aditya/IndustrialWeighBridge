package com.rifqi.industrialweighbridge.domain.usecase.transaction

import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import com.rifqi.industrialweighbridge.domain.utils.TicketGenerator

/**
 * Use case for creating a Weigh-In transaction.
 *
 * Validations:
 * - Weight must be >= minimum threshold (50 kg default)
 * - Vehicle, Driver, Product must be selected
 */
class CreateWeighInUseCase(private val repository: TransactionRepository) {
    companion object {
        const val MIN_WEIGHT_KG = 50.0
    }

    /**
     * Creates a new weigh-in transaction.
     *
     * @param vehicleId Selected vehicle ID
     * @param driverId Selected driver ID
     * @param productId Selected product ID
     * @param weight Weight in kg
     * @param isManual Whether this is manual input (true) or auto from scale (false)
     * @return Result containing ticket number on success, or error message on failure
     */
    suspend operator fun invoke(
            vehicleId: Long,
            driverId: Long,
            productId: Long,
            weight: Double,
            isManual: Boolean = true
    ): Result<String> {
        // Validation
        if (vehicleId <= 0) {
            return Result.failure(IllegalArgumentException("Pilih kendaraan terlebih dahulu"))
        }

        if (driverId <= 0) {
            return Result.failure(IllegalArgumentException("Pilih driver terlebih dahulu"))
        }

        if (productId <= 0) {
            return Result.failure(IllegalArgumentException("Pilih produk terlebih dahulu"))
        }

        if (weight < MIN_WEIGHT_KG) {
            return Result.failure(IllegalArgumentException("Berat minimal $MIN_WEIGHT_KG kg"))
        }

        return try {
            val ticket = TicketGenerator.generate()

            repository.createWeighIn(
                    ticket = ticket,
                    vehicleId = vehicleId,
                    driverId = driverId,
                    productId = productId,
                    weight = weight,
                    isManual = isManual
            )

            Result.success(ticket)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
