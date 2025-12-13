package com.rifqi.industrialweighbridge.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.rifqi.industrialweighbridge.presentation.screens.DashboardScreen
import com.rifqi.industrialweighbridge.presentation.screens.DriverListScreen
import com.rifqi.industrialweighbridge.presentation.screens.SettingsScreen
import com.rifqi.industrialweighbridge.presentation.screens.VehicleListScreen

// ============================================
// Dashboard Tab
// ============================================
object DashboardTab : Tab {
    private fun readResolve(): Any = DashboardTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember { TabOptions(index = 0u, title = "Dashboard", icon = icon) }
        }

    @Composable
    override fun Content() {
        DashboardScreen()
    }
}

// ============================================
// Drivers Tab
// ============================================
object DriversTab : Tab {
    private fun readResolve(): Any = DriversTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Person)
            return remember { TabOptions(index = 1u, title = "Drivers", icon = icon) }
        }

    @Composable
    override fun Content() {
        DriverListScreen()
    }
}

// ============================================
// Vehicles Tab
// ============================================
object VehiclesTab : Tab {
    private fun readResolve(): Any = VehiclesTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.LocalShipping)
            return remember { TabOptions(index = 2u, title = "Vehicles", icon = icon) }
        }

    @Composable
    override fun Content() {
        VehicleListScreen()
    }
}

// ============================================
// Settings Tab
// ============================================
object SettingsTab : Tab {
    private fun readResolve(): Any = SettingsTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Settings)
            return remember { TabOptions(index = 3u, title = "Settings", icon = icon) }
        }

    @Composable
    override fun Content() {
        SettingsScreen()
    }
}
