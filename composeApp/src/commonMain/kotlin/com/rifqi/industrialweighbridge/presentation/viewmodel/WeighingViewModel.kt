package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.Driver
import com.rifqi.industrialweighbridge.db.Product
import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.db.Vehicle
import com.rifqi.industrialweighbridge.domain.usecase.driver.GetAllDriversUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.GetAllProductsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.GetAllTransactionsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.GetOpenTransactionsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.GetAllVehiclesUseCase
import com.rifqi.industrialweighbridge.engine.TransactionType
import com.rifqi.industrialweighbridge.engine.WeighInRequest
import com.rifqi.industrialweighbridge.engine.WeighOutRequest
import com.rifqi.industrialweighbridge.engine.WeighingEngine
import com.rifqi.industrialweighbridge.engine.WeighingResult
import com.rifqi.industrialweighbridge.engine.WeighingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for Weighing Screen.
 *
 * According to HLD Section 3.1: "Presentation Layer hanya berfungsi sebagai command initiator dan
 * state renderer"
 */
data class WeighingUiState(
        // Weight display (from Engine)
        val currentWeight: Double = 0.0,
        val isStable: Boolean = false,
        val isManualMode: Boolean = true,

        // Engine State
        val engineState: WeighingState = WeighingState.Idle,

        // Selections (UI only)
        val selectedVehicleId: Long? = null,
        val selectedDriverId: Long? = null,
        val selectedProductId: Long? = null,
        val selectedTransactionType: TransactionType = TransactionType.INBOUND,

        // Data lists
        val vehicles: List<Vehicle> = emptyList(),
        val drivers: List<Driver> = emptyList(),
        val products: List<Product> = emptyList(),
        val openTransactions: List<SelectOpenTransactions> = emptyList(),
        val allTransactions: List<SelectAllTransactions> = emptyList(),

        // UI state
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val successMessage: String? = null,
        val lastTicketNumber: String? = null
)

/**
 * ViewModel for Weighing operations.
 *
 * According to HLD Section 3.1 (Presentation Layer):
 * - Mengirimkan perintah pengguna (command) ke Core Weighing Engine
 * - Menampilkan status sistem dan hasil proses penimbangan
 * - Menyajikan error dan warning yang dihasilkan oleh Core Weighing Engine
 *
 * DOES NOT:
 * - Perform business logic validation (delegated to Engine)
 * - Determine weight validity (delegated to Engine)
 * - Manage transaction state (delegated to Engine)
 */
