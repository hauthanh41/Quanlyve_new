package com.example.qlydatve

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.qlydatve.model.User
import com.example.qlydatve.network.TokenManager
import com.example.qlydatve.service.AuthService
import com.example.qlydatve.ui.theme.QlydatveTheme
import com.example.qlydatve.view.admin.AdminApp
import com.example.qlydatve.view.customer.CustomerApp
import com.example.qlydatve.view.login.LoginScreen
import com.example.qlydatve.view.login.RegisterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TokenManager.init(applicationContext)
        setContent {
            QlydatveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AirlineApp()
                }
            }
        }
    }
}

@Composable
fun AirlineApp() {
    var currentUser by remember { mutableStateOf<User?>(null) }
    var showRegister by remember { mutableStateOf(false) }
    val authService = remember { AuthService() }

    if (showRegister) {
        RegisterScreen(
            onRegisterSuccess = { showRegister = false },
            onBack = { showRegister = false }
        )
        return
    }

    if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = { currentUser = it },
            onNavigateToRegister = { showRegister = true }
        )
    } else {
        when (currentUser!!.role) {
            "ADMIN", "STAFF" -> AdminApp(
                user = currentUser!!,
                onLogout = { authService.logout(); currentUser = null }
            )
            else -> CustomerApp(
                user = currentUser!!,
                onLogout = { authService.logout(); currentUser = null }
            )
        }
    }
}
