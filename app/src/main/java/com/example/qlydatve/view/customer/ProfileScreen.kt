package com.example.qlydatve.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.User
import com.example.qlydatve.viewmodel.ProfileViewModel

private val NavyColor   = Color(0xFF1A2B4A)
private val BgColor     = Color(0xFFF4F6FA)
private val CardBg      = Color.White
private val BorderColor = Color(0xFFDDE3ED)
private val HintColor   = Color(0xFF9AA5B8)
private val GoldColor   = Color(0xFFF59E0B)
private val RedColor    = Color(0xFFE53935)
private val RedBg       = Color(0xFFFFF0F0)
private val BlueAccent  = Color(0xFF1565C0)

@Composable
fun ProfileScreen(
    user: User,
    onLogout: () -> Unit,
    onUserUpdated: (User) -> Unit = {},
    onContactSupport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val vm: ProfileViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    var currentUser by remember { mutableStateOf(user) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Handle success/error
    LaunchedEffect(state.successMessage, state.updatedUser) {
        state.updatedUser?.let { updated ->
            currentUser = updated.copy(email = currentUser.email, role = currentUser.role)
            onUserUpdated(currentUser)
        }
    }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearState()
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearState()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header card ──────────────────────────────────
            ProfileHeaderCard(user = currentUser)

            // ── Menu items ───────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        iconBg = Color(0xFFE8F0FE),
                        iconTint = BlueAccent,
                        title = "Chỉnh sửa hồ sơ",
                        subtitle = "Cập nhật họ tên và số điện thoại",
                        showDivider = true,
                        onClick = { showEditDialog = true }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Lock,
                        iconBg = Color(0xFFF3E5F5),
                        iconTint = Color(0xFF7B1FA2),
                        title = "Đổi mật khẩu",
                        subtitle = "Bảo mật tài khoản của bạn",
                        showDivider = true,
                        onClick = { showPasswordDialog = true }
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        iconBg = Color(0xFFFFF8E1),
                        iconTint = GoldColor,
                        title = "Thông báo",
                        subtitle = "Quản lý các cập nhật chuyến bay",
                        showDivider = false,
                        onClick = {}
                    )
                }
            }

            // ── Logout ───────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { showLogoutDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RedBg),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = RedColor
                    ) {
                        Icon(
                            Icons.Default.ExitToApp, null,
                            tint = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Column {
                        Text("Đăng xuất", fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold, color = RedColor)
                        Text("Kết thúc phiên làm việc.", fontSize = 12.sp,
                            color = RedColor.copy(alpha = 0.7f))
                    }
                }
            }

            // ── Help banner ──────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyColor),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Cần hỗ trợ?", fontSize = 17.sp,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "Đội ngũ chăm sóc khách hàng luôn sẵn sàng 24/7.",
                            fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 17.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onContactSupport,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(Color.White)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text("Liên hệ ngay", fontSize = 12.sp, color = Color.White)
                        }
                    }
                    Icon(
                        Icons.Default.HeadsetMic, null,
                        tint = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // ── Dialogs ──────────────────────────────────────────────
    if (showEditDialog) {
        EditProfileDialog(
            user = currentUser,
            onDismiss = { showEditDialog = false },
            onConfirm = { fullName, phone ->
                showEditDialog = false
                vm.updateProfile(currentUser.id, fullName, phone)
            }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { current, new ->
                showPasswordDialog = false
                vm.changePassword(currentUser.id, current, new)
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.ExitToApp, null, tint = RedColor) },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc muốn đăng xuất khỏi tài khoản?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Đăng xuất", color = RedColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

// ── Edit Profile Dialog ───────────────────────────────────────────────────────

@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (fullName: String, phone: String) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var phone by remember { mutableStateOf(user.phone) }
    var nameError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa hồ sơ", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; nameError = "" },
                    label = { Text("Họ và tên") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    isError = nameError.isNotEmpty(),
                    supportingText = { if (nameError.isNotEmpty()) Text(nameError) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Email (read-only)
                OutlinedTextField(
                    value = user.email,
                    onValueChange = {},
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (fullName.isBlank()) { nameError = "Họ tên không được để trống"; return@Button }
                onConfirm(fullName.trim(), phone.trim())
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

// ── Change Password Dialog ────────────────────────────────────────────────────

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (current: String, new: String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi mật khẩu", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (error.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RedBg
                    ) {
                        Text(error, color = RedColor, fontSize = 13.sp,
                            modifier = Modifier.padding(10.dp))
                    }
                }
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it; error = "" },
                    label = { Text("Mật khẩu hiện tại") },
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { showCurrent = !showCurrent }) {
                            Icon(
                                if (showCurrent) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null
                            )
                        }
                    },
                    visualTransformation = if (showCurrent) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it; error = "" },
                    label = { Text("Mật khẩu mới") },
                    leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(
                                if (showNew) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null
                            )
                        }
                    },
                    visualTransformation = if (showNew) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it; error = "" },
                    label = { Text("Xác nhận mật khẩu mới") },
                    leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    current.isBlank() -> error = "Nhập mật khẩu hiện tại"
                    newPass.length < 6 -> error = "Mật khẩu mới phải ít nhất 6 ký tự"
                    newPass != confirm -> error = "Mật khẩu xác nhận không khớp"
                    else -> onConfirm(current, newPass)
                }
            }) {
                Text("Đổi mật khẩu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

// ── Shared composables ────────────────────────────────────────────────────────

@Composable
private fun ProfileHeaderCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier.size(88.dp).clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(Color(0xFF4A90D9), NavyColor))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.fullName.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
                Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = NavyColor) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White,
                        modifier = Modifier.padding(5.dp))
                }
            }

            Text(user.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NavyColor)

            Surface(shape = RoundedCornerShape(20.dp), color = GoldColor) {
                Text("HỘI VIÊN GOLD",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, letterSpacing = 0.5.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Email, null, tint = HintColor, modifier = Modifier.size(14.dp))
                Text(user.email, fontSize = 13.sp, color = HintColor)
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Phone, null, tint = HintColor, modifier = Modifier.size(14.dp))
                Text(user.phone.ifBlank { "Chưa cập nhật" }, fontSize = 13.sp, color = HintColor)
            }

            HorizontalDivider(color = BorderColor)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("12", "CHUYẾN BAY")
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(BorderColor))
                StatItem("24,500", "DẶM BAY")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
        Text(label, fontSize = 11.sp, color = HintColor, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    showDivider: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = iconBg) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.padding(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyColor)
                Text(subtitle, fontSize = 12.sp, color = HintColor)
            }
            Icon(Icons.Default.ChevronRight, null, tint = HintColor, modifier = Modifier.size(20.dp))
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(start = 74.dp, end = 16.dp), color = BorderColor)
        }
    }
}
