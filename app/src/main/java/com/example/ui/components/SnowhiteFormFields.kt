package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

enum class CountryPhoneOption(
    val flag: String,
    val dialCode: String,
    val countryName: String,
    val examplePlaceholder: String
) {
    PAKISTAN("🇵🇰", "+92", "Pakistan", "03001234567 or 3001234567"),
    UAE("🇦🇪", "+971", "UAE", "501234567 (9 digits)"),
    SAUDI("🇸🇦", "+966", "Saudi Arabia", "501234567 (9 digits)"),
    UK("🇬🇧", "+44", "United Kingdom", "7700900123 (10 digits)"),
    USA("🇺🇸", "+1", "USA / Canada", "2025550125 (10 digits)")
}

object SnowhitePhoneValidator {

    fun detectCountryAndDigits(rawPhone: String?): Pair<CountryPhoneOption, String> {
        val cleaned = rawPhone?.trim().orEmpty()
        if (cleaned.isEmpty()) return CountryPhoneOption.PAKISTAN to ""

        return when {
            cleaned.startsWith("+971") -> {
                CountryPhoneOption.UAE to cleaned.removePrefix("+971").filter { it.isDigit() }.take(9)
            }
            cleaned.startsWith("+966") -> {
                CountryPhoneOption.SAUDI to cleaned.removePrefix("+966").filter { it.isDigit() }.take(9)
            }
            cleaned.startsWith("+44") -> {
                CountryPhoneOption.UK to cleaned.removePrefix("+44").filter { it.isDigit() }.take(10)
            }
            cleaned.startsWith("+1") -> {
                CountryPhoneOption.USA to cleaned.removePrefix("+1").filter { it.isDigit() }.take(10)
            }
            cleaned.startsWith("+92") -> {
                val after = cleaned.removePrefix("+92").filter { it.isDigit() }
                val normalized = if (after.length == 10 && after.startsWith("3")) "0$after" else after.take(11)
                CountryPhoneOption.PAKISTAN to normalized
            }
            cleaned.startsWith("923") && cleaned.filter { it.isDigit() }.length == 12 -> {
                val digits = cleaned.filter { it.isDigit() }
                CountryPhoneOption.PAKISTAN to "0${digits.removePrefix("92")}"
            }
            else -> {
                val digits = cleaned.filter { it.isDigit() }
                val maxLen = if (digits.startsWith("0")) 11 else 10
                CountryPhoneOption.PAKISTAN to digits.take(maxLen)
            }
        }
    }

    fun filterPhoneInput(country: CountryPhoneOption, rawInput: String): String {
        val digitsOnly = rawInput.filter { it.isDigit() }
        return when (country) {
            CountryPhoneOption.PAKISTAN -> {
                when {
                    digitsOnly.startsWith("923") && digitsOnly.length >= 12 -> {
                        ("0" + digitsOnly.removePrefix("92")).take(11)
                    }
                    digitsOnly.startsWith("0") -> digitsOnly.take(11)
                    else -> digitsOnly.take(10)
                }
            }
            CountryPhoneOption.UAE, CountryPhoneOption.SAUDI -> {
                digitsOnly.trimStart('0').take(9)
            }
            CountryPhoneOption.UK, CountryPhoneOption.USA -> {
                digitsOnly.trimStart('0').take(10)
            }
        }
    }

    fun validatePhone(country: CountryPhoneOption, rawInput: String): String? {
        val digits = rawInput.filter { it.isDigit() }
        if (digits.isEmpty()) {
            return "Phone number is required"
        }
        return when (country) {
            CountryPhoneOption.PAKISTAN -> {
                when {
                    digits.startsWith("03") -> {
                        if (digits.length != 11) {
                            "Pakistan mobile starting with 03 must be 11 digits (${digits.length}/11)"
                        } else null
                    }
                    digits.startsWith("3") -> {
                        if (digits.length != 10) {
                            "Pakistan mobile after +92 must be 10 digits: 3XXXXXXXXX (${digits.length}/10)"
                        } else null
                    }
                    else -> "Pakistan mobile number must start with 03 (11 digits) or 3 (10 digits)"
                }
            }
            CountryPhoneOption.UAE -> {
                if (digits.length != 9) {
                    "UAE mobile number must be 9 digits after +971 (${digits.length}/9)"
                } else null
            }
            CountryPhoneOption.SAUDI -> {
                if (digits.length != 9) {
                    "Saudi mobile number must be 9 digits after +966 (${digits.length}/9)"
                } else null
            }
            CountryPhoneOption.UK -> {
                if (digits.length != 10) {
                    "UK mobile number must be 10 digits after +44 (${digits.length}/10)"
                } else null
            }
            CountryPhoneOption.USA -> {
                if (digits.length != 10) {
                    "US/Canada phone number must be 10 digits after +1 (${digits.length}/10)"
                } else null
            }
        }
    }

