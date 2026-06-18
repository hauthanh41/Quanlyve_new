package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.model.AppNotification
import com.example.qlydatve.service.NotificationService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)

class NotificationViewModel : ViewModel() {
    private val service = NotificationService()
    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            service.getNotifications()
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        notifications = list,
                        unreadCount = list.count { !it.isRead },
                        isLoading = false
                    )
                }
                .onFailure { _state.value = _state.value.copy(isLoading = false) }
        }
    }

    fun markRead(id: Int) {
        viewModelScope.launch {
            service.markRead(id)
            _state.value = _state.value.copy(
                notifications = _state.value.notifications.map {
                    if (it.id == id) it.copy(isRead = true) else it
                },
                unreadCount = maxOf(0, _state.value.unreadCount - 1)
            )
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            service.markAllRead()
            _state.value = _state.value.copy(
                notifications = _state.value.notifications.map { it.copy(isRead = true) },
                unreadCount = 0
            )
        }
    }

    // Polling badge mỗi 30s
    fun startPolling() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                val count = service.getUnreadCount()
                _state.value = _state.value.copy(unreadCount = count)
            }
        }
    }
}