class WeighingViewModel(
        private val weighingEngine: WeighingEngine,
        private val getAllVehiclesUseCase: GetAllVehiclesUseCase,
        private val getAllDriversUseCase: GetAllDriversUseCase,
        private val getAllProductsUseCase: GetAllProductsUseCase,
        private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
        private val getOpenTransactionsUseCase: GetOpenTransactionsUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(WeighingUiState())
    val uiState: StateFlow<WeighingUiState> = _uiState.asStateFlow()

    init {
        loadMasterData()
        loadTransactions()
        observeEngine()
    }

    /** Observes state changes from WeighingEngine. Maps engine state to UI state. */
    private fun observeEngine() {
        // Observe engine state
        scope.launch {
            weighingEngine.state.collect { state ->
                _uiState.value = _uiState.value.copy(engineState = state)
            }
        }

        // Observe current weight
        scope.launch {
            weighingEngine.currentWeight.collect { weight ->
                _uiState.value = _uiState.value.copy(currentWeight = weight)
            }
        }

        // Observe stability
        scope.launch {
            weighingEngine.isStable.collect { stable ->
                _uiState.value = _uiState.value.copy(isStable = stable)
            }
        }

        // Observe manual mode
        scope.launch {
            weighingEngine.isManualMode.collect { manual ->
                _uiState.value = _uiState.value.copy(isManualMode = manual)
            }
        }

        // Observe error messages
        scope.launch {
            weighingEngine.errorMessage.collect { error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(errorMessage = error)
                }
            }
        }

        // Observe success messages
        scope.launch {
            weighingEngine.successMessage.collect { success ->
                if (success != null) {
                    _uiState.value = _uiState.value.copy(successMessage = success)
                }
            }
        }
    }

    private fun loadMasterData() {
        scope.launch {
            getAllVehiclesUseCase().collect { vehicles ->
                _uiState.value = _uiState.value.copy(vehicles = vehicles)
            }
        }
        scope.launch {
            getAllDriversUseCase().collect { drivers ->
                _uiState.value = _uiState.value.copy(drivers = drivers)
            }
        }
        scope.launch {
            getAllProductsUseCase().collect { products ->
                _uiState.value = _uiState.value.copy(products = products)
            }
        }
    }

    private fun loadTransactions() {
        scope.launch {
            getOpenTransactionsUseCase().collect { transactions ->
                _uiState.value = _uiState.value.copy(openTransactions = transactions)
            }
        }
        scope.launch {
            getAllTransactionsUseCase().collect { transactions ->
                _uiState.value = _uiState.value.copy(allTransactions = transactions)
            }
        }
    }

    // === User Commands - Delegated to Engine ===

    /** Sets weight value (for manual input mode). Command is sent to Engine. */
    fun setWeight(weight: Double) {
        if (_uiState.value.isManualMode) {
            weighingEngine.setManualWeight(weight)
        }
    }

    /** Toggles manual input mode. Command is sent to Engine. */
    fun toggleManualMode() {
        val newMode = !_uiState.value.isManualMode
        weighingEngine.setManualMode(newMode)
    }

    // === Selection Methods (UI State Only) ===

    fun selectVehicle(vehicleId: Long?) {
        _uiState.value = _uiState.value.copy(selectedVehicleId = vehicleId)
    }

    fun selectDriver(driverId: Long?) {
        _uiState.value = _uiState.value.copy(selectedDriverId = driverId)
    }

    fun selectProduct(productId: Long?) {
        _uiState.value = _uiState.value.copy(selectedProductId = productId)
    }

    fun selectTransactionType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedTransactionType = type)
    }

    // === Weigh-In Operation ===

    /** Starts weigh-in mode. Sends command to Engine to prepare for first weighing. */
    fun startWeighIn() {
        val state = _uiState.value

        // Validate UI selections before sending to engine
        if (state.selectedVehicleId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pilih kendaraan terlebih dahulu")
            return
        }
        if (state.selectedDriverId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pilih driver terlebih dahulu")
            return
        }
        if (state.selectedProductId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pilih produk terlebih dahulu")
            return
        }

        val result =
                weighingEngine.startWeighIn(
                        WeighInRequest(
                                vehicleId = state.selectedVehicleId,
                                driverId = state.selectedDriverId,
                                productId = state.selectedProductId,
                                transactionType = state.selectedTransactionType,
                                isManualMode = state.isManualMode
                        )
                )

        if (result is WeighingResult.Failure) {
            _uiState.value = _uiState.value.copy(errorMessage = result.error)
        }
    }

    /** Captures weight for weigh-in and creates transaction. Command is sent to Engine. */
    fun captureWeighIn() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = weighingEngine.captureWeighIn()) {
                is WeighingResult.Success -> {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    lastTicketNumber = result.data,
                                    // Reset selections after successful weigh-in
                                    selectedVehicleId = null,
                                    selectedDriverId = null,
                                    selectedProductId = null
                            )
                }
                is WeighingResult.Failure -> {
                    _uiState.value =
                            _uiState.value.copy(isLoading = false, errorMessage = result.error)
                }
            }
        }
    }

    // === Weigh-Out Operation ===

    /** Starts weigh-out mode for an existing open transaction. Sends command to Engine. */
    fun startWeighOut(
            ticketNumber: String,
            firstWeight: Double,
            transactionType: TransactionType = TransactionType.INBOUND
    ) {
        val openTransaction =
                _uiState.value.openTransactions.find { it.ticket_number == ticketNumber }

        val result =
                weighingEngine.startWeighOut(
                        WeighOutRequest(
                                ticketNumber = ticketNumber,
                                firstWeight = firstWeight,
                                transactionType = transactionType,
                                vehicleId = openTransaction?.vehicle_id ?: 0,
                                driverId = openTransaction?.driver_id ?: 0,
                                productId = openTransaction?.product_id ?: 0,
                                isManualMode = _uiState.value.isManualMode
                        )
                )

        if (result is WeighingResult.Failure) {
            _uiState.value = _uiState.value.copy(errorMessage = result.error)
        }
    }

    /** Captures weight for weigh-out and completes transaction. Command is sent to Engine. */
    fun captureWeighOut() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = weighingEngine.captureWeighOut()) {
                is WeighingResult.Success -> {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    successMessage =
                                            "Transaksi selesai! Net: ${result.data.netWeight} kg"
                            )
                }
                is WeighingResult.Failure -> {
                    _uiState.value =
                            _uiState.value.copy(isLoading = false, errorMessage = result.error)
                }
            }
        }
    }

    // === Legacy Methods for Backward Compatibility ===

    /** Performs weigh-in directly (legacy method). For backward compatibility with existing UI. */
    fun performWeighIn() {
        startWeighIn()
        // If already in WeighingIn state, capture
        if (_uiState.value.engineState is WeighingState.WeighingIn) {
            captureWeighIn()
        }
    }

    /** Performs weigh-out directly (legacy method). For backward compatibility with existing UI. */
    fun performWeighOut(ticketNumber: String, entryWeight: Double) {
        startWeighOut(ticketNumber, entryWeight)
        // If already in WeighingOut state, capture
        if (_uiState.value.engineState is WeighingState.WeighingOut) {
            captureWeighOut()
        }
    }

    // === Cancel & Clear Operations ===

    /** Cancels current weighing operation. */
    fun cancelOperation() {
        weighingEngine.cancelOperation()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        weighingEngine.clearError()
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
        weighingEngine.clearSuccess()
    }

    fun resetForm() {
        weighingEngine.cancelOperation()
        _uiState.value =
                _uiState.value.copy(
                        selectedVehicleId = null,
                        selectedDriverId = null,
                        selectedProductId = null,
                        errorMessage = null,
                        successMessage = null
                )
    }
}
