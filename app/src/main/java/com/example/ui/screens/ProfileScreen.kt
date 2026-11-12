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

    // 2-Step OTP Verification States
    var showOtpDialog by remember { mutableStateOf(false) }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf("") }

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
                            val formattedPhone = SnowhitePhoneValidator.formatPhoneForApi(selectedCountry, phoneDigits)
                            if (newPassword.isBlank()) {
                                // Direct profile save when adding/updating email, delivery address, name, phone, or profile picture without changing password
                                isLoading = true
                                viewModel.updateProfile(
                                    name = name.trim(),
                                    phone = formattedPhone,
                                    email = email.trim(),
                                    address = address.trim().ifBlank { "Karachi" },
                                    newPassword = null,
                                    profileImage = profileImageUrl.ifBlank { currentUser?.profile_image }
                                ) { success, msg ->
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        msg ?: if (success) "Profile saved successfully!" else "Failed to update profile",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                showOtpDialog = true
                            }
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
        AlertDialog(
            onDismissRequest = {
                showOtpDialog = false
                isOtpSent = false
                otpCode = ""
                selectedChannel = ""
            },
            title = {
                Text(
                    text = if (!isOtpSent) "Security Verification" else "Enter OTP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!isOtpSent) {
                        Text(
                            text = "Where should we send your OTP to authorize these changes?",
                            fontSize = 14.sp,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val formattedPhone = SnowhitePhoneValidator.formatPhoneForApi(selectedCountry, phoneDigits)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedChannel = "whatsapp"
                                    isLoading = true
                                    viewModel.requestProfileUpdateOtp(
                                        channel = "whatsapp",
                                        newEmail = email.trim(),
                                        newPhone = formattedPhone
                                    ) { success, msg ->
                                        isLoading = false
                                        if (success) {
                                            isOtpSent = true
                                            Toast.makeText(context, msg ?: "OTP sent via WhatsApp", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, msg ?: "Failed to send OTP", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isLoading
                            ) {
                                Text("Via WhatsApp", fontSize = 12.sp, color = Color(0xFF0061E0), fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    selectedChannel = "email"
                                    isLoading = true
                                    viewModel.requestProfileUpdateOtp(
                                        channel = "email",
                                        newEmail = email.trim(),
                                        newPhone = formattedPhone
                                    ) { success, msg ->
                                        isLoading = false
                                        if (success) {
                                            isOtpSent = true
                                            Toast.makeText(context, msg ?: "OTP sent via Email", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, msg ?: "Failed to send OTP", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isLoading
                            ) {
                                Text("Via Email", fontSize = 12.sp, color = Color(0xFF0061E0), fontWeight = FontWeight.Bold)
                            }
                        }
                        if (isLoading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF0061E0))
                            }
                        }
                    } else {
                        Text(
                            text = "Please enter the 6-digit code sent to your ${if (selectedChannel.isNotEmpty()) selectedChannel else "channel"}.",
                            fontSize = 14.sp,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { if (it.length <= 6) otpCode = it },
                            label = { Text("6-Digit OTP") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isLoading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF0061E0))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (isOtpSent) {
                    Button(
                        onClick = {
                            if (otpCode.isBlank()) {
                                Toast.makeText(context, "Please enter the 6-digit OTP code", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val formattedPhone = SnowhitePhoneValidator.formatPhoneForApi(selectedCountry, phoneDigits)
                            isLoading = true
                            viewModel.verifyAndUpdateProfile(
                                otp = otpCode,
                                newName = name.trim(),
                                newPhone = formattedPhone,
                                newEmail = email.trim(),
                                newAddress = address.trim().ifBlank { "Karachi" },
                                newPassword = if (newPassword.isBlank()) null else newPassword,
                                profileImage = profileImageUrl.ifBlank { currentUser?.profile_image }
                            ) { success, msg ->
                                isLoading = false
                                Toast.makeText(context, msg ?: if (success) "Profile updated successfully!" else "Update failed", Toast.LENGTH_LONG).show()
                                if (success) {
                                    showOtpDialog = false
                                    isOtpSent = false
                                    otpCode = ""
                                    selectedChannel = ""
                                    newPassword = ""
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Verify & Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showOtpDialog = false
                        isOtpSent = false
                        otpCode = ""
                        selectedChannel = ""
                    }
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
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
