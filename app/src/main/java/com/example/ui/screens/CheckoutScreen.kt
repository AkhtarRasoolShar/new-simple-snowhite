package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.models.CartItem
import com.example.viewmodel.SnowhiteViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CheckoutScreen(
    viewModel: SnowhiteViewModel,
    onNavigateToAuth: () -> Unit,
    onOrderPlacedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val isSwMember by viewModel.isSwMember.collectAsState()
    val swMember by viewModel.swMember.collectAsState()
    val swCardNumberFromVm by viewModel.swCardNumber.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val freeOrdersBalance by viewModel.freeOrdersBalance.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            onNavigateToAuth()
        }
    }

    val user = currentUser ?: return

    var name by remember(user) { mutableStateOf(user.name ?: "") }
    var phone by remember(user) { mutableStateOf(user.phone ?: "") }
    var email by remember(user) { mutableStateOf(user.email ?: "") }
    var address by remember(user) { mutableStateOf(user.address ?: user.serviceZone ?: "") }
    var notes by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableIntStateOf(0) }

    var swCardNumber by remember { mutableStateOf(swCardNumberFromVm ?: "") }
    var isSwVerified by remember { mutableStateOf(isSwMember) }
    var discountAmount by remember { mutableStateOf(0.0) }
    var isVerifyingCard by remember { mutableStateOf(false) }

    LaunchedEffect(cartItems, isSwMember, swMember) {
        if (isSwMember && swMember != null) {
            val member = swMember
            if (member != null) {
                val pPct = member.premiumPct ?: 30.0
                val rPct = member.regularPct ?: 20.0
                discountAmount = calculateSwDiscount(cartItems, pPct, rPct)
                isSwVerified = true
                if (swCardNumber.isBlank()) {
                    swCardNumber = member.cardNumber ?: ""
                }
            }
        }
    }

    val karachiTimestamp = remember {
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy, hh:mm a 'PKT'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Karachi")
        sdf.format(Date())
    }

    val baseSubtotal = cartItems.sumOf { it.product.numericPrice * it.quantity }
    val deliveryFee = viewModel.calculateDeliveryFee()
    val totalSavedWithSw = if (isSwVerified) discountAmount else 0.0
    val grossTotalAfterSw = maxOf(0.0, baseSubtotal + deliveryFee - totalSavedWithSw)
    val netTotalPayable = if (freeOrdersBalance > 0) 0.0 else grossTotalAfterSw

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (freeOrdersBalance > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎉 You have $freeOrdersBalance Free Order(s) remaining!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF15803D)
                    )
                }
            }
        }

        Text(
            text = "Pickup & Order Summary",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF0061E0),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Booking Timestamp (Karachi PKT):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0061E0)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = karachiTimestamp,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Pickup & Contact Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pickup & Contact Details",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

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
                    label = { Text("Phone Number") },
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
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery / Pickup Address in Karachi") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Laundry Instructions (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SW Gold Membership Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SW Gold Membership",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = swCardNumber,
                        onValueChange = { swCardNumber = it },
                        label = { Text("Card Number (e.g. B2153)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isVerifyingCard
                    )

                    Button(
                        onClick = {
                            if (swCardNumber.isBlank()) {
                                Toast.makeText(context, "Please enter SW Card Number", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isVerifyingCard = true
                            viewModel.verifySwCardLive(swCardNumber) { success, member, msg ->
                                isVerifyingCard = false
                                if (success && member != null) {
                                    val pPct = member.premiumPct ?: 30.0
                                    val rPct = member.regularPct ?: 20.0
                                    val calculated = calculateSwDiscount(cartItems, pPct, rPct)
                                    discountAmount = calculated
                                    isSwVerified = true
                                    Toast.makeText(
                                        context,
                                        "SW Card Verified! Saved Rs. ${calculated.toInt()} PKR",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    isSwVerified = false
                                    discountAmount = 0.0
                                    Toast.makeText(
                                        context,
                                        msg ?: "Invalid SW Card Number",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                        enabled = !isVerifyingCard && swCardNumber.isNotBlank()
                    ) {
                        if (isVerifyingCard) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Verify & Apply", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                if (isSwVerified && discountAmount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SW Gold Card ($swCardNumber) Active! Saved Rs. ${discountAmount.toInt()} PKR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Payment Method Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Payment Method",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val methods = listOf(
                    "Cash on Delivery (COD)" to true,
                    "Easypaisa (Mobile Wallet)" to false,
                    "JazzCash (Mobile Wallet)" to false,
                    "Credit / Debit Card" to false
                )

                methods.forEachIndexed { index, (methodName, isAvailable) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isAvailable) {
                                    selectedPaymentMethod = index
                                } else {
                                    Toast
                                        .makeText(context, "$methodName - Coming Soon!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == index,
                            onClick = {
                                if (isAvailable) {
                                    selectedPaymentMethod = index
                                } else {
                                    Toast
                                        .makeText(context, "$methodName - Coming Soon!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0061E0))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = methodName,
                            fontSize = 15.sp,
                            fontWeight = if (selectedPaymentMethod == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAvailable) Color(0xFF0F172A) else Color(0xFF94A3B8)
                        )
                        if (!isAvailable) {
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Coming Soon",
                                fontSize = 11.sp,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (index < methods.size - 1) {
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Order Summary & Bill Breakdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Items in Bag (${cartItems.sumOf { it.quantity }})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                cartItems.forEach { item ->
                    val itemBasePrice = item.product.numericPrice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.product.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Qty: ${item.quantity} × Rs. ${itemBasePrice.toInt()} PKR",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = "Rs. ${(itemBasePrice * item.quantity).toInt()} PKR",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0061E0)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subtotal", color = Color(0xFF64748B))
                    Text("Rs. ${baseSubtotal.toInt()} PKR", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Delivery Fee (Karachi)", color = Color(0xFF64748B))
                    Text("Rs. ${deliveryFee.toInt()} PKR", fontWeight = FontWeight.SemiBold)
                }

                if (isSwVerified && discountAmount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("SW Gold Discount", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        Text("- Rs. ${discountAmount.toInt()} PKR", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                }

                if (freeOrdersBalance > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Free Order Applied", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        Text("- Rs. ${grossTotalAfterSw.toInt()} PKR", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Payable", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    val payableText = if (freeOrdersBalance > 0) "Rs. 0 PKR" else "Rs. ${netTotalPayable.toInt()} PKR"
                    val payableColor = if (freeOrdersBalance > 0) Color(0xFF16A34A) else Color(0xFF0061E0)
                    Text(payableText, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = payableColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val fullAddress = if (notes.isNotBlank()) "$address (Note: $notes)" else address
                viewModel.createOrder(
                    address = fullAddress,
                    timeSlotPkt = karachiTimestamp,
                    totalPayableOverride = netTotalPayable
                ) { success, msg ->
                    if (success) {
                        Toast.makeText(context, msg ?: "Pickup scheduled!", Toast.LENGTH_LONG).show()
                        onOrderPlacedSuccess()
                    } else {
                        Toast.makeText(context, msg ?: "Failed to place order", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isLoading && cartItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "CONFIRM & SCHEDULE PICKUP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

private fun calculateSwDiscount(
    cartItems: List<CartItem>,
    premiumPctRaw: Double,
    regularPctRaw: Double
): Double {
    val premRatio = if (premiumPctRaw > 1.0) premiumPctRaw / 100.0 else premiumPctRaw
    val regRatio = if (regularPctRaw > 1.0) regularPctRaw / 100.0 else regularPctRaw

    return cartItems.sumOf { cartItem ->
        val p = cartItem.product
        if (p.numericManualDiscount > 0) {
            0.0
        } else {
            val tier = p.discountTier?.lowercase() ?: "auto"
            val isPremium = when (tier) {
                "premium" -> true
                "regular" -> false
                else -> {
                    val catName = p.categoryName ?: ""
                    p.name.contains("Bridal", ignoreCase = true) ||
                            p.name.contains("Heavy", ignoreCase = true) ||
                            p.name.contains("Suit", ignoreCase = true) ||
                            p.name.contains("Formal", ignoreCase = true) ||
                            p.name.contains("Panel", ignoreCase = true) ||
                            p.name.contains("Sherwani", ignoreCase = true) ||
                            catName.contains("Premium", ignoreCase = true) ||
                            catName.contains("Household", ignoreCase = true) ||
                            p.categoryId == 2L
                }
            }

            val itemBasePrice = p.numericPrice
            val discountRate = if (isPremium) premRatio else regRatio
            (itemBasePrice * discountRate) * cartItem.quantity
        }
    }
}
