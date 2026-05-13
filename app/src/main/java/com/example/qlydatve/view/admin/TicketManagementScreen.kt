package com.example.qlydatve.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.Booking
import com.example.qlydatve.viewmodel.BookingViewModel

// ── Colors ────────────────────────────────────────────────────────────────────
private val NavyColor    = Color(0xFF1A2B4A)
private val BgColor      = Color(0xFFF4F6FA)
private val CardBg       = Color.White
private val HintColor    = Color(0xFF9AA5B8)
private val BorderColor  = Color(0xFFDDE3ED)
private val GreenColor   = Color(0xFF2E7D32)
private val GreenBg      = Color(0xFFE8F5E9)
private val RedColor     = Color(0xFFE53935)
private val RedBg        = Color(0xFFFFF0F0)
private val OrangeColor  = Color(0xFFE65100)
private val OrangeBg     = Color(0xFFFFF8E1)
private val TableHeaderBg = Color(0xFFF0F4FA)

private const val PAGE_SIZE = 6

@Composable
fun TicketManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadBookings() }

    // Reset page when filter changes
    LaunchedEffect(searchQuery, selectedStatus) { currentPage = 0 }

    val filtered = uiState.bookings.filter { b ->
        val q = searchQuery.trim()
        val matchQuery = q.isBlank() ||
            b.fullName.contains(q, ignoreCase = true) ||
            b.id.toString().contains(q) ||
            b.flightCode.contains(q, ignoreCase = true)
        val matchStatus = selectedStatus.isBlank() || b.status == selectedStatus
        matchQuery && matchStatus
    }

    val totalPages = maxOf(1, (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE)
    val pageItems = filtered.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)

    // Stats
    val totalToday = uiState.bookings.size
    val activeTickets = uiState.bookings.count { it.status == "CONFIRMED" }
    val cancelled = uiState.bookings.count { it.status == "CANCELLED" }
    val revenue = uiState.bookings
        .filter { it.status == "CONFIRMED" }
        .sumOf { it.totalAmount }

    Column(
        modifier = modifier.fillMaxSize().background(BgColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Title ──────────────────────────────────────────────────────
            item {
                Text(
                    "Quản lý vé máy bay",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyColor
                )
                Text(
                    "Tra cứu, điều chỉnh và xử lý hoàn vé cho hành khách.",
                    fontSize = 13.sp, color = HintColor, modifier = Modifier.padding(top = 2.dp)
                )
            }

            // ── Search + Filter ────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm PNR hoặc tên khách...", color = HintColor, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = HintColor,
                                modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderColor,
                            focusedBorderColor = NavyColor,
                            unfocusedContainerColor = CardBg,
                            focusedContainerColor = CardBg
                        )
                    )
                    Button(
                        onClick = { showFilter = !showFilter },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyColor),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Lọc kết quả", fontSize = 13.sp)
                    }

                    if (showFilter) {
                        FilterChipRow(
                            selected = selectedStatus,
                            onSelect = { selectedStatus = if (selectedStatus == it) "" else it }
                        )
                    }
                }
            }

            // ── Stat cards ─────────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TicketStatCard("TỔNG VÉ HÔM NAY", totalToday.toString(), NavyColor)
                    TicketStatCard("VÉ ĐÃ SỬ DỤNG", activeTickets.toString(), Color(0xFF1565C0))
                    TicketStatCard("ĐÃ HỦY/HOÀN", cancelled.toString(), RedColor)
                    TicketStatCard(
                        "DOANH THU (VNĐ)",
                        formatRevenue(revenue),
                        GreenColor
                    )
                }
            }

            // ── Table header ───────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TableHeaderBg)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text("MÃ ĐẶT CHỖ\n(PNR)", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = NavyColor,
                            modifier = Modifier.weight(1f))
                        Text("HÀNH\nKHÁCH", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = NavyColor,
                            modifier = Modifier.weight(1.4f))
                        Text("CHUYẾN\nBAY", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = NavyColor,
                            modifier = Modifier.weight(1f))
                    }
                }
            }

            // ── Table rows ─────────────────────────────────────────────────
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = NavyColor) }
                }
            } else if (pageItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("Không có vé nào", color = HintColor) }
                }
            } else {
                items(pageItems) { booking ->
                    TicketTableRow(booking)
                }
            }

            // ── Pagination ─────────────────────────────────────────────────
            item {
                PaginationBar(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    totalItems = filtered.size,
                    pageSize = PAGE_SIZE,
                    onPageChange = { currentPage = it }
                )
            }
        }
    }
}

