package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.data.repository.SettingsRepository
import com.rifqi.industrialweighbridge.domain.model.CompanySettings
import com.rifqi.industrialweighbridge.domain.model.PaperSize
import com.rifqi.industrialweighbridge.domain.model.PrintSettings
import com.rifqi.industrialweighbridge.presentation.theme.LocalThemeState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
        val themeState = LocalThemeState.current
        val settingsRepository: SettingsRepository = koinInject()
        val scrollState = rememberScrollState()

        // Company Settings State
        var companyName by remember { mutableStateOf("") }
        var companyAddress by remember { mutableStateOf("") }
        var companyPhone by remember { mutableStateOf("") }
        var companyFax by remember { mutableStateOf("") }
        var companySaved by remember { mutableStateOf(false) }

        // Print Settings State
        var selectedPaperSize by remember { mutableStateOf(PaperSize.A4) }
        var showLogo by remember { mutableStateOf(true) }
        var footerText by remember { mutableStateOf("") }
        var printSaved by remember { mutableStateOf(false) }
        var paperSizeExpanded by remember { mutableStateOf(false) }

        // Load settings on start
        LaunchedEffect(Unit) {
                val company = settingsRepository.loadCompanySettings()
                companyName = company.companyName
                companyAddress = company.companyAddress
                companyPhone = company.companyPhone
                companyFax = company.companyFax

                val print = settingsRepository.loadPrintSettings()
                selectedPaperSize = print.paperSize
                showLogo = print.showLogo
                footerText = print.footerText
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                // Header
                Text(
                        text = "Pengaturan",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 24.dp)
                )

                // ==================== COMPANY SETTINGS ====================
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
                                                imageVector = Icons.Default.Business,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                                text = "Data Perusahaan",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                        )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                        value = companyName,
                                        onValueChange = {
                                                companyName = it
                                                companySaved = false
                                        },
                                        label = { Text("Nama Perusahaan") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                        value = companyAddress,
                                        onValueChange = {
                                                companyAddress = it
                                                companySaved = false
                                        },
                                        label = { Text("Alamat") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                                value = companyPhone,
                                                onValueChange = {
                                                        companyPhone = it
                                                        companySaved = false
                                                },
                                                label = { Text("No. Telepon") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                                value = companyFax,
                                                onValueChange = {
                                                        companyFax = it
                                                        companySaved = false
                                                },
                                                label = { Text("No. Fax") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                        )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                        onClick = {
                                                settingsRepository.saveCompanySettings(
                                                        CompanySettings(
                                                                companyName = companyName,
                                                                companyAddress = companyAddress,
                                                                companyPhone = companyPhone,
                                                                companyFax = companyFax
                                                        )
                                                )
                                                companySaved = true
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                ) {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                                if (companySaved) "Tersimpan ✓"
                                                else "Simpan Data Perusahaan"
                                        )
                                }
                        }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==================== PRINT SETTINGS ====================
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
                                                style = MaterialTheme.typography.titleMedium,
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
                                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                                                expanded = paperSizeExpanded
                                                        )
                                                },
                                                modifier = Modifier.fillMaxWidth().menuAnchor()
                                        )
                                        ExposedDropdownMenu(
                                                expanded = paperSizeExpanded,
                                                onDismissRequest = { paperSizeExpanded = false }
                                        ) {
                                                PaperSize.entries.forEach { size ->
                                                        DropdownMenuItem(
                                                                text = { Text(size.displayName) },
                                                                onClick = {
                                                                        selectedPaperSize = size
                                                                        paperSizeExpanded = false
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
                                                                paperSize = selectedPaperSize,
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

                // ==================== THEME TOGGLE ====================
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
