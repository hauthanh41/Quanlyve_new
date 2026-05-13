package com.example.qlydatve.view.flight

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
import com.example.qlydatve.model.Flight
import com.example.qlydatve.viewmodel.FlightViewModel

private val NavyColor  = Color(0xFF1A2B4A)
private val BgColor    = Color(0xFFEEF2F7)
private val CardBg     = Color.White
private val BorderColor = Color(0xFFDDE3ED)
private val HintColor  = Color(0xFF9AA5B8)
private val DelayedRed = Color(0xFFE53935)

@Composable
fun FlightScreen(
    isAdmin: Boolean,
    onBookFlight: (Flight) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FlightViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingFlight by remember { mutableStateOf<Flight?>(null) }
    var deletingFlight by remember { mutableStateOf<Flight?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Tất cả trạng thái") }
    var statusExpanded by remember { mutableStateOf(false) }

    val filtered = uiState.flights.filter { f ->
        val matchQuery = searchQuery.isBlank() ||
            f.flightNumber.contains(searchQuery, ignoreCase = true) ||
            f.departureAirport.contains(searchQuery, ignoreCase = true) ||
            f.arrivalAirport.contains(searchQuery, ignoreCase = true)
        val matchStatus = selectedStatus == "Tất cả trạng thái" ||
            f.status == selectedStatus
        matchQuery && matchStatus
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Title ──────────────────────────────────────────
            item {
                Text(
                    "Flight Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor
                )
                Text(
                    "Monitor and manage all scheduled flight operations.",
                    fontSize = 13.sp,
                    color = HintColor
                )
            }

            // ── Add button (admin only) ─────────────────────────
            if (isAdmin) {
                item {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Thêm chuyến bay mới", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Filter card ────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search
                        Text("Số hiệu / Lộ trình", fontSize = 13.sp,
                            fontWeight = FontWeight.Medium, color = NavyColor)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Tìm kiếm chuyến bay...", color = HintColor) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null,
                                    tint = HintColor, modifier = Modifier.size(18.dp))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        // Status dropdown
                        Text("Trạng thái", fontSize = 13.sp,
                            fontWeight = FontWeight.Medium, color = NavyColor)
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedStatus,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded)
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false }
                            ) {
                                listOf("Tất cả trạng thái", "AVAILABLE", "DELAYED",
                                    "CANCELLED", "FULL").forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s) },
                                        onClick = { selectedStatus = s; statusExpanded = false }
                                    )
                                }
                            }
                        }

                        // Filter button
                        OutlinedButton(
                            onClick = { viewModel.search(searchQuery, "") },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.5.dp
                            )
                        ) {
                            Text("Lọc kết quả", color = NavyColor, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // ── Flight table ───────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        // Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F7FA))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text("Số hiệu", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = NavyColor, modifier = Modifier.weight(1.2f))
                            Text("Lộ trình", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = NavyColor, modifier = Modifier.weight(2f))
                            Text("Khởi hành", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = NavyColor, modifier = Modifier.weight(1.3f))
                            if (isAdmin) Spacer(Modifier.weight(0.8f))
                        }
                        HorizontalDivider(color = BorderColor)

                        if (uiState.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        } else if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("Không có chuyến bay nào", color = HintColor) }
                        }
                    }
                }
            }

            // Flight rows
            items(filtered) { flight ->
                FlightRowCard(
                    flight = flight,
                    isAdmin = isAdmin,
                    onEdit = { editingFlight = flight },
                    onDelete = { deletingFlight = flight },
                    onBook = { onBookFlight(flight) }
                )
            }

            // Pagination hint
            if (filtered.isNotEmpty()) {
                item {
                    Text(
                        "Hiển thị 1 - ${filtered.size} của ${filtered.size} chuyến bay",
                        fontSize = 12.sp,
                        color = HintColor
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddFlightDialog(
            airports = uiState.airports,
            airplanes = uiState.airplanes,
            onConfirm = { viewModel.addFlight(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
    editingFlight?.let { f ->
        AddFlightDialog(
            flight = f,
            airports = uiState.airports,
            airplanes = uiState.airplanes,
            onConfirm = { viewModel.updateFlight(it); editingFlight = null },
            onDismiss = { editingFlight = null }
        )
    }
    deletingFlight?.let { f ->
        AlertDialog(
            onDismissRequest = { deletingFlight = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Xóa chuyến bay ${f.flightNumber}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteFlight(f.id); deletingFlight = null }) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingFlight = null }) { Text("Hủy") }
            }
        )
    }

    // Snackbars
    uiState.successMessage?.let { msg ->
        LaunchedEffect(msg) { kotlinx.coroutines.delay(2000); viewModel.clearMessages() }
    }
}

@Composable
private fun FlightRowCard(
    flight: Flight,
    isAdmin: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBook: () -> Unit
) {
    val timeColor = if (flight.status == "DELAYED") DelayedRed else NavyColor

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flight number + airplane
            Column(modifier = Modifier.weight(1.2f)) {
                Text(flight.flightNumber, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, color = NavyColor)
                Text(flight.airplaneName.ifBlank { "—" },
                    fontSize = 11.sp, color = HintColor)
            }

            // Route
            Row(
                modifier = Modifier.weight(2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(flight.departureAirport.take(3).uppercase(),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                    Text(flight.departureAirport, fontSize = 10.sp, color = HintColor, maxLines = 1)
                }
                Icon(Icons.Default.ArrowForward, contentDescription = null,
                    tint = HintColor, modifier = Modifier.size(14.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(flight.arrivalAirport.take(3).uppercase(),
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyColor)
                    Text(flight.arrivalAirport, fontSize = 10.sp, color = HintColor, maxLines = 1)
                }
            }

            // Time
            Column(
                modifier = Modifier.weight(1.3f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    flight.departureTime.take(16).substringAfterLast(" ").take(5),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = timeColor
                )
                Text(
                    flight.departureTime.take(10),
                    fontSize = 10.sp, color = HintColor
                )
            }

            // Actions
            if (isAdmin) {
                Row(modifier = Modifier.weight(0.8f),
                    horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa",
                            tint = NavyColor, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa",
                            tint = DelayedRed, modifier = Modifier.size(16.dp))
                    }
                }
            } else if (flight.status == "AVAILABLE") {
                TextButton(
                    onClick = onBook,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Đặt", fontSize = 12.sp, color = NavyColor,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
