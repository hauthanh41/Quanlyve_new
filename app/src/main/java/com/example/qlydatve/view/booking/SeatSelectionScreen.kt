package com.example.qlydatve.view.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qlydatve.model.Flight
import com.example.qlydatve.model.Seat

// ── Colors ────────────────────────────────────────────────────────────────────
private val BusinessBlue = Color(0xFF1A3A5C)
private val EconomyBlue  = Color(0xFF4A90D9)
private val SelectedBlue = Color(0xFF90C8F0)
private val TakenGray    = Color(0xFFD0D0D0)
private val SectionBg    = Color(0xFFEAF2FB)
private val FooterBg     = Color(0xFFF5F8FB)
private val BorderColor  = Color(0xFFDDE6F0)
private val LabelText    = Color(0xFF5A7A9A)
private val ExitRed      = Color(0xFFD32F2F)

// ── Seat UI state ─────────────────────────────────────────────────────────────
private enum class SeatUiStatus { AVAILABLE, TAKEN, SELECTED }

private data class SeatUiModel(
    val seat: Seat,
    val status: SeatUiStatus
)

private fun Seat.isBusinessClass(): Boolean {
    // Giả sử seatNumber dạng "1A", "2C", "10B" — hàng <= 2 là thương gia
    val row = seatNumber.dropLast(1).toIntOrNull() ?: return false
    return row <= 2
}

// Chuyển List<Seat> từ ViewModel sang danh sách hiển thị theo hàng/cột
// Mỗi phần tử null = khoảng cách giữa 2 nhóm ghế (aisle)
private fun buildSeatGrid(
    seats: List<Seat>,
    selected: Set<String>
): Map<Int, List<SeatUiModel?>> {
    val byCols = listOf("A", "B", "C", null, "D", "E", "F")
    return seats
        .groupBy { it.seatNumber.dropLast(1).toIntOrNull() ?: 0 }
        .toSortedMap()
        .mapValues { (_, rowSeats) ->
            byCols.map { col ->
                if (col == null) null
                else {
                    val seat = rowSeats.firstOrNull { it.seatNumber.endsWith(col) }
                    seat?.let {
                        SeatUiModel(
                            seat = it,
                            status = when {
                                it.seatNumber in selected -> SeatUiStatus.SELECTED
                                !it.isAvailable          -> SeatUiStatus.TAKEN
                                else                     -> SeatUiStatus.AVAILABLE
                            }
                        )
                    }
                }
            }
        }
}

// ── Seat cell ─────────────────────────────────────────────────────────────────
@Composable
private fun SeatCell(model: SeatUiModel?, onToggle: (Seat) -> Unit) {
    val size = 36.dp
    if (model == null) { Spacer(Modifier.width(16.dp)); return }

    val isBiz = model.seat.isBusinessClass()
    val (bg, textColor) = when (model.status) {
        SeatUiStatus.AVAILABLE -> (if (isBiz) BusinessBlue else EconomyBlue) to Color.White
        SeatUiStatus.TAKEN     -> TakenGray to Color(0xFF999999)
        SeatUiStatus.SELECTED  -> SelectedBlue to BusinessBlue
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .then(
                if (model.status != SeatUiStatus.TAKEN)
                    Modifier.clickable { onToggle(model.seat) }
                else Modifier
            )
    ) {
        Text(model.seat.seatNumber, color = textColor, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Seat row ──────────────────────────────────────────────────────────────────
@Composable
private fun SeatRow(cells: List<SeatUiModel?>, onToggle: (Seat) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        cells.forEach { model ->
            SeatCell(model, onToggle)
            Spacer(Modifier.width(5.dp))
        }
    }
}

// ── Section label ─────────────────────────────────────────────────────────────
@Composable
private fun SectionLabel(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Text(
            text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            color = LabelText, letterSpacing = 1.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(SectionBg)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

// ── Exit row ──────────────────────────────────────────────────────────────────
@Composable
private fun ExitRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Text("→", color = ExitRed, fontSize = 18.sp, modifier = Modifier.padding(start = 8.dp))
        Text(
            "LỐI THOÁT HIỂM", color = LabelText, fontSize = 11.sp, letterSpacing = 1.sp,
            modifier = Modifier.weight(1f), textAlign = TextAlign.Center
        )
        Text("→", color = ExitRed, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
    }
}

// ── Legend item ───────────────────────────────────────────────────────────────
@Composable
private fun LegendItem(color: Color, borderColor: Color = Color.Transparent, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)).background(color)
                .then(if (borderColor != Color.Transparent)
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(4.dp))
                else Modifier)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = LabelText)
    }
}

// ── Selected badge ────────────────────────────────────────────────────────────
@Composable
private fun SelectedBadge(seat: Seat) {
    val isBiz = seat.isBusinessClass()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isBiz) BusinessBlue else SelectedBlue)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            seat.seatNumber,
            color = if (isBiz) Color.White else BusinessBlue,
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold
        )
    }
}

// ── Main Screen ───────────────────────────────────────────────────────────────
/**
 * @param flight    Chuyến bay đang đặt (từ CustomerApp)
 * @param seats     Danh sách ghế do BookingViewModel cung cấp
 * @param isLoading Trạng thái đang tải dữ liệu
 * @param error     Thông báo lỗi nếu có
 * @param onRetry   Thử lại khi gặp lỗi
 * @param onConfirm Callback khi người dùng xác nhận ghế đã chọn
 * @param onBack    Callback quay lại
 */
