package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.config.AppConfig
import com.example.viewmodel.SnowhiteViewModel

@Composable
fun DepartmentsScreen(
    viewModel: SnowhiteViewModel,
    onSelectLaundry: () -> Unit,
    onSelectRetail: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()

    // 1. Dry Cleaning / Laundry image: category ID 1 or name containing "Gent" / "Laundry"
    val laundryCat = categories.find { it.id == 1L }
        ?: categories.find { it.name.contains("Gent", ignoreCase = true) || it.name.contains("Laundry", ignoreCase = true) }
        ?: categories.firstOrNull()

    // 2. Washing Powder and Products image: category ID 4 or name containing "Powder", "Retail", or "Product"
    val retailCat = categories.find { it.id == 4L }
        ?: categories.find { 
            it.name.contains("Powder", ignoreCase = true) || 
            it.name.contains("Retail", ignoreCase = true) || 
            it.name.contains("Product", ignoreCase = true) 
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MAIN CARD 1: DRY CLEANING / LAUNDRY
        DepartmentCard(
            imageUrl = laundryCat?.imageUrl,
            buttonText = "DRY CLEANING / LAUNDRY",
            onClick = onSelectLaundry
        )

        Spacer(modifier = Modifier.height(20.dp))

        // MAIN CARD 2: WASHING POWDER AND PRODUCTS
        DepartmentCard(
            imageUrl = retailCat?.imageUrl,
            buttonText = "WASHING POWDER AND PRODUCTS",
            onClick = onSelectRetail
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun DepartmentCard(
    imageUrl: String?,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val resolvedUrl = imageUrl?.takeIf { it.isNotBlank() && it != "null" }?.let { AppConfig.resolveImageUrl(it) }

                if (resolvedUrl != null) {
                    SubcomposeAsyncImage(
                        model = resolvedUrl,
                        contentDescription = buttonText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF0061E0), strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            CategoryFallbackVectorIcon(buttonText = buttonText)
                        }
                    )
                } else {
                    CategoryFallbackVectorIcon(buttonText = buttonText)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .defaultMinSize(minHeight = 54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061E0)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFF0061E0))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CategoryFallbackVectorIcon(buttonText: String) {
    val isRetail = buttonText.contains("POWDER", ignoreCase = true) ||
            buttonText.contains("RETAIL", ignoreCase = true) ||
            buttonText.contains("PRODUCT", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF0F7FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isRetail) Icons.Default.ShoppingBag else Icons.Default.LocalLaundryService,
                contentDescription = buttonText,
                tint = Color(0xFF0061E0),
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buttonText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0061E0)
            )
        }
    }
}
