package com.example.qlydatve.view.customer

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.Airport
import com.example.qlydatve.model.Flight
import com.example.qlydatve.viewmodel.FlightViewModel
import java.text.SimpleDateFormat
import java.util.*

private val NavyDark    = Color(0xFF0D1B3E)
private val NavyColor   = Color(0xFF1A2B4A)
private val BlueAccent  = Color(0xFF1565C0)
private val BgColor     = Color(0xFFF4F6FA)
private val CardBg      = Color.White
private val BorderColor = Color(0xFFDDE3ED)
private val HintColor   = Color(0xFF9AA5B8)
private val GoldColor   = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFlightScreen(
    onFlightSelected: (Flight) -> Unit,
    modifier: Modifier = Modifier,
    showAllFlights: Boolean = false,
    viewModel: FlightViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var tripType by remember { mutableStateOf("Khứ hồi") }
    var fromCity by remember { mutableStateOf("") }
    var toCity by remember { mutableStateOf("") }
    var seatClass by remember { mutableStateOf("Phổ thông") }
    var classExpanded by remember { mutableStateOf(false) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(showAllFlights) }

    LaunchedEffect(showAllFlights) {
        if (showAllFlights) {
            viewModel.loadFlights()
        }
    }

    // Date state — default = today
    val calendar = remember { Calendar.getInstance() }
    var departDate by remember {
        mutableStateOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        )
    }
    var returnDate by remember { mutableStateOf("") }

    // Date pickers
    val depDatePicker = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                departDate = "%02d/%02d/%04d".format(d, m + 1, y)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.minDate = System.currentTimeMillis() }
    }
    val retDatePicker = remember {
        DatePickerDialog(
            context,
            { _, y, m, d ->
                returnDate = "%02d/%02d/%04d".format(d, m + 1, y)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.minDate = System.currentTimeMillis() }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(BgColor),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── Hero ─────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Brush.verticalGradient(listOf(NavyDark, BlueAccent)))
            ) {
                Icon(Icons.Default.Send, null,
                    tint = Color.White.copy(alpha = 0.07f),
                    modifier = Modifier.size(200.dp).align(Alignment.CenterEnd)
                        .offset(x = 40.dp, y = (-20).dp))
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Text("Xin chào, Bạn", fontSize = 28.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text("muốn đi đâu?", fontSize = 28.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text("Khám phá thế giới rộng lớn cùng hàng ngàn chuyến bay.",
                            fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                    }
                    // Trip type toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(4.dp)
                    ) {
                        listOf("Khứ hồi", "Một chiều").forEach { type ->
                            val sel = tripType == type
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (sel) Color.White else Color.Transparent)
                                    .clickable { tripType = type }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(type, fontSize = 13.sp,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sel) NavyColor else Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ── Search form ───────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── From airport ──────────────────────────
                    FieldLabel("Điểm đi")
                    if (uiState.airports.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = fromExpanded,
                            onExpandedChange = { fromExpanded = it }
                        ) {
                            AirportField(
                                value = fromCity,
                                placeholder = "Chọn thành phố đi",
                                icon = Icons.Default.Send,
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                    .clickable { fromExpanded = true }
                            )
                            ExposedDropdownMenu(
                                expanded = fromExpanded,
                                onDismissRequest = { fromExpanded = false }
                            ) {
                                uiState.airports.forEach { ap ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${ap.code} - ${ap.city}",
                                                    fontWeight = FontWeight.Medium)
                                                Text(ap.name, fontSize = 11.sp, color = HintColor)
                                            }
                                        },
                                        onClick = {
                                            fromCity = "${ap.city} (${ap.code})"
                                            fromExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        AirportTextField(fromCity, { fromCity = it }, "Thành phố đi", Icons.Default.Send)
                    }

                    // Swap
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center) {
                        HorizontalDivider(color = BorderColor)
                        Surface(modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp), color = CardBg,
                            shadowElevation = 2.dp) {
                            IconButton(onClick = { val t = fromCity; fromCity = toCity; toCity = t },
                                modifier = Modifier.fillMaxSize()) {
                                Icon(Icons.Default.SwapVert, null, tint = NavyColor,
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // ── To airport ────────────────────────────
                    FieldLabel("Điểm đến")
                    if (uiState.airports.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = toExpanded,
                            onExpandedChange = { toExpanded = it }
                        ) {
                            AirportField(
                                value = toCity,
                                placeholder = "Chọn thành phố đến",
                                icon = Icons.Default.LocationOn,
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                    .clickable { toExpanded = true }
                            )
                            ExposedDropdownMenu(
                                expanded = toExpanded,
                                onDismissRequest = { toExpanded = false }
                            ) {
                                uiState.airports.forEach { ap ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${ap.code} - ${ap.city}",
                                                    fontWeight = FontWeight.Medium)
                                                Text(ap.name, fontSize = 11.sp, color = HintColor)
                                            }
                                        },
                                        onClick = {
                                            toCity = "${ap.city} (${ap.code})"
                                            toExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        AirportTextField(toCity, { toCity = it }, "Thành phố đến", Icons.Default.LocationOn)
                    }

                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))

                    // ── Dates ─────────────────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Depart date
                        Column(modifier = Modifier.weight(1f)) {
                            FieldLabel("Ngày đi")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF5F7FA))
                                    .clickable { depDatePicker.show() }
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, null,
                                    tint = NavyColor, modifier = Modifier.size(16.dp))
                                Text(
                                    departDate.ifBlank { "dd/mm/yyyy" },
                                    fontSize = 13.sp,
                                    color = if (departDate.isBlank()) HintColor else NavyColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        // Return date (only for round trip)
                        if (tripType == "Khứ hồi") {
                            Column(modifier = Modifier.weight(1f)) {
                                FieldLabel("Ngày về")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF5F7FA))
                                        .clickable { retDatePicker.show() }
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, null,
                                        tint = NavyColor, modifier = Modifier.size(16.dp))
                                    Text(
                                        returnDate.ifBlank { "dd/mm/yyyy" },
                                        fontSize = 13.sp,
                                        color = if (returnDate.isBlank()) HintColor else NavyColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 8.dp))

                    // ── Seat class ────────────────────────────
                    FieldLabel("Hạng ghế")
                    ExposedDropdownMenuBox(
                        expanded = classExpanded,
                        onExpandedChange = { classExpanded = it }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F7FA))
                                .clickable { classExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.EventSeat, null,
                                    tint = NavyColor, modifier = Modifier.size(16.dp))
                                Text(seatClass, fontSize = 13.sp, color = NavyColor,
                                    fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = HintColor)
                        }
                        ExposedDropdownMenu(expanded = classExpanded,
                            onDismissRequest = { classExpanded = false }) {
                            listOf("Phổ thông", "Thương gia", "Hạng nhất").forEach { c ->
                                DropdownMenuItem(text = { Text(c) },
                                    onClick = { seatClass = c; classExpanded = false })
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Search button ─────────────────────────
                    Button(
                        onClick = {
                            // Extract airport code từ "Hà Nội (HAN)" → "HAN"
                            // Nếu không có code thì dùng city name
                            val from = fromCity.substringAfterLast("(").trimEnd(')')
                                .trim().ifBlank { fromCity.substringBefore(" (").trim() }
                            val to = toCity.substringAfterLast("(").trimEnd(')')
                                .trim().ifBlank { toCity.substringBefore(" (").trim() }
                            // Convert date từ dd/MM/yyyy → yyyy-MM-dd
                            val dateParam = runCatching {
                                val parts = departDate.split("/")
                                "${parts[2]}-${parts[1]}-${parts[0]}"
                            }.getOrDefault("")
                            viewModel.search(from, to, dateParam)
                            showResults = true
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyColor),
                        enabled = fromCity.isNotBlank() && toCity.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White,
                                modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tìm chuyến bay", fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── Error ─────────────────────────────────────────────
        uiState.error?.let { err ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(err, modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        // ── Search results ────────────────────────────────────
        if (showResults) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kết quả tìm kiếm", fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, color = NavyColor)
                    Text("${uiState.flights.size} chuyến bay",
                        fontSize = 13.sp, color = HintColor)
                }
            }
            if (uiState.flights.isEmpty() && !uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.SearchOff, null,
                                tint = HintColor, modifier = Modifier.size(48.dp))
                            Text("Không tìm thấy chuyến bay",
                                fontSize = 15.sp, color = HintColor)
                            TextButton(onClick = { showResults = false; viewModel.loadFlights() }) {
                                Text("Xem tất cả chuyến bay")
                            }
                        }
                    }
                }
            }
            items(uiState.flights) { flight ->
                FlightResultCard(flight = flight, onClick = { onFlightSelected(flight) })
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(text, fontSize = 11.sp, color = HintColor,
        modifier = Modifier.padding(bottom = 4.dp))
}

