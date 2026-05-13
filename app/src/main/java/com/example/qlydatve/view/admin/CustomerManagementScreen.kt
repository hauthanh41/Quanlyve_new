package com.example.qlydatve.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.User
import com.example.qlydatve.viewmodel.CustomerViewModel

// ── Colors ────────────────────────────────────────────────────────────────────
private val NavyColor    = Color(0xFF1A2B4A)
private val BgColor      = Color(0xFFF4F6FA)
private val CardBg       = Color.White
private val HintColor    = Color(0xFF9AA5B8)
private val BorderColor  = Color(0xFFDDE3ED)
private val TableHeaderBg = Color(0xFFF0F4FA)
private val PlatinumBg   = Color(0xFF1A2B4A)
private val GoldBg       = Color(0xFFB8860B)
private val SilverBg     = Color(0xFF607D8B)
private val BlueBg       = Color(0xFF1565C0)
private val RedColor     = Color(0xFFE53935)
private val GreenColor   = Color(0xFF2E7D32)
private val HighlightBg  = Color(0xFFE3F0FF)

private const val PAGE_SIZE = 6

// Membership tier based on total bookings (simulated from user id for demo)
private fun memberTier(user: User): String = when (user.id % 4) {
    0 -> "PLATINUM"
    1 -> "GOLD"
    2 -> "SILVER"
    else -> "MEMBER"
}

private fun tierColor(tier: String): Color = when (tier) {
    "PLATINUM" -> PlatinumBg
    "GOLD"     -> GoldBg
    "SILVER"   -> SilverBg
    else       -> BlueBg
}

// Simulated miles based on user id
private fun simulatedMiles(user: User): Int = (user.id * 12345) % 200_000 + 5_000

private fun avatarInitials(name: String): String {
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) "${parts[0].first()}${parts.last().first()}".uppercase()
    else name.take(2).uppercase()
}

private val avatarColors = listOf(
    Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFF6A1B9A),
    Color(0xFFE65100), Color(0xFF00838F), Color(0xFFC62828)
)

@Composable
fun CustomerManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: CustomerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf("Tất cả hạng hội viên") }
    var tierExpanded by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var deletingUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(searchQuery, selectedTier) { currentPage = 0 }

    val customers = uiState.users.filter { it.role == "CUSTOMER" }

    val filtered = customers.filter { u ->
        val q = searchQuery.trim()
        val matchQuery = q.isBlank() ||
            u.fullName.contains(q, ignoreCase = true) ||
            u.email.contains(q, ignoreCase = true) ||
            u.phone.contains(q)
        val matchTier = selectedTier == "Tất cả hạng hội viên" || memberTier(u) == selectedTier
        matchQuery && matchTier
    }

    val totalPages = maxOf(1, (filtered.size + PAGE_SIZE - 1) / PAGE_SIZE)
    val pageItems = filtered.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)

    // Stats
    val totalCustomers = customers.size
    val platinumCount = customers.count { memberTier(it) == "PLATINUM" }
    val avgMiles = if (customers.isEmpty()) 0 else customers.sumOf { simulatedMiles(it) } / customers.size

    Column(modifier = modifier.fillMaxSize().background(BgColor)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Title ──────────────────────────────────────────────────────
            item {
                Text("Quản lý khách hàng", fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = NavyColor)
                Text("Xem và quản lý thông tin hành khách đã đăng ký.",
                    fontSize = 13.sp, color = HintColor,
                    modifier = Modifier.padding(top = 2.dp))
            }

            // ── Add button ─────────────────────────────────────────────────
            item {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                ) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm khách hàng", fontWeight = FontWeight.SemiBold)
                }
            }

            // ── Stat cards ─────────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CustomerStatCard("TỔNG HÀNH KHÁCH",
                        "%,d".format(totalCustomers), NavyColor)
                    CustomerStatCard("HỘI VIÊN PLATINUM",
                        "%,d".format(platinumCount), Color(0xFF1565C0))
                    // Highlight card with airplane icon
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = HighlightBg),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ĐẶM BAY TRUNG BÌNH", fontSize = 11.sp,
                                    color = Color(0xFF1565C0), letterSpacing = 0.5.sp)
                                Text("%,d Dặm".format(avgMiles),
                                    fontSize = 26.sp, fontWeight = FontWeight.Bold,
                                    color = NavyColor, modifier = Modifier.padding(top = 4.dp))
                            }
                            Icon(Icons.Default.Flight, null,
                                tint = Color(0xFF90CAF9),
                                modifier = Modifier.size(56.dp))
                        }
                    }
                }
            }

            // ── Search + Filter ────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Tìm kiếm tên, email hoặc số điện tho...",
                                color = HintColor, fontSize = 13.sp)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null,
                                tint = HintColor, modifier = Modifier.size(18.dp))
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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Tier dropdown
                        @OptIn(ExperimentalMaterial3Api::class)
                        ExposedDropdownMenuBox(
                            expanded = tierExpanded,
                            onExpandedChange = { tierExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedTier,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(tierExpanded)
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = BorderColor,
                                    unfocusedContainerColor = CardBg,
                                    focusedContainerColor = CardBg
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = tierExpanded,
                                onDismissRequest = { tierExpanded = false }
                            ) {
                                listOf("Tất cả hạng hội viên",
                                    "PLATINUM", "GOLD", "SILVER", "MEMBER"
                                ).forEach { tier ->
                                    DropdownMenuItem(
                                        text = { Text(tier, fontSize = 13.sp) },
                                        onClick = { selectedTier = tier; tierExpanded = false }
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { /* filter applied reactively */ },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(56.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                        ) {
                            Icon(Icons.Default.FilterList, null,
                                modifier = Modifier.size(16.dp), tint = NavyColor)
                            Spacer(Modifier.width(4.dp))
                            Text("Lọc", color = NavyColor, fontSize = 13.sp)
                        }
                    }
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
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HÀNH\nKHÁCH", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = NavyColor,
                            modifier = Modifier.weight(2f))
                        Text("HẠNG THÀNH\nVIÊN", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = NavyColor,
                            modifier = Modifier.weight(1.2f))
                        Text("TỔNG\nDẶM BAY", fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, color = NavyColor,
                            modifier = Modifier.weight(1f))
                    }
                }
            }

            // ── Table rows ─────────────────────────────────────────────────
            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NavyColor)
                    }
                }
            } else if (pageItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center) {
                        Text("Không có khách hàng nào", color = HintColor)
                    }
                }
            } else {
                items(pageItems) { user ->
                    CustomerTableRow(
                        user = user,
                        onDelete = { deletingUser = user }
                    )
                }
            }

            // ── Pagination ─────────────────────────────────────────────────
            item {
                CustomerPaginationBar(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    totalItems = filtered.size,
                    pageSize = PAGE_SIZE,
                    onPageChange = { currentPage = it }
                )
            }
        }
    }

    // ── Delete confirm dialog ──────────────────────────────────────────────
    deletingUser?.let { u ->
        AlertDialog(
            onDismissRequest = { deletingUser = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Xóa khách hàng \"${u.fullName}\"? Hành động này không thể hoàn tác.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(u.id)
                    deletingUser = null
                }) { Text("Xóa", color = RedColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deletingUser = null }) { Text("Hủy") }
            }
        )
    }

    // ── Add dialog (simple) ────────────────────────────────────────────────
    if (showAddDialog) {
        AddCustomerDialog(onDismiss = { showAddDialog = false })
    }

    // ── Snackbars ──────────────────────────────────────────────────────────
    uiState.successMessage?.let { msg ->
        LaunchedEffect(msg) { kotlinx.coroutines.delay(2000); viewModel.clearMessages() }
    }
}

