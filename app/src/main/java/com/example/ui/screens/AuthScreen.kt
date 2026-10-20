package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.SnowhiteRepository
import com.example.ui.components.SnowhiteBrandIcon
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

    var isRegisterMode by remember { mutableStateOf(initialRegisterMode) }
    var isForgotMode by remember { mutableStateOf(false) }
    var forgotStep by remember { mutableIntStateOf(1) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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

                    Spacer(modifier = Modifier.height(16.dp))

                    if (forgotStep == 1) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Registered Email or WhatsApp Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (email.isBlank()) {
                                        Toast.makeText(context, "Enter registered Email or Phone", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    scope.launch {
                                        repository.forgotPasswordRequest(email, "email").fold(
                                            onSuccess = { msg ->
                                                isLoading = false
                                                forgotStep = 2
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
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                Text("Send via Email", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (email.isBlank()) {
                                        Toast.makeText(context, "Enter registered Email or Phone", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    scope.launch {
                                        repository.forgotPasswordRequest(email, "whatsapp").fold(
                                            onSuccess = { msg ->
                                                isLoading = false
                                                forgotStep = 2
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
                                    .weight(1f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                Text("Send via WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = otp,
                            onValueChange = { otp = it },
                            label = { Text("Enter 6-Digit OTP") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("Enter New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (otp.isBlank() || newPassword.isBlank()) {
                                    Toast.makeText(context, "Fill in all fields", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isLoading = true
                                scope.launch {
                                    repository.resetPasswordWithOtp(email, otp, newPassword).fold(
                                        onSuccess = { msg ->
                                            isLoading = false
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            isForgotMode = false
                                            forgotStep = 1
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
                            if (isLoading) CircularProgressIndicator(color = Color.White)
                            else Text("RESET PASSWORD", fontWeight = FontWeight.Bold)
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
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (Karachi)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Fill in all fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.register(name, phone, email, password) { success, msg ->
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
