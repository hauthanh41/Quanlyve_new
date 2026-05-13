package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.model.User
import com.example.qlydatve.service.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CustomerUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class CustomerViewModel : ViewModel() {

    private val userService = UserService()
    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState

    init { loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            userService.getAllUsers()
                .onSuccess { _uiState.value = _uiState.value.copy(users = it, isLoading = false) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message, isLoading = false) }
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            userService.deleteUser(id)
                .onSuccess { loadUsers(); _uiState.value = _uiState.value.copy(successMessage = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
