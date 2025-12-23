package com.rifqi.industrialweighbridge.presentation.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.data.repository.SettingsRepository
import com.rifqi.industrialweighbridge.db.SelectAllTransactions
import com.rifqi.industrialweighbridge.domain.utils.DateTimeUtils
import com.rifqi.industrialweighbridge.presentation.components.SearchBar
import com.rifqi.industrialweighbridge.presentation.utils.WeightFormatter
import com.rifqi.industrialweighbridge.presentation.viewmodel.WeighingViewModel
import org.koin.compose.koinInject

enum class TransactionFilter {
    ALL,
    OPEN,
    CLOSED
}

@Composable
fun TransactionHistoryScreen() {
    val viewModel: WeighingViewModel = koinInject()
    val settingsRepository: SettingsRepository = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TransactionFilter.ALL) }

    // Filter transactions
    val filteredTransactions =
            uiState.allTransactions.filter { txn ->
                val matchesSearch =
                        searchQuery.isEmpty() ||
                                txn.ticket_number.contains(searchQuery, ignoreCase = true) ||
                                (txn.plate_number?.contains(searchQuery, ignoreCase = true) ==
                                        true) ||
                                (txn.driver_name?.contains(searchQuery, ignoreCase = true) ==
                                        true) ||
                                (txn.product_name?.contains(searchQuery, ignoreCase = true) == true)

                val matchesFilter =
                        when (filter) {
                            TransactionFilter.ALL -> true
                            TransactionFilter.OPEN -> txn.status.name == "OPEN"
                            TransactionFilter.CLOSED -> txn.status.name == "CLOSED"
                        }

                matchesSearch && matchesFilter
            }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // Title
            Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Cari ticket, plat, driver, produk..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                        selected = filter == TransactionFilter.ALL,
                        onClick = { filter = TransactionFilter.ALL },
                        label = { Text("Semua") }
                )
                FilterChip(
                        selected = filter == TransactionFilter.OPEN,
                        onClick = { filter = TransactionFilter.OPEN },
                        label = { Text("Open") }
                )
                FilterChip(
                        selected = filter == TransactionFilter.CLOSED,
                        onClick = { filter = TransactionFilter.CLOSED },
                        label = { Text("Closed") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats summary
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                        text = "Total: ${filteredTransactions.size} transaksi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction list
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                            text =
                                    if (searchQuery.isNotEmpty())
                                            "Tidak ada hasil untuk \"$searchQuery\""
                                    else "Belum ada transaksi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredTransactions) { txn ->
                        TransactionCard(
                                transaction = txn,
                                onPrint = { transaction ->
                                    com.rifqi.industrialweighbridge.presentation.print
                                            .printTransaction(transaction, settingsRepository)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
        transaction: SelectAllTransactions,
        onPrint: ((SelectAllTransactions) -> Unit)? = null
) {
    val isOpen = transaction.status.name == "OPEN"

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (isOpen)
                                            MaterialTheme.colorScheme.secondaryContainer.copy(
                                                    alpha = 0.5f
                                            )
                                    else MaterialTheme.colorScheme.surfaceVariant
                    ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Ticket + Status
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = transaction.ticket_number,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                            imageVector =
                                    if (isOpen) Icons.Default.Schedule
                                    else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isOpen) Color(0xFFFF9800) else Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                            text = if (isOpen) "OPEN" else "CLOSED",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isOpen) Color(0xFFFF9800) else Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle & Driver
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = "Kendaraan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = transaction.plate_number ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = "Driver",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = transaction.driver_name ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product
            Text(
                    text = "Produk",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                    text = transaction.product_name ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Weights
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                            text = "Berat Masuk",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = WeightFormatter.formatWeight(transaction.weigh_in_weight),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                            text = "Berat Keluar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text =
                                    transaction.weigh_out_weight?.let {
                                        WeightFormatter.formatWeight(it)
                                    }
                                            ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                    )
                }

                Column {
                    Text(
                            text = "Netto",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                            text = transaction.net_weight?.let { WeightFormatter.formatWeight(it) }
                                            ?: "-",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamps
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                        text =
                                "In: ${DateTimeUtils.formatForDisplay(transaction.weigh_in_timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                transaction.weigh_out_timestamp?.let {
                    Text(
                            text = "Out: ${DateTimeUtils.formatForDisplay(it)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Print button (only for closed transactions)
            if (!isOpen && onPrint != null) {
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.OutlinedButton(
                        onClick = { onPrint(transaction) },
                        modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Print,
                            contentDescription = "Print"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cetak Tiket")
                }
            }

            // Manual indicator
            if (transaction.is_manual == 1L) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                        text = "⚠️ Input Manual",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800)
                )
            }
        }
    }
}
