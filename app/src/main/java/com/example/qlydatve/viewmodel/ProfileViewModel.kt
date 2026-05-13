package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.model.User
import com.example.qlydatve.network.RetrofitClient
import com.example.qlydatve.network.TokenManager
import com.example.qlydatve.network.dto.UpdateProfileRequest
import com.example.qlydatve.network.dto.ChangePasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null,
    val updatedUser: User? = null
)

class ProfileViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun updateProfile(userId: Int, fullName: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            try {
                val token = "Bearer ${TokenManager.getToken()}"
                val res = api.updateProfile(token, userId, UpdateProfileRequest(fullName, phone))
                if (res.isSuccessful) {
                    _uiState.value = ProfileUiState(
                        successMessage = "Cập nhật hồ sơ thành công",
                        updatedUser = User(
                            id = userId,
                            fullName = fullName,
                            phone = phone,
                            email = "" // caller merges with existing user
                        )
                    )
                } else {
                    _uiState.value = ProfileUiState(error = "Cập nhật thất bại")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(error = "Không thể kết nối server")
            }
        }
    }

    fun changePassword(userId: Int, currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            try {
                val token = "Bearer ${TokenManager.getToken()}"
                val res = api.changePassword(token, userId, ChangePasswordRequest(currentPassword, newPassword))
                if (res.isSuccessful) {
                    _uiState.value = ProfileUiState(successMessage = "Đổi mật khẩu thành công")
                } else {
                    _uiState.value = ProfileUiState(error = "Mật khẩu hiện tại không đúng")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(error = "Không thể kết nối server")
            }
        }
    }

    fun clearState() {
        _uiState.value = ProfileUiState()
    }
}
