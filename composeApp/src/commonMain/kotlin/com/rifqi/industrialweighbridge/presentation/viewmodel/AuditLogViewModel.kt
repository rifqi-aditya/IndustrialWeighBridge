package com.rifqi.industrialweighbridge.presentation.viewmodel

import com.rifqi.industrialweighbridge.infrastructure.AuditAction
import com.rifqi.industrialweighbridge.infrastructure.AuditEntry
import com.rifqi.industrialweighbridge.infrastructure.AuditLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Audit Log screen. Provides functionality to view, filter, and search audit
 * logs.
 */
class AuditLogViewModel(private val auditLogger: AuditLogger) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // All log entries
    private val _entries = MutableStateFlow<List<AuditEntry>>(emptyList())
    val entries: StateFlow<List<AuditEntry>> = _entries.asStateFlow()

    // Filtered entries based on search/filter
    private val _filteredEntries = MutableStateFlow<List<AuditEntry>>(emptyList())
    val filteredEntries: StateFlow<List<AuditEntry>> = _filteredEntries.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Selected action filter
    private val _selectedActionFilter = MutableStateFlow<AuditAction?>(null)
    val selectedActionFilter: StateFlow<AuditAction?> = _selectedActionFilter.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Total log count
    private val _totalCount = MutableStateFlow(0L)
    val totalCount: StateFlow<Long> = _totalCount.asStateFlow()

    init {
        loadLogs()
    }

    /** Load all logs from the database */
    fun loadLogs() {
        scope.launch {
            _isLoading.value = true
            try {
                val logs = auditLogger.getAllEntries()
                _entries.value = logs
                _totalCount.value = auditLogger.countEntries()
                applyFilters()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Refresh logs (reload from database) */
    fun refresh() {
        loadLogs()
    }

    /** Set action filter */
    fun setActionFilter(action: AuditAction?) {
        _selectedActionFilter.value = action
        applyFilters()
    }

    /** Set search query */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /** Apply current filters to the entries */
    private fun applyFilters() {
        val action = _selectedActionFilter.value
        val query = _searchQuery.value.lowercase().trim()

        var filtered = _entries.value

        // Filter by action type
        if (action != null) {
            filtered = filtered.filter { it.action == action }
        }

        // Filter by search query
        if (query.isNotEmpty()) {
            filtered =
                filtered.filter { entry ->
                    entry.description.lowercase().contains(query) ||
                            entry.username.lowercase().contains(query) ||
                            entry.entityId?.lowercase()?.contains(query) == true ||
                            entry.action.name.lowercase().contains(query)
                }
        }

        _filteredEntries.value = filtered
    }

    /** Clear all filters */
    fun clearFilters() {
        _selectedActionFilter.value = null
        _searchQuery.value = ""
        applyFilters()
    }

    /** Get all available action types for filter dropdown */
    fun getAvailableActions(): List<AuditAction> = AuditAction.entries.toList()
}
