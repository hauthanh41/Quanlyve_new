package com.example.qlydatve.view.admin

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
import com.example.qlydatve.model.UserConversation
import com.example.qlydatve.viewmodel.ChatViewModel

private val Navy    = Color(0xFF1A2B4A)
private val BgColor = Color(0xFFF0F4F8)
private val CardBg  = Color.White
private val Hint    = Color(0xFF9AA5B8)
private val Blue    = Color(0xFF1565C0)

@Composable
fun MessagingScreen(
    adminId: Int,
    modifier: Modifier = Modifier,
    vm: ChatViewModel = viewModel(key = "admin_chat")
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var selectedConv by remember { mutableStateOf<UserConversation?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadConversations() }

    if (selectedConv != null) {
        val conv = selectedConv!!
        AdminChatScreen(
            conv = conv,
            adminId = adminId,
            vm = vm,
            modifier = modifier,
            onBack = {
                selectedConv = null
                vm.resetPolling()
                vm.loadConversations()
            }
        )
        return
    }

    Column(modifier = modifier.fillMaxSize().background(BgColor)) {

        Box(modifier = Modifier.fillMaxWidth().background(CardBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text("Tin nhắn", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Navy)
        }

        // Search bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardBg)
                .border(1.dp, Color(0xFFDDE3ED), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Search, null, tint = Hint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = Navy),
                decorationBox = { inner ->
                    if (searchQuery.isEmpty())
                        Text("Tìm kiếm cuộc trò chuyện...", fontSize = 14.sp, color = Hint)
                    inner()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.isLoading && state.conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue)
            }
        } else if (state.conversations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, null,
                        tint = Hint, modifier = Modifier.size(56.dp))
                    Text("Chưa có tin nhắn nào", color = Hint, fontSize = 15.sp)
                }
            }
        } else {
            val filtered = state.conversations.filter {
                searchQuery.isBlank() || it.fullName.contains(searchQuery, ignoreCase = true)
            }
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(filtered) { conv ->
                    ConversationItem(conv = conv, onClick = {
                        selectedConv = conv
                        vm.resetPolling()
                        vm.loadMessages(conv.userId)
                        vm.startPolling(conv.userId)
                    })
                    if (conv != filtered.last())
                        HorizontalDivider(modifier = Modifier.padding(start = 80.dp),
                            color = Color(0xFFEEF2F7))
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(conv: UserConversation, onClick: () -> Unit) {
    val hasUnread = conv.unreadCount > 0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .background(if (hasUnread) Color(0xFFEBF2FF) else CardBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFFDDE8F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(conv.fullName.firstOrNull()?.uppercase() ?: "?",
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Navy)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(conv.fullName, fontSize = 15.sp,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = Navy)
                Text(conv.lastTime.take(16).replace("T", " "), fontSize = 11.sp,
                    color = if (hasUnread) Blue else Hint)
            }
            Spacer(Modifier.height(3.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(conv.lastMessage, fontSize = 13.sp,
                    color = if (hasUnread) Navy else Hint,
                    fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1, modifier = Modifier.weight(1f))
                if (hasUnread) {
                    Spacer(Modifier.width(8.dp))
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier.size(20.dp).clip(CircleShape).background(Blue)) {
                        Text(conv.unreadCount.toString(), fontSize = 11.sp,
                            color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminChatScreen(
    conv: UserConversation,
    adminId: Int,
    vm: ChatViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty())
            listState.animateScrollToItem(state.messages.size - 1)
    }

    Column(modifier = modifier.fillMaxSize().background(BgColor)) {

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(CardBg)
                .padding(horizontal = 8.dp, vertical = 10.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Navy)
            }
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFDDE8F5)),
                contentAlignment = Alignment.Center) {
                Text(conv.fullName.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(conv.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text(conv.email, fontSize = 11.sp, color = Hint)
            }
        }

        HorizontalDivider(color = Color(0xFFEEF2F7))

        Box(modifier = Modifier.weight(1f)) {
            if (state.isLoading && state.messages.isEmpty()) {
                CircularProgressIndicator(color = Blue, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages) { msg ->
                        AdminMsgBubble(msg = msg, adminId = adminId, customerName = conv.fullName)
                    }
                }
            }
        }

        // Input
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().background(CardBg)
                .padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BgColor)
                    .padding(horizontal = 16.dp, vertical = 10.dp)) {
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
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(if (inputText.isNotBlank() && !state.isSending) Blue else Color(0xFFDDE3ED))
                    .clickable(enabled = inputText.isNotBlank() && !state.isSending) {
                        val text = inputText.trim()
                        inputText = ""
                        vm.sendMessage(text, receiverId = conv.userId)
                    }) {
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
private fun AdminMsgBubble(msg: Message, adminId: Int, customerName: String) {
    val isFromAdmin = msg.senderId == adminId
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromAdmin) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom) {
        if (!isFromAdmin) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFDDE8F5)),
                contentAlignment = Alignment.Center) {
                Text(customerName.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
            }
            Spacer(Modifier.width(6.dp))
        }
        Column(horizontalAlignment = if (isFromAdmin) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 260.dp)) {
            Box(modifier = Modifier.clip(
                RoundedCornerShape(16.dp, 16.dp,
                    if (isFromAdmin) 4.dp else 16.dp,
                    if (isFromAdmin) 16.dp else 4.dp))
                .background(if (isFromAdmin) Blue else CardBg)
                .padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(msg.content, fontSize = 14.sp,
                    color = if (isFromAdmin) Color.White else Navy, lineHeight = 20.sp)
            }
            Spacer(Modifier.height(2.dp))
            Text(msg.createdAt.take(16).replace("T", " "), fontSize = 10.sp, color = Hint)
        }
        if (isFromAdmin) {
            Spacer(Modifier.width(6.dp))
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Navy),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AdminPanelSettings, null,
                    tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
