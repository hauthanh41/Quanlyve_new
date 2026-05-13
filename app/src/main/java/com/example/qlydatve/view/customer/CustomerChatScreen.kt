package com.example.qlydatve.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.Message
import com.example.qlydatve.viewmodel.ChatViewModel

private val Navy   = Color(0xFF1A2B4A)
private val BgChat = Color(0xFFF0F4F8)
private val CardW  = Color.White
private val Hint   = Color(0xFF9AA5B8)
private val Blue   = Color(0xFF1565C0)

@Composable
fun CustomerChatScreen(
    currentUserId: Int,
    userName: String,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel(key = "customer_chat")
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val quickReplies = listOf("Xác nhận chuyến bay", "Đổi vé", "Hoàn tiền", "Hỏi thêm")

    // Load tin nhắn khi vào màn hình (server tự tìm admin)
    LaunchedEffect(Unit) {
        vm.loadMessages(currentUserId)
        vm.startPolling(currentUserId)
    }

    // Scroll xuống cuối khi có tin mới
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty())
            listState.animateScrollToItem(state.messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize().background(BgChat)) {

        // ── Top bar ───────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(CardW)
                .padding(horizontal = 4.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Navy)
            }
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Navy),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.HeadsetMic, null,
                    tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Hỗ trợ trực tuyến", fontSize = 15.sp,
                    fontWeight = FontWeight.Bold, color = Navy)
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                        .background(Color(0xFF4CAF50)))
                    Text("Đang hoạt động", fontSize = 11.sp, color = Color(0xFF4CAF50))
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEF2F7))

        // ── Messages ──────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            if (state.isLoading && state.messages.isEmpty()) {
                CircularProgressIndicator(
                    color = Blue,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFDDE8F5)) {
                                Text("Hôm nay", fontSize = 11.sp, color = Navy,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                            }
                        }
                    }

                    // Tin nhắn chào nếu chưa có lịch sử
                    if (state.messages.isEmpty()) {
                        item {
                            SupportBubble("Xin chào! Tôi là trợ lý hỗ trợ của SkyLine Airways. 👋")
                        }
                        item {
                            SupportBubble("Tôi có thể giúp bạn với:\n• Xác nhận chuyến bay\n• Đổi/hủy vé\n• Hoàn tiền\n• Các vấn đề khác")
                        }
                    }

                    items(state.messages) { msg ->
                        ChatBubble(msg = msg, currentUserId = currentUserId, userName = userName)
                    }
                }
            }
        }

        // ── Quick replies (chỉ hiện khi chưa có tin nhắn) ────
        if (state.messages.isEmpty() && !state.isLoading) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickReplies.take(2).forEach { reply ->
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE8EEF7),
                            modifier = Modifier.clickable {
                                vm.sendMessage(reply)
                            }) {
                            Text(reply, fontSize = 12.sp, color = Blue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickReplies.drop(2).forEach { reply ->
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE8EEF7),
                            modifier = Modifier.clickable {
                                vm.sendMessage(reply)
                            }) {
                            Text(reply, fontSize = 12.sp, color = Blue,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }

        // ── Error snackbar ────────────────────────────────────
        state.error?.let { err ->
            LaunchedEffect(err) {
                kotlinx.coroutines.delay(3000)
                vm.clearError()
            }
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFCDD2)
            ) {
                Text(err, color = Color(0xFFB71C1C), fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp))
            }
        }

        // ── Input bar ─────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(CardW)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BgChat)
                    .border(1.dp, Color(0xFFDDE3ED), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = TextStyle(fontSize = 14.sp, color = Navy),
                    decorationBox = { inner ->
                        if (inputText.isEmpty())
                            Text("Nhập tin nhắn...", fontSize = 14.sp, color = Hint)
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp).clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !state.isSending) Blue
                        else Color(0xFFDDE3ED)
                    )
                    .clickable(enabled = inputText.isNotBlank() && !state.isSending) {
                        val text = inputText.trim()
                        inputText = ""
                        vm.sendMessage(text)
                    }
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(color = Color.White,
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, null,
                        tint = if (inputText.isNotBlank()) Color.White else Hint,
                        modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SupportBubble(text: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Navy),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.HeadsetMic, null, tint = Color.White,
                modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.widthIn(max = 260.dp)) {
            Text("Hỗ trợ viên", fontSize = 11.sp, color = Hint,
                modifier = Modifier.padding(bottom = 2.dp))
            Box(modifier = Modifier.clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .background(CardW).padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(text, fontSize = 14.sp, color = Navy, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: Message, currentUserId: Int, userName: String) {
    val isFromMe = msg.senderId == currentUserId
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isFromMe) {
            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Navy),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.HeadsetMic, null, tint = Color.White,
                    modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(6.dp))
        }
        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            if (!isFromMe) Text("Hỗ trợ viên", fontSize = 11.sp, color = Hint,
                modifier = Modifier.padding(bottom = 2.dp))
            Box(
                modifier = Modifier.clip(
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isFromMe) 16.dp else 4.dp,
                        bottomEnd = if (isFromMe) 4.dp else 16.dp
                    )
                ).background(if (isFromMe) Blue else CardW)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(msg.content, fontSize = 14.sp,
                    color = if (isFromMe) Color.White else Navy, lineHeight = 20.sp)
            }
            Spacer(Modifier.height(2.dp))
            Text(msg.createdAt.take(16).replace("T", " "), fontSize = 10.sp, color = Hint)
        }
        if (isFromMe) {
            Spacer(Modifier.width(6.dp))
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFDDE8F5)),
                contentAlignment = Alignment.Center) {
                Text(userName.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
            }
        }
    }
}
