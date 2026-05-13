package com.example.qlydatve.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen { DASHBOARD, FLIGHTS, BOOKINGS, PROFILE }

data class NavItem(val label: String, val icon: ImageVector, val screen: Screen)

val navItems = listOf(
    NavItem("Dashboard", Icons.Default.Home, Screen.DASHBOARD),
    NavItem("Chuyến bay", Icons.Default.Send, Screen.FLIGHTS),
    NavItem("Đặt vé", Icons.Default.DateRange, Screen.BOOKINGS),
    NavItem("Hồ sơ", Icons.Default.Person, Screen.PROFILE)
)

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentScreen == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
