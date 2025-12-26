package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.Vehicle
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.AddVehicleUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.DeleteVehicleUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.GetAllVehiclesUseCase
import com.rifqi.industrialweighbridge.domain.usecase.vehicle.UpdateVehicleUseCase
import com.rifqi.industrialweighbridge.infrastructure.AuditAction
import com.rifqi.industrialweighbridge.infrastructure.AuditLogger
import com.rifqi.industrialweighbridge.infrastructure.EntityType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VehicleUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class VehicleViewModel(
    private val getAllVehicles: GetAllVehiclesUseCase,
    private val addVehicle: AddVehicleUseCase,
    private val updateVehicle: UpdateVehicleUseCase,
    private val deleteVehicle: DeleteVehicleUseCase,
    private val auditLogger: AuditLogger,
    private val getCurrentUsername: () -> String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    init {
        loadVehicles()
    }

    private fun loadVehicles() {
        scope.launch {
            getAllVehicles().collect { vehicleList ->
                _uiState.value = _uiState.value.copy(vehicles = vehicleList, isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredVehicles(): List<Vehicle> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isEmpty()) {
            _uiState.value.vehicles
        } else {
            _uiState.value.vehicles.filter { vehicle ->
                vehicle.plate_number.lowercase().contains(query) ||
                        (vehicle.description?.lowercase()?.contains(query) == true)
            }
        }
    }

    fun add(plateNumber: String, description: String?, tareWeight: Double?) {
        scope.launch {
            try {
                addVehicle(plateNumber, description, tareWeight)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.VEHICLE_CREATED,
                    username = getCurrentUsername(),
                    description = "Kendaraan ditambahkan: $plateNumber",
                    entityType = EntityType.VEHICLE.name,
                    entityId = plateNumber,
                    details = "Tare: ${tareWeight ?: "-"} kg"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun update(id: Long, plateNumber: String, description: String?, tareWeight: Double?) {
        scope.launch {
            try {
                updateVehicle(id, plateNumber, description, tareWeight)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.VEHICLE_UPDATED,
                    username = getCurrentUsername(),
                    description = "Kendaraan diperbarui: $plateNumber",
                    entityType = EntityType.VEHICLE.name,
                    entityId = id.toString(),
                    details = "Plate: $plateNumber, Tare: ${tareWeight ?: "-"} kg"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun delete(id: Long) {
        scope.launch {
            try {
                // Get vehicle info before deletion for logging
                val vehicleToDelete = _uiState.value.vehicles.find { it.id == id }

                deleteVehicle(id)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.VEHICLE_DELETED,
                    username = getCurrentUsername(),
                    description =
                        "Kendaraan dihapus: ${vehicleToDelete?.plate_number ?: "ID $id"}",
                    entityType = EntityType.VEHICLE.name,
                    entityId = id.toString(),
                    details = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
