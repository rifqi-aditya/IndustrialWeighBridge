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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.rifqi.industrialweighbridge.db.PartnerType
import com.rifqi.industrialweighbridge.presentation.utils.WeightFormatter

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
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                                // Title
                                Text(
                                        text = if (isEdit) "Ubah Driver" else "Tambah Driver",
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
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                        OutlinedButton(onClick = onDismiss) { Text("Batal") }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Button(
                                                onClick = {
                                                        if (name.isBlank()) {
                                                                nameError = true
                                                        } else {
                                                                onConfirm(
                                                                        name.trim(),
                                                                        licenseNo.trim().ifEmpty {
                                                                                null
                                                                        }
                                                                )
                                                        }
                                                },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
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
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
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

                                // Tare Weight Field with auto-formatting
                                OutlinedTextField(
                                        value = tareWeight,
                                        onValueChange = { newValue ->
                                                tareWeight =
                                                        WeightFormatter.formatInputAsYouType(
                                                                newValue
                                                        )
                                                weightError = false
                                        },
                                        label = { Text("Berat Kosong / Tare (kg)") },
                                        placeholder = { Text("Contoh: 1.234,56") },
                                        isError = weightError,
                                        supportingText =
                                                if (weightError) {
                                                        { Text("Masukkan angka yang valid") }
                                                } else {
                                                        { Text("Format: 1.234,56 kg") }
                                                },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Buttons
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                        OutlinedButton(onClick = onDismiss) { Text("Batal") }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Button(
                                                onClick = {
                                                        if (plateNumber.isBlank()) {
                                                                plateError = true
                                                                return@Button
                                                        }

                                                        val weight =
                                                                WeightFormatter.parseWeight(
                                                                        tareWeight
                                                                )

                                                        if (tareWeight.isNotBlank() &&
                                                                        weight == null
                                                        ) {
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
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                        ) { Text(if (isEdit) "Simpan" else "Tambah") }
                                }
                        }
                }
        }
}

