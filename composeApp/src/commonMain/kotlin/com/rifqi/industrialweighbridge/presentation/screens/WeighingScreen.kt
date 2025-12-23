package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.RadioButton
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
import com.rifqi.industrialweighbridge.engine.TransactionType
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
            // Tab selection: Penimbangan Pertama / Penimbangan Kedua
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("BUKA TRANSAKSI") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("TUTUP TRANSAKSI") }
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
                0 -> FirstWeighingForm(viewModel)
                1 -> SecondWeighingForm(viewModel, uiState.openTransactions)
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
                    text =
                        if (isManualMode) "Mode: Input Manual"
                        else "Mode: Otomatis (Timbangan)",
                    style = MaterialTheme.typography.labelLarge
                )
                Switch(
                    checked = isManualMode,
                    onCheckedChange = { onManualModeToggle() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weight display or input
            if (isManualMode) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { newValue ->
                        weightInput =
                            WeightFormatter.formatInputAsYouType(
                                newValue
                            )
                        WeightFormatter.parseWeight(weightInput)?.let {
                            onWeightChange(it)
                        }
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
                                color =
                                    MaterialTheme.colorScheme
                                        .surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 2.dp,
                                color =
                                    if (isStable) Color.Green
                                    else
                                        MaterialTheme
                                            .colorScheme
                                            .outline,
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
                        if (isStable) Icons.Default.CheckCircle
                        else Icons.Default.Scale,
                    contentDescription = null,
                    tint =
                        if (isStable) Color.Green
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text =
                        if (isStable) "STABIL"
                        else "Menunggu berat stabil...",
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (isStable) Color.Green
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Form untuk Penimbangan Pertama (Buka Transaksi) Sesuai FR-05.1 sampai FR-05.4 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstWeighingForm(viewModel: WeighingViewModel) {
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
                text = "Penimbangan Pertama (Buka Transaksi)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Catat Berat 1 untuk kendaraan yang baru tiba",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // === JENIS TRANSAKSI (Radio Button) - FR-05.2 ===
            Text(
                text = "Jenis Transaksi *",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Penerimaan (Inbound)
                TransactionTypeOption(
                    label = "Penerimaan",
                    description = "Berat 1 = Gross",
                    selected =
                        uiState.selectedTransactionType ==
                                TransactionType.INBOUND,
                    onClick = {
                        viewModel.selectTransactionType(
                            TransactionType.INBOUND
                        )
                    },
                    modifier = Modifier.weight(1f)
                )

                // Pengiriman (Outbound)
                TransactionTypeOption(
                    label = "Pengiriman",
                    description = "Berat 1 = Tare",
                    selected =
                        uiState.selectedTransactionType ==
                                TransactionType.OUTBOUND,
                    onClick = {
                        viewModel.selectTransactionType(
                            TransactionType.OUTBOUND
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Dropdown
            ExposedDropdownMenuBox(
                expanded = vehicleExpanded,
                onExpandedChange = { vehicleExpanded = it }
            ) {
                val selectedVehicle =
                    uiState.vehicles.find { it.id == uiState.selectedVehicleId }
                OutlinedTextField(
                    value = selectedVehicle?.plate_number ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nomor Kendaraan *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = vehicleExpanded
                        )
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
                                        Text(
                                            it,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
                                        )
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
                val selectedDriver =
                    uiState.drivers.find { it.id == uiState.selectedDriverId }
                OutlinedTextField(
                    value = selectedDriver?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Supir *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = driverExpanded
                        )
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
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
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
                val selectedProduct =
                    uiState.products.find { it.id == uiState.selectedProductId }
                OutlinedTextField(
                    value = selectedProduct?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Barang/Material *") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = productExpanded
                        )
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
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodySmall
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
                ) { Text("SIMPAN BERAT 1") }
            }
        }
    }
}

/** Radio button option untuk Jenis Transaksi */
@Composable
fun TransactionTypeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
            ),
        border =
            if (selected) {
                androidx.compose.foundation.BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary
                )
            } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Form untuk Penimbangan Kedua (Tutup Transaksi) Sesuai FR-05.5 sampai FR-05.9 */
@Composable
fun SecondWeighingForm(
    viewModel: WeighingViewModel,
    openTransactions: List<SelectOpenTransactions>
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTransaction by remember { mutableStateOf<SelectOpenTransactions?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Penimbangan Kedua (Tutup Transaksi)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Pilih transaksi aktif untuk catat Berat 2",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (openTransactions.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme
                                    .surfaceVariant
                        )
                ) {
                    Text(
                        text = "Tidak ada transaksi aktif",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Transaksi Aktif (${openTransactions.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                openTransactions.forEach { txn ->
                    val isSelected =
                        selectedTransaction?.ticket_number ==
                                txn.ticket_number

                    Card(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (isSelected)
                                        MaterialTheme
                                            .colorScheme
                                            .primaryContainer
                                    else
                                        MaterialTheme
                                            .colorScheme
                                            .surfaceVariant
                            ),
                        onClick = { selectedTransaction = txn }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    selectedTransaction = txn
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = txn.ticket_number,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text =
                                        "Plat: ${txn.plate_number ?: "-"}",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "Berat 1",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                                Text(
                                    text =
                                        WeightFormatter
                                            .formatWeight(
                                                txn.weigh_in_weight
                                            ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium,
                                    fontWeight =
                                        FontWeight.Bold,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )
                            }
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
                ) { Text("SIMPAN BERAT 2 & TUTUP TRANSAKSI") }
            }
        }
    }
}
