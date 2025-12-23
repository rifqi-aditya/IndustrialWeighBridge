package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.db.Vehicle
import com.rifqi.industrialweighbridge.presentation.components.SearchBar
import com.rifqi.industrialweighbridge.presentation.components.VehicleCard
import com.rifqi.industrialweighbridge.presentation.components.VehicleFormDialog
import com.rifqi.industrialweighbridge.presentation.utils.WeightFormatter
import com.rifqi.industrialweighbridge.presentation.viewmodel.VehicleViewModel
import org.koin.compose.koinInject

@Composable
fun VehicleListScreen() {
    VehicleListContent()
}

@Composable
fun VehicleListContent() {
    val viewModel: VehicleViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingVehicle by remember { mutableStateOf<Vehicle?>(null) }

    // Show error snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                        onClick = { showAddDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Tambah Kendaraan") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                )
            },
            containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Search Bar
            SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Cari plat nomor atau deskripsi..."
            )

            // Content
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredVehicles = viewModel.getFilteredVehicles()

                if (filteredVehicles.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                                text =
                                        if (uiState.searchQuery.isEmpty())
                                                "Belum ada data kendaraan"
                                        else "Tidak ditemukan",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredVehicles, key = { it.id }) { vehicle ->
                            VehicleCard(
                                    plateNumber = vehicle.plate_number,
                                    description = vehicle.description,
                                    tareWeight = vehicle.tare_weight,
                                    onEdit = { editingVehicle = vehicle },
                                    onDelete = { viewModel.delete(vehicle.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        VehicleFormDialog(
                isEdit = false,
                onDismiss = { showAddDialog = false },
                onConfirm = { plateNumber, description, tareWeight ->
                    viewModel.add(plateNumber, description, tareWeight)
                    showAddDialog = false
                }
        )
    }

    // Edit Dialog
    editingVehicle?.let { vehicle ->
        VehicleFormDialog(
                isEdit = true,
                initialPlateNumber = vehicle.plate_number,
                initialDescription = vehicle.description ?: "",
                initialTareWeight = vehicle.tare_weight?.let { WeightFormatter.formatNumber(it) }
                                ?: "",
                onDismiss = { editingVehicle = null },
                onConfirm = { plateNumber, description, tareWeight ->
                    viewModel.update(vehicle.id, plateNumber, description, tareWeight)
                    editingVehicle = null
                }
        )
    }
}
