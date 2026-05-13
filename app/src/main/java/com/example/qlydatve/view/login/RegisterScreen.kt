package com.example.qlydatve.view.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.viewmodel.RegisterViewModel

private val BgColor     = Color(0xFFEEF2F7)
private val NavyColor   = Color(0xFF1A2B4A)
private val FieldBg     = Color(0xFFF5F7FA)
private val FieldBorder = Color(0xFFDDE3ED)
private val HintColor   = Color(0xFF9AA5B8)
private val LinkColor   = Color(0xFF3B7DD8)
private val GreenColor  = Color(0xFF2E7D32)

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var fullName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmVisible   by remember { mutableStateOf(false) }
    var localError       by remember { mutableStateOf("") }

    // Đăng ký thành công → quay về login
    LaunchedEffect(uiState.success) {
        if (uiState.success) onRegisterSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Send, null, tint = NavyColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Text("SkyLine Airways", fontSize = 24.sp,
                fontWeight = FontWeight.Bold, color = NavyColor)
        }
        Spacer(Modifier.height(6.dp))
        Text("Tạo tài khoản mới", fontSize = 14.sp, color = HintColor)

        Spacer(Modifier.height(24.dp))

        // Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {

                Text("Đăng ký", fontSize = 22.sp,
                    fontWeight = FontWeight.Bold, color = NavyColor)
                Text("Điền thông tin để tạo tài khoản.",
                    fontSize = 13.sp, color = HintColor)

                // Họ tên
                RegField("Họ và tên", fullName, { fullName = it },
                    "Nguyễn Văn A", Icons.Default.Person)

                // Email
                RegField("Email", email, { email = it },
                    "example@email.com", Icons.Default.Email,
                    keyboardType = KeyboardType.Email)

                // Số điện thoại
                RegField("Số điện thoại", phone, { phone = it },
                    "0901234567", Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone)

                // Mật khẩu
                RegPasswordField(
                    label = "Mật khẩu",
                    value = password,
                    onValueChange = { password = it },
                    visible = passwordVisible,
                    onToggle = { passwordVisible = !passwordVisible }
                )

                // Xác nhận mật khẩu
                RegPasswordField(
                    label = "Xác nhận mật khẩu",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    visible = confirmVisible,
                    onToggle = { confirmVisible = !confirmVisible }
                )

                // Lỗi
                val errorMsg = localError.ifBlank { uiState.error ?: "" }
                if (errorMsg.isNotBlank()) {
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                // Nút đăng ký
                Button(
                    onClick = {
                        localError = ""
                        when {
                            fullName.isBlank() -> localError = "Vui lòng nhập họ tên"
                            email.isBlank()    -> localError = "Vui lòng nhập email"
                            password.length < 6 -> localError = "Mật khẩu phải ít nhất 6 ký tự"
                            password != confirmPassword -> localError = "Mật khẩu xác nhận không khớp"
                            else -> viewModel.register(fullName.trim(), email.trim(), password, phone.trim())
                        }
                    },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White,
                            modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Tạo tài khoản", fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                // Link quay lại đăng nhập
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Đã có tài khoản? ", fontSize = 13.sp, color = HintColor)
                    TextButton(
                        onClick = onBack,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Đăng nhập", fontSize = 13.sp,
                            color = LinkColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RegField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(FieldBg)
                .border(1.dp, FieldBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = HintColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp, color = NavyColor
                ),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = HintColor)
                    inner()
                }
            )
        }
    }
}

@Composable
private fun RegPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggle: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(FieldBg)
                .border(1.dp, FieldBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, null, tint = HintColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp, color = NavyColor
                ),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text("••••••••", fontSize = 14.sp, color = HintColor)
                    inner()
                }
            )
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    null, tint = HintColor, modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
