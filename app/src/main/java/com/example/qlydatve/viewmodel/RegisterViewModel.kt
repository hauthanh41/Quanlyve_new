package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

class RegisterViewModel : ViewModel() {

    private val authService = AuthService()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(fullName: String, email: String, password: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true)
            authService.register(fullName, email, password, phone)
                .onSuccess { _uiState.value = RegisterUiState(success = true) }
                .onFailure { _uiState.value = RegisterUiState(error = it.message) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
