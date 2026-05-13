package com.example.qlydatve.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.Booking
import com.example.qlydatve.model.Flight
import com.example.qlydatve.model.User
import com.example.qlydatve.viewmodel.BookingViewModel
import com.example.qlydatve.viewmodel.FlightViewModel
import java.util.Calendar

// ── Colors ────────────────────────────────────────────────────────────────────
private val NavyDark   = Color(0xFF0D1B3E)
private val NavyColor  = Color(0xFF1A2B4A)
private val BlueAccent = Color(0xFF1565C0)
private val BgColor    = Color(0xFFF4F6FA)
private val CardBg     = Color.White
private val HintColor  = Color(0xFF9AA5B8)
private val GreenColor = Color(0xFF2E7D32)
private val GreenBg    = Color(0xFFE8F5E9)
private val GoldColor  = Color(0xFFF59E0B)

// ── Quick actions ─────────────────────────────────────────────────────────────
private data class QuickAction(
    val icon: ImageVector, val label: String,
    val bg: Color, val tint: Color
)

private val quickActions = listOf(
    QuickAction(Icons.Default.Send,       "Đặt vé",    Color(0xFFE8F0FE), BlueAccent),
    QuickAction(Icons.Default.Hotel,      "Khách sạn", Color(0xFFE8F5E9), GreenColor),
    QuickAction(Icons.Default.CheckCircle,"Check-in",  Color(0xFFFFF8E1), GoldColor),
    QuickAction(Icons.Default.Explore,    "Khám phá",  Color(0xFFF3E5F5), Color(0xFF7B1FA2)),
)

@Composable
fun HomeScreen(
    user: User,
    onSearchFlight: () -> Unit,
    onFlightSelected: (Flight) -> Unit,
    onViewBookings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flightVm: FlightViewModel = viewModel()
    val bookingVm: BookingViewModel = viewModel(key = "home_booking")
    val flightState by flightVm.uiState.collectAsStateWithLifecycle()
    val bookingState by bookingVm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        flightVm.loadFlights()
        bookingVm.loadBookings()
    }

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11  -> "Chào buổi sáng,"
            in 12..17 -> "Chào buổi chiều,"
            else      -> "Chào buổi tối,"
        }
    }

    // Lấy booking gần nhất còn active
    val upcomingBooking = remember(bookingState.bookings) {
        bookingState.bookings.firstOrNull { it.status == "CONFIRMED" || it.status == "PENDING" }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(BgColor),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Header ────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(NavyDark, NavyColor)))
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = if (upcomingBooking != null) 80.dp else 28.dp)
            ) {
                // Decorative plane icon
                Icon(
                    Icons.Default.Send, null,
                    tint = Color.White.copy(alpha = 0.06f),
                    modifier = Modifier.size(180.dp).align(Alignment.CenterEnd)
                        .offset(x = 30.dp, y = (-10).dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(greeting, fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.75f))
                            Text(
                                user.fullName.split(" ").lastOrNull() ?: user.fullName,
                                fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                        // Avatar
                        Box(
                            modifier = Modifier.size(44.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.fullName.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sẵn sàng cho chuyến hành trình tiếp theo của bạn chưa?",
                        fontSize = 13.sp, color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }

        // ── Upcoming flight card (overlapping header) ─────────
        if (upcomingBooking != null) {
            item {
                UpcomingFlightCard(
                    booking = upcomingBooking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-60).dp)
                )
            }
        }

        // ── Quick actions ─────────────────────────────────────
        item {
            val topOffset = if (upcomingBooking != null) (-44).dp else 0.dp
            Card(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = if (upcomingBooking != null) 0.dp else 16.dp)
                    .offset(y = topOffset),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    quickActions.forEachIndexed { i, action ->
                        QuickActionItem(
                            action = action,
                            onClick = { if (i == 0) onSearchFlight() }
                        )
                    }
                }
            }
        }

        // ── Recent flights ────────────────────────────────────
        if (flightState.flights.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chuyến bay mới nhất", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = NavyColor)
                    TextButton(onClick = onSearchFlight) {
                        Text("Xem tất cả", fontSize = 12.sp, color = BlueAccent)
                    }
                }
            }
            items(flightState.flights.take(3)) { flight ->
                RecentFlightCard(flight = flight, onClick = { onFlightSelected(flight) })
            }
        }
    }
}

// ── Upcoming Flight Card ──────────────────────────────────────────────────────

@Composable
private fun UpcomingFlightCard(booking: Booking, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = NavyColor) {
                        Text("upcoming flight",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
                Icon(Icons.Default.Send, null, tint = NavyColor,
                    modifier = Modifier.size(20.dp))
            }

            // Flight code
            Text(
                booking.flightCode.ifBlank { "SL-402" },
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = HintColor
            )

            // Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Departure
                Column {
                    Text(booking.departureAirport.take(3).uppercase().ifBlank { "SGN" },
                        fontSize = 32.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                    Text(booking.departureCity.ifBlank { "TP Hồ Chí Minh" },
                        fontSize = 11.sp, color = HintColor)
                    Text(booking.departureTime.ifBlank { "08:45 AM" },
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyColor)
                }

                // Arrow
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Send, null, tint = HintColor,
                        modifier = Modifier.size(18.dp))
                    Text("2h 10m", fontSize = 10.sp, color = HintColor)
                    Text("Non-stop", fontSize = 10.sp, color = HintColor)
                }

                // Arrival
                Column(horizontalAlignment = Alignment.End) {
                    Text(booking.arrivalAirport.take(3).uppercase().ifBlank { "HAN" },
                        fontSize = 32.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                    Text(booking.arrivalCity.ifBlank { "Hà Nội" },
                        fontSize = 11.sp, color = HintColor)
                    Text(booking.arrivalTime.ifBlank { "11:00 AM" },
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyColor)
                }
            }

            HorizontalDivider(color = Color(0xFFDDE3ED))

            // Details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("HÀNH KHÁCH", fontSize = 10.sp, color = HintColor)
                    Text(booking.fullName.ifBlank { "Guest" },
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TRẠNG THÁI", fontSize = 10.sp, color = HintColor)
                    val statusColor = when(booking.status) {
                        "CONFIRMED" -> GreenColor
                        "PENDING" -> GoldColor
                        else -> Color.Red
                    }
                    Text(booking.status,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = statusColor)
                }
            }
        }
    }
}

@Composable
private fun QuickActionItem(action: QuickAction, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                .background(action.bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(action.icon, null, tint = action.tint, modifier = Modifier.size(24.dp))
        }
        Text(action.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NavyColor)
    }
}

@Composable
private fun RecentFlightCard(flight: Flight, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Airline logo placeholder
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(BgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AirplanemodeActive, null, tint = BlueAccent,
                    modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${flight.departureAirport} → ${flight.arrivalAirport}",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                    Text("${String.format("%,.0f", flight.price)}đ",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${flight.departureTime} • ${flight.airplaneName}",
                    fontSize = 12.sp, color = HintColor
                )
            }
        }
    }
}