// ── Stat card ─────────────────────────────────────────────────────────────────
@Composable
private fun CustomerStatCard(label: String, value: String, valueColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, fontSize = 11.sp, color = HintColor, letterSpacing = 0.5.sp)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                color = valueColor, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// ── Table row ─────────────────────────────────────────────────────────────────
@Composable
private fun CustomerTableRow(user: User, onDelete: () -> Unit) {
    val tier = memberTier(user)
    val miles = simulatedMiles(user)
    val initials = avatarInitials(user.fullName)
    val avatarColor = avatarColors[user.id % avatarColors.size]
    val skyId = "SKY-${(user.id * 1234 % 9000 + 1000)}"

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar + name
                Row(
                    modifier = Modifier.weight(2f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(avatarColor)
                    ) {
                        Text(initials, color = Color.White,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(user.fullName, fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold, color = NavyColor,
                            lineHeight = 17.sp)
                        Text("ID: $skyId", fontSize = 10.sp, color = HintColor)
                    }
                }

                // Tier badge
                Box(modifier = Modifier.weight(1.2f)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = tierColor(tier)
                    ) {
                        Text(
                            tier,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Miles + delete
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "%,d\ndặm".format(miles),
                        fontSize = 11.sp, color = NavyColor,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
            HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
        }
    }
}

// ── Pagination ────────────────────────────────────────────────────────────────
@Composable
private fun CustomerPaginationBar(
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
        Text("Hiển thị $start - $end trên\n$totalItems",
            fontSize = 11.sp, color = HintColor, modifier = Modifier.weight(1f))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
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
                CustPageButton(page = p, isSelected = p == currentPage) { onPageChange(p) }
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
private fun CustPageButton(page: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) NavyColor else Color.Transparent)
            .clickable { onClick() }
    ) {
        Text((page + 1).toString(), fontSize = 12.sp,
            color = if (isSelected) Color.White else NavyColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

// ── Add customer dialog ───────────────────────────────────────────────────────
@Composable
private fun AddCustomerDialog(onDismiss: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm khách hàng", fontWeight = FontWeight.Bold, color = NavyColor) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fullName, onValueChange = { fullName = it },
                    label = { Text("Họ tên") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp), singleLine = true
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp), singleLine = true
                )
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp), singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss, // TODO: wire to register API
                colors = ButtonDefaults.buttonColors(containerColor = NavyColor),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Thêm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
