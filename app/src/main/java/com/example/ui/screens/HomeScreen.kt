package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.config.AppConfig
import com.example.ui.components.SnowhiteImageCache
import com.example.ui.components.SnowhiteUserAvatar
import com.example.viewmodel.SnowhiteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    viewModel: SnowhiteViewModel,
    onNavigateToDepartments: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToMembership: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isSwMember by viewModel.isSwMember.collectAsState()
    val swCardNumber by viewModel.swCardNumber.collectAsState()
    val freeOrdersBalance by viewModel.freeOrdersBalance.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val userInitials = remember(currentUser?.name) {
        val display = (currentUser?.name ?: "").trim()
        if (display.isBlank()) "SW"
        else {
            val parts = display.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "SW"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // 1. Top "Get Started" Hero Card
        HeroSectionCard(
            logoUrl = settings.logoUrl,
            onBrowseClick = onNavigateToDepartments
        )

        // 2. Welcome Back & Customer Status Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome Back,",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = currentUser?.name ?: "Valued Customer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        if (!currentUser?.phone.isNullOrBlank()) {
                            Text(
                                text = currentUser?.phone ?: "",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    if (!currentUser?.effectiveProfileImage.isNullOrBlank()) {
                        SnowhiteUserAvatar(
                            profileImageUrl = currentUser?.effectiveProfileImage,
                            initials = userInitials,
                            size = 48.dp,
                            fontSize = 16.sp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color(0xFFEFF6FF), CircleShape)
                                .border(1.dp, Color(0xFFBFDBFE), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "User Profile",
                                tint = Color(0xFF0061E0),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (freeOrdersBalance > 0 || isSwMember) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (freeOrdersBalance > 0) {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$freeOrdersBalance Free Order(s)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }

                        if (isSwMember) {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFDE047))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (!swCardNumber.isNullOrBlank()) "SW Gold • $swCardNumber" else "SW Gold Member",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Quick Actions Section ("Orders & Tracking" and "SW Membership")
        Text(
            text = "Quick Actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Link 1: My Orders & Tracking
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clickable { onNavigateToOrders() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = "My Orders & Tracking",
                            tint = Color(0xFF0061E0),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Orders & Tracking",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            // Quick Link 2: SW Membership
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .clickable { onNavigateToMembership() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFFEF3C7), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = "SW Membership",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = if (isSwMember) "SW Gold Active" else "SW Membership",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }

        // 4. Recent Orders Section (if any orders exist)
        if (orders.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Orders",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "View All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0061E0),
                    modifier = Modifier.clickable { onNavigateToOrders() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                orders.take(3).forEach { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToOrders() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Order #${order.displayId}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Rs. ${order.numericTotal.toInt()} PKR • ${order.effectiveStatus}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0061E0),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View Order",
                                tint = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSectionCard(
    logoUrl: String?,
    onBrowseClick: () -> Unit
) {
    val context = LocalContext.current
    val resolvedUrl = remember(logoUrl) {
        logoUrl?.takeIf { it.isNotBlank() && it != "null" }?.let { AppConfig.resolveImageUrl(it) }
    }
    val initialCachedBitmap = remember(resolvedUrl) {
        resolvedUrl?.let { SnowhiteImageCache.getMemoryBitmap(it) }
    }
    var bitmapState by remember(resolvedUrl) { mutableStateOf(initialCachedBitmap) }

    if (bitmapState == null && !resolvedUrl.isNullOrBlank()) {
        LaunchedEffect(resolvedUrl) {
            val bmp = withContext(Dispatchers.IO) {
                SnowhiteImageCache.loadFromDisk(context, resolvedUrl)
                    ?: SnowhiteImageCache.downloadAndCache(context, resolvedUrl)
            }
            if (bmp != null) {
                bitmapState = bmp
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(210.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFF4F9FF), Color.White)
                    )
                )
                .border(1.dp, Color(0xFFDCEBFF), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Rounded-square white container (84.dp size, 22.dp corner radius, 1.5.dp blue border Color(0xFF0061E0))
                Surface(
                    modifier = Modifier.size(84.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, Color(0xFF0061E0)),
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = bitmapState ?: initialCachedBitmap
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Snowhite Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_snowhite_logo),
                                contentDescription = "Snowhite Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Pill-shaped solid blue button ("Get Started")
                Button(
                    onClick = onBrowseClick,
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F62FE)),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 0.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // 4.dp solid blue bottom accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF0061E0))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
