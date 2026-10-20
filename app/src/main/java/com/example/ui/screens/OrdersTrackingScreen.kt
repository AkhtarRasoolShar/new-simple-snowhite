package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.models.Order
import com.example.viewmodel.SnowhiteViewModel

@Composable
fun OrdersTrackingScreen(
    viewModel: SnowhiteViewModel,
    onNavigateToAuth: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val unreadChatCounts by viewModel.unreadChatCounts.collectAsState()
    val activeChatOrderId by viewModel.activeChatOrderId.collectAsState()

    var activeChatOrder by remember { mutableStateOf<Order?>(null) }
    var activeRatingOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            viewModel.loadOrders()
        }
    }

    LaunchedEffect(activeChatOrderId, orders) {
        val targetId = activeChatOrderId
        if (targetId != null && orders.isNotEmpty()) {
            val targetOrder = orders.find { it.displayId == targetId }
            if (targetOrder != null) {
                activeChatOrder = targetOrder
            }
        }
    }

    if (currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Please log in to view your orders and track live status.",
                    fontSize = 16.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToAuth,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0))
                ) {
                    Text("Log In or Register")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Orders & Live Tracking",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            IconButton(onClick = { viewModel.loadOrders() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF0061E0))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF0061E0))
                } else {
                    Text(
                        text = "You have no active orders yet.",
                        fontSize = 15.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(orders) { order ->
                    val unread = unreadChatCounts[order.displayId] ?: 0
                    OrderTrackingCard(
                        order = order,
                        unreadCount = unread,
                        onOpenChat = {
                            viewModel.clearUnreadChatForOrder(order.displayId)
                            activeChatOrder = order
                        },
                        onOpenRating = { activeRatingOrder = order }
                    )
                }
            }
        }
    }

    activeChatOrder?.let { order ->
        ChatWithRiderDialog(
            order = order,
            viewModel = viewModel,
            onDismiss = { activeChatOrder = null }
        )
    }

    activeRatingOrder?.let { order ->
        RatingDialog(
            order = order,
            viewModel = viewModel,
            onDismiss = { activeRatingOrder = null }
        )
    }
}

@Composable
fun OrderTrackingCard(
    order: Order,
    unreadCount: Int = 0,
    onOpenChat: () -> Unit,
    onOpenRating: () -> Unit
) {
    val status = order.effectiveStatus
    val (statusColor, statusBg) = getStatusColors(status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Order #${order.displayId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0061E0)
                    )
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = status.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Booking Date: ${order.date ?: "Recently Scheduled"}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OrderProgressIndicator(status = status)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pickup Address: ${order.pickupAddress ?: "Karachi"}",
                    fontSize = 13.sp,
                    color = Color(0xFF334155),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total Amount: Rs. ${order.numericTotal.toInt()} PKR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061E0)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Assigned Rider: ${order.riderName ?: "Karachi Hub Rider"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = order.riderPhone ?: "03111110500",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(containerColor = Color(0xFFDC2626), contentColor = Color.White) {
                                        Text("$unreadCount")
                                    }
                                }
                            }
                        ) {
                            OutlinedButton(
                                onClick = onOpenChat,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0061E0))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rider Chat", fontSize = 12.sp, color = Color(0xFF0061E0))
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        if (status.uppercase().contains("DELIVERED") || status.uppercase().contains("COMPLETED")) {
                            Button(
                                onClick = onOpenRating,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rate Service", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onOpenRating,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFEAB308))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Rate", fontSize = 11.sp, color = Color(0xFF334155))
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Color(0xFF0061E0))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun OrderProgressIndicator(status: String) {
    val upper = status.uppercase()
    val step = when {
        upper.contains("DELIVERED") -> 4
        upper.contains("OUT FOR") -> 3
        upper.contains("WASHING") || upper.contains("CLEANING") -> 2
        upper.contains("HUB") || upper.contains("RECEIVED") -> 1
        else -> 0
    }

    val labels = listOf("Collecting", "Hub", "In Washing", "Out Delivery", "Delivered")

    Column(modifier = Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress = { (step + 1) / 5f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFF0061E0),
            trackColor = Color(0xFFE2E8F0)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { idx, label ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = if (idx <= step) FontWeight.Bold else FontWeight.Normal,
                    color = if (idx <= step) Color(0xFF0061E0) else Color(0xFF94A3B8)
                )
            }
        }
    }
}

private fun getStatusColors(status: String): Pair<Color, Color> {
    val upper = status.uppercase()
    return when {
        upper.contains("DELIVERED") -> Pair(Color(0xFF15803D), Color(0xFFDCFCE7))
        upper.contains("OUT FOR") || upper.contains("WASHING") -> Pair(Color(0xFF0061E0), Color(0xFFF0F7FF))
        upper.contains("HUB") -> Pair(Color(0xFFD97706), Color(0xFFFEF3C7))
        else -> Pair(Color(0xFF0284C7), Color(0xFFE0F2FE))
    }
}
