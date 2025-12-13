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
import com.rifqi.industrialweighbridge.db.Driver
import com.rifqi.industrialweighbridge.presentation.components.DriverCard
import com.rifqi.industrialweighbridge.presentation.components.DriverFormDialog
import com.rifqi.industrialweighbridge.presentation.components.SearchBar
import com.rifqi.industrialweighbridge.presentation.viewmodel.DriverViewModel
import org.koin.compose.koinInject

@Composable
fun DriverListScreen() {
    val viewModel: DriverViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDriver by remember { mutableStateOf<Driver?>(null) }

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
                        text = { Text("Tambah Driver") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                )
            },
            containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Header
            Text(
                    text = "Manajemen Driver",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Cari nama atau SIM..."
            )

            // Content
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredDrivers = viewModel.getFilteredDrivers()

                if (filteredDrivers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                                text =
                                        if (uiState.searchQuery.isEmpty()) "Belum ada data driver"
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
                        items(filteredDrivers, key = { it.id }) { driver ->
                            DriverCard(
                                    name = driver.name,
                                    licenseNo = driver.license_no,
                                    onEdit = { editingDriver = driver },
                                    onDelete = { viewModel.delete(driver.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        DriverFormDialog(
                isEdit = false,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, licenseNo ->
                    viewModel.add(name, licenseNo)
                    showAddDialog = false
                }
        )
    }

    // Edit Dialog
    editingDriver?.let { driver ->
        DriverFormDialog(
                isEdit = true,
                initialName = driver.name,
                initialLicenseNo = driver.license_no ?: "",
                onDismiss = { editingDriver = null },
                onConfirm = { name, licenseNo ->
                    viewModel.update(driver.id, name, licenseNo)
                    editingDriver = null
                }
        )
    }
}
