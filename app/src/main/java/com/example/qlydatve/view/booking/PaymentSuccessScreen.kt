package com.example.qlydatve.view.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlydatve.model.Flight
import com.example.qlydatve.model.Seat

private val NavyColor  = Color(0xFF1A2B4A)
private val BgColor2   = Color(0xFFF4F6FA)
private val CardBg2    = Color.White
private val BorderColor2 = Color(0xFFDDE3ED)
private val HintColor2 = Color(0xFF9AA5B8)
private val BlueLight  = Color(0xFFE3F0FF)
private val BlueIcon   = Color(0xFF1565C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSuccessScreen(
    bookingCode: String,
    flight: Flight,
    seat: Seat,
    passengerName: String,
    maskedEmail: String,
    onViewTickets: () -> Unit,
    onGoHome: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("SkyLine Airways", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = NavyColor)
                },
                actions = {
                    Surface(shape = CircleShape, color = NavyColor,
                        modifier = Modifier.size(36.dp).padding(end = 8.dp)) {
                        Icon(Icons.Default.Person, null, tint = Color.White,
                            modifier = Modifier.padding(6.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = BgColor2
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Success icon ─────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = BlueIcon, modifier = Modifier.size(48.dp))
            }

            // ── Title ────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Đặt vé thành công!",
                    fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    color = NavyColor, textAlign = TextAlign.Center)
                Text(
                    "Chuyến đi tuyệt vời của bạn đang chờ đợi. Thông tin xác nhận đã được gửi đến email của bạn.",
                    fontSize = 14.sp, color = HintColor2,
                    textAlign = TextAlign.Center, lineHeight = 20.sp
                )
            }

            // ── Ticket card ──────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyColor),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // PNR code
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("MÃ ĐẶT CHỖ", fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                            Text("(PNR)", fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f))
                        }
                        Text(bookingCode, fontSize = 22.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    // Departure
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("KHỞI HÀNH", fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                        Text(flight.departureAirport.take(3).uppercase(),
                            fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(flight.departureAirport, fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f))
                        Text(flight.departureTime.take(16), fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f))
                    }

                    // Flight duration indicator
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HorizontalDivider(modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.2f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Send, null, tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp))
                            Text("2H 15M", fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.5f))
                        }
                        HorizontalDivider(modifier = Modifier.weight(1f),
                            color = Color.White.copy(alpha = 0.2f))
                    }

                    // Arrival
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("ĐẾN", fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                        Text(flight.arrivalAirport.take(3).uppercase(),
                            fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(flight.arrivalAirport, fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f))
                        Text(flight.arrivalTime.take(16), fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f))
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    // Passenger + class
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("HÀNH KHÁCH", fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                            Text(passengerName.uppercase(), fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("HẠNG VÉ", fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BlueLight
                            ) {
                                Text(
                                    if (seat.classType == "BUSINESS") "BUSINESS\nCLASS"
                                    else "ECONOMY\nCLASS",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = BlueIcon, textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // ── Email notice ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg2)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Email, null, tint = BlueIcon,
                    modifier = Modifier.size(20.dp).padding(top = 2.dp))
                Text(
                    "Chúng tôi đã gửi vé điện tử và hóa đơn chi tiết đến địa chỉ $maskedEmail. Vui lòng kiểm tra cả hộp thư Spam nếu bạn không thấy email trong vài phút tới.",
                    fontSize = 12.sp, color = NavyColor, lineHeight = 18.sp
                )
            }

            // ── Action buttons ───────────────────────────────
            Button(
                onClick = onViewTickets,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
            ) {
                Icon(Icons.Default.ConfirmationNumber, null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Xem vé của tôi", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp)
            ) {
                Icon(Icons.Default.Home, null, tint = NavyColor,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Về trang chủ", fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold, color = NavyColor)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
