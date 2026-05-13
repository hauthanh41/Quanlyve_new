package com.example.qlydatve.view.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.viewmodel.DashboardViewModel

private val NavyColor   = Color(0xFF1A2B4A)
private val BgColor     = Color(0xFFF4F6FA)
private val CardBg      = Color.White
private val HintColor   = Color(0xFF9AA5B8)
private val GreenColor  = Color(0xFF2E7D32)
private val RedColor    = Color(0xFFE53935)
private val BlueColor   = Color(0xFF1565C0)

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        Text(
            "Tổng quan hệ thống",
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyColor
        )
        Text(
            "Thống kê hoạt động chuyến bay và đặt vé.",
            fontSize = 13.sp, color = HintColor
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = NavyColor) }
        } else {
            // Stat cards — vertical stack like the design
            DashboardStatCard(
                label = "TỔNG CHUYẾN BAY",
                value = uiState.totalFlights.toString(),
                icon = Icons.Default.Send,
                valueColor = NavyColor,
                iconBg = Color(0xFFE8EEF7)
            )
            DashboardStatCard(
                label = "TỔNG ĐẶT VÉ",
                value = uiState.totalBookings.toString(),
                icon = Icons.Default.ConfirmationNumber,
                valueColor = BlueColor,
                iconBg = Color(0xFFE3F0FF)
            )
            DashboardStatCard(
                label = "ĐÃ XÁC NHẬN",
                value = uiState.confirmedBookings.toString(),
                icon = Icons.Default.CheckCircle,
                valueColor = GreenColor,
                iconBg = Color(0xFFE8F5E9)
            )
            DashboardStatCard(
                label = "ĐÃ HỦY",
                value = uiState.cancelledBookings.toString(),
                icon = Icons.Default.Cancel,
                valueColor = RedColor,
                iconBg = Color(0xFFFFF0F0)
            )

            // Quick summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tỷ lệ xác nhận", fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold, color = NavyColor)
                    val rate = if (uiState.totalBookings > 0)
                        uiState.confirmedBookings.toFloat() / uiState.totalBookings
                    else 0f
                    LinearProgressIndicator(
                        progress = { rate },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = GreenColor,
                        trackColor = Color(0xFFE8F5E9)
                    )
                    Text(
                        "%.0f%%".format(rate * 100),
                        fontSize = 13.sp, color = GreenColor, fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        uiState.error?.let {
            Text(it, color = RedColor, fontSize = 13.sp)
        }
    }
}

@Composable
private fun DashboardStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color,
    iconBg: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(12.dp))
            ) {
                Icon(icon, contentDescription = label,
                    tint = valueColor, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(label, fontSize = 11.sp, color = HintColor, letterSpacing = 0.5.sp)
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = valueColor, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}
