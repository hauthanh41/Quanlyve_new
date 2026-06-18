package com.example.qlydatve.view.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qlydatve.model.AppNotification
import com.example.qlydatve.viewmodel.NotificationViewModel

private val Navy  = Color(0xFF1A2B4A)
private val BgN   = Color(0xFFF0F4F8)
private val CardN = Color.White
private val Hint  = Color(0xFF9AA5B8)
private val Blue  = Color(0xFF1565C0)

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    vm: NotificationViewModel = viewModel(key = "notif_vm")
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.load() }

    Column(modifier = modifier.fillMaxSize().background(BgN)) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(CardN)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Thông báo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Navy)
            if (state.unreadCount > 0) {
                TextButton(onClick = { vm.markAllRead() }) {
                    Text("Đọc tất cả", fontSize = 13.sp, color = Blue)
                }
            }
        }

        HorizontalDivider(color = Color(0xFFEEF2F7))

        if (state.isLoading && state.notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue)
            }
        } else if (state.notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.NotificationsNone, null,
                        tint = Hint, modifier = Modifier.size(64.dp))
                    Text("Chưa có thông báo nào", color = Hint, fontSize = 15.sp)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(state.notifications) { notif ->
                    NotificationItem(
                        notif = notif,
                        onClick = { if (!notif.isRead) vm.markRead(notif.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(notif: AppNotification, onClick: () -> Unit) {
    val isUnread = !notif.isRead
    val (icon, iconBg, iconTint) = when (notif.type) {
        "BOOKING"      -> Triple(Icons.Default.ConfirmationNumber, Color(0xFFE8F0FE), Blue)
        "CONFIRM"      -> Triple(Icons.Default.CheckCircle, Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "CANCEL"       -> Triple(Icons.Default.Cancel, Color(0xFFFFEAEA), Color(0xFFE53935))
        "DELAYED"      -> Triple(Icons.Default.AccessTime, Color(0xFFFFF8E1), Color(0xFFE65100))
        "CANCELLED"    -> Triple(Icons.Default.FlightLand, Color(0xFFFFEAEA), Color(0xFFE53935))
        "GATE_CHANGE"  -> Triple(Icons.Default.MeetingRoom, Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        "FLIGHT_ALERT" -> Triple(Icons.Default.Warning, Color(0xFFFFF3E0), Color(0xFFF57C00))
        else           -> Triple(Icons.Default.Notifications, Color(0xFFE8EEF7), Blue)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isUnread) Color(0xFFEBF2FF) else CardN)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    notif.title,
                    fontSize = 14.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                    color = Navy,
                    modifier = Modifier.weight(1f)
                )
                if (isUnread) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(Blue)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(notif.body, fontSize = 13.sp, color = if (isUnread) Navy.copy(alpha = 0.8f) else Hint,
                lineHeight = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                notif.createdAt.take(16).replace("T", " "),
                fontSize = 11.sp, color = Hint
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 74.dp), color = Color(0xFFEEF2F7))
}