    fun formatPhoneForApi(country: CountryPhoneOption, rawInput: String): String {
        val digits = rawInput.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return when (country) {
            CountryPhoneOption.PAKISTAN -> {
                when {
                    digits.length == 10 && digits.startsWith("3") -> "0$digits"
                    digits.length == 12 && digits.startsWith("923") -> "0${digits.removePrefix("92")}"
                    else -> digits
                }
            }
            else -> "${country.dialCode}${digits.trimStart('0')}"
        }
    }
}

object SnowhiteEmailValidator {
    fun validateEmail(email: String, required: Boolean = true): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) {
            return if (required) "Email address is required" else null
        }
        if (!trimmed.contains("@")) {
            return "Invalid email: must include '@' (e.g. name@gmail.com)"
        }
        val localPart = trimmed.substringBefore("@")
        val domainPart = trimmed.substringAfter("@", "")
        if (localPart.isBlank()) {
            return "Invalid email: missing username before '@'"
        }
        if (!domainPart.contains(".") || domainPart.startsWith(".") || domainPart.endsWith(".")) {
            return "Invalid email: enter a valid domain after '@' (e.g. @gmail.com)"
        }
        val tld = domainPart.substringAfterLast(".")
        if (tld.length < 2) {
            return "Invalid email domain extension"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            return "Please enter a valid email address (e.g. user@gmail.com)"
        }
        return null
    }
}

