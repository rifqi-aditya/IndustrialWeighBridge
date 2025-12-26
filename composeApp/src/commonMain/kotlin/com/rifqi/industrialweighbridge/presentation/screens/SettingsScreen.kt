package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.rifqi.industrialweighbridge.data.repository.SettingsRepository
import com.rifqi.industrialweighbridge.domain.model.CompanySettings
import com.rifqi.industrialweighbridge.domain.model.PaperSize
import com.rifqi.industrialweighbridge.domain.model.PrintSettings
import com.rifqi.industrialweighbridge.engine.AuthState
import com.rifqi.industrialweighbridge.engine.AuthenticationManager
import com.rifqi.industrialweighbridge.presentation.components.CompanySettingsDialog
import com.rifqi.industrialweighbridge.presentation.theme.LocalThemeState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val themeState = LocalThemeState.current
    val settingsRepository: SettingsRepository = koinInject()
    val scrollState = rememberScrollState()

    // Auth state for role-based visibility
    val authManager = koinInject<AuthenticationManager>()
    val authState by authManager.authState.collectAsState()
    val currentUser = (authState as? AuthState.Authenticated)?.user
    val isAdmin = currentUser?.isAdmin == true

    // Company Settings State
    var companySettings by remember { mutableStateOf(CompanySettings()) }
    var showCompanyDialog by remember { mutableStateOf(false) }

    // Print Settings State
    var selectedPaperSize by remember { mutableStateOf(PaperSize.A4) }
    var showLogo by remember { mutableStateOf(true) }
    var footerText by remember { mutableStateOf("") }
    var printSaved by remember { mutableStateOf(false) }
    var paperSizeExpanded by remember { mutableStateOf(false) }

    // Load settings on start
    LaunchedEffect(Unit) {
        companySettings = settingsRepository.loadCompanySettings()

        val print = settingsRepository.loadPrintSettings()
        selectedPaperSize = print.paperSize
        showLogo = print.showLogo
        footerText = print.footerText
    }

    // User Management State (Admin Only)
    var showUserManagement by remember { mutableStateOf(false) }

    // Audit Log State (Admin Only)
    var showAuditLog by remember { mutableStateOf(false) }

    // User Management Dialog (Fullscreen but leaves space for title bar)
    if (showUserManagement) {
        Dialog(
            onDismissRequest = { showUserManagement = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier =
                    Modifier.fillMaxWidth().fillMaxHeight(0.92f).padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kelola Pengguna",
                            style =
                                MaterialTheme.typography
                                    .headlineSmall
                        )
                        OutlinedButton(
                            onClick = { showUserManagement = false }
                        ) { Text("Tutup") }
                    }
                    UserManagementScreen()
                }
            }
        }
    }

    // Audit Log Dialog (Fullscreen but leaves space for title bar)
    if (showAuditLog) {
        Dialog(
            onDismissRequest = { showAuditLog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier =
                    Modifier.fillMaxWidth().fillMaxHeight(0.92f).padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) { AuditLogScreen(onBack = { showAuditLog = false }) }
        }
    }

    // Company Settings Dialog
    if (showCompanyDialog) {
        CompanySettingsDialog(
            initialName = companySettings.companyName,
            initialAddress = companySettings.companyAddress,
            initialPhone = companySettings.companyPhone,
            initialFax = companySettings.companyFax,
            initialLogoPath = companySettings.logoPath,
            onDismiss = { showCompanyDialog = false },
            onConfirm = { name, address, phone, fax, logoPath ->
                val newSettings =
                    CompanySettings(
                        companyName = name,
                        companyAddress = address,
                        companyPhone = phone,
                        companyFax = fax,
                        logoPath = logoPath
                    )
                settingsRepository.saveCompanySettings(newSettings)
                companySettings = newSettings
                showCompanyDialog = false
            },
            onSelectLogo = { onLogoSelected ->
                // Using native FileDialog for Windows native file picker
                val dialog =
                    java.awt.FileDialog(
                        null as java.awt.Frame?,
                        "Pilih Logo Perusahaan",
                        java.awt.FileDialog.LOAD
                    )
                dialog.setFilenameFilter { _, name ->
                    val lower = name.lowercase()
                    lower.endsWith(".png") ||
                            lower.endsWith(".jpg") ||
                            lower.endsWith(".jpeg")
                }
                dialog.isVisible = true
                if (dialog.file != null) {
                    onLogoSelected(dialog.directory + dialog.file)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        // Header
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // ==================== COMPANY SETTINGS (Admin Only) ====================
        if (isAdmin) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.Business,
                                contentDescription = null,
                                tint =
                                    MaterialTheme.colorScheme
                                        .primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Data Perusahaan",
                                style =
                                    MaterialTheme.typography
                                        .titleMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )
                        }
                        OutlinedButton(
                            onClick = { showCompanyDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Company Info Summary
                    if (companySettings.companyName.isNotBlank()) {
                        Text(
                            text = companySettings.companyName,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (companySettings.companyAddress.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text =
                                    companySettings
                                        .companyAddress,
                                style =
                                    MaterialTheme.typography
                                        .bodyMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                        if (companySettings.companyPhone.isNotBlank() ||
                            companySettings.companyFax
                                .isNotBlank()
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val contactInfo = buildString {
                                if (companySettings.companyPhone
                                        .isNotBlank()
                                ) {
                                    append(
                                        "Telp: ${companySettings.companyPhone}"
                                    )
                                }
                                if (companySettings.companyFax
                                        .isNotBlank()
                                ) {
                                    if (isNotEmpty())
                                        append(" | ")
                                    append(
                                        "Fax: ${companySettings.companyFax}"
                                    )
                                }
                            }
                            Text(
                                text = contactInfo,
                                style =
                                    MaterialTheme.typography
                                        .bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                        if (companySettings.logoPath != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✓ Logo tersimpan",
                                style =
                                    MaterialTheme.typography
                                        .bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .primary
                            )
                        }
                    } else {
                        Text(
                            text = "Belum ada data perusahaan",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                        Text(
                            text =
                                "Klik Edit untuk menambahkan data perusahaan",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== PRINT SETTINGS (Admin Only) ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pengaturan Tiket",
                            style =
                                MaterialTheme.typography
                                    .titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Paper Size Dropdown
                    ExposedDropdownMenuBox(
                        expanded = paperSizeExpanded,
                        onExpandedChange = {
                            paperSizeExpanded = !paperSizeExpanded
                        }
                    ) {
                        OutlinedTextField(
                            value = selectedPaperSize.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ukuran Kertas") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults
                                    .TrailingIcon(
                                        expanded =
                                            paperSizeExpanded
                                    )
                            },
                            modifier =
                                Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = paperSizeExpanded,
                            onDismissRequest = {
                                paperSizeExpanded = false
                            }
                        ) {
                            PaperSize.entries.forEach { size ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            size.displayName
                                        )
                                    },
                                    onClick = {
                                        selectedPaperSize =
                                            size
                                        paperSizeExpanded =
                                            false
                                        printSaved = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Show Logo Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tampilkan Logo",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = showLogo,
                            onCheckedChange = {
                                showLogo = it
                                printSaved = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = footerText,
                        onValueChange = {
                            footerText = it
                            printSaved = false
                        },
                        label = { Text("Footer Tiket") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Contoh: Terima kasih telah menggunakan jasa kami"
                            )
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            settingsRepository.savePrintSettings(
                                PrintSettings(
                                    paperSize =
                                        selectedPaperSize,
                                    showLogo = showLogo,
                                    footerText = footerText
                                )
                            )
                            printSaved = true
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (printSaved) "Tersimpan ✓"
                            else "Simpan Pengaturan Tiket"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== USER MANAGEMENT (Admin Only) ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = "Kelola Pengguna",
                                style =
                                    MaterialTheme.typography
                                        .titleMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )
                            Text(
                                text = "Tambah atau hapus pengguna",
                                style =
                                    MaterialTheme.typography
                                        .bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(onClick = { showUserManagement = true }) {
                        Text("Kelola")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== AUDIT LOG (Admin Only) ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = "Audit Log",
                                style =
                                    MaterialTheme.typography
                                        .titleMedium,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurface
                            )
                            Text(
                                text =
                                    "Lihat riwayat aktivitas sistem",
                                style =
                                    MaterialTheme.typography
                                        .bodySmall,
                                color =
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(onClick = { showAuditLog = true }) {
                        Text("Lihat")
                    }
                }
            }
        } // End isAdmin block

        Spacer(modifier = Modifier.height(16.dp))

        // ==================== THEME TOGGLE (All Users) ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector =
                            if (themeState.isDarkMode)
                                Icons.Default.DarkMode
                            else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = "Mode Gelap",
                            style =
                                MaterialTheme.typography
                                    .titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text =
                                if (themeState.isDarkMode) "Aktif"
                                else "Nonaktif",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = themeState.isDarkMode,
                    onCheckedChange = { themeState.toggleTheme() },
                    colors =
                        SwitchDefaults.colors(
                            checkedThumbColor =
                                MaterialTheme.colorScheme.primary,
                            checkedTrackColor =
                                MaterialTheme.colorScheme
                                    .primaryContainer
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==================== ACCOUNT SECTION ====================
        AccountSection()

        Spacer(modifier = Modifier.height(16.dp))

        // App Info
        Text(
            text = "Sistem Jembatan Timbang v1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/** Account section showing current user info and logout button. */
@Composable
private fun AccountSection() {
    val authManager = koinInject<AuthenticationManager>()
    val authState by authManager.authState.collectAsState()

    val currentUser = (authState as? AuthState.Authenticated)?.user

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = "Akun",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (currentUser != null) {
                        Text(
                            text =
                                "${currentUser.username} (${currentUser.role.name})",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = { authManager.logout() },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar")
            }
        }
    }
}
