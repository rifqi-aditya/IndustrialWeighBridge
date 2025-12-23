package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rifqi.industrialweighbridge.presentation.components.weighing.DigitalWeightDisplay
import com.rifqi.industrialweighbridge.presentation.components.weighing.FirstWeighingForm
import com.rifqi.industrialweighbridge.presentation.components.weighing.SecondWeighingForm
import com.rifqi.industrialweighbridge.presentation.viewmodel.WeighingViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab selection with PrimaryTabRow (non-deprecated)
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "BUKA TRANSAKSI",
                            fontWeight =
                                if (selectedTab == 0) FontWeight.Bold
                                else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "TUTUP TRANSAKSI",
                            fontWeight =
                                if (selectedTab == 1) FontWeight.Bold
                                else FontWeight.Normal
                        )
                    }
                )
            }

            // Two-column layout for wider screens
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column - Weight Display
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    DigitalWeightDisplay(
                        weight = uiState.currentWeight,
                        isStable = uiState.isStable,
                        isManualMode = uiState.isManualMode,
                        onManualModeToggle = { viewModel.toggleManualMode() },
                        onWeightChange = { viewModel.setWeight(it) }
                    )
                }

                // Right Column - Form
                Column(
                    modifier =
                        Modifier.weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        0 -> FirstWeighingForm(viewModel)
                        1 -> SecondWeighingForm(viewModel, uiState.openTransactions)
                    }
                }
            }
        }
    }
}
