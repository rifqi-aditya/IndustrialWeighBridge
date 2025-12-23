package com.rifqi.industrialweighbridge.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Screen Master Data yang menggabungkan Driver, Kendaraan, dan Produk dalam satu halaman dengan tab
 * navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterDataScreen() {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs =
        listOf(
            MasterDataTab("Driver", Icons.Default.Person),
            MasterDataTab("Kendaraan", Icons.Default.LocalShipping),
            MasterDataTab("Produk", Icons.Default.Inventory2)
        )

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Master Data",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tab Row (Using SecondaryTabRow as TabRow is deprecated)
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight =
                                if (selectedTabIndex == index)
                                    FontWeight.Bold
                                else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint =
                                if (selectedTabIndex == index)
                                    MaterialTheme.colorScheme
                                        .primary
                                else
                                    MaterialTheme.colorScheme
                                        .onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Spacing between TabRow and content
        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> DriverListContent()
            1 -> VehicleListContent()
            2 -> ProductListContent()
        }
    }
}

private data class MasterDataTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