@Composable
private fun AirportField(
    value: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = NavyColor, modifier = Modifier.size(16.dp))
        Text(
            value.ifBlank { placeholder },
            fontSize = 13.sp,
            color = if (value.isBlank()) HintColor else NavyColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.KeyboardArrowDown, null, tint = HintColor,
            modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun AirportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F7FA))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = NavyColor, modifier = Modifier.size(16.dp))
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp, color = NavyColor, fontWeight = FontWeight.Medium
            ),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, fontSize = 13.sp, color = HintColor)
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FlightResultCard(flight: Flight, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(flight.flightNumber, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = NavyColor)
                Surface(shape = RoundedCornerShape(6.dp),
                    color = if (flight.status == "AVAILABLE") Color(0xFFE8F5E9)
                            else Color(0xFFFFF3E0)) {
                    Text(flight.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        color = if (flight.status == "AVAILABLE") Color(0xFF2E7D32)
                                else Color(0xFFE65100))
                }
            }
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(flight.departureTime.substringAfter(" ").take(5).ifBlank { "--:--" },
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                    Text(flight.departureAirport, fontSize = 12.sp, color = HintColor)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Send, null, tint = HintColor,
                        modifier = Modifier.size(16.dp))
                    Text("Bay thẳng", fontSize = 10.sp, color = HintColor)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(flight.arrivalTime.substringAfter(" ").take(5).ifBlank { "--:--" },
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                    Text(flight.arrivalAirport, fontSize = 12.sp, color = HintColor)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("%.0f đ".format(flight.price),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                    Text("/người", fontSize = 10.sp, color = HintColor)
                }
            }
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
            ) {
                Text("Chọn chuyến bay", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
