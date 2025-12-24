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
import com.rifqi.industrialweighbridge.db.Partner
import com.rifqi.industrialweighbridge.presentation.components.PartnerCard
import com.rifqi.industrialweighbridge.presentation.components.PartnerFormDialog
import com.rifqi.industrialweighbridge.presentation.components.SearchBar
import com.rifqi.industrialweighbridge.presentation.viewmodel.PartnerViewModel
import org.koin.compose.koinInject

@Composable
fun PartnerListScreen() {
    PartnerListContent()
}

@Composable
fun PartnerListContent() {
    val viewModel: PartnerViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPartner by remember { mutableStateOf<Partner?>(null) }

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
                        text = { Text("Tambah Partner") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                )
            },
            containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    placeholder = "Cari nama, alamat, telepon...",
                    modifier = Modifier.padding(paddingValues)
            )

            // Content
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredPartners = viewModel.getFilteredPartners()

                if (filteredPartners.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                                text =
                                        if (uiState.searchQuery.isEmpty()) "Belum ada data partner"
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
                        items(filteredPartners, key = { it.id }) { partner ->
                            PartnerCard(
                                    name = partner.name,
                                    type = partner.type,
                                    address = partner.address,
                                    phone = partner.phone,
                                    code = partner.code,
                                    onEdit = { editingPartner = partner },
                                    onDelete = { viewModel.delete(partner.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        PartnerFormDialog(
                isEdit = false,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, type, address, phone, code ->
                    viewModel.add(name, type, address, phone, code)
                    showAddDialog = false
                }
        )
    }

    // Edit Dialog
    editingPartner?.let { partner ->
        PartnerFormDialog(
                isEdit = true,
                initialName = partner.name,
                initialType = partner.type,
                initialAddress = partner.address ?: "",
                initialPhone = partner.phone ?: "",
                initialCode = partner.code ?: "",
                onDismiss = { editingPartner = null },
                onConfirm = { name, type, address, phone, code ->
                    viewModel.update(partner.id, name, type, address, phone, code)
                    editingPartner = null
                }
        )
    }
}
