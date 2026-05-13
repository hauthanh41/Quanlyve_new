package com.example.qlydatve.view.flight

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.qlydatve.model.Airport
import com.example.qlydatve.model.Flight
import java.util.Calendar

private val NavyColor   = Color(0xFF1A2B4A)
private val FieldBorder = Color(0xFFDDE3ED)
private val FieldBg     = Color(0xFFF5F7FA)
private val HintColor   = Color(0xFF9AA5B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFlightDialog(
    flight: Flight? = null,
    airports: List<Airport> = emptyList(),
    airplanes: List<com.example.qlydatve.model.Airplane> = emptyList(),
    onConfirm: (Flight) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEdit = flight != null

    var depAirportId   by remember { mutableStateOf(flight?.departureAirportId?.toString() ?: "") }
    var arrAirportId   by remember { mutableStateOf(flight?.arrivalAirportId?.toString() ?: "") }
    var airplaneId     by remember { mutableStateOf(flight?.airplaneId?.toString() ?: "") }
    var depDateTime    by remember { mutableStateOf(flight?.departureTime ?: "") }
    var arrDateTime    by remember { mutableStateOf(flight?.arrivalTime ?: "") }
    var price          by remember { mutableStateOf(flight?.price?.toString() ?: "") }
    var status         by remember { mutableStateOf(flight?.status ?: "AVAILABLE") }
    var statusExpanded by remember { mutableStateOf(false) }
    var depExpanded    by remember { mutableStateOf(false) }
    var arrExpanded    by remember { mutableStateOf(false) }
    var planeExpanded  by remember { mutableStateOf(false) }

    val depAirport  = airports.find { it.id.toString() == depAirportId }
    val arrAirport  = airports.find { it.id.toString() == arrAirportId }
    val selAirplane = airplanes.find { it.id.toString() == airplaneId }

    // Tự động generate flight code từ airport codes
    val flightCode = remember(depAirportId, arrAirportId) {
        val dep = airports.find { it.id.toString() == depAirportId }?.code ?: ""
        val arr = airports.find { it.id.toString() == arrAirportId }?.code ?: ""
        if (dep.isNotBlank() && arr.isNotBlank()) "VN-$dep$arr" else flight?.flightNumber ?: ""
    }

    // Helper: show date then time picker, returns "yyyy-MM-dd HH:mm:ss"
    fun showDateTimePicker(current: String, onResult: (String) -> Unit) {
        val cal = Calendar.getInstance()
        // Try parse existing value
        if (current.length >= 10) {
            try {
                val parts = current.split(" ", "T")
                val dateParts = parts[0].split("-")
                cal.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
            } catch (_: Exception) {}
        }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onResult("%04d-%02d-%02d %02d:%02d:00".format(year, month + 1, day, hour, minute))
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Header ────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isEdit) "Sửa chuyến bay" else "Thêm chuyến bay mới",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyColor
                        )
                        Text(
                            if (isEdit) "Cập nhật thông tin chuyến bay"
                            else "Điền thông tin chuyến bay mới",
                            fontSize = 13.sp, color = HintColor
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = HintColor)
                    }
                }

                HorizontalDivider(color = FieldBorder)

                // ── Flight code (tự động) ─────────────────────
                FormField("Số hiệu chuyến bay") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FieldBg)
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            flightCode.ifBlank { "Tự động sau khi chọn sân bay" },
                            fontSize = 14.sp,
                            color = if (flightCode.isBlank()) HintColor else NavyColor,
                            fontWeight = if (flightCode.isBlank()) FontWeight.Normal else FontWeight.SemiBold
                        )
                        if (flightCode.isNotBlank()) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // ── Departure airport ─────────────────────────
                FormField("Sân bay khởi hành *") {
                    if (airports.isNotEmpty()) {
                        ExposedDropdownMenuBox(depExpanded, { depExpanded = it }) {
                            OutlinedTextField(
                                value = depAirport?.let { "${it.code} - ${it.name}" } ?: depAirportId,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Chọn sân bay đi", color = HintColor) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(depExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = FieldBorder,
                                    unfocusedContainerColor = FieldBg)
                            )
                            ExposedDropdownMenu(depExpanded, { depExpanded = false }) {
                                airports.forEach { ap ->
                                    DropdownMenuItem(
                                        text = { Text("${ap.code} - ${ap.name}") },
                                        onClick = { depAirportId = ap.id.toString(); depExpanded = false }
                                    )
                                }
                            }
                        }
                    } else {
                        StyledTextField(depAirportId, { depAirportId = it },
                            "ID sân bay đi", KeyboardType.Number)
                    }
                }

                // ── Arrival airport ───────────────────────────
                FormField("Sân bay đến *") {
                    if (airports.isNotEmpty()) {
                        ExposedDropdownMenuBox(arrExpanded, { arrExpanded = it }) {
                            OutlinedTextField(
                                value = arrAirport?.let { "${it.code} - ${it.name}" } ?: arrAirportId,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Chọn sân bay đến", color = HintColor) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(arrExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = FieldBorder,
                                    unfocusedContainerColor = FieldBg)
                            )
                            ExposedDropdownMenu(arrExpanded, { arrExpanded = false }) {
                                airports.forEach { ap ->
                                    DropdownMenuItem(
                                        text = { Text("${ap.code} - ${ap.name}") },
                                        onClick = { arrAirportId = ap.id.toString(); arrExpanded = false }
                                    )
                                }
                            }
                        }
                    } else {
                        StyledTextField(arrAirportId, { arrAirportId = it },
                            "ID sân bay đến", KeyboardType.Number)
                    }
                }

                // ── Airplane dropdown ─────────────────────────
                FormField("Máy bay *") {
                    if (airplanes.isNotEmpty()) {
                        ExposedDropdownMenuBox(planeExpanded, { planeExpanded = it }) {
                            OutlinedTextField(
                                value = selAirplane?.let { "${it.name} (${it.code})" } ?: airplaneId,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Chọn máy bay", color = HintColor) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(planeExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = FieldBorder,
                                    unfocusedContainerColor = FieldBg)
                            )
                            ExposedDropdownMenu(planeExpanded, { planeExpanded = false }) {
                                airplanes.forEach { plane ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("${plane.name} (${plane.code})",
                                                    fontWeight = FontWeight.Medium)
                                                Text("${plane.totalSeats} ghế",
                                                    fontSize = 11.sp, color = HintColor)
                                            }
                                        },
                                        onClick = { airplaneId = plane.id.toString(); planeExpanded = false }
                                    )
                                }
                            }
                        }
                    } else {
                        StyledTextField(airplaneId, { airplaneId = it }, "ID máy bay", KeyboardType.Number)
                    }
                }

                // ── Date time pickers ─────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormField("Giờ khởi hành *", modifier = Modifier.weight(1f)) {
                        DateTimePickerButton(
                            value = depDateTime,
                            placeholder = "Chọn ngày giờ",
                            onClick = { showDateTimePicker(depDateTime) { depDateTime = it } }
                        )
                    }
                    FormField("Giờ đến *", modifier = Modifier.weight(1f)) {
                        DateTimePickerButton(
                            value = arrDateTime,
                            placeholder = "Chọn ngày giờ",
                            onClick = { showDateTimePicker(arrDateTime) { arrDateTime = it } }
                        )
                    }
                }

                // ── Price ─────────────────────────────────────
                FormField("Giá vé (VNĐ) *") {
                    StyledTextField(price, { price = it }, "VD: 1500000", KeyboardType.Number)
                }

                // ── Status ────────────────────────────────────
                FormField("Trạng thái") {
                    ExposedDropdownMenuBox(statusExpanded, { statusExpanded = it }) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = FieldBorder,
                                unfocusedContainerColor = FieldBg)
                        )
                        ExposedDropdownMenu(statusExpanded, { statusExpanded = false }) {
                            listOf("AVAILABLE", "DELAYED", "CANCELLED", "FULL").forEach { s ->
                                DropdownMenuItem(text = { Text(s) },
                                    onClick = { status = s; statusExpanded = false })
                            }
                        }
                    }
                }

                // Validation hint
                val isValid = flightCode.isNotBlank() && price.isNotBlank() &&
                    depAirportId.isNotBlank() && depAirportId != "0" &&
                    arrAirportId.isNotBlank() && arrAirportId != "0" &&
                    airplaneId.isNotBlank() && airplaneId != "0" &&
                    depDateTime.isNotBlank() && arrDateTime.isNotBlank()
                if (!isValid) {
                    Text("* Vui lòng điền đầy đủ các trường bắt buộc",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }

                HorizontalDivider(color = FieldBorder)

                // ── Buttons ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Hủy", color = NavyColor) }

                    Button(
                        onClick = {
                            onConfirm(
                                Flight(
                                    id = flight?.id ?: 0,
                                    flightNumber = flightCode.trim(),
                                    departureAirportId = depAirportId.toIntOrNull() ?: 0,
                                    arrivalAirportId = arrAirportId.toIntOrNull() ?: 0,
                                    airplaneId = airplaneId.toIntOrNull() ?: 0,
                                    departureTime = depDateTime,
                                    arrivalTime = arrDateTime,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    status = status
                                )
                            )
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                    ) {
                        Text(if (isEdit) "Cập nhật" else "Thêm mới",
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateTimePickerButton(
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FieldBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.CalendarMonth, null,
            tint = if (value.isBlank()) HintColor else NavyColor,
            modifier = Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (value.isNotBlank()) {
                // Show date and time on separate lines
                val parts = value.split(" ")
                Text(parts.getOrElse(0) { "" }, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold, color = NavyColor)
                Text(parts.getOrElse(1) { "" }.take(5), fontSize = 11.sp, color = HintColor)
            } else {
                Text(placeholder, fontSize = 13.sp, color = HintColor)
            }
        }
        Icon(Icons.Default.Edit, null, tint = HintColor, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyColor)
        content()
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = HintColor, fontSize = 13.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = FieldBorder,
            unfocusedContainerColor = FieldBg
        )
    )
}
