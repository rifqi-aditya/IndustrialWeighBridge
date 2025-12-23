package com.rifqi.industrialweighbridge.engine

import com.rifqi.industrialweighbridge.domain.repository.TransactionRepository
import com.rifqi.industrialweighbridge.domain.utils.TicketGenerator
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core Weighing Engine - The central controller for all weighing operations.
 *
 * According to HLD Section 3.2: "Core Weighing Engine merupakan pusat logika bisnis dan pengendali
 * utama seluruh siklus penimbangan. Modul ini berperan sebagai otoritas tunggal dalam pengambilan
 * keputusan dan pengelolaan status transaksi."
 *
 * Responsibilities:
 * - Manages transaction state (Idle, Weigh-In, Weigh-Out, Completed)
 * - Processes raw weight data from Infrastructure Layer
 * - Determines weight stability
 * - Captures and locks weight values
 * - Determines weight role (Gross/Tare) based on transaction context
 * - Calculates Net Weight
 * - Orchestrates Data Layer and Infrastructure Layer interactions
 *
 * Constraints:
 * - No other module can modify transaction state
 * - All Data Layer and Infrastructure Layer access must go through this Engine
 * - Engine is independent of UI or specific device implementations
 */
class WeighingEngine(
        private val transactionRepository: TransactionRepository,
        private val stabilityConfig: StabilityConfig = StabilityConfig()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // State management
    private val _state = MutableStateFlow<WeighingState>(WeighingState.Idle)
    val state: StateFlow<WeighingState> = _state.asStateFlow()

    // Stability detector for current operation
    private var stabilityDetector = StabilityDetector(stabilityConfig)

    // Current weight reading (real-time from device)
    private val _currentWeight = MutableStateFlow(0.0)
    val currentWeight: StateFlow<Double> = _currentWeight.asStateFlow()

    // Stability status
    private val _isStable = MutableStateFlow(false)
    val isStable: StateFlow<Boolean> = _isStable.asStateFlow()

    // Manual mode flag
    private val _isManualMode = MutableStateFlow(false)
    val isManualMode: StateFlow<Boolean> = _isManualMode.asStateFlow()

    // Error message for display
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success message for display
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
     * Updates the current weight reading from the scale device. Called by Infrastructure Layer when
     * new weight data is received.
     *
     * In manual mode, this is set directly by user input.
     */
    fun updateWeight(weight: Double) {
        _currentWeight.value = weight

        // Add to stability detector if not in manual mode
        if (!_isManualMode.value) {
            _isStable.value = stabilityDetector.addReading(weight)
        }

        // Update state with current weight
        updateCurrentStateWeight(weight, _isStable.value)
    }

    /** Sets weight directly in manual mode. Bypasses stability detection. */
    fun setManualWeight(weight: Double) {
        if (_isManualMode.value) {
            _currentWeight.value = weight
            _isStable.value = true // Manual entry is always considered "stable"
            updateCurrentStateWeight(weight, true)
        }
    }

    /**
     * Enables or disables manual input mode. According to HLD, this should require authorization
     * and be logged.
     */
    fun setManualMode(enabled: Boolean) {
        _isManualMode.value = enabled
        stabilityDetector.reset()
        _isStable.value = false
    }

    // === State Transitions ===

    /** Starts a new weigh-in operation. Transitions to WeighingIn state. */
    fun startWeighIn(request: WeighInRequest): WeighingResult<Unit> {
        val currentState = _state.value

        // Allow from Idle, Completed, Error, or same WeighingIn (retry)
        val allowedStates =
                currentState is WeighingState.Idle ||
                        currentState is WeighingState.Completed ||
                        currentState is WeighingState.Error ||
                        currentState is WeighingState.WeighingIn

        if (!allowedStates) {
            return WeighingResult.Failure(
                    "Tidak dapat memulai penimbangan. Selesaikan transaksi aktif terlebih dahulu.",
                    ErrorType.BUSINESS_RULE_VIOLATION
            )
        }

        // Reset stability detector for new operation
        stabilityDetector.reset()
        _isStable.value = false
        _isManualMode.value = request.isManualMode

        _state.value =
                WeighingState.WeighingIn(
                        selectedVehicleId = request.vehicleId,
                        selectedDriverId = request.driverId,
                        selectedProductId = request.productId,
                        transactionType = request.transactionType,
                        currentWeight = _currentWeight.value,
                        isStable = false,
                        isManualMode = request.isManualMode
                )

        return WeighingResult.Success(Unit)
    }

    /**
     * Starts a weigh-out operation for an existing transaction. Transitions to WeighingOut state.
     */
    fun startWeighOut(request: WeighOutRequest): WeighingResult<Unit> {
        val currentState = _state.value

        // Allow from Idle, Completed, Error, or same WeighingOut (retry)
        val allowedStates =
                currentState is WeighingState.Idle ||
                        currentState is WeighingState.Completed ||
                        currentState is WeighingState.Error ||
                        currentState is WeighingState.WeighingOut

        if (!allowedStates) {
            return WeighingResult.Failure(
                    "Tidak dapat memulai penimbangan. Selesaikan transaksi aktif terlebih dahulu.",
                    ErrorType.BUSINESS_RULE_VIOLATION
            )
        }

        // Reset stability detector for new operation
        stabilityDetector.reset()
        _isStable.value = false
        _isManualMode.value = request.isManualMode

        _state.value =
                WeighingState.WeighingOut(
                        ticketNumber = request.ticketNumber,
                        firstWeight = request.firstWeight,
                        transactionType = request.transactionType,
                        currentWeight = _currentWeight.value,
                        isStable = false,
                        isManualMode = request.isManualMode,
                        vehicleId = request.vehicleId,
                        driverId = request.driverId,
                        productId = request.productId
                )

        return WeighingResult.Success(Unit)
    }

    /**
     * Captures the current weight for weigh-in and creates a new transaction. According to HLD:
     * "Mengunci nilai berat sebagai First Weight"
     */
    suspend fun captureWeighIn(): WeighingResult<String> {
        val currentState = _state.value

        if (currentState !is WeighingState.WeighingIn) {
            return WeighingResult.Failure("Not in weigh-in mode", ErrorType.BUSINESS_RULE_VIOLATION)
        }

        // Check stability (or manual mode)
        if (!_isStable.value && !currentState.isManualMode) {
            return WeighingResult.Failure(
                    "Berat belum stabil. Tunggu hingga stabil sebelum capture.",
                    ErrorType.UNSTABLE_WEIGHT
            )
        }

        val weight = _currentWeight.value

        // Validate minimum weight
        if (weight < stabilityConfig.minimumWeightKg) {
            return WeighingResult.Failure(
                    "Berat minimum ${stabilityConfig.minimumWeightKg} kg",
                    ErrorType.BUSINESS_RULE_VIOLATION
            )
        }

        return try {
            // Generate ticket number
            val ticketNumber = TicketGenerator.generate()

            // Save to Data Layer
            transactionRepository.createWeighIn(
                    ticket = ticketNumber,
                    vehicleId = currentState.selectedVehicleId,
                    driverId = currentState.selectedDriverId,
                    productId = currentState.selectedProductId,
                    weight = weight,
                    isManual = currentState.isManualMode
            )

            // Reset to Idle
            _state.value = WeighingState.Idle
            _successMessage.value = "Weigh-In berhasil! Tiket: $ticketNumber"

            WeighingResult.Success(ticketNumber)
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Gagal menyimpan transaksi"
            WeighingResult.Failure(e.message ?: "Unknown error", ErrorType.UNKNOWN)
        }
    }

    /**
     * Captures the current weight for weigh-out and completes the transaction. According to HLD:
     * - Determines Gross/Tare based on transaction type
     * - Calculates Net Weight
     * - Closes the transaction
     */
    suspend fun captureWeighOut(): WeighingResult<CompletedTransaction> {
        val currentState = _state.value

        if (currentState !is WeighingState.WeighingOut) {
            return WeighingResult.Failure(
                    "Not in weigh-out mode",
                    ErrorType.BUSINESS_RULE_VIOLATION
            )
        }

        // Check stability (or manual mode)
        if (!_isStable.value && !currentState.isManualMode) {
            return WeighingResult.Failure(
                    "Berat belum stabil. Tunggu hingga stabil sebelum capture.",
                    ErrorType.UNSTABLE_WEIGHT
            )
        }

        val secondWeight = _currentWeight.value
        val firstWeight = currentState.firstWeight

        // Validate minimum weight
        if (secondWeight < stabilityConfig.minimumWeightKg) {
            return WeighingResult.Failure(
                    "Berat minimum ${stabilityConfig.minimumWeightKg} kg",
                    ErrorType.BUSINESS_RULE_VIOLATION
            )
        }

        // Determine Gross and Tare based on transaction type
        val (grossWeight, tareWeight) =
                when (currentState.transactionType) {
                    TransactionType.INBOUND -> {
                        // Inbound: First = Gross, Second = Tare
                        Pair(firstWeight, secondWeight)
                    }
                    TransactionType.OUTBOUND -> {
                        // Outbound: First = Tare, Second = Gross
                        Pair(secondWeight, firstWeight)
                    }
                }

        // Calculate Net Weight (absolute difference)
        val netWeight = abs(grossWeight - tareWeight)

        // Validate net weight
        if (netWeight <= 0) {
            return WeighingResult.Failure(
                    "Net weight tidak boleh nol atau negatif",
                    ErrorType.BUSINESS_RULE_VIOLATION
            )
        }

        return try {
            // Update in Data Layer
            transactionRepository.updateWeighOut(
                    ticket = currentState.ticketNumber,
                    exitWeight = secondWeight,
                    netWeight = netWeight
            )

            val completedTransaction =
                    CompletedTransaction(
                            ticketNumber = currentState.ticketNumber,
                            vehicleId = currentState.vehicleId,
                            driverId = currentState.driverId,
                            productId = currentState.productId,
                            grossWeight = grossWeight,
                            tareWeight = tareWeight,
                            netWeight = netWeight,
                            transactionType = currentState.transactionType,
                            isManualEntry = currentState.isManualMode
                    )

            // Transition to Completed state
            _state.value =
                    WeighingState.Completed(
                            ticketNumber = currentState.ticketNumber,
                            grossWeight = grossWeight,
                            tareWeight = tareWeight,
                            netWeight = netWeight,
                            transactionType = currentState.transactionType
                            // completedAtMillis uses default System.currentTimeMillis()
                            )

            _successMessage.value = "Transaksi selesai! Net: $netWeight kg"

            WeighingResult.Success(completedTransaction)
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Gagal menyelesaikan transaksi"
            WeighingResult.Failure(e.message ?: "Unknown error", ErrorType.UNKNOWN)
        }
    }

    /** Cancels the current operation and returns to Idle. */
    fun cancelOperation() {
        _state.value = WeighingState.Idle
        stabilityDetector.reset()
        _isStable.value = false
        _isManualMode.value = false
    }

    /** Acknowledges completed state and returns to Idle. */
    fun acknowledgeCompletion() {
        if (_state.value is WeighingState.Completed) {
            _state.value = WeighingState.Idle
        }
    }

    /** Clears any error message. */
    fun clearError() {
        _errorMessage.value = null

        // If in error state, transition back to previous or Idle
        val currentState = _state.value
        if (currentState is WeighingState.Error) {
            _state.value = currentState.previousState ?: WeighingState.Idle
        }
    }

    /** Clears success message. */
    fun clearSuccess() {
        _successMessage.value = null
    }

    // === Helper Functions ===

    private fun updateCurrentStateWeight(weight: Double, isStable: Boolean) {
        when (val currentState = _state.value) {
            is WeighingState.WeighingIn -> {
                _state.value = currentState.copy(currentWeight = weight, isStable = isStable)
            }
            is WeighingState.WeighingOut -> {
                _state.value = currentState.copy(currentWeight = weight, isStable = isStable)
            }
            else -> {
                /* No weight update needed for other states */
            }
        }
    }
}