@Composable
fun ProductFormDialog(
        isEdit: Boolean = false,
        initialName: String = "",
        initialCode: String = "",
        onDismiss: () -> Unit,
        onConfirm: (name: String, code: String?) -> Unit
) {
        var name by remember { mutableStateOf(initialName) }
        var code by remember { mutableStateOf(initialCode) }
        var nameError by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = onDismiss) {
                Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                                // Title
                                Text(
                                        text = if (isEdit) "Edit Produk" else "Tambah Produk",
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
                                        label = { Text("Nama Produk *") },
                                        isError = nameError,
                                        supportingText =
                                                if (nameError) {
                                                        { Text("Nama wajib diisi") }
                                                } else null,
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Code Field
                                OutlinedTextField(
                                        value = code,
                                        onValueChange = { code = it },
                                        label = { Text("Kode Produk (Opsional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Buttons
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                        OutlinedButton(onClick = onDismiss) { Text("Batal") }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Button(
                                                onClick = {
                                                        if (name.isBlank()) {
                                                                nameError = true
                                                        } else {
                                                                onConfirm(
                                                                        name.trim(),
                                                                        code.trim().ifEmpty { null }
                                                                )
                                                        }
                                                },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                        ) { Text(if (isEdit) "Simpan" else "Tambah") }
                                }
                        }
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerFormDialog(
        isEdit: Boolean = false,
        initialName: String = "",
        initialType: PartnerType = PartnerType.SUPPLIER,
        initialAddress: String = "",
        initialPhone: String = "",
        initialCode: String = "",
        onDismiss: () -> Unit,
        onConfirm:
                (
                        name: String,
                        type: PartnerType,
                        address: String?,
                        phone: String?,
                        code: String?) -> Unit
) {
        var name by remember { mutableStateOf(initialName) }
        var selectedType by remember { mutableStateOf(initialType) }
        var address by remember { mutableStateOf(initialAddress) }
        var phone by remember { mutableStateOf(initialPhone) }
        var code by remember { mutableStateOf(initialCode) }
        var nameError by remember { mutableStateOf(false) }
        var typeExpanded by remember { mutableStateOf(false) }

        val typeOptions =
                listOf(
                        PartnerType.SUPPLIER to "Supplier",
                        PartnerType.CUSTOMER to "Customer",
                        PartnerType.BOTH to "Keduanya"
                )

        Dialog(onDismissRequest = onDismiss) {
                Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                                // Title
                                Text(
                                        text = if (isEdit) "Edit Partner" else "Tambah Partner",
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
                                        label = { Text("Nama Partner *") },
                                        isError = nameError,
                                        supportingText =
                                                if (nameError) {
                                                        { Text("Nama wajib diisi") }
                                                } else null,
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Type Dropdown
                                ExposedDropdownMenuBox(
                                        expanded = typeExpanded,
                                        onExpandedChange = { typeExpanded = it }
                                ) {
                                        OutlinedTextField(
                                                value =
                                                        typeOptions
                                                                .find { it.first == selectedType }
                                                                ?.second
                                                                ?: "",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Tipe Partner *") },
                                                trailingIcon = {
                                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                                                expanded = typeExpanded
                                                        )
                                                },
                                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                                shape = RoundedCornerShape(12.dp)
                                        )
                                        ExposedDropdownMenu(
                                                expanded = typeExpanded,
                                                onDismissRequest = { typeExpanded = false }
                                        ) {
                                                typeOptions.forEach { (type, label) ->
                                                        DropdownMenuItem(
                                                                text = { Text(label) },
                                                                onClick = {
                                                                        selectedType = type
                                                                        typeExpanded = false
                                                                }
                                                        )
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Address Field
                                OutlinedTextField(
                                        value = address,
                                        onValueChange = { address = it },
                                        label = { Text("Alamat (Opsional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Phone Field
                                OutlinedTextField(
                                        value = phone,
                                        onValueChange = { phone = it },
                                        label = { Text("Telepon (Opsional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Code Field
                                OutlinedTextField(
                                        value = code,
                                        onValueChange = { code = it },
                                        label = { Text("Kode (Opsional)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Buttons
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                        OutlinedButton(onClick = onDismiss) { Text("Batal") }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Button(
                                                onClick = {
                                                        if (name.isBlank()) {
                                                                nameError = true
                                                        } else {
                                                                onConfirm(
                                                                        name.trim(),
                                                                        selectedType,
                                                                        address.trim().ifEmpty {
                                                                                null
                                                                        },
                                                                        phone.trim().ifEmpty {
                                                                                null
                                                                        },
                                                                        code.trim().ifEmpty { null }
                                                                )
                                                        }
                                                },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                        ) { Text(if (isEdit) "Simpan" else "Tambah") }
                                }
                        }
                }
        }
}

/** Dialog for editing company settings with logo upload */
@Composable
fun CompanySettingsDialog(
        initialName: String = "",
        initialAddress: String = "",
        initialPhone: String = "",
        initialFax: String = "",
        initialLogoPath: String? = null,
        onDismiss: () -> Unit,
        onConfirm:
                (
                        name: String,
                        address: String,
                        phone: String,
                        fax: String,
                        logoPath: String?) -> Unit,
        onSelectLogo: (onLogoSelected: (String?) -> Unit) -> Unit
) {
        var name by remember { mutableStateOf(initialName) }
        var address by remember { mutableStateOf(initialAddress) }
        var phone by remember { mutableStateOf(initialPhone) }
        var fax by remember { mutableStateOf(initialFax) }
        var logoPath by remember { mutableStateOf(initialLogoPath) }

        Dialog(onDismissRequest = onDismiss) {
                Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                )
                ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                        text = "Data Perusahaan",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Nama Perusahaan") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                        value = address,
                                        onValueChange = { address = it },
                                        label = { Text("Alamat") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                                value = phone,
                                                onValueChange = { phone = it },
                                                label = { Text("No. Telepon") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                                value = fax,
                                                onValueChange = { fax = it },
                                                label = { Text("No. Fax") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                        )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Logo section
                                Text(
                                        text = "Logo Perusahaan",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                        text = "Ukuran yang disarankan: 200 x 80 pixel (PNG/JPG)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Image Preview
                                if (logoPath != null) {
                                        val imageBitmap =
                                                remember(logoPath) {
                                                        try {
                                                                val file = java.io.File(logoPath!!)
                                                                if (file.exists()) {
                                                                        val bufferedImage =
                                                                                javax.imageio
                                                                                        .ImageIO
                                                                                        .read(file)
                                                                        if (bufferedImage != null) {
                                                                                val baos =
                                                                                        java.io
                                                                                                .ByteArrayOutputStream()
                                                                                javax.imageio
                                                                                        .ImageIO
                                                                                        .write(
                                                                                                bufferedImage,
                                                                                                "png",
                                                                                                baos
                                                                                        )
                                                                                val bytes =
                                                                                        baos.toByteArray()
                                                                                androidx.compose.ui
                                                                                        .res
                                                                                        .loadImageBitmap(
                                                                                                bytes.inputStream()
                                                                                        )
                                                                        } else null
                                                                } else null
                                                        } catch (e: Exception) {
                                                                null
                                                        }
                                                }
                                        if (imageBitmap != null) {
                                                androidx.compose.foundation.Image(
                                                        bitmap = imageBitmap,
                                                        contentDescription = "Logo Preview",
                                                        modifier = Modifier.height(60.dp),
                                                        contentScale =
                                                                androidx.compose.ui.layout
                                                                        .ContentScale.Fit
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                        }
                                }

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                                if (logoPath != null) {
                                                        val fileName =
                                                                logoPath!!
                                                                        .substringAfterLast("/")
                                                                        .substringAfterLast("\\")
                                                        Text(
                                                                text = "✓ $fileName",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                } else {
                                                        Text(
                                                                text = "Belum ada logo",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                        )
                                                }
                                        }
                                        Row {
                                                if (logoPath != null) {
                                                        OutlinedButton(
                                                                onClick = { logoPath = null }
                                                        ) { Text("Hapus") }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                OutlinedButton(
                                                        onClick = {
                                                                onSelectLogo { selectedPath ->
                                                                        logoPath = selectedPath
                                                                }
                                                        }
                                                ) {
                                                        Text(
                                                                if (logoPath != null) "Ganti"
                                                                else "Pilih Logo"
                                                        )
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                        OutlinedButton(onClick = onDismiss) { Text("Batal") }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                                onClick = {
                                                        onConfirm(
                                                                name,
                                                                address,
                                                                phone,
                                                                fax,
                                                                logoPath
                                                        )
                                                },
                                                colors =
                                                        ButtonDefaults.buttonColors(
                                                                containerColor =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                        ) { Text("Simpan") }
                                }
                        }
                }
        }
}
