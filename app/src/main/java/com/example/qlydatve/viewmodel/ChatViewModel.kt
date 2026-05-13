package com.example.qlydatve.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qlydatve.model.Message
import com.example.qlydatve.model.UserConversation
import com.example.qlydatve.service.MessageService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val conversations: List<UserConversation> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    private val service = MessageService()
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state
    private var pollingUserId: Int = -1

    // ── Admin: load danh sách hội thoại ──────────────────────
    fun loadConversations() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            service.getConversations()
                .onSuccess { _state.value = _state.value.copy(conversations = it, isLoading = false) }
                .onFailure { _state.value = _state.value.copy(error = it.message, isLoading = false) }
        }
    }

    // ── Load tin nhắn với 1 user ─────────────────────────────
    fun loadMessages(userId: Int) {
        viewModelScope.launch {
            // Chỉ show loading nếu chưa có tin nhắn nào
            if (_state.value.messages.isEmpty()) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }
            service.getMessages(userId)
                .onSuccess { _state.value = _state.value.copy(messages = it, isLoading = false) }
                .onFailure { _state.value = _state.value.copy(error = it.message, isLoading = false) }
        }
    }

    // ── Gửi tin nhắn ─────────────────────────────────────────
    fun sendMessage(content: String, receiverId: Int? = null, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSending = true)
            service.sendMessage(content, receiverId)
                .onSuccess { msg ->
                    _state.value = _state.value.copy(
                        messages = _state.value.messages + msg,
                        isSending = false
                    )
                    onDone()
                }
                .onFailure {
                    _state.value = _state.value.copy(error = it.message, isSending = false)
                }
        }
    }

    // ── Polling: tự refresh mỗi 3 giây, chỉ chạy 1 instance ─
    fun startPolling(userId: Int) {
        if (pollingUserId == userId) return  // đã đang poll userId này rồi
        pollingUserId = userId
        viewModelScope.launch {
            while (true) {
                delay(3_000)
                if (pollingUserId != userId) break  // dừng nếu đã chuyển sang user khác
                service.getMessages(userId)
                    .onSuccess { msgs ->
                        val lastId = _state.value.messages.lastOrNull()?.id
                        val newLastId = msgs.lastOrNull()?.id
                        if (newLastId != lastId) {
                            _state.value = _state.value.copy(messages = msgs)
                        }
                    }
            }
        }
    }

    // Reset để cho phép poll lại khi chuyển conversation
    fun resetPolling() {
        pollingUserId = -1
        _state.value = _state.value.copy(messages = emptyList())
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
