package com.example.qlydatve.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlydatve.model.User
import com.example.qlydatve.view.dashboard.DashboardScreen
import com.example.qlydatve.view.flight.FlightScreen
import com.example.qlydatve.model.Flight

private val NavyColor = Color(0xFF1A2B4A)

enum class AdminScreen { DASHBOARD, FLIGHTS, TICKETS, CUSTOMERS, REVENUE, MESSAGING, PROFILE }

private data class AdminNavItem(
    val screen: AdminScreen,
    val label: String,
    val icon: ImageVector
)

private val adminNavItems = listOf(
    AdminNavItem(AdminScreen.DASHBOARD,  "Trang chủ",  Icons.Default.Home),
    AdminNavItem(AdminScreen.FLIGHTS,    "Chuyến bay", Icons.Default.Flight),
    AdminNavItem(AdminScreen.TICKETS,    "Vé",         Icons.Default.ConfirmationNumber),
    AdminNavItem(AdminScreen.CUSTOMERS,  "Khách hàng", Icons.Default.People),
    AdminNavItem(AdminScreen.MESSAGING,  "Tin nhắn",   Icons.Default.Message),
    AdminNavItem(AdminScreen.REVENUE,    "Doanh thu",  Icons.Default.BarChart),
    AdminNavItem(AdminScreen.PROFILE,    "Hồ sơ",      Icons.Default.Person)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApp(user: User, onLogout: () -> Unit) {
    var currentScreen by remember { mutableStateOf(AdminScreen.DASHBOARD) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                adminNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentScreen == item.screen,
                        onClick = { currentScreen = item.screen },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyColor,
                            selectedTextColor = NavyColor,
                            indicatorColor = Color(0xFFE8EEF7),
                            unselectedIconColor = Color(0xFF9AA5B8),
                            unselectedTextColor = Color(0xFF9AA5B8)
                        )
                    )
                }
            }
        }
    ) { padding ->
        when (currentScreen) {
            AdminScreen.DASHBOARD -> DashboardScreen(
                modifier = Modifier.padding(padding)
            )
            AdminScreen.FLIGHTS -> FlightScreen(
                isAdmin = true,
                onBookFlight = { /* admin không đặt vé */ },
                modifier = Modifier.padding(padding)
            )
            AdminScreen.TICKETS -> TicketManagementScreen(
                modifier = Modifier.padding(padding)
            )
            AdminScreen.CUSTOMERS -> CustomerManagementScreen(
                modifier = Modifier.padding(padding)
            )
            AdminScreen.REVENUE -> RevenueScreen(
                modifier = Modifier.padding(padding)
            )
            AdminScreen.MESSAGING -> MessagingScreen(
                adminId = user.id,
                modifier = Modifier.padding(padding)
            )
            AdminScreen.PROFILE -> AdminProfileScreen(
                user = user,
                onLogout = onLogout,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// ── Placeholder screens ───────────────────────────────────────────────────────
@Composable
private fun NotificationsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(Color(0xFFF4F6FA)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Notifications, null,
                tint = Color(0xFF9AA5B8), modifier = Modifier.size(64.dp))
            Text("Không có thông báo mới", color = Color(0xFF9AA5B8), fontSize = 15.sp)
        }
    }
}

@Composable
private fun AdminProfileScreen(
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6FA))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Hồ sơ quản trị", fontSize = 22.sp,
            fontWeight = FontWeight.Bold, color = NavyColor)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileRow(Icons.Default.Person, "Họ tên", user.fullName)
                HorizontalDivider(color = Color(0xFFEEF2F7))
                ProfileRow(Icons.Default.Email, "Email", user.email)
                HorizontalDivider(color = Color(0xFFEEF2F7))
                ProfileRow(Icons.Default.Phone, "Điện thoại", user.phone.ifBlank { "—" })
                HorizontalDivider(color = Color(0xFFEEF2F7))
                ProfileRow(Icons.Default.Shield, "Vai trò", user.role)
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, null,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Đăng xuất", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = NavyColor, modifier = Modifier.size(18.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF9AA5B8))
            Text(value, fontSize = 14.sp, color = NavyColor, fontWeight = FontWeight.Medium)
        }
    }
}
