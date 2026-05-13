package com.example.qlydatve.view.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.User
import com.example.qlydatve.viewmodel.LoginViewModel

private val BgColor      = Color(0xFFEEF2F7)
private val NavyColor    = Color(0xFF1A2B4A)
private val NavyLight    = Color(0xFF2C3E6B)
private val FieldBg      = Color(0xFFF5F7FA)
private val FieldBorder  = Color(0xFFDDE3ED)
private val HintColor    = Color(0xFF9AA5B8)
private val LinkColor    = Color(0xFF3B7DD8)

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loggedInUser) {
        uiState.loggedInUser?.let {
            viewModel.clearUser()
            onLoginSuccess(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header ──────────────────────────────────────────────
        Spacer(Modifier.height(56.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                tint = NavyColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "SkyLine Airways",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = NavyColor
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Admin Control Portal",
            fontSize = 14.sp,
            color = HintColor,
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.height(32.dp))

        // ── Card ─────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(28.dp)) {

                Text(
                    "Hệ thống Quản trị",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyColor
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Vui lòng đăng nhập để truy cập bảng điều khiển.",
                    fontSize = 13.sp,
                    color = HintColor,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(24.dp))

                // Email field
                Text("Email / Username", fontSize = 13.sp, color = NavyColor, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                LoginField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "admin@skyline.com",
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null,
                            tint = HintColor, modifier = Modifier.size(18.dp))
                    },
                    keyboardType = KeyboardType.Email
                )

                Spacer(Modifier.height(16.dp))

                // Password label row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mật khẩu", fontSize = 13.sp, color = NavyColor, fontWeight = FontWeight.Medium)
                    TextButton(
                        onClick = {},
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Quên mật khẩu?", fontSize = 13.sp, color = LinkColor)
                    }
                }
                Spacer(Modifier.height(6.dp))
                LoginField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "••••••••",
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null,
                            tint = HintColor, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (passwordVisible) Icons.Default.Lock else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (passwordVisible) NavyColor else HintColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password
                )

                Spacer(Modifier.height(14.dp))

                // Remember me
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        modifier = Modifier.size(20.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = NavyColor,
                            uncheckedColor = FieldBorder
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Ghi nhớ đăng nhập", fontSize = 13.sp, color = NavyColor)
                }

                Spacer(Modifier.height(20.dp))

                // Error
                uiState.error?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Login button
                Button(
                    onClick = { viewModel.login(email, password) },
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Đăng nhập hệ thống",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("→", fontSize = 16.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Link đăng ký
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chưa có tài khoản? ", fontSize = 13.sp, color = HintColor)
                    TextButton(
                        onClick = onNavigateToRegister,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Đăng ký ngay", fontSize = 13.sp,
                            color = LinkColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ── Footer ───────────────────────────────────────────────
        Spacer(Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Điều khoản dịch vụ", fontSize = 12.sp, color = HintColor)
            Text("  •  ", fontSize = 12.sp, color = HintColor)
            Text("Chính sách bảo mật", fontSize = 12.sp, color = HintColor)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "© 2024 SkyLine Airways Global. All rights reserved.",
            fontSize = 11.sp,
            color = HintColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FieldBg)
            .border(1.dp, FieldBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leadingIcon()
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            visualTransformation = visualTransformation,
            keyboardType = keyboardType,
            modifier = Modifier.weight(1f)
        )
        trailingIcon?.invoke()
    }
}

@Composable
private fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visualTransformation: VisualTransformation,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.padding(vertical = 14.dp),
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            color = NavyColor
        ),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 14.sp, color = HintColor)
            }
            inner()
        }
    )
}