@Composable
fun SeatSelectionScreen(
    flight: Flight,
    seats: List<Seat>,
    isLoading: Boolean = false,
    error: String? = null,
    onRetry: () -> Unit = {},
    onConfirm: (Seat) -> Unit,
    onBack: () -> Unit
) {
    // Ghế đang được chọn (chỉ cho phép 1 ghế / lần đặt)
    var selectedSeat by remember { mutableStateOf<Seat?>(null) }

    fun toggleSeat(seat: Seat) {
        selectedSeat = if (selectedSeat?.seatNumber == seat.seatNumber) null else seat
    }

    // Tách business / economy
    val bizSeats = seats.filter { it.isBusinessClass() }
    val ecoSeats = seats.filter { !it.isBusinessClass() }

    val selectedId = selectedSeat?.seatNumber ?: ""
    val bizGrid = buildSeatGrid(bizSeats, setOf(selectedId))
    val ecoGrid = buildSeatGrid(ecoSeats, setOf(selectedId))

    // Tìm hàng exit (giữa economy — sau hàng 15)
    val ecoRowNumbers = ecoGrid.keys.toList()
    val exitAfterIndex = ecoRowNumbers.indexOfFirst { it > 15 }.takeIf { it > 0 }

    val price = selectedSeat?.let {
        if (it.isBusinessClass()) 2_000_000L else 1_250_000L
    } ?: 0L

    Column(Modifier.fillMaxSize().background(Color.White)) {

        // ── App bar ──────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 22.sp, color = BusinessBlue)
            }
            Text(
                "SkyLine Airways", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = BusinessBlue, modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.size(48.dp))
        }

        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            "Chọn Chỗ Ngồi", fontSize = 22.sp, fontWeight = FontWeight.Bold,
            color = BusinessBlue, modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            "Chuyến bay ${flight.flightNumber} | ${flight.departureAirport} - ${flight.arrivalAirport}",
            fontSize = 12.sp, color = LabelText,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
            textAlign = TextAlign.Center
        )

        // ── Legend ────────────────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(FooterBg)
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendItem(BusinessBlue, label = "Thương gia")
                LegendItem(TakenGray,   label = "Đã đặt")
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendItem(EconomyBlue,  label = "Phổ thông")
                LegendItem(SelectedBlue, BusinessBlue, "Đang chọn")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Seat map ──────────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp))
                .background(FooterBg)
                .border(1.dp, BorderColor,
                    RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Cockpit
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(TakenGray)
                    .padding(horizontal = 40.dp, vertical = 10.dp)
            ) {
                Text("BUỒNG LÁI", fontSize = 11.sp, color = LabelText, letterSpacing = 1.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Column headers
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("A","B","C","","D","E","F").forEach { lbl ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(if (lbl.isEmpty()) 21.dp else 41.dp)
                    ) {
                        Text(lbl, fontSize = 11.sp, color = LabelText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))

            // Business section
            if (bizGrid.isNotEmpty()) {
                SectionLabel("HẠNG THƯƠNG GIA")
                bizGrid.values.forEach { row ->
                    SeatRow(row) { toggleSeat(it) }
                    Spacer(Modifier.height(6.dp))
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = BorderColor)
                ExitRow()
                HorizontalDivider(color = BorderColor)
                Spacer(Modifier.height(8.dp))
            }

            // Economy section
            if (ecoGrid.isNotEmpty()) {
                SectionLabel("HẠNG PHỔ THÔNG")
                ecoGrid.entries.forEachIndexed { idx, (_, row) ->
                    SeatRow(row) { toggleSeat(it) }
                    Spacer(Modifier.height(6.dp))
                    if (exitAfterIndex != null && idx == exitAfterIndex - 1) {
                        HorizontalDivider(color = BorderColor)
                        ExitRow()
                        HorizontalDivider(color = BorderColor)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // Loading / Error / Empty state
            if (isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    CircularProgressIndicator(color = BusinessBlue)
                }
            } else if (error != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    Text(error, color = Color.Red, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = BusinessBlue)
                    ) {
                        Text("Thử lại", color = Color.White)
                    }
                }
            } else if (seats.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    Text("Không tìm thấy thông tin ghế", color = LabelText)
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Footer ────────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, BorderColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text("GHẾ ĐÃ CHỌN", fontSize = 11.sp, color = LabelText)
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    selectedSeat?.let { SelectedBadge(it) }
                        ?: Text("Chưa chọn ghế", fontSize = 13.sp, color = LabelText)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("TỔNG CỘNG", fontSize = 11.sp, color = LabelText)
                Text(
                    if (price > 0) "%,d VND".format(price) else "—",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BusinessBlue
                )
            }
        }

        // ── Buttons ───────────────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Quay lại", fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = { selectedSeat?.let { onConfirm(it) } },
                enabled = selectedSeat != null,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BusinessBlue),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Tiếp tục", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun Preview() {
    val fakeFlight = Flight(
        id = 1,
        flightNumber = "VN-124",
        departureAirport = "Hà Nội (HAN)",
        arrivalAirport = "TP. Hồ Chí Minh (SGN)",
        airplaneId = 1
    )
    val fakeSeats = buildList {
        (1..2).forEach { r ->
            "ABCDEF".forEach { c ->
                add(
                    Seat(
                        seatNumber = "$r$c",
                        seatStatus = if (!(r == 1 && c == 'B')) "AVAILABLE" else "TAKEN"
                    )
                )
            }
        }
        (10..30).forEach { r ->
            "ABCDEF".forEach { c ->
                add(
                    Seat(
                        seatNumber = "$r$c",
                        seatStatus = if (Math.random() > 0.2) "AVAILABLE" else "TAKEN"
                    )
                )
            }
        }
    }
    MaterialTheme {
        SeatSelectionScreen(
            flight = fakeFlight,
            seats = fakeSeats,
            onConfirm = {},
            onBack = {}
        )
    }
}