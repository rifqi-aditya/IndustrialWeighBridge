package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.presentation.utils.WeightFormatter
import com.rifqi.industrialweighbridge.presentation.viewmodel.WeighingViewModel
import org.koin.compose.koinInject

@Composable
fun WeighingScreen() {
    val viewModel: WeighingViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(0) }

    // Show error/success messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
        ) {
            // Tab selection: Weigh-In / Weigh-Out
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("WEIGH-IN") }
                )
                Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("WEIGH-OUT") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight Display
            WeightDisplayCard(
                    weight = uiState.currentWeight,
                    isStable = uiState.isStable,
                    isManualMode = uiState.isManualMode,
                    onManualModeToggle = { viewModel.toggleManualMode() },
                    onWeightChange = { viewModel.setWeight(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> WeighInForm(viewModel)
                1 -> WeighOutForm(viewModel, uiState.openTransactions)
            }
        }
    }
}

@Composable
fun WeightDisplayCard(
        weight: Double,
        isStable: Boolean,
        isManualMode: Boolean,
        onManualModeToggle: () -> Unit,
        onWeightChange: (Double) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode toggle
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = if (isManualMode) "Mode: Manual" else "Mode: Auto (Scale)",
                        style = MaterialTheme.typography.labelLarge
                )
                Switch(checked = isManualMode, onCheckedChange = { onManualModeToggle() })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight display or input
            if (isManualMode) {
                OutlinedTextField(
                        value = weightInput,
                        onValueChange = { newValue ->
                            weightInput = WeightFormatter.formatInputAsYouType(newValue)
                            WeightFormatter.parseWeight(weightInput)?.let { onWeightChange(it) }
                        },
                        label = { Text("Masukkan Berat (kg)") },
                        placeholder = { Text("Contoh: 12.345,67") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle =
                                MaterialTheme.typography.headlineMedium.copy(
                                        textAlign = TextAlign.Center
                                )
                )
            } else {
                // Auto mode - display from scale
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(100.dp)
                                        .background(
                                                color = MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                                width = 2.dp,
                                                color =
                                                        if (isStable) Color.Green
                                                        else MaterialTheme.colorScheme.outline,
                                                shape = RoundedCornerShape(8.dp)
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = WeightFormatter.formatWeight(weight),
                            style =
                                    MaterialTheme.typography.displayMedium.copy(
                                            fontWeight = FontWeight.Bold
                                    ),
                            color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stability indicator
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                        imageVector =
                                if (isStable) Icons.Default.CheckCircle else Icons.Default.Scale,
                        contentDescription = null,
                        tint =
                                if (isStable) Color.Green
                                else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                        text = if (isStable) "STABIL" else "Menunggu berat stabil...",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                                if (isStable) Color.Green
                                else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeighInForm(viewModel: WeighingViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var vehicleExpanded by remember { mutableStateOf(false) }
    var driverExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                    text = "Form Weigh-In",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Dropdown
            ExposedDropdownMenuBox(
                    expanded = vehicleExpanded,
                    onExpandedChange = { vehicleExpanded = it }
            ) {
                val selectedVehicle = uiState.vehicles.find { it.id == uiState.selectedVehicleId }
                OutlinedTextField(
                        value = selectedVehicle?.plate_number ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Kendaraan *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                        expanded = vehicleExpanded,
                        onDismissRequest = { vehicleExpanded = false }
                ) {
                    uiState.vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(vehicle.plate_number)
                                        vehicle.description?.let {
                                            Text(it, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectVehicle(vehicle.id)
                                    vehicleExpanded = false
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Driver Dropdown
            ExposedDropdownMenuBox(
                    expanded = driverExpanded,
                    onExpandedChange = { driverExpanded = it }
            ) {
                val selectedDriver = uiState.drivers.find { it.id == uiState.selectedDriverId }
                OutlinedTextField(
                        value = selectedDriver?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Driver *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                        expanded = driverExpanded,
                        onDismissRequest = { driverExpanded = false }
                ) {
                    uiState.drivers.forEach { driver ->
                        DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(driver.name)
                                        driver.license_no?.let {
                                            Text(
                                                    "SIM: $it",
                                                    style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectDriver(driver.id)
                                    driverExpanded = false
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Product Dropdown
            ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = it }
            ) {
                val selectedProduct = uiState.products.find { it.id == uiState.selectedProductId }
                OutlinedTextField(
                        value = selectedProduct?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Produk *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                ) {
                    uiState.products.forEach { product ->
                        DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(product.name)
                                        product.code?.let {
                                            Text(
                                                    "Kode: $it",
                                                    style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectProduct(product.id)
                                    productExpanded = false
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                        onClick = { viewModel.resetForm() },
                        modifier = Modifier.weight(1f)
                ) { Text("Reset") }

                Button(
                        onClick = { viewModel.performWeighIn() },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.isStable && !uiState.isLoading
                ) { Text("CAPTURE WEIGH-IN") }
            }
        }
    }
}

@Composable
fun WeighOutForm(viewModel: WeighingViewModel, openTransactions: List<SelectOpenTransactions>) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTransaction by remember { mutableStateOf<SelectOpenTransactions?>(null) }

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                    text = "Pilih Transaksi untuk Weigh-Out",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (openTransactions.isEmpty()) {
                Text(
                        text = "Tidak ada transaksi yang perlu Weigh-Out",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                openTransactions.forEach { txn ->
                    val isSelected = selectedTransaction?.ticket_number == txn.ticket_number

                    Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    if (isSelected)
                                                            MaterialTheme.colorScheme
                                                                    .primaryContainer
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                            onClick = { selectedTransaction = txn }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                    text = txn.ticket_number,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                            )
                            Text(
                                    text = "Plat: ${txn.plate_number ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                    text =
                                            "Berat Masuk: ${WeightFormatter.formatWeight(txn.weigh_in_weight)}",
                                    style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                        onClick = {
                            selectedTransaction?.let { txn ->
                                viewModel.performWeighOut(
                                        ticketNumber = txn.ticket_number,
                                        entryWeight = txn.weigh_in_weight
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled =
                                selectedTransaction != null &&
                                        uiState.isStable &&
                                        !uiState.isLoading
                ) { Text("CAPTURE WEIGH-OUT") }
            }
        }
    }
}
