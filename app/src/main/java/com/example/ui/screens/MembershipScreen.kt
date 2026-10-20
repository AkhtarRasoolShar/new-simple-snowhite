package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SnowhiteBrandIcon
import com.example.ui.components.SnowhiteNetworkImage
import com.example.viewmodel.SnowhiteViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    viewModel: SnowhiteViewModel,
    onBackClick: () -> Unit,
    onNavigateToDepartments: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isSwMember by viewModel.isSwMember.collectAsState()
    val swMember by viewModel.swMember.collectAsState()
    val swCardNumber by viewModel.swCardNumber.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showSwCardDialog by remember { mutableStateOf(false) }
    var swCardInput by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Snowhite Membership",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Full-screen background image of Folded Clothes
            SnowhiteNetworkImage(
                url = "https://images.unsplash.com/photo-1582735689369-4fe89db7114c?w=800&q=80",
                productName = "Folded Clothes Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Bright white overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.85f))
            )

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SnowhiteBrandIcon(logoUrl = settings.logoUrl, size = 64.dp)

                Spacer(modifier = Modifier.height(20.dp))

                if (isSwMember) {
                    val cardNo = swMember?.cardNumber ?: swCardNumber ?: "N/A"
                    val memberName = swMember?.memberName ?: currentUser?.name ?: "Valued Member"
                    val phoneNum = swMember?.phone ?: currentUser?.phone

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = BorderStroke(1.5.dp, Color(0xFFEAB308)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ACTIVE SW GOLD MEMBER",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFFDE68A))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Card: $cardNo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF78350F)
                                )
                                Text(
                                    text = "Name: $memberName",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF78350F)
                                )
                            }

                            if (!phoneNum.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "Registered Phone: $phoneNum",
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F)
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Privilege: 30% OFF Premium Plus & 20% OFF Regular / Retail",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF15803D)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = onNavigateToDepartments,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Continue to Departments",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Button 1: Non Members
                MembershipButtonCard(
                    icon = Icons.Default.Person,
                    title = "Non Members",
                    onClick = {
                        viewModel.setSwMemberStatus(false)
                        onNavigateToDepartments()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Button 2: SW Card Member
                MembershipButtonCard(
                    icon = Icons.Default.Badge,
                    title = if (isSwMember) "SW Card Active (Re-verify)" else "SW Card Member",
                    onClick = {
                        showSwCardDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Button 3: To become a member
                MembershipButtonCard(
                    icon = Icons.Default.CardMembership,
                    title = "To become a member",
                    onClick = {
                        onNavigateToRegister()
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showSwCardDialog) {
        AlertDialog(
            onDismissRequest = { if (!isVerifying) showSwCardDialog = false },
            title = {
                Text(
                    text = "Verify SW Gold Membership",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your SW Card Number (e.g. B0001) or Registered Phone to verify live membership.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = swCardInput,
                        onValueChange = { swCardInput = it },
                        label = { Text("Enter SW Card No (e.g. B0001) or Registered Phone") },
                        placeholder = { Text("e.g. B0001 or 03001234567") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isVerifying
                    )
                    if (isVerifying) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF0061E0),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifying card in database...", fontSize = 12.sp, color = Color(0xFF0061E0))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val input = swCardInput.trim()
                        if (input.isBlank()) {
                            Toast.makeText(context, "Please enter SW Card Number or Registered Phone", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isVerifying = true
                        viewModel.verifySwCardLive(input) { success, member, message ->
                            isVerifying = false
                            if (success) {
                                Toast.makeText(context, message ?: "SW Gold Membership Verified!", Toast.LENGTH_LONG).show()
                                showSwCardDialog = false
                                onNavigateToDepartments()
                            } else {
                                Toast.makeText(context, message ?: "Invalid SW Card Number or Phone. Please contact Snowhite support.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                    enabled = !isVerifying
                ) {
                    Text("Verify Card", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSwCardDialog = false },
                    enabled = !isVerifying
                ) {
                    Text("CANCEL", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun MembershipButtonCard(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFFF0F7FF),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color(0xFF0061E0),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

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
