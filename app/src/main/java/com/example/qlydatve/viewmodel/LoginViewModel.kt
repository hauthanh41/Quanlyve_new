package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.model.User
import com.example.qlydatve.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val loggedInUser: User? = null,
    val error: String? = null
)

class LoginViewModel : ViewModel() {

    private val authService = AuthService()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)
            authService.login(email, password)
                .onSuccess { _uiState.value = LoginUiState(loggedInUser = it) }
                .onFailure { _uiState.value = LoginUiState(error = it.message) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearUser() {
        _uiState.value = _uiState.value.copy(loggedInUser = null)
    }
}
