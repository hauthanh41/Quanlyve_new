package com.example.qlydatve.view.booking

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.Booking
import com.example.qlydatve.model.TicketDetail
import com.example.qlydatve.model.User
import com.example.qlydatve.utils.Constants
import com.example.qlydatve.viewmodel.BookingViewModel

private val NavyColor  = Color(0xFF1A2B4A)
private val BgColor    = Color(0xFFF4F6FA)
private val CardBg     = Color.White
private val HintColor  = Color(0xFF9AA5B8)
private val GreenColor = Color(0xFF2E7D32)
private val GreenBg    = Color(0xFFE8F5E9)
private val RedColor   = Color(0xFFE53935)
private val RedBg      = Color(0xFFFFF0F0)
private val OrangeBg   = Color(0xFFFFF8E1)
private val OrangeColor = Color(0xFFE65100)

@Composable
fun BookingScreen(
    currentUser: User,
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = viewModel(key = "booking_screen")
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadBookings() }

    Column(
        modifier = modifier.fillMaxSize().background(BgColor)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyColor)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    if (currentUser.role == Constants.ROLE_ADMIN) "Quản lý đặt vé"
                    else "Vé của tôi",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White
                )
                Text(
                    if (currentUser.role == Constants.ROLE_ADMIN)
                        "Tất cả đặt vé trong hệ thống"
                    else "Lịch sử đặt vé của bạn",
                    fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyColor)
            }
        } else if (uiState.bookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.ConfirmationNumber, null,
                        tint = HintColor, modifier = Modifier.size(64.dp))
                    Text("Chưa có đặt vé nào", fontSize = 16.sp, color = HintColor)
                    if (currentUser.role != Constants.ROLE_ADMIN) {
                        Text("Hãy tìm và đặt chuyến bay đầu tiên của bạn!",
                            fontSize = 13.sp, color = HintColor)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stats row (admin only)
                if (currentUser.role == Constants.ROLE_ADMIN) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MiniStatCard("Tổng", uiState.bookings.size.toString(),
                                NavyColor, Modifier.weight(1f))
                            MiniStatCard("Chờ",
                                uiState.bookings.count { it.status == "PENDING" }.toString(),
                                OrangeColor, Modifier.weight(1f))
                            MiniStatCard("Xác nhận",
                                uiState.bookings.count { it.status == "CONFIRMED" }.toString(),
                                GreenColor, Modifier.weight(1f))
                            MiniStatCard("Hủy",
                                uiState.bookings.count { it.status == "CANCELLED" }.toString(),
                                RedColor, Modifier.weight(1f))
                        }
                    }
                }

                items(uiState.bookings) { booking ->
                    BookingCard(
                        booking = booking,
                        isAdmin = currentUser.role == Constants.ROLE_ADMIN,
                        onCancel = { viewModel.cancelBooking(booking.id) },
                        onPay = { viewModel.processPayment(booking.id, booking.totalAmount) }
                    )
                }
            }
        }

        // Snackbars
        uiState.successMessage?.let { msg ->
            LaunchedEffect(msg) { kotlinx.coroutines.delay(2000); viewModel.clearMessages() }
            Snackbar(modifier = Modifier.padding(8.dp)) { Text(msg) }
        }
        uiState.error?.let { err ->
            LaunchedEffect(err) { kotlinx.coroutines.delay(3000); viewModel.clearMessages() }
            Snackbar(modifier = Modifier.padding(8.dp),
                containerColor = MaterialTheme.colorScheme.errorContainer) { Text(err) }
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = HintColor)
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    isAdmin: Boolean,
    onCancel: () -> Unit,
    onPay: () -> Unit
) {
    val (statusBg, statusColor) = when (booking.status) {
        "CONFIRMED" -> GreenBg to GreenColor
        "CANCELLED" -> RedBg to RedColor
        else -> OrangeBg to OrangeColor
    }
    val statusLabel = when (booking.status) {
        "CONFIRMED" -> "Đã xác nhận"
        "CANCELLED" -> "Đã hủy"
        else -> "Chờ thanh toán"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Header row
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ConfirmationNumber, null,
                        tint = NavyColor, modifier = Modifier.size(18.dp))
                    Text("Đặt vé #${booking.id}", fontSize = 15.sp,
                        fontWeight = FontWeight.Bold, color = NavyColor)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusBg) {
                    Text(statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
                }
            }

            if (isAdmin) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, null, tint = HintColor,
                        modifier = Modifier.size(14.dp))
                    Text(booking.fullName, fontSize = 13.sp, color = NavyColor)
                    Text("•", color = HintColor)
                    Text(booking.email, fontSize = 12.sp, color = HintColor)
                }
            }

            // Date & amount
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarMonth, null, tint = HintColor,
                        modifier = Modifier.size(13.dp))
                    Text(booking.bookingDate.take(10), fontSize = 12.sp, color = HintColor)
                }
                Text("%.0f VNĐ".format(booking.totalAmount),
                    fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyColor)
            }

            // Tickets
            if (booking.tickets.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFEEF2F7))
                booking.tickets.forEach { ticket ->
                    TicketRow(ticket)
                }
            }

            // Action buttons
            if (booking.status == Constants.BOOKING_PENDING) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onPay,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                    ) {
                        Icon(Icons.Default.Payment, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Thanh toán", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = RedColor,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = HintColor
                        )
                    ) {
                        Text("Hủy vé", fontSize = 13.sp, color = RedColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketRow(ticket: TicketDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFF), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Send, null, tint = NavyColor,
                modifier = Modifier.size(14.dp))
            Column {
                Text(ticket.flightCode, fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, color = NavyColor)
                Text("${ticket.passengerName} • Ghế ${ticket.seatNumber}",
                    fontSize = 11.sp, color = HintColor)
            }
        }
        Surface(shape = RoundedCornerShape(6.dp),
            color = if (ticket.classType == "BUSINESS") Color(0xFFFFF8E1)
                    else Color(0xFFE8F0FE)) {
            Text(ticket.classType,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = if (ticket.classType == "BUSINESS") OrangeColor
                        else Color(0xFF1565C0))
        }
    }
}
