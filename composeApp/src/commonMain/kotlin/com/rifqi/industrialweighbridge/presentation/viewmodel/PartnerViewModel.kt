package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.db.Partner
import com.rifqi.industrialweighbridge.db.PartnerType
import com.rifqi.industrialweighbridge.domain.usecase.partner.AddPartnerUseCase
import com.rifqi.industrialweighbridge.domain.usecase.partner.DeletePartnerUseCase
import com.rifqi.industrialweighbridge.domain.usecase.partner.GetAllPartnersUseCase
import com.rifqi.industrialweighbridge.domain.usecase.partner.UpdatePartnerUseCase
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

data class PartnerUiState(
    val partners: List<Partner> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val filterType: PartnerType? = null, // null = show all
    val errorMessage: String? = null
)

class PartnerViewModel(
    private val getAllPartners: GetAllPartnersUseCase,
    private val addPartner: AddPartnerUseCase,
    private val updatePartner: UpdatePartnerUseCase,
    private val deletePartner: DeletePartnerUseCase,
    private val auditLogger: AuditLogger,
    private val getCurrentUsername: () -> String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(PartnerUiState())
    val uiState: StateFlow<PartnerUiState> = _uiState.asStateFlow()

    init {
        loadPartners()
    }

    private fun loadPartners() {
        scope.launch {
            getAllPartners().collect { partnerList ->
                _uiState.value = _uiState.value.copy(partners = partnerList, isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterTypeChange(type: PartnerType?) {
        _uiState.value = _uiState.value.copy(filterType = type)
    }

    fun getFilteredPartners(): List<Partner> {
        val query = _uiState.value.searchQuery.lowercase()
        val filterType = _uiState.value.filterType

        return _uiState.value.partners.filter { partner ->
            val matchesQuery =
                query.isEmpty() ||
                        partner.name.lowercase().contains(query) ||
                        (partner.address?.lowercase()?.contains(query) == true) ||
                        (partner.phone?.lowercase()?.contains(query) == true) ||
                        (partner.code?.lowercase()?.contains(query) == true)

            val matchesType =
                filterType == null ||
                        partner.type == filterType ||
                        partner.type == PartnerType.BOTH

            matchesQuery && matchesType
        }
    }

    fun add(name: String, type: PartnerType, address: String?, phone: String?, code: String?) {
        scope.launch {
            try {
                addPartner(name, type, address, phone, code)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.PARTNER_CREATED,
                    username = getCurrentUsername(),
                    description = "Partner ditambahkan: $name (${type.name})",
                    entityType = EntityType.PARTNER.name,
                    entityId = name,
                    details = "Type: ${type.name}, Code: ${code ?: "-"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun update(
        id: Long,
        name: String,
        type: PartnerType,
        address: String?,
        phone: String?,
        code: String?
    ) {
        scope.launch {
            try {
                updatePartner(id, name, type, address, phone, code)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.PARTNER_UPDATED,
                    username = getCurrentUsername(),
                    description = "Partner diperbarui: $name",
                    entityType = EntityType.PARTNER.name,
                    entityId = id.toString(),
                    details = "Name: $name, Type: ${type.name}, Code: ${code ?: "-"}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun delete(id: Long) {
        scope.launch {
            try {
                // Get partner info before deletion for logging
                val partnerToDelete = _uiState.value.partners.find { it.id == id }

                deletePartner(id)
                _uiState.value = _uiState.value.copy(errorMessage = null)

                // Log the action
                auditLogger.log(
                    action = AuditAction.PARTNER_DELETED,
                    username = getCurrentUsername(),
                    description = "Partner dihapus: ${partnerToDelete?.name ?: "ID $id"}",
                    entityType = EntityType.PARTNER.name,
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
