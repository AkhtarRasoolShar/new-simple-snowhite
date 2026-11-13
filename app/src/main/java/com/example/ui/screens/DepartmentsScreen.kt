package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SnowhiteViewModel

@Composable
fun DepartmentsScreen(
    viewModel: SnowhiteViewModel,
    onSelectLaundry: () -> Unit,
    onSelectRetail: () -> Unit
) {
    val rawCategories by viewModel.categories.collectAsState()
    // Keep superadmin sequence sorting active
    remember(rawCategories) {
        rawCategories.sortedBy { it.sort_order ?: 999 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MAIN CARD 1: DRY CLEANING / LAUNDRY
        DepartmentCard(
            buttonText = "DRY CLEANING / LAUNDRY",
            categoryIcon = Icons.Filled.LocalLaundryService,
            onClick = onSelectLaundry
        )

        Spacer(modifier = Modifier.height(20.dp))

        // MAIN CARD 2: WASHING POWDER AND PRODUCTS
        DepartmentCard(
            buttonText = "WASHING POWDER AND PRODUCTS",
            categoryIcon = Icons.Filled.ShoppingBag,
            onClick = onSelectRetail
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun DepartmentCard(
    buttonText: String,
    categoryIcon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Light-Blue Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFEEF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = buttonText,
                            tint = Color(0xFF0066EB),
                            modifier = Modifier.size(56.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = buttonText,
                            color = Color(0xFF0052CC),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Solid Blue Button
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066EB))
                ) {
                    Text(
                        text = buttonText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Solid blue bottom accent border/strip (4.dp height, Color(0xFF0066EB))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color(0xFF0066EB))
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
