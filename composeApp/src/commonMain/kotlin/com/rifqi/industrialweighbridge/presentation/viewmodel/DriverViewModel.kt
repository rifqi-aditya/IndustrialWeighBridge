package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.Driver
import com.rifqi.industrialweighbridge.domain.usecase.driver.AddDriverUseCase
import com.rifqi.industrialweighbridge.domain.usecase.driver.DeleteDriverUseCase
import com.rifqi.industrialweighbridge.domain.usecase.driver.GetAllDriversUseCase
import com.rifqi.industrialweighbridge.domain.usecase.driver.UpdateDriverUseCase
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

data class DriverUiState(
    val drivers: List<Driver> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class DriverViewModel(
    private val getAllDrivers: GetAllDriversUseCase,
    private val addDriver: AddDriverUseCase,
    private val updateDriver: UpdateDriverUseCase,
    private val deleteDriver: DeleteDriverUseCase,
    private val auditLogger: AuditLogger,
    private val getCurrentUsername: () -> String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    init {
        loadDrivers()
    }

    private fun loadDrivers() {
        scope.launch {
            getAllDrivers().collect { driverList ->
                _uiState.value = _uiState.value.copy(drivers = driverList, isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getFilteredDrivers(): List<Driver> {
        val query = _uiState.value.searchQuery.lowercase()
        return if (query.isEmpty()) {
            _uiState.value.drivers
        } else {
            _uiState.value.drivers.filter { driver ->
                driver.name.lowercase().contains(query) ||
                        (driver.license_no?.lowercase()?.contains(query) == true)
            }
        }
    }

    fun add(name: String, licenseNo: String?) {
        scope.launch {
            try {
                addDriver(name, licenseNo)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.DRIVER_CREATED,
                    username = getCurrentUsername(),
                    description = "Driver ditambahkan: $name",
                    entityType = EntityType.DRIVER.name,
                    entityId = name,
                    details = licenseNo?.let { "License: $it" }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun update(id: Long, name: String, licenseNo: String?) {
        scope.launch {
            try {
                updateDriver(id, name, licenseNo)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.DRIVER_UPDATED,
                    username = getCurrentUsername(),
                    description = "Driver diperbarui: $name",
                    entityType = EntityType.DRIVER.name,
                    entityId = id.toString(),
                    details = "Name: $name, License: ${licenseNo ?: "-"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun delete(id: Long) {
        scope.launch {
            try {
                // Get driver name before deletion for logging
                val driverToDelete = _uiState.value.drivers.find { it.id == id }

                deleteDriver(id)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.DRIVER_DELETED,
                    username = getCurrentUsername(),
                    description = "Driver dihapus: ${driverToDelete?.name ?: "ID $id"}",
                    entityType = EntityType.DRIVER.name,
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
