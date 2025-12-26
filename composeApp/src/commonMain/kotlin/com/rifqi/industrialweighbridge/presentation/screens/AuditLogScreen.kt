package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.infrastructure.AuditAction
import com.rifqi.industrialweighbridge.infrastructure.AuditEntry
import com.rifqi.industrialweighbridge.presentation.viewmodel.AuditLogViewModel
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Screen for viewing audit logs. Shows a table of all logged actions with filtering and search
 * capabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(onBack: () -> Unit, viewModel: AuditLogViewModel = koinInject()) {
    val entries by viewModel.filteredEntries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedAction by viewModel.selectedActionFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    var showFilterDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "Total: $totalCount entries (showing ${entries.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Filters row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.setSearchQuery("")
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    }
                )

                // Action filter dropdown
                ExposedDropdownMenuBox(
                    expanded = showFilterDropdown,
                    onExpandedChange = { showFilterDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedAction?.name ?: "All Actions",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.width(200.dp).menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = showFilterDropdown
                            )
                        },
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = showFilterDropdown,
                        onDismissRequest = { showFilterDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Actions") },
                            onClick = {
                                viewModel.setActionFilter(null)
                                showFilterDropdown = false
                            }
                        )
                        viewModel.getAvailableActions().forEach { action ->
                            DropdownMenuItem(
                                text = { Text(action.name) },
                                onClick = {
                                    viewModel.setActionFilter(
                                        action
                                    )
                                    showFilterDropdown = false
                                }
                            )
                        }
                    }
                }

                // Clear filters button
                if (selectedAction != null || searchQuery.isNotEmpty()) {
                    OutlinedButton(onClick = { viewModel.clearFilters() }) {
                        Icon(
                            Icons.Default.FilterListOff,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Log entries table
            Card(
                modifier = Modifier.fillMaxSize(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                if (entries.isEmpty() && !isLoading) {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No log entries found",
                                style =
                                    MaterialTheme.typography
                                        .bodyLarge,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Header row
                        item { LogTableHeader() }

                        items(entries) { entry -> LogEntryRow(entry) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogTableHeader() {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Timestamp",
            modifier = Modifier.width(160.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Action",
            modifier = Modifier.width(180.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "User",
            modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Entity",
            modifier = Modifier.width(150.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Description",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LogEntryRow(entry: AuditEntry) {
    val formattedTime =
        remember(entry.timestamp) {
            try {
                val instant = Instant.parse(entry.timestamp)
                val localDateTime =
                    instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                localDateTime.format(formatter)
            } catch (e: Exception) {
                entry.timestamp // Fallback to raw timestamp if parsing fails
            }
        }

    val actionColor =
        remember(entry.action) {
            when (entry.action) {
                AuditAction.LOGIN, AuditAction.LOGOUT ->
                    androidx.compose.ui.graphics.Color(0xFF4CAF50)

                AuditAction.LOGIN_FAILED ->
                    androidx.compose.ui.graphics.Color(0xFFF44336)

                AuditAction.WEIGH_IN_COMPLETED, AuditAction.WEIGH_OUT_COMPLETED ->
                    androidx.compose.ui.graphics.Color(0xFF2196F3)

                AuditAction.TRANSACTION_CANCELLED ->
                    androidx.compose.ui.graphics.Color(0xFFFF9800)

                AuditAction.MANUAL_MODE_ENABLED, AuditAction.MANUAL_MODE_DISABLED ->
                    androidx.compose.ui.graphics.Color(0xFF9C27B0)

                else -> androidx.compose.ui.graphics.Color(0xFF607D8B)
            }
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timestamp
            Text(
                text = formattedTime,
                modifier = Modifier.width(160.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Action badge
            Surface(
                modifier = Modifier.width(180.dp),
                shape = RoundedCornerShape(4.dp),
                color = actionColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = entry.action.name.replace("_", " "),
                    modifier =
                        Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),
                    style = MaterialTheme.typography.labelSmall,
                    color = actionColor,
                    fontWeight = FontWeight.Medium
                )
            }

            // Username
            Text(
                text = entry.username,
                modifier = Modifier.width(100.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Entity
            Text(
                text =
                    if (entry.entityType != null)
                        "${entry.entityType}: ${entry.entityId ?: "-"}"
                    else "-",
                modifier = Modifier.width(150.dp),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Description
            Text(
                text = entry.description,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
