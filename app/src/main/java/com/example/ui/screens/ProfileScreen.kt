package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppEnvironment
import com.example.ui.components.CountryPhoneOption
import com.example.ui.components.SnowhiteCountryPhoneField
import com.example.ui.components.SnowhiteEmailField
import com.example.ui.components.SnowhiteEmailValidator
import com.example.ui.components.SnowhitePhoneValidator
import com.example.ui.components.SnowhiteUserAvatar
import com.example.viewmodel.SnowhiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SnowhiteViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isSwMember by viewModel.isSwMember.collectAsState()
    val swMember by viewModel.swMember.collectAsState()
    val swCardNumber by viewModel.swCardNumber.collectAsState()
    val freeOrdersBalance by viewModel.freeOrdersBalance.collectAsState()

    val initialCountryAndPhone = remember(currentUser?.phone) {
        SnowhitePhoneValidator.detectCountryAndDigits(currentUser?.phone)
    }

    var name by remember(currentUser?.name) { mutableStateOf(currentUser?.name ?: "") }
    var selectedCountry by remember(currentUser?.phone) { mutableStateOf(initialCountryAndPhone.first) }
    var phoneDigits by remember(currentUser?.phone) { mutableStateOf(initialCountryAndPhone.second) }
    var email by remember(currentUser?.email) { mutableStateOf(currentUser?.email ?: "") }
    var address by remember(currentUser?.address, currentUser?.serviceZone) {
        mutableStateOf(currentUser?.address ?: currentUser?.serviceZone ?: "Karachi")
    }
    var profileImageUrl by remember(currentUser?.profile_image) {
        mutableStateOf(currentUser?.profile_image ?: "")
    }
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var showValidationErrors by remember { mutableStateOf(false) }

    // 2-Step Mandatory Single-Channel OTP Verification States
    var showOtpDialog by remember { mutableStateOf(false) }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf("email") }
    var resendCountdown by remember { mutableIntStateOf(0) }
    var resendTimerTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(isOtpSent, resendTimerTrigger) {
        if (isOtpSent) {
            resendCountdown = 60
            while (resendCountdown > 0) {
                kotlinx.coroutines.delay(1000L)
                resendCountdown--
            }
        } else {
            resendCountdown = 0
        }
    }

    // Hidden 5-Tap Developer Mode Trigger State (3-second window)
    var devTapCount by remember { mutableIntStateOf(0) }
    var firstTapTimestamp by remember { mutableLongStateOf(0L) }
    var showDeveloperDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingAvatar = true
            viewModel.uploadProfilePicture(context, uri) { success, resultUrlOrMsg ->
                isUploadingAvatar = false
                if (success && !resultUrlOrMsg.isNullOrBlank()) {
                    profileImageUrl = resultUrlOrMsg
                    Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        context,
                        resultUrlOrMsg ?: "Failed to upload picture",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val userInitials = remember(currentUser?.name, name) {
        val display = (currentUser?.name ?: name).trim()
        if (display.isBlank()) {
            "SW"
        } else {
            val parts = display.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "SW"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Profile & Settings",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val now = System.currentTimeMillis()
                            if (now - firstTapTimestamp > 3000L) {
                                devTapCount = 1
                                firstTapTimestamp = now
                            } else {
                                devTapCount++
                                if (devTapCount >= 5) {
                                    devTapCount = 0
                                    firstTapTimestamp = 0L
                                    showDeveloperDialog = true
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0061E0)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Profile Header Card (Gradient + Circular Initials Avatar + Status Badges)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0061E0), Color(0xFF1E40AF))
                            )
                        )
                        .padding(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive Circular User Avatar with Camera Overlay Badge
                        SnowhiteUserAvatar(
                            profileImageUrl = profileImageUrl.ifBlank { currentUser?.profile_image },
                            initials = userInitials,
                            size = 70.dp,
                            fontSize = 24.sp,
                            isUploading = isUploadingAvatar,
                            onEditClick = {
                                imagePickerLauncher.launch("image/*")
                            }
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name?.takeIf { it.isNotBlank() } ?: name.ifBlank { "Valued Customer" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentUser?.phone?.takeIf { it.isNotBlank() }
                                    ?: phoneDigits.takeIf { it.isNotBlank() }
                                    ?: "Karachi Account",
                                fontSize = 13.sp,
                                color = Color(0xFFDBEAFE)
                            )
                            val displayEmail = currentUser?.email?.takeIf { it.isNotBlank() } ?: email.takeIf { it.isNotBlank() }
                            if (!displayEmail.isNullOrBlank()) {
                                Text(
                                    text = displayEmail,
                                    fontSize = 11.sp,
                                    color = Color(0xFFBAE6FD)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Active Status Badge
                                if (isSwMember) {
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Color(0xFFFDE047))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                tint = Color(0xFFD97706),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "SW Gold Member",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.VerifiedUser,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Verified Customer",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // Free Orders Balance Pill
                                if (freeOrdersBalance > 0) {
                                    Surface(
                                        color = Color(0xFFDCFCE7),
                                        shape = RoundedCornerShape(50),
                                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CardGiftcard,
                                                contentDescription = null,
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Free Orders: $freeOrdersBalance",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isSwMember) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = BorderStroke(1.5.dp, Color(0xFFEAB308)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE SW GOLD MEMBER CARD",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFFDE68A))
                        Text(
                            text = "Card Number: ${swMember?.cardNumber ?: currentUser?.swCardNumber ?: swCardNumber ?: "N/A"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF78350F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Member Name: ${swMember?.memberName ?: currentUser?.name ?: "N/A"}",
                            fontSize = 13.sp,
                            color = Color(0xFF78350F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Registered Phone: ${swMember?.phone ?: currentUser?.phone ?: "N/A"}",
                            fontSize = 13.sp,
                            color = Color(0xFF78350F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Active Privilege: 30% OFF Premium Plus & 20% OFF Regular / Retail",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Personal Information Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = null,
                                tint = Color(0xFF0061E0),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Personal Information",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Update your contact details & pickup address",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    val textFieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0061E0),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF0061E0)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SnowhiteCountryPhoneField(
                        country = selectedCountry,
                        onCountryChange = { selectedCountry = it },
                        phoneDigits = phoneDigits,
                        onPhoneDigitsChange = { phoneDigits = it },
                        label = "Phone Number",
                        showValidationError = showValidationErrors,
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SnowhiteEmailField(
                        email = email,
                        onEmailChange = { email = it },
                        label = "Email Address",
                        required = true,
                        showValidationError = showValidationErrors,
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Address / Service Zone") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0061E0)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (Optional)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF0061E0)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = textFieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            showValidationErrors = true
                            if (name.isBlank()) {
                                Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val phoneError = SnowhitePhoneValidator.validatePhone(selectedCountry, phoneDigits)
                            if (phoneError != null) {
                                Toast.makeText(context, phoneError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val emailError = SnowhiteEmailValidator.validateEmail(email, required = true)
                            if (emailError != null) {
                                Toast.makeText(context, emailError, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedChannel.isBlank()) {
                                selectedChannel = "email"
                            }
                            isOtpSent = false
                            otpCode = ""
                            showOtpDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "SAVING...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SAVE CHANGES",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Clean Sign Out / Logout Outlined Button
            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    onBackClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, Color(0xFFFECACA)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFFDC2626)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign Out",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out / Logout",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDeveloperDialog) {
        SecretDeveloperSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showDeveloperDialog = false }
        )
    }

    if (showOtpDialog) {
        val formattedPhone = SnowhitePhoneValidator.formatPhoneForApi(selectedCountry, phoneDigits)
        val hasTypedEmail = email.trim().isNotBlank()
        val effectiveChannel = remember(selectedChannel, hasTypedEmail) {
            if (selectedChannel == "email" && !hasTypedEmail) "whatsapp" else selectedChannel.ifBlank { if (hasTypedEmail) "email" else "whatsapp" }
        }

        AlertDialog(
            onDismissRequest = {
                if (!isLoading) {
                    showOtpDialog = false
                    isOtpSent = false
                    otpCode = ""
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Security Verification",
                                tint = Color(0xFF0061E0),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (!isOtpSent) "Security Verification" else "Enter 6-Digit OTP",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (!isOtpSent) "Mandatory OTP Required (Step 1 of 2)" else "Verify & Authorize Changes (Step 2 of 2)",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!isOtpSent) {
                        Text(
                            text = "Choose ONE channel to receive your 6-digit verification code:",
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Option A: Send OTP via Email
                        val isEmailSelected = effectiveChannel == "email"
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !isLoading) {
                                    if (hasTypedEmail) {
                                        selectedChannel = "email"
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please enter an Email Address in the form first, or select WhatsApp.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isEmailSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (isEmailSelected) 2.dp else 1.dp,
                                color = if (isEmailSelected) Color(0xFF0061E0) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isEmailSelected,
                                    onClick = {
                                        if (hasTypedEmail) {
                                            selectedChannel = "email"
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Please enter an Email Address in the form first, or select WhatsApp.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    enabled = !isLoading,
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0061E0))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Send OTP via Email",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hasTypedEmail) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = if (hasTypedEmail) email.trim() else "No email typed in form",
                                        fontSize = 12.sp,
                                        color = if (isEmailSelected) Color(0xFF0061E0) else Color(0xFF64748B)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = if (isEmailSelected) Color(0xFF0061E0) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Option B: Send OTP via WhatsApp
                        val isWhatsappSelected = effectiveChannel == "whatsapp"
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !isLoading) {
                                    selectedChannel = "whatsapp"
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isWhatsappSelected) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (isWhatsappSelected) 2.dp else 1.dp,
                                color = if (isWhatsappSelected) Color(0xFF10B981) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isWhatsappSelected,
                                    onClick = { selectedChannel = "whatsapp" },
                                    enabled = !isLoading,
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Send OTP via WhatsApp",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = formattedPhone.ifBlank { "No phone number entered" },
                                        fontSize = 12.sp,
                                        color = if (isWhatsappSelected) Color(0xFF059669) else Color(0xFF64748B)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (isWhatsappSelected) Color(0xFF10B981) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (isLoading) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF0061E0), strokeWidth = 2.5.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Sending 6-Digit OTP...", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                    } else {
                        val targetDisplay = if (effectiveChannel == "email") email.trim() else formattedPhone
                        val channelLabel = if (effectiveChannel == "email") "Email" else "WhatsApp"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "OTP sent via $channelLabel to:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = targetDisplay,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }.take(6)
                                otpCode = digitsOnly
                            },
                            label = { Text("Enter 6-Digit OTP") },
                            placeholder = { Text("000000") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (resendCountdown > 0) {
                                Text(
                                    text = "Resend OTP in ${resendCountdown}s",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "Resend OTP",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0061E0),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable(enabled = !isLoading) {
                                        isLoading = true
                                        viewModel.requestProfileUpdateOtp(
                                            channel = effectiveChannel,
                                            newEmail = email.trim(),
                                            newPhone = formattedPhone
                                        ) { success, msg ->
                                            isLoading = false
                                            if (success) {
                                                resendTimerTrigger++
                                                Toast.makeText(context, msg ?: "OTP resent via $channelLabel", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, msg ?: "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }

                            val alternateChannelLabel = if (effectiveChannel == "email") "WhatsApp" else "Email"
                            Text(
                                text = "Try via $alternateChannelLabel instead",
                                fontSize = 12.sp,
                                color = Color(0xFF0061E0),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(enabled = !isLoading) {
                                    val nextChannel = if (effectiveChannel == "email") "whatsapp" else "email"
                                    if (nextChannel == "email" && !hasTypedEmail) {
                                        Toast.makeText(context, "Please enter an Email Address in the form first.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        selectedChannel = nextChannel
                                        isOtpSent = false
                                        otpCode = ""
                                    }
                                }
                            )
                        }

                        if (isLoading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF0061E0), strokeWidth = 2.5.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Verifying & Saving Profile...", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!isOtpSent) {
                    Button(
                        onClick = {
                            val channelToUse = effectiveChannel
                            if (channelToUse == "email" && !hasTypedEmail) {
                                Toast.makeText(context, "Please enter a valid Email or choose WhatsApp", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            selectedChannel = channelToUse
                            isLoading = true
                            viewModel.requestProfileUpdateOtp(
                                channel = channelToUse,
                                newEmail = email.trim(),
                                newPhone = formattedPhone
                            ) { success, msg ->
                                isLoading = false
                                if (success) {
                                    isOtpSent = true
                                    resendTimerTrigger++
                                    Toast.makeText(
                                        context,
                                        msg ?: "6-Digit OTP sent via ${if (channelToUse == "email") "Email" else "WhatsApp"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, msg ?: "Failed to send OTP", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send 6-Digit OTP", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (otpCode.length < 4) {
                                Toast.makeText(context, "Please enter the 6-digit OTP code", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            viewModel.verifyAndUpdateProfile(
                                otp = otpCode.trim(),
                                newName = name.trim(),
                                newPhone = formattedPhone,
                                newEmail = email.trim(),
                                newAddress = address.trim().ifBlank { "Karachi" },
                                newPassword = if (newPassword.isBlank()) null else newPassword,
                                profileImage = profileImageUrl.ifBlank { currentUser?.profile_image }
                            ) { success, msg ->
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    msg ?: if (success) "Profile updated & saved permanently!" else "Verification failed",
                                    Toast.LENGTH_LONG
                                ).show()
                                if (success) {
                                    showOtpDialog = false
                                    isOtpSent = false
                                    otpCode = ""
                                    newPassword = ""
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isLoading) {
                            showOtpDialog = false
                            isOtpSent = false
                            otpCode = ""
                        }
                    }
                ) {
                    Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
fun SecretDeveloperSettingsDialog(
    viewModel: SnowhiteViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentEnv by viewModel.currentEnvironment.collectAsState()
    var isSyncing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = Color(0xFF0061E0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Developer & Environment Settings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Internal diagnostics & base URL switcher",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "API SERVER ENVIRONMENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                AppEnvironment.values().forEach { env ->
                    val isSelected = currentEnv == env
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (!isSelected) {
                                    viewModel.switchEnvironment(context, env)
                                    Toast.makeText(
                                        context,
                                        "Switched to ${env.displayName}. Session reset.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        viewModel.switchEnvironment(context, env)
                                        Toast.makeText(
                                            context,
                                            "Switched to ${env.displayName}. Session reset.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0061E0))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = env.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF1E40AF) else Color(0xFF0F172A)
                                    )
                                    if (env == AppEnvironment.PRODUCTION) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "DEFAULT LIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = env.baseUrl,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFE2E8F0))

                Text(
                    text = "DEVELOPER DIAGNOSTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Test Notification Sound Button (Moved from public Notifications dialog)
                OutlinedButton(
                    onClick = {
                        viewModel.testNotificationSound(context)
                        Toast.makeText(context, "Test notification triggered!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color(0xFF0061E0),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Notification Sound",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0061E0)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Clear Local Cache & Force Sync Button
                Button(
                    onClick = {
                        isSyncing = true
                        viewModel.clearLocalCacheAndForceSync(context) {
                            isSyncing = false
                            Toast.makeText(
                                context,
                                "Local cache cleared & live catalog synced!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing Live Server...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Force Sync & Clear Cache",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = Color(0xFF0061E0), fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp)
    )
}
