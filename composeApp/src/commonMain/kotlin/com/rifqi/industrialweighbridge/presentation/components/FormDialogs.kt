package com.rifqi.industrialweighbridge.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun DriverFormDialog(
        isEdit: Boolean = false,
        initialName: String = "",
        initialLicenseNo: String = "",
        onDismiss: () -> Unit,
        onConfirm: (name: String, licenseNo: String?) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var licenseNo by remember { mutableStateOf(initialLicenseNo) }
    var nameError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title
                Text(
                        text = if (isEdit) "Edit Driver" else "Tambah Driver",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Name Field
                OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = false
                        },
                        label = { Text("Nama Driver *") },
                        isError = nameError,
                        supportingText =
                                if (nameError) {
                                    { Text("Nama wajib diisi") }
                                } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // License Number Field
                OutlinedTextField(
                        value = licenseNo,
                        onValueChange = { licenseNo = it },
                        label = { Text("Nomor SIM (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Batal") }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                            onClick = {
                                if (name.isBlank()) {
                                    nameError = true
                                } else {
                                    onConfirm(name.trim(), licenseNo.trim().ifEmpty { null })
                                }
                            },
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                    )
                    ) { Text(if (isEdit) "Simpan" else "Tambah") }
                }
            }
        }
    }
}

@Composable
fun VehicleFormDialog(
        isEdit: Boolean = false,
        initialPlateNumber: String = "",
        initialDescription: String = "",
        initialTareWeight: String = "",
        onDismiss: () -> Unit,
        onConfirm: (plateNumber: String, description: String?, tareWeight: Double?) -> Unit
) {
    var plateNumber by remember { mutableStateOf(initialPlateNumber) }
    var description by remember { mutableStateOf(initialDescription) }
    var tareWeight by remember { mutableStateOf(initialTareWeight) }
    var plateError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Title
                Text(
                        text = if (isEdit) "Edit Kendaraan" else "Tambah Kendaraan",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Plate Number Field
                OutlinedTextField(
                        value = plateNumber,
                        onValueChange = {
                            plateNumber = it
                            plateError = false
                        },
                        label = { Text("Plat Nomor *") },
                        isError = plateError,
                        supportingText =
                                if (plateError) {
                                    { Text("Plat nomor wajib diisi") }
                                } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description Field
                OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tare Weight Field
                OutlinedTextField(
                        value = tareWeight,
                        onValueChange = {
                            tareWeight = it
                            weightError = false
                        },
                        label = { Text("Berat Kosong / Tare (kg)") },
                        isError = weightError,
                        supportingText =
                                if (weightError) {
                                    { Text("Masukkan angka yang valid") }
                                } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Batal") }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                            onClick = {
                                if (plateNumber.isBlank()) {
                                    plateError = true
                                    return@Button
                                }

                                val weight =
                                        if (tareWeight.isBlank()) {
                                            null
                                        } else {
                                            tareWeight.toDoubleOrNull()
                                        }

                                if (tareWeight.isNotBlank() && weight == null) {
                                    weightError = true
                                    return@Button
                                }

                                onConfirm(
                                        plateNumber.trim(),
                                        description.trim().ifEmpty { null },
                                        weight
                                )
                            },
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                    )
                    ) { Text(if (isEdit) "Simpan" else "Tambah") }
                }
            }
        }
    }
}
