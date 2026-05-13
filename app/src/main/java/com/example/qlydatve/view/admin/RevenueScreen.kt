package com.example.qlydatve.view.admin

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.viewmodel.RevenueViewModel

// ── Colors ────────────────────────────────────────────────────────────────────
private val NavyColor   = Color(0xFF1A2B4A)
private val BgColor     = Color(0xFFF4F6FA)
private val CardBg      = Color.White
private val HintColor   = Color(0xFF9AA5B8)
private val BorderColor = Color(0xFFDDE3ED)
private val GreenColor  = Color(0xFF2E7D32)
private val BlueColor   = Color(0xFF1565C0)
private val OrangeColor = Color(0xFFE65100)
private val BarColor    = Color(0xFF90CAF9)
private val BarSelected = Color(0xFF1565C0)

@Composable
fun RevenueScreen(
    modifier: Modifier = Modifier,
    viewModel: RevenueViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedPeriod by remember { mutableStateOf(0) } // 0=Tháng, 1=Năm, 2=Tùy chọn

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Title ──────────────────────────────────────────────────────────
        Text("Báo cáo Tài chính", fontSize = 22.sp,
            fontWeight = FontWeight.Bold, color = NavyColor)
        Text("Phân tích chi tiết doanh thu thống kê theo thời gian thực.",
            fontSize = 13.sp, color = HintColor)

        // ── Period tabs ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE8EEF7))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("4 MÃ\nTHÁNG", "NĂM\nNAY", "TÙNG\nCHỌN").forEachIndexed { i, label ->
                val selected = selectedPeriod == i
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) CardBg else Color.Transparent)
                        .padding(vertical = 8.dp)
                ) {
                    Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (selected) NavyColor else HintColor,
                        textAlign = TextAlign.Center, lineHeight = 13.sp)
                }
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyColor)
            }
            return@Column
        }

        // ── KPI cards ──────────────────────────────────────────────────────
        RevenueKpiCard(
            label = "TỔNG DOANH THU (VNĐ)",
            value = formatAmount(uiState.totalRevenue),
            change = "+12.5% so với tháng trước",
            changePositive = true,
            icon = Icons.Default.TrendingUp,
            iconBg = Color(0xFFE3F0FF),
            iconTint = BlueColor
        )
        RevenueKpiCard(
            label = "LỢI NHUẬN TRÊN MỖI GHẾ (VNĐ)",
            value = formatAmount(uiState.revenuePerPassenger),
            change = "▲ 4.2%",
            changePositive = true,
            icon = Icons.Default.AirlineSeatReclineNormal,
            iconBg = Color(0xFFE8F5E9),
            iconTint = GreenColor
        )
        RevenueKpiCard(
            label = "HỆ SỐ TẢI TRUNG BÌNH (%)",
            value = "%.1f%%".format(uiState.loadFactor),
            change = "▼ 1.1% tháng này",
            changePositive = false,
            icon = Icons.Default.Speed,
            iconBg = Color(0xFFFFF8E1),
            iconTint = OrangeColor
        )
        RevenueKpiCard(
            label = "GIÁ VÉ TRUNG BÌNH (VNĐ)",
            value = formatAmount(uiState.avgTicketPrice),
            change = "▲ 3.1%",
            changePositive = true,
            icon = Icons.Default.ConfirmationNumber,
            iconBg = Color(0xFFF3E5F5),
            iconTint = Color(0xFF6A1B9A)
        )

        // ── Bar chart — monthly revenue ────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Xu hướng Doanh thu Tháng",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                        Text("Báo cáo doanh thu theo từng tháng ghi nhận.",
                            fontSize = 11.sp, color = HintColor)
                    }
                    // Legend
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(BarSelected, "Tháng")
                        LegendDot(BarColor, "Năm trước")
                    }
                }
                MonthlyBarChart(data = uiState.monthlyRevenue)
            }
        }

        // ── Class distribution ─────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Phân bổ Hạng Ghế",
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                Text("Tỷ lệ doanh thu theo hạng ghế.",
                    fontSize = 11.sp, color = HintColor)

                val dist = uiState.classDistribution
                ClassBar("HẠNG THƯỜNG GIÁ", dist["ECONOMY"] ?: 0f, Color(0xFF1565C0))
                ClassBar("HẠNG THƯỜNG GIÁ CAO CẤP", dist["ECONOMY_PLUS"] ?: 0f, Color(0xFF42A5F5))
                ClassBar("HẠNG THƯƠNG GIA", dist["BUSINESS"] ?: 0f, Color(0xFF0D47A1))
                ClassBar("DỊCH VỤ BỔ TRỢ", dist["OTHER"] ?: 0f, Color(0xFF90CAF9))

                // Insight box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE3F0FF)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Lightbulb, null,
                            tint = BlueColor, modifier = Modifier.size(18.dp))
                        Text(
                            "Doanh thu hạng thương gia chiếm 150% doanh thu ghế phổ thông dù chỉ chiếm khoảng 9% số ghế.",
                            fontSize = 12.sp, color = NavyColor, lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // ── Top routes ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tuyến bay có doanh suất cao nhất",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                    Text("Xem tất cả", fontSize = 12.sp, color = BlueColor)
                }

                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F4FA), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("TUYẾN BAY", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = NavyColor,
                        modifier = Modifier.weight(1.5f))
                    Text("SỐ CHUYẾN", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = NavyColor,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("DOANH THU", fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, color = NavyColor,
                        modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                if (uiState.topRoutes.isEmpty()) {
                    // Demo rows
                    val demoRoutes = listOf(
                        Triple("SGN → HAN", 74, 1_370_000_000.0),
                        Triple("HAN → SGN", 21, 880_000_000.0),
                        Triple("SGN → PQC", 14, 560_000_000.0),
                        Triple("HAN → DIN", 10, 320_000_000.0)
                    )
                    demoRoutes.forEachIndexed { i, (route, count, rev) ->
                        RouteRow(rank = i + 1, route = route, count = count, revenue = rev)
                        if (i < demoRoutes.lastIndex)
                            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                    }
                } else {
                    uiState.topRoutes.forEachIndexed { i, (route, count, rev) ->
                        RouteRow(rank = i + 1, route = route, count = count, revenue = rev)
                        if (i < uiState.topRoutes.lastIndex)
                            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text("Xem tất cả tuyến bay", fontSize = 12.sp,
                    color = HintColor, modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── KPI card ──────────────────────────────────────────────────────────────────
@Composable
private fun RevenueKpiCard(
    label: String,
    value: String,
    change: String,
    changePositive: Boolean,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
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
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, RoundedCornerShape(12.dp))
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 10.sp, color = HintColor, letterSpacing = 0.3.sp)
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = NavyColor, modifier = Modifier.padding(top = 2.dp))
                Text(change, fontSize = 11.sp,
                    color = if (changePositive) GreenColor else Color(0xFFE53935),
                    modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// ── Monthly bar chart ─────────────────────────────────────────────────────────
@Composable
private fun MonthlyBarChart(data: List<Pair<String, Double>>) {
    if (data.isEmpty()) return
    val maxVal = data.maxOf { it.second }.coerceAtLeast(1.0)
    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { i, (label, value) ->
            val fraction = (value / maxVal).toFloat().coerceIn(0.05f, 1f)
            val isCurrentMonth = (i + 1) == currentMonth
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (isCurrentMonth) BarSelected else BarColor)
                    )
                }
                Text(label, fontSize = 8.sp, color = HintColor,
                    modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

// ── Class distribution bar ────────────────────────────────────────────────────
@Composable
private fun ClassBar(label: String, fraction: Float, color: Color) {
    val pct = (fraction * 100).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = NavyColor)
            Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE8EEF7))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

// ── Route row ─────────────────────────────────────────────────────────────────
@Composable
private fun RouteRow(rank: Int, route: String, count: Int, revenue: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        when (rank) {
                            1 -> Color(0xFFFFF8E1)
                            2 -> Color(0xFFF5F5F5)
                            3 -> Color(0xFFFBE9E7)
                            else -> Color(0xFFF4F6FA)
                        },
                        RoundedCornerShape(6.dp)
                    )
            ) {
                Text(rank.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = when (rank) {
                        1 -> Color(0xFFB8860B)
                        2 -> Color(0xFF607D8B)
                        3 -> Color(0xFFE65100)
                        else -> HintColor
                    })
            }
            Text(route, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyColor)
        }
        Text("$count\nchuyến bay", fontSize = 11.sp, color = HintColor,
            modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
            lineHeight = 15.sp)
        Text(formatAmount(revenue), fontSize = 12.sp,
            fontWeight = FontWeight.Bold, color = NavyColor,
            modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

// ── Legend dot ────────────────────────────────────────────────────────────────
@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp)
            .clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 10.sp, color = HintColor)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun formatAmount(amount: Double): String = when {
    amount >= 1_000_000_000 -> "${"%.2f".format(amount / 1_000_000_000)}B"
    amount >= 1_000_000     -> "${"%.2f".format(amount / 1_000_000)}M"
    amount >= 1_000         -> "${"%.1f".format(amount / 1_000)}K"
    else                    -> "%,.0f".format(amount)
}