@Composable
fun SnowhiteCountryPhoneField(
    country: CountryPhoneOption,
    onCountryChange: (CountryPhoneOption) -> Unit,
    phoneDigits: String,
    onPhoneDigitsChange: (String) -> Unit,
    label: String = "Phone Number",
    modifier: Modifier = Modifier,
    showValidationError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF0061E0),
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFF8FAFC)
    )
) {
    var expanded by remember { mutableStateOf(false) }
    val errorText = remember(country, phoneDigits, showValidationError) {
        if (phoneDigits.isNotEmpty() || showValidationError) {
            SnowhitePhoneValidator.validatePhone(country, phoneDigits)
        } else null
    }

    val expectedMax = remember(country, phoneDigits) {
        when (country) {
            CountryPhoneOption.PAKISTAN -> if (phoneDigits.startsWith("0")) 11 else if (phoneDigits.startsWith("3")) 10 else 11
            CountryPhoneOption.UAE, CountryPhoneOption.SAUDI -> 9
            CountryPhoneOption.UK, CountryPhoneOption.USA -> 10
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = phoneDigits,
            onValueChange = { raw ->
                val filtered = SnowhitePhoneValidator.filterPhoneInput(country, raw)
                onPhoneDigitsChange(filtered)
            },
            label = { Text(label) },
            placeholder = {
                Text(
                    text = country.examplePlaceholder,
                    fontSize = 13.sp,
                    color = Color(0xFF94A3B8)
                )
            },
            leadingIcon = {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEFF6FF))
                            .clickable { expanded = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(text = country.flag, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = country.dialCode,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061E0)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Country Code",
                            tint = Color(0xFF0061E0),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        CountryPhoneOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(option.flag, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${option.countryName} (${option.dialCode})",
                                            fontSize = 13.sp,
                                            fontWeight = if (option == country) FontWeight.Bold else FontWeight.Normal,
                                            color = if (option == country) Color(0xFF0061E0) else Color(0xFF0F172A)
                                        )
                                    }
                                },
                                onClick = {
                                    expanded = false
                                    onCountryChange(option)
                                    onPhoneDigitsChange(
                                        SnowhitePhoneValidator.filterPhoneInput(option, phoneDigits)
                                    )
                                }
                            )
                        }
                    }
                }
            },
            trailingIcon = {
                Text(
                    text = "${phoneDigits.length}/$expectedMax",
                    fontSize = 11.sp,
                    color = if (errorText == null && phoneDigits.isNotEmpty()) Color(0xFF16A34A) else Color(0xFF94A3B8),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 10.dp)
                )
            },
            isError = errorText != null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = imeAction
            ),
            colors = colors,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorText,
                    fontSize = 11.sp,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SnowhiteEmailField(
    email: String,
    onEmailChange: (String) -> Unit,
    label: String = "Email Address",
    modifier: Modifier = Modifier,
    required: Boolean = true,
    showValidationError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF0061E0),
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFF8FAFC)
    )
) {
    val errorText = remember(email, required, showValidationError) {
        if (email.isNotEmpty() || showValidationError) {
            SnowhiteEmailValidator.validateEmail(email, required = required)
        } else null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = email,
            onValueChange = { onEmailChange(it.trim()) },
            label = { Text(label) },
            placeholder = { Text("e.g. name@gmail.com", fontSize = 13.sp, color = Color(0xFF94A3B8)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = if (errorText != null) Color(0xFFDC2626) else Color(0xFF0061E0)
                )
            },
            isError = errorText != null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = imeAction
            ),
            colors = colors,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorText,
                    fontSize = 11.sp,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SnowhiteUserAvatar(
    profileImageUrl: String?,
    initials: String,
    size: Dp = 68.dp,
    fontSize: TextUnit = 24.sp,
    isUploading: Boolean = false,
    onEditClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val cleanUrl = remember(profileImageUrl) {
        profileImageUrl?.takeIf { it.isNotBlank() && it != "null" }?.let { raw ->
            if (raw.startsWith("file://") || raw.startsWith("content://") || raw.startsWith("/")) {
                raw
            } else {
                AppConfig.resolveImageUrl(raw)
            }
        }
    }

    val initialMemBitmap = remember(cleanUrl) {
        cleanUrl?.let { SnowhiteImageCache.getMemoryBitmap(it) }
    }
    var bitmapState by remember(cleanUrl) { mutableStateOf<Bitmap?>(initialMemBitmap) }

    LaunchedEffect(cleanUrl) {
        if (cleanUrl.isNullOrBlank()) {
            bitmapState = null
            return@LaunchedEffect
        }
        val mem = SnowhiteImageCache.getMemoryBitmap(cleanUrl)
        if (mem != null) {
            bitmapState = mem
            return@LaunchedEffect
        }
        val loaded = withContext(Dispatchers.IO) {
            try {
                when {
                    cleanUrl.startsWith("/") -> {
                        val file = File(cleanUrl)
                        if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                    }
                    cleanUrl.startsWith("file://") -> {
                        val path = Uri.parse(cleanUrl).path
                        if (path != null && File(path).exists()) BitmapFactory.decodeFile(path) else null
                    }
                    cleanUrl.startsWith("content://") -> {
                        context.contentResolver.openInputStream(Uri.parse(cleanUrl))?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                    else -> {
                        SnowhiteImageCache.loadFromDisk(context, cleanUrl)
                            ?: SnowhiteImageCache.downloadAndCache(context, cleanUrl)
                    }
                }?.also { bmp ->
                    SnowhiteImageCache.putMemoryBitmap(cleanUrl, bmp)
                }
            } catch (_: Exception) {
                null
            }
        }
        if (loaded != null) {
            bitmapState = loaded
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .let { mod ->
                if (onEditClick != null) {
                    mod.clickable { onEditClick() }
                } else mod
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val bmp = bitmapState ?: initialMemBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "User Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initials,
                    fontSize = fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(size * 0.35f),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        if (onEditClick != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFF0061E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Upload Profile Picture",
                    tint = Color(0xFF0061E0),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}
