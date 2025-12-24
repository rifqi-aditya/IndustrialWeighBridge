package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.Driver
import com.rifqi.industrialweighbridge.db.Partner
import com.rifqi.industrialweighbridge.db.PartnerType
import com.rifqi.industrialweighbridge.db.Product
import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.db.Vehicle
import com.rifqi.industrialweighbridge.domain.usecase.driver.GetAllDriversUseCase
import com.rifqi.industrialweighbridge.domain.usecase.partner.GetPartnersByTypeUseCase
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
 * UI State for Weighing Screen. Sesuai HLD Section 3.1: Presentation Layer sebagai command
 * initiator dan state renderer.
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
        val selectedPartnerId: Long? = null,
        val selectedTransactionType: TransactionType = TransactionType.INBOUND,

        // Data lists
        val vehicles: List<Vehicle> = emptyList(),
        val drivers: List<Driver> = emptyList(),
        val products: List<Product> = emptyList(),
        val partners: List<Partner> = emptyList(),
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
 * Sesuai HLD Section 3.1 (Presentation Layer):
 * - Mengirimkan perintah pengguna (command) ke Core Weighing Engine
 * - Menampilkan status sistem dan hasil proses penimbangan
 * - Menyajikan error dan warning dari Core Weighing Engine
 */
class WeighingViewModel(
        private val weighingEngine: WeighingEngine,
        private val getAllVehiclesUseCase: GetAllVehiclesUseCase,
        private val getAllDriversUseCase: GetAllDriversUseCase,
        private val getAllProductsUseCase: GetAllProductsUseCase,
        private val getPartnersByTypeUseCase: GetPartnersByTypeUseCase,
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

    // === Engine State Observers ===

    private fun observeEngine() {
        scope.launch {
            weighingEngine.state.collect { state ->
                _uiState.value = _uiState.value.copy(engineState = state)
            }
        }
        scope.launch {
            weighingEngine.currentWeight.collect { weight ->
                _uiState.value = _uiState.value.copy(currentWeight = weight)
            }
        }
        scope.launch {
            weighingEngine.isStable.collect { stable ->
                _uiState.value = _uiState.value.copy(isStable = stable)
            }
        }
        scope.launch {
            weighingEngine.isManualMode.collect { manual ->
                _uiState.value = _uiState.value.copy(isManualMode = manual)
            }
        }
        scope.launch {
            weighingEngine.errorMessage.collect { error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(errorMessage = error)
                }
            }
        }
        scope.launch {
            weighingEngine.successMessage.collect { success ->
                if (success != null) {
                    _uiState.value = _uiState.value.copy(successMessage = success)
                }
            }
        }
    }

    // === Data Loading ===

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
        // Load partners based on current transaction type
        loadPartnersByTransactionType(_uiState.value.selectedTransactionType)
    }

    private fun loadPartnersByTransactionType(transactionType: TransactionType) {
        val partnerType =
                when (transactionType) {
                    TransactionType.INBOUND -> PartnerType.SUPPLIER
                    TransactionType.OUTBOUND -> PartnerType.CUSTOMER
                }
        scope.launch {
            getPartnersByTypeUseCase(partnerType).collect { partners ->
                _uiState.value = _uiState.value.copy(partners = partners, selectedPartnerId = null)
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

    // === User Commands ===

    /** Sets weight value (manual input mode). */
    fun setWeight(weight: Double) {
        if (_uiState.value.isManualMode) {
            weighingEngine.setManualWeight(weight)
        }
    }

    /** Toggles manual input mode. */
    fun toggleManualMode() {
        weighingEngine.setManualMode(!_uiState.value.isManualMode)
    }

    // === UI Selections ===

    fun selectVehicle(vehicleId: Long?) {
        _uiState.value = _uiState.value.copy(selectedVehicleId = vehicleId)
    }

    fun selectDriver(driverId: Long?) {
        _uiState.value = _uiState.value.copy(selectedDriverId = driverId)
    }

    fun selectProduct(productId: Long?) {
        _uiState.value = _uiState.value.copy(selectedProductId = productId)
    }

    fun selectPartner(partnerId: Long?) {
        _uiState.value = _uiState.value.copy(selectedPartnerId = partnerId)
    }

    fun selectTransactionType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedTransactionType = type)
        // Reload partners when transaction type changes
        loadPartnersByTransactionType(type)
    }

    // === Weighing Operations ===

    /**
     * Performs Weigh-In (Buka Transaksi / Penimbangan Pertama)
     * - Validates UI selections
     * - Starts weigh-in on Engine
     * - Captures weight and creates transaction
     */
    fun performWeighIn() {
        val state = _uiState.value

        // Validate UI selections
        if (state.selectedVehicleId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pilih kendaraan terlebih dahulu")
            return
        }
        if (state.selectedDriverId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pilih supir terlebih dahulu")
            return
        }
        if (state.selectedProductId == null) {
            _uiState.value =
                    _uiState.value.copy(errorMessage = "Pilih barang/material terlebih dahulu")
            return
        }
        if (state.currentWeight <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Masukkan berat terlebih dahulu")
            return
        }

        // Start weigh-in on Engine
        val startResult =
                weighingEngine.startWeighIn(
                        WeighInRequest(
                                vehicleId = state.selectedVehicleId,
                                driverId = state.selectedDriverId,
                                productId = state.selectedProductId,
                                partnerId = state.selectedPartnerId,
                                transactionType = state.selectedTransactionType,
                                isManualMode = state.isManualMode
                        )
                )

        if (startResult is WeighingResult.Failure) {
            _uiState.value = _uiState.value.copy(errorMessage = startResult.error)
            return
        }

        // Capture weight and save transaction
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = weighingEngine.captureWeighIn()) {
                is WeighingResult.Success -> {
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    successMessage = "Berat 1 tersimpan! Tiket: ${result.data}",
                                    lastTicketNumber = result.data,
                                    currentWeight = 0.0,
                                    selectedVehicleId = null,
                                    selectedDriverId = null,
                                    selectedProductId = null,
                                    selectedPartnerId = null
                            )
                }
                is WeighingResult.Failure -> {
                    _uiState.value =
                            _uiState.value.copy(isLoading = false, errorMessage = result.error)
                }
            }
        }
    }

    /**
     * Performs Weigh-Out (Tutup Transaksi / Penimbangan Kedua)
     * - Starts weigh-out on Engine with existing transaction
     * - Captures weight, calculates Gross/Tare/Net
     * - Closes transaction
     */
    fun performWeighOut(ticketNumber: String, entryWeight: Double) {
        val state = _uiState.value

        if (state.currentWeight <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Masukkan berat terlebih dahulu")
            return
        }

        val openTransaction = state.openTransactions.find { it.ticket_number == ticketNumber }

        // Start weigh-out on Engine
        val startResult =
                weighingEngine.startWeighOut(
                        WeighOutRequest(
                                ticketNumber = ticketNumber,
                                firstWeight = entryWeight,
                                transactionType = openTransaction?.transaction_type
                                                ?: TransactionType.INBOUND,
                                vehicleId = openTransaction?.vehicle_id ?: 0,
                                driverId = openTransaction?.driver_id ?: 0,
                                productId = openTransaction?.product_id ?: 0,
                                partnerId = openTransaction?.partner_id,
                                isManualMode = state.isManualMode
                        )
                )

        if (startResult is WeighingResult.Failure) {
            _uiState.value = _uiState.value.copy(errorMessage = startResult.error)
            return
        }

        // Capture weight and complete transaction
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = weighingEngine.captureWeighOut()) {
                is WeighingResult.Success -> {
                    val txn = result.data
                    _uiState.value =
                            _uiState.value.copy(
                                    isLoading = false,
                                    successMessage =
                                            "Transaksi selesai!\n" +
                                                    "Gross: ${txn.grossWeight} kg\n" +
                                                    "Tare: ${txn.tareWeight} kg\n" +
                                                    "Netto: ${txn.netWeight} kg",
                                    currentWeight = 0.0
                            )
                }
                is WeighingResult.Failure -> {
                    _uiState.value =
                            _uiState.value.copy(isLoading = false, errorMessage = result.error)
                }
            }
        }
    }

    // === Utility Functions ===

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
