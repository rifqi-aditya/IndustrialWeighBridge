package com.rifqi.industrialweighbridge.presentation.components.weighing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.db.SelectOpenTransactions
import com.rifqi.industrialweighbridge.presentation.utils.WeightFormatter
import com.rifqi.industrialweighbridge.presentation.viewmodel.WeighingViewModel

@Composable
fun SecondWeighingForm(
    viewModel: WeighingViewModel,
    openTransactions: List<SelectOpenTransactions>
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTransaction by remember { mutableStateOf<SelectOpenTransactions?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Text(
                text = "Tutup Transaksi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Pilih transaksi aktif untuk catat Berat 2",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (openTransactions.isEmpty()) {
                // Empty state
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tidak ada transaksi aktif",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Buka transaksi baru terlebih dahulu",
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = 0.7f
                                )
                        )
                    }
                }
            } else {
                // Transaction count badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Transaksi Aktif",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier =
                            Modifier.clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${openTransactions.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transaction list
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(openTransactions) { txn ->
                        val isSelected = selectedTransaction?.ticket_number == txn.ticket_number

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        if (isSelected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme
                                                .surfaceVariant
                                ),
                            elevation =
                                CardDefaults.cardElevation(
                                    defaultElevation = if (isSelected) 4.dp else 0.dp
                                ),
                            onClick = { selectedTransaction = txn }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTransaction = txn },
                                    colors =
                                        androidx.compose.material3.RadioButtonDefaults
                                            .colors(
                                                selectedColor =
                                                    MaterialTheme.colorScheme
                                                        .onPrimary,
                                                unselectedColor =
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                            )
                                )
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = txn.ticket_number,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                            if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary
                                    )
                                    Row {
                                        Icon(
                                            Icons.Default.LocalShipping,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint =
                                                if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimary
                                                        .copy(alpha = 0.8f)
                                                else
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = txn.plate_number ?: "-",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color =
                                                if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimary
                                                        .copy(alpha = 0.8f)
                                                else
                                                    MaterialTheme.colorScheme
                                                        .onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Berat 1",
                                        style = MaterialTheme.typography.labelSmall,
                                        color =
                                            if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                                    .copy(alpha = 0.7f)
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text =
                                            WeightFormatter.formatWeight(
                                                txn.weigh_in_weight
                                            ),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                            if (isSelected)
                                                MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                                !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIMPAN BERAT 2 & TUTUP", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