// ── Stat card (vertical, full-width) ─────────────────────────────────────────
@Composable
private fun TicketStatCard(label: String, value: String, valueColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, fontSize = 11.sp, color = HintColor, letterSpacing = 0.5.sp)
            Text(
                value, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = valueColor, modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ── Filter chips ──────────────────────────────────────────────────────────────
@Composable
private fun FilterChipRow(selected: String, onSelect: (String) -> Unit) {
    val statuses = listOf("PENDING" to "Chờ TT", "CONFIRMED" to "Xác nhận", "CANCELLED" to "Đã hủy")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        statuses.forEach { (value, label) ->
            val isSelected = selected == value
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) NavyColor else CardBg,
                modifier = Modifier
                    .border(1.dp, if (isSelected) NavyColor else BorderColor, RoundedCornerShape(20.dp))
                    .clickable { onSelect(value) }
            ) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    color = if (isSelected) Color.White else NavyColor
                )
            }
        }
    }
}

// ── Table row ─────────────────────────────────────────────────────────────────
@Composable
private fun TicketTableRow(booking: Booking) {
    val (statusBg, statusColor) = when (booking.status) {
        "CONFIRMED" -> GreenBg to GreenColor
        "CANCELLED" -> RedBg to RedColor
        else -> OrangeBg to OrangeColor
    }
    val ticket = booking.tickets.firstOrNull()
    val flightInfo = if (ticket != null) {
        val code = ticket.flightCode
        // Try to parse route from flight code or use booking data
        code
    } else "—"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // PNR
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "SL${booking.id.toString().padStart(4, '0')}${('A' + (booking.id % 26)).uppercaseChar()}",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            when (booking.status) {
                                "CONFIRMED" -> "Xác nhận"
                                "CANCELLED" -> "Đã hủy"
                                else -> "Chờ TT"
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }

                // Passenger
                Column(modifier = Modifier.weight(1.4f)) {
                    Text(
                        booking.fullName.uppercase(),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyColor,
                        lineHeight = 16.sp
                    )
                    Text(
                        "Hạng: ${
                            when (ticket?.classType) {
                                "BUSINESS" -> "Thương gia"
                                "FIRST" -> "Phổ thông đặc biệt"
                                else -> "Phổ thông"
                            }
                        }",
                        fontSize = 11.sp, color = HintColor
                    )
                }

                // Flight
                Column(modifier = Modifier.weight(1f)) {
                    if (ticket != null) {
                        Text(
                            ticket.flightCode,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyColor
                        )
                        Text(
                            "(${ticket.flightCode})",
                            fontSize = 11.sp, color = HintColor
                        )
                    } else {
                        Text("—", fontSize = 12.sp, color = HintColor)
                    }
                }
            }
            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
        }
    }
}

// ── Pagination bar ────────────────────────────────────────────────────────────
@Composable
private fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    pageSize: Int,
    onPageChange: (Int) -> Unit
) {
    val start = currentPage * pageSize + 1
    val end = minOf(start + pageSize - 1, totalItems)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Hiển thị $start - $end\ntrên $totalItems",
            fontSize = 11.sp, color = HintColor,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentPage > 0) onPageChange(currentPage - 1) },
                enabled = currentPage > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ChevronLeft, null,
                    tint = if (currentPage > 0) NavyColor else HintColor,
                    modifier = Modifier.size(18.dp))
            }

            val pagesToShow = (0 until totalPages).filter { p ->
                p == 0 || p == totalPages - 1 || kotlin.math.abs(p - currentPage) <= 1
            }
            var lastShown = -1
            pagesToShow.forEach { p ->
                if (lastShown != -1 && p - lastShown > 1) {
                    Text("…", fontSize = 12.sp, color = HintColor,
                        modifier = Modifier.padding(horizontal = 2.dp))
                }
                PageButton(page = p, isSelected = p == currentPage) { onPageChange(p) }
                lastShown = p
            }

            IconButton(
                onClick = { if (currentPage < totalPages - 1) onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages - 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ChevronRight, null,
                    tint = if (currentPage < totalPages - 1) NavyColor else HintColor,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun PageButton(page: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) NavyColor else Color.Transparent)
            .clickable { onClick() }
    ) {
        Text(
            (page + 1).toString(),
            fontSize = 12.sp,
            color = if (isSelected) Color.White else NavyColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun formatRevenue(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> "%.1fB".format(amount / 1_000_000_000)
        amount >= 1_000_000 -> "%.1fM".format(amount / 1_000_000)
        else -> "%,.0f".format(amount)
    }
}
