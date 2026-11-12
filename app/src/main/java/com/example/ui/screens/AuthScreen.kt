package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.SnowhiteRepository
import com.example.ui.components.CountryPhoneOption
import com.example.ui.components.SnowhiteBrandIcon
import com.example.ui.components.SnowhiteCountryPhoneField
import com.example.ui.components.SnowhiteEmailField
import com.example.ui.components.SnowhiteEmailValidator
import com.example.ui.components.SnowhitePhoneValidator
import com.example.viewmodel.SnowhiteViewModel
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: SnowhiteViewModel,
    initialRegisterMode: Boolean = false,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { SnowhiteRepository() }
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onAuthSuccess()
        }
    }

    var isRegisterMode by remember { mutableStateOf(initialRegisterMode) }
    var isForgotMode by remember { mutableStateOf(false) }
    var forgotStep by remember { mutableIntStateOf(1) }
    var forgotChannel by remember { mutableStateOf("email") }
    var forgotResendCountdown by remember { mutableIntStateOf(0) }
    var forgotResendTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(forgotStep, forgotResendTrigger) {
        if (forgotStep == 2) {
            forgotResendCountdown = 60
            while (forgotResendCountdown > 0) {
                kotlinx.coroutines.delay(1000L)
                forgotResendCountdown--
            }
        } else {
            forgotResendCountdown = 0
        }
    }

    var name by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(CountryPhoneOption.PAKISTAN) }
    var phone by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showRegisterValidation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0061E0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SnowhiteBrandIcon(logoUrl = settings.logoUrl, size = 68.dp)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SNOWHITE",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Laundry & Retail Customer Account",
                    fontSize = 13.sp,
                    color = Color(0xFFE0EFFE)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (isForgotMode) {
                    Text(
                        text = "Reset Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = if (forgotStep == 1) "Step 1 of 2: Choose OTP Delivery Channel" else "Step 2 of 2: Enter 6-Digit OTP & New Password",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (forgotStep == 1) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Registered Email or WhatsApp Phone") },
                            placeholder = { Text("e.g. name@gmail.com or 03001234567") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Select ONE channel to receive your 6-digit OTP:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val isEmailChannel = forgotChannel == "email"
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !isLoading) { forgotChannel = "email" },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isEmailChannel) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (isEmailChannel) 2.dp else 1.dp,
                                color = if (isEmailChannel) Color(0xFF0061E0) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isEmailChannel,
                                    onClick = { forgotChannel = "email" },
                                    enabled = !isLoading,
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0061E0))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Send OTP via Email",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = if (email.isNotBlank()) email.trim() else "Send to your registered email address",
                                        fontSize = 12.sp,
                                        color = if (isEmailChannel) Color(0xFF0061E0) else Color(0xFF64748B)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = if (isEmailChannel) Color(0xFF0061E0) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val isWhatsappChannel = forgotChannel == "whatsapp"
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !isLoading) { forgotChannel = "whatsapp" },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isWhatsappChannel) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                            border = BorderStroke(
                                width = if (isWhatsappChannel) 2.dp else 1.dp,
                                color = if (isWhatsappChannel) Color(0xFF10B981) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isWhatsappChannel,
                                    onClick = { forgotChannel = "whatsapp" },
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
                                        text = if (email.isNotBlank()) email.trim() else "Send to your registered WhatsApp number",
                                        fontSize = 12.sp,
                                        color = if (isWhatsappChannel) Color(0xFF059669) else Color(0xFF64748B)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (isWhatsappChannel) Color(0xFF10B981) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val targetInput = email.trim()
                                if (targetInput.isBlank()) {
                                    Toast.makeText(context, "Enter your registered Email or WhatsApp Phone", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (forgotChannel == "email" && targetInput.contains("@")) {
                                    val emailErr = SnowhiteEmailValidator.validateEmail(targetInput, required = true)
                                    if (emailErr != null) {
                                        Toast.makeText(context, emailErr, Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                }
                                isLoading = true
                                scope.launch {
                                    repository.forgotPasswordRequest(targetInput, forgotChannel).fold(
                                        onSuccess = { msg ->
                                            isLoading = false
                                            forgotStep = 2
                                            forgotResendTrigger++
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        },
                                        onFailure = { err ->
                                            isLoading = false
                                            Toast.makeText(context, err.message ?: "Failed to send OTP", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            } else {
                                Text("Send 6-Digit OTP", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            }
                        }
                    } else {
                        val channelName = if (forgotChannel == "email") "Email" else "WhatsApp"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "6-Digit OTP sent via $channelName to:",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = email.trim(),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = otp,
                            onValueChange = { input -> otp = input.filter { it.isDigit() }.take(6) },
                            label = { Text("Enter 6-Digit OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Enter New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (forgotResendCountdown > 0) {
                                Text(
                                    text = "Resend OTP in ${forgotResendCountdown}s",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            } else {
                                Text(
                                    text = "Resend OTP",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0061E0),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable(enabled = !isLoading) {
                                        isLoading = true
                                        scope.launch {
                                            repository.forgotPasswordRequest(email.trim(), forgotChannel).fold(
                                                onSuccess = { msg ->
                                                    isLoading = false
                                                    forgotResendTrigger++
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                },
                                                onFailure = { err ->
                                                    isLoading = false
                                                    Toast.makeText(context, err.message ?: "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            val otherChannelLabel = if (forgotChannel == "email") "WhatsApp" else "Email"
                            Text(
                                text = "Try via $otherChannelLabel instead",
                                fontSize = 12.sp,
                                color = Color(0xFF0061E0),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable(enabled = !isLoading) {
                                    forgotChannel = if (forgotChannel == "email") "whatsapp" else "email"
                                    forgotStep = 1
                                    otp = ""
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (otp.isBlank() || newPassword.isBlank()) {
                                    Toast.makeText(context, "Fill in 6-digit OTP and new password", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isLoading = true
                                scope.launch {
                                    repository.resetPasswordWithOtp(email.trim(), otp.trim(), newPassword).fold(
                                        onSuccess = { msg ->
                                            isLoading = false
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            isForgotMode = false
                                            forgotStep = 1
                                            otp = ""
                                            newPassword = ""
                                        },
                                        onFailure = { err ->
                                            isLoading = false
                                            Toast.makeText(context, err.message ?: "Reset failed", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            else Text("VERIFY & RESET PASSWORD", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Back to Login",
                        color = Color(0xFF0061E0),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                isForgotMode = false
                                forgotStep = 1
                            }
                            .padding(8.dp)
                    )
                } else if (isRegisterMode) {
                    Text(
                        text = "Create New Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SnowhiteCountryPhoneField(
                        country = selectedCountry,
                        onCountryChange = { selectedCountry = it },
                        phoneDigits = phone,
                        onPhoneDigitsChange = { phone = it },
                        label = "Phone Number",
                        showValidationError = showRegisterValidation
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SnowhiteEmailField(
                        email = email,
                        onEmailChange = { email = it },
                        label = "Email Address",
                        required = true,
                        showValidationError = showRegisterValidation
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            showRegisterValidation = true
                            if (name.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please enter your full name and password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val phoneErr = SnowhitePhoneValidator.validatePhone(selectedCountry, phone)
                            if (phoneErr != null) {
                                Toast.makeText(context, phoneErr, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val emailErr = SnowhiteEmailValidator.validateEmail(email, required = true)
                            if (emailErr != null) {
                                Toast.makeText(context, emailErr, Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val formattedPhone = SnowhitePhoneValidator.formatPhoneForApi(selectedCountry, phone)
                            viewModel.register(name.trim(), formattedPhone, email.trim(), password) { success, msg ->
                                if (success) {
                                    Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess()
                                } else {
                                    Toast.makeText(context, msg ?: "Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("REGISTER & LOG IN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have an account? ", color = Color(0xFF64748B))
                        Text(
                            text = "Log In",
                            color = Color(0xFF0061E0),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { isRegisterMode = false }
                        )
                    }
                } else {
                    Text(
                        text = "Sign In to Your Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        label = { Text("Email or Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFF0061E0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable { isForgotMode = true }
                                .padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (emailOrPhone.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Enter credentials", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.login(emailOrPhone, password) { success, msg ->
                                if (success) {
                                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                    onAuthSuccess()
                                } else {
                                    Toast.makeText(context, msg ?: "Login failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("LOG IN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Don't have an account? ", color = Color(0xFF64748B))
                        Text(
                            text = "Register Now",
                            color = Color(0xFF0061E0),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { isRegisterMode = true }
                        )
                    }
                }
            }
        }
    }
}
