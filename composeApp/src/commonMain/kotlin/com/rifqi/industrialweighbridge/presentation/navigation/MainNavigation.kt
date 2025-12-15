package com.rifqi.industrialweighbridge.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator

@Composable
fun MainNavigationScreen() {
    TabNavigator(DashboardTab) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Side Navigation Rail
            NavigationSideBar()

            // Main Content Area
            Box(
                    modifier =
                            Modifier.fillMaxHeight()
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(16.dp)
            ) { CurrentTab() }
        }
    }
}

@Composable
private fun NavigationSideBar() {
    val tabNavigator = LocalTabNavigator.current

    NavigationRail(
            modifier = Modifier.fillMaxHeight().width(100.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationRailTabItem(WeighingTab)
        NavigationRailTabItem(HistoryTab)
        NavigationRailTabItem(DashboardTab)
        NavigationRailTabItem(DriversTab)
        NavigationRailTabItem(VehiclesTab)
        NavigationRailTabItem(ProductsTab)
        NavigationRailTabItem(SettingsTab)
    }
}

@Composable
private fun NavigationRailTabItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current == tab

    NavigationRailItem(
            selected = isSelected,
            onClick = { tabNavigator.current = tab },
            icon = {
                tab.options.icon?.let { painter ->
                    Icon(painter = painter, contentDescription = tab.options.title)
                }
            },
            label = { Text(text = tab.options.title, style = MaterialTheme.typography.labelSmall) },
            colors =
                    NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
            alwaysShowLabel = true
    )
}
