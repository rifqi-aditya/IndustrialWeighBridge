package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.Driver
import com.rifqi.industrialweighbridge.db.Product
import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.db.Vehicle
import com.rifqi.industrialweighbridge.domain.usecase.driver.GetAllDriversUseCase
import com.rifqi.industrialweighbridge.domain.usecase.product.GetAllProductsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.CreateWeighInUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.GetAllTransactionsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.GetOpenTransactionsUseCase
import com.rifqi.industrialweighbridge.domain.usecase.transaction.UpdateWeighOutUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.GetAllVehiclesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI State for Weighing Screen */
data class WeighingUiState(
        // Weight display
        val currentWeight: Double = 0.0,
        val isStable: Boolean = false,
        val isManualMode: Boolean = true, // Default to manual since no scale

        // Selections
        val selectedVehicleId: Long? = null,
        val selectedDriverId: Long? = null,
        val selectedProductId: Long? = null,

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

/** ViewModel for Weighing operations */
class WeighingViewModel(
        private val getAllVehiclesUseCase: GetAllVehiclesUseCase,
        private val getAllDriversUseCase: GetAllDriversUseCase,
        private val getAllProductsUseCase: GetAllProductsUseCase,
        private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
        private val getOpenTransactionsUseCase: GetOpenTransactionsUseCase,
        private val createWeighInUseCase: CreateWeighInUseCase,
        private val updateWeighOutUseCase: UpdateWeighOutUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(WeighingUiState())
    val uiState: StateFlow<WeighingUiState> = _uiState.asStateFlow()

    init {
        loadMasterData()
        loadTransactions()
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

    // === Weight Input ===
    fun setWeight(weight: Double) {
        _uiState.value =
                _uiState.value.copy(
                        currentWeight = weight,
                        isStable = weight > 0 // In manual mode, any weight > 0 is considered stable
                )
    }

    // === Selection Methods ===
    fun selectVehicle(vehicleId: Long?) {
        _uiState.value = _uiState.value.copy(selectedVehicleId = vehicleId)
    }

    fun selectDriver(driverId: Long?) {
        _uiState.value = _uiState.value.copy(selectedDriverId = driverId)
    }

    fun selectProduct(productId: Long?) {
        _uiState.value = _uiState.value.copy(selectedProductId = productId)
    }

    // === Mode Toggle ===
    fun toggleManualMode() {
        _uiState.value = _uiState.value.copy(isManualMode = !_uiState.value.isManualMode)
    }

    // === Weigh-In Operation ===
    fun performWeighIn() {
        val state = _uiState.value

        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result =
                    createWeighInUseCase(
                            vehicleId = state.selectedVehicleId ?: 0,
                            driverId = state.selectedDriverId ?: 0,
                            productId = state.selectedProductId ?: 0,
                            weight = state.currentWeight,
                            isManual = state.isManualMode
                    )

            result.fold(
                    onSuccess = { ticket ->
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        successMessage = "Weigh-In berhasil! Ticket: $ticket",
                                        lastTicketNumber = ticket,
                                        // Reset selections
                                        currentWeight = 0.0,
                                        selectedVehicleId = null,
                                        selectedDriverId = null,
                                        selectedProductId = null
                                )
                    },
                    onFailure = { error ->
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = error.message ?: "Gagal melakukan Weigh-In"
                                )
                    }
            )
        }
    }

    // === Weigh-Out Operation ===
    fun performWeighOut(ticketNumber: String, entryWeight: Double) {
        val state = _uiState.value

        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result =
                    updateWeighOutUseCase(
                            ticketNumber = ticketNumber,
                            entryWeight = entryWeight,
                            exitWeight = state.currentWeight
                    )

            result.fold(
                    onSuccess = { netWeight ->
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        successMessage =
                                                "Weigh-Out berhasil! Netto: %.2f kg".format(
                                                        netWeight
                                                ),
                                        currentWeight = 0.0
                                )
                    },
                    onFailure = { error ->
                        _uiState.value =
                                _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = error.message ?: "Gagal melakukan Weigh-Out"
                                )
                    }
            )
        }
    }

    // === Clear Messages ===
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    // === Reset Form ===
    fun resetForm() {
        _uiState.value =
                _uiState.value.copy(
                        currentWeight = 0.0,
                        selectedVehicleId = null,
                        selectedDriverId = null,
                        selectedProductId = null,
                        errorMessage = null,
                        successMessage = null
                )
    }
}
