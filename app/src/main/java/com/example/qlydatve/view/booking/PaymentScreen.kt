package com.example.qlydatve.view.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.CreateBookingRequest
import com.example.qlydatve.model.Flight
import com.example.qlydatve.model.PassengerRequest
import com.example.qlydatve.model.Seat
import com.example.qlydatve.model.TicketRequest
import com.example.qlydatve.viewmodel.BookingViewModel

private val NavyColor    = Color(0xFF1A2B4A)
private val PayBgColor   = Color(0xFFF4F6FA)
private val PayCardBg    = Color.White
private val PayBorderColor = Color(0xFFDDE3ED)
private val HintColor    = Color(0xFF9AA5B8)
private val BlueAccent   = Color(0xFF1565C0)
private val MomoColor    = Color(0xFFAE2070)
private val ZaloColor    = Color(0xFF0068FF)
private val CyanBtn     = Color(0xFF00B4D8)
private val RedColor    = Color(0xFFE53935)

enum class PaymentMethod { CARD, MOMO, ZALOPAY, BANKING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    flight: Flight,
    seat: Seat,
    passengerName: String,
    onPaymentSuccess: (bookingCode: String) -> Unit,
    onBack: () -> Unit,
    bookingVm: BookingViewModel = viewModel()
) {
    val uiState by bookingVm.uiState.collectAsStateWithLifecycle()

    // ── Passenger form state ──────────────────────────────────
    var fullName    by remember { mutableStateOf(passengerName) }
    var gender      by remember { mutableStateOf("MALE") }
    var dob         by remember { mutableStateOf("") }
    var passport    by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("Vietnamese") }

    // ── Payment state ─────────────────────────────────────────
    var selectedMethod by remember { mutableStateOf(PaymentMethod.BANKING) }
    var cardNumber     by remember { mutableStateOf("") }
    var expiry         by remember { mutableStateOf("") }
    var cvv            by remember { mutableStateOf("") }
    var couponCode     by remember { mutableStateOf("") }
    var couponApplied  by remember { mutableStateOf(false) }

    // ── Validation error ──────────────────────────────────────
    var formError by remember { mutableStateOf("") }

    // ── Price calculation ─────────────────────────────────────
    val basePrice = when (seat.classType) {
        "FIRST"    -> flight.price * 2.5
        "BUSINESS" -> flight.price * 1.6
        else       -> flight.price
    }
    val tax      = basePrice * 0.1
    val discount = if (couponApplied) 150000.0 else 0.0
    val total    = basePrice + tax - discount

    // ── Handle booking success → trigger payment ──────────────
    var pendingBookingId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState.successMessage) {
        val msg = uiState.successMessage ?: return@LaunchedEffect
        // Server trả "BOOKING_ID:123" sau khi tạo booking thành công
        val bookingId = if (msg.startsWith("BOOKING_ID:")) {
            msg.removePrefix("BOOKING_ID:").toIntOrNull()
        } else {
            pendingBookingId
        }
        bookingVm.clearMessages()
        onPaymentSuccess("SKL-${bookingId ?: (1000..9999).random()}")
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) formError = uiState.error ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NavyColor)
                    }
                },
                title = {
                    Text("Đặt vé & Thanh toán", fontSize = 17.sp,
                        fontWeight = FontWeight.Bold, color = NavyColor)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = PayBgColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Flight summary ────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyColor),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(flight.flightNumber, fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(flight.departureAirport.take(3).uppercase(),
                                    fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp))
                                Text(flight.arrivalAirport.take(3).uppercase(),
                                    fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(flight.departureTime.take(10), fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f))
                        }
                        Column(horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = RoundedCornerShape(6.dp),
                                color = Color.White.copy(alpha = 0.15f)) {
                                Text(
                                    when (seat.classType) {
                                        "FIRST" -> "Hạng Nhất"
                                        "BUSINESS" -> "Thương Gia"
                                        else -> "Phổ Thông"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White
                                )
                            }
                            Text("Ghế ${seat.seatNumber}", fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // ── Passenger info form ───────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PayCardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Thông tin hành khách", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = NavyColor)

                        // Full name
                        FormField("Họ và tên *") {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it; formError = "" },
                                placeholder = { Text("Nguyễn Văn A", color = HintColor) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = HintColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = PayBorderColor)
                            )
                        }

                        // Gender
                        FormField("Giới tính *") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("MALE" to "Nam", "FEMALE" to "Nữ").forEach { (value, label) ->
                                    val sel = gender == value
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (sel) 2.dp else 1.dp,
                                                color = if (sel) BlueAccent else PayBorderColor,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .background(if (sel) Color(0xFFF0F7FF) else PayCardBg)
                                            .clickable { gender = value }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        RadioButton(selected = sel, onClick = { gender = value },
                                            modifier = Modifier.size(18.dp),
                                            colors = RadioButtonDefaults.colors(selectedColor = BlueAccent))
                                        Text(label, fontSize = 14.sp, color = NavyColor)
                                    }
                                }
                            }
                        }

                        // Date of birth
                        FormField("Ngày sinh (dd/mm/yyyy)") {
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                placeholder = { Text("01/01/1990", color = HintColor) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = HintColor) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = PayBorderColor)
                            )
                        }

                        // Passport
                        FormField("Số hộ chiếu / CCCD *") {
                            OutlinedTextField(
                                value = passport,
                                onValueChange = { passport = it; formError = "" },
                                placeholder = { Text("B12345678", color = HintColor) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Badge, null, tint = HintColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = PayBorderColor)
                            )
                        }

                        // Nationality
                        FormField("Quốc tịch") {
                            OutlinedTextField(
                                value = nationality,
                                onValueChange = { nationality = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Flag, null, tint = HintColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = PayBorderColor)
                            )
                        }
                    }
                }
            }

            // ── Payment methods ───────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PayCardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Phương thức thanh toán", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = NavyColor)
                        Spacer(Modifier.height(4.dp))

                        PaymentMethodItem(
                            selected = selectedMethod == PaymentMethod.BANKING,
                            onClick = { selectedMethod = PaymentMethod.BANKING },
                            label = "Chuyển khoản ngân hàng",
                            trailingIcon = {
                                Icon(Icons.Default.AccountBalance, null,
                                    tint = HintColor, modifier = Modifier.size(20.dp))
                            }
                        )
                        PaymentMethodItem(
                            selected = selectedMethod == PaymentMethod.CARD,
                            onClick = { selectedMethod = PaymentMethod.CARD },
                            label = "Thẻ tín dụng / Ghi nợ",
                            sublabel = "Visa, Mastercard, JCB",
                            trailingIcon = {
                                Icon(Icons.Default.CreditCard, null,
                                    tint = HintColor, modifier = Modifier.size(20.dp))
                            }
                        )
                        PaymentMethodItem(
                            selected = selectedMethod == PaymentMethod.MOMO,
                            onClick = { selectedMethod = PaymentMethod.MOMO },
                            label = "Ví MoMo",
                            badge = "Momo", badgeColor = MomoColor
                        )
                        PaymentMethodItem(
                            selected = selectedMethod == PaymentMethod.ZALOPAY,
                            onClick = { selectedMethod = PaymentMethod.ZALOPAY },
                            label = "ZaloPay",
                            badge = "Zalo", badgeColor = ZaloColor
                        )

                        // Card fields
                        if (selectedMethod == PaymentMethod.CARD) {
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16) cardNumber = it },
                                label = { Text("Số thẻ") },
                                placeholder = { Text("0000 0000 0000 0000", color = HintColor) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = PayBorderColor)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = expiry,
                                    onValueChange = { expiry = it },
                                    label = { Text("Hết hạn") },
                                    placeholder = { Text("MM/YY", color = HintColor) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = PayBorderColor)
                                )
                                OutlinedTextField(
                                    value = cvv,
                                    onValueChange = { if (it.length <= 3) cvv = it },
                                    label = { Text("CVV") },
                                    placeholder = { Text("•••", color = HintColor) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = PayBorderColor)
                                )
                            }
                        }
                    }
                }
            }

            // ── Coupon ────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PayCardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Mã giảm giá", fontSize = 14.sp,
                            fontWeight = FontWeight.Medium, color = NavyColor)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = couponCode,
                                onValueChange = { couponCode = it },
                                placeholder = {
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.LocalOffer, null,
                                            tint = HintColor, modifier = Modifier.size(16.dp))
                                        Text("Nhập mã coupon", color = HintColor, fontSize = 13.sp)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = PayBorderColor)
                            )
                            Button(
                                onClick = {
                                    if (couponCode.uppercase() == "SKY20") couponApplied = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("Áp dụng", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (couponApplied) {
                            Text("✓ Mã SKY20 đã được áp dụng (-150.000đ)",
                                fontSize = 12.sp, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }

            // ── Order summary + Pay button ────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyColor),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Tóm tắt đơn hàng", fontSize = 15.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        SummaryRow("Giá vé (${seat.classType})", "%,.0f đ".format(basePrice))
                        SummaryRow("Thuế & Phí (10%)", "%,.0f đ".format(tax))
                        if (couponApplied) {
                            SummaryRow("Giảm giá (SKY20)", "-150.000 đ",
                                valueColor = Color(0xFF80CBC4))
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom) {
                            Text("Tổng cộng", fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.8f))
                            Column(horizontalAlignment = Alignment.End) {
                                Text("%,.0f VND".format(total),
                                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Đã bao gồm VAT", fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.6f))
                            }
                        }

                        // Error message
                        if (formError.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFCDD2)) {
                                Text(formError,
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 12.sp, color = RedColor)
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Pay button — calls real API
                        Button(
                            onClick = {
                                // Validate
                                when {
                                    fullName.isBlank() -> { formError = "Vui lòng nhập họ tên hành khách"; return@Button }
                                    passport.isBlank() -> { formError = "Vui lòng nhập số hộ chiếu / CCCD"; return@Button }
                                    seat.id == 0 -> { formError = "Ghế không hợp lệ, vui lòng chọn lại"; return@Button }
                                    flight.id == 0 -> { formError = "Chuyến bay không hợp lệ"; return@Button }
                                }
                                formError = ""

                                // Parse dob from dd/mm/yyyy → yyyy-mm-dd
                                val dobFormatted = if (dob.contains("/")) {
                                    val parts = dob.split("/")
                                    if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else ""
                                } else dob

                                val request = CreateBookingRequest(
                                    tickets = listOf(
                                        TicketRequest(
                                            passenger = PassengerRequest(
                                                fullName = fullName.trim(),
                                                gender = gender,
                                                dateOfBirth = dobFormatted,
                                                passportNumber = passport.trim(),
                                                nationality = nationality.trim()
                                            ),
                                            flightId = flight.id,
                                            seatId = seat.id,
                                            ticketPrice = total
                                        )
                                    )
                                )
                                // Store a temp booking id for navigation after success
                                pendingBookingId = (1000..9999).random()
                                bookingVm.createBooking(request)
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanBtn),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = Color.White,
                                    modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Xác nhận đặt vé", fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("GIAO DỊCH ĐƯỢC BẢO MẬT BỞI SSL",
                            fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center, letterSpacing = 0.5.sp)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = BlueAccent,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp))
                    Text(
                        "Bằng việc nhấn \"Xác nhận đặt vé\", bạn đồng ý với Điều khoản sử dụng và Chính sách bảo mật của SkyLine Airways.",
                        fontSize = 11.sp, color = NavyColor, lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 12.sp, color = HintColor)
        content()
    }
}

@Composable
private fun PaymentMethodItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    sublabel: String? = null,
    badge: String? = null,
    badgeColor: Color = Color.Gray,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) BlueAccent else PayBorderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .background(if (selected) Color(0xFFF0F7FF) else PayCardBg)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected, onClick = onClick,
            modifier = Modifier.size(20.dp),
            colors = RadioButtonDefaults.colors(selectedColor = BlueAccent)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NavyColor)
            sublabel?.let { Text(it, fontSize = 11.sp, color = HintColor) }
        }
        badge?.let {
            Surface(shape = RoundedCornerShape(4.dp), color = badgeColor) {
                Text(it, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        trailingIcon?.invoke()
    }
}

@Composable
private fun SummaryRow(
    label: String, value: String,
    valueColor: Color = Color.White.copy(alpha = 0.85f)
) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
        Text(value, fontSize = 13.sp, color = valueColor)
    }
}
