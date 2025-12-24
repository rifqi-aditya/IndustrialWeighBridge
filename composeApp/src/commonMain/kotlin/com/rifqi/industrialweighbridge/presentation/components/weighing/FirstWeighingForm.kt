package com.rifqi.industrialweighbridge.presentation.components.weighing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.engine.TransactionType
import com.rifqi.industrialweighbridge.presentation.viewmodel.WeighingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstWeighingForm(viewModel: WeighingViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var vehicleExpanded by remember { mutableStateOf(false) }
    var driverExpanded by remember { mutableStateOf(false) }
    var productExpanded by remember { mutableStateOf(false) }
    var partnerExpanded by remember { mutableStateOf(false) }

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Text(
                    text = "Buka Transaksi Baru",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
            )

            Text(
                    text = "Catat Berat 1 untuk kendaraan yang baru tiba",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Transaction Type Selection
            SectionHeader(icon = Icons.Default.SwapHoriz, title = "Jenis Transaksi")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TransactionTypeCard(
                        label = "Penerimaan",
                        description = "Berat 1 = Gross",
                        selected = uiState.selectedTransactionType == TransactionType.INBOUND,
                        onClick = { viewModel.selectTransactionType(TransactionType.INBOUND) },
                        modifier = Modifier.weight(1f)
                )

                TransactionTypeCard(
                        label = "Pengiriman",
                        description = "Berat 1 = Tare",
                        selected = uiState.selectedTransactionType == TransactionType.OUTBOUND,
                        onClick = { viewModel.selectTransactionType(TransactionType.OUTBOUND) },
                        modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vehicle Selection
            SectionHeader(icon = Icons.Default.LocalShipping, title = "Kendaraan")

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                    expanded = vehicleExpanded,
                    onExpandedChange = { vehicleExpanded = it }
            ) {
                val selectedVehicle = uiState.vehicles.find { it.id == uiState.selectedVehicleId }
                OutlinedTextField(
                        value = selectedVehicle?.plate_number ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Kendaraan") },
                        placeholder = { Text("Ketuk untuk memilih...") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                        expanded = vehicleExpanded,
                        onDismissRequest = { vehicleExpanded = false }
                ) {
                    uiState.vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(vehicle.plate_number, fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Driver Selection
            SectionHeader(icon = Icons.Default.Person, title = "Supir")

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                    expanded = driverExpanded,
                    onExpandedChange = { driverExpanded = it }
            ) {
                val selectedDriver = uiState.drivers.find { it.id == uiState.selectedDriverId }
                OutlinedTextField(
                        value = selectedDriver?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Supir") },
                        placeholder = { Text("Ketuk untuk memilih...") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                        expanded = driverExpanded,
                        onDismissRequest = { driverExpanded = false }
                ) {
                    uiState.drivers.forEach { driver ->
                        DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(driver.name, fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Product Selection
            SectionHeader(icon = Icons.Default.Inventory2, title = "Barang/Material")

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                    expanded = productExpanded,
                    onExpandedChange = { productExpanded = it }
            ) {
                val selectedProduct = uiState.products.find { it.id == uiState.selectedProductId }
                OutlinedTextField(
                        value = selectedProduct?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Barang") },
                        placeholder = { Text("Ketuk untuk memilih...") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                        expanded = productExpanded,
                        onDismissRequest = { productExpanded = false }
                ) {
                    uiState.products.forEach { product ->
                        DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(product.name, fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Partner Selection (dynamic label based on transaction type)
            val partnerLabel =
                    if (uiState.selectedTransactionType == TransactionType.INBOUND) {
                        "Supplier"
                    } else {
                        "Customer"
                    }

            SectionHeader(icon = Icons.Default.Business, title = partnerLabel)

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                    expanded = partnerExpanded,
                    onExpandedChange = { partnerExpanded = it }
            ) {
                val selectedPartner = uiState.partners.find { it.id == uiState.selectedPartnerId }
                OutlinedTextField(
                        value = selectedPartner?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih $partnerLabel") },
                        placeholder = { Text("Ketuk untuk memilih...") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = partnerExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                        expanded = partnerExpanded,
                        onDismissRequest = { partnerExpanded = false }
                ) {
                    uiState.partners.forEach { partner ->
                        DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(partner.name, fontWeight = FontWeight.Bold)
                                        partner.address?.let {
                                            Text(it, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectPartner(partner.id)
                                    partnerExpanded = false
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action buttons
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                        onClick = { viewModel.resetForm() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                ) { Text("Reset", fontWeight = FontWeight.Bold) }

                Button(
                        onClick = { viewModel.performWeighIn() },
                        modifier = Modifier.weight(2f),
                        enabled = uiState.isStable && !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    Icon(Icons.Default.Scale, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIMPAN BERAT 1", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
