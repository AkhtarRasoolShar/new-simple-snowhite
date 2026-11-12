package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.config.AppConfig
import com.example.ui.components.SnowhiteImageCache
import com.example.viewmodel.SnowhiteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DepartmentsScreen(
    viewModel: SnowhiteViewModel,
    onSelectLaundry: () -> Unit,
    onSelectRetail: () -> Unit
) {
    val rawCategories by viewModel.categories.collectAsState()
    val categories = remember(rawCategories) {
        rawCategories.sortedBy { it.sort_order ?: 999 }
    }

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
            defaultDrawableRes = R.drawable.img_dryclean_bg,
            onClick = onSelectLaundry
        )

        Spacer(modifier = Modifier.height(20.dp))

        // MAIN CARD 2: WASHING POWDER AND PRODUCTS
        DepartmentCard(
            imageUrl = retailCat?.imageUrl,
            buttonText = "WASHING POWDER AND PRODUCTS",
            defaultDrawableRes = R.drawable.img_folded_clothes_bg,
            onClick = onSelectRetail
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun DepartmentCard(
    imageUrl: String?,
    buttonText: String,
    defaultDrawableRes: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val resolvedUrl = remember(imageUrl) {
        imageUrl?.takeIf { it.isNotBlank() && it != "null" }?.let { AppConfig.resolveImageUrl(it) }
    }

    val initialMemBitmap = remember(resolvedUrl) {
        resolvedUrl?.let { SnowhiteImageCache.getMemoryBitmap(it) }
    }
    var bitmapState by remember(resolvedUrl) { mutableStateOf(initialMemBitmap) }

    if (bitmapState == null && !resolvedUrl.isNullOrBlank()) {
        LaunchedEffect(resolvedUrl) {
            val loaded = withContext(Dispatchers.IO) {
                SnowhiteImageCache.loadFromDisk(context, resolvedUrl)
                    ?: SnowhiteImageCache.downloadAndCache(context, resolvedUrl)
            }
            if (loaded != null) {
                bitmapState = loaded
            }
        }
    }

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
                val bmp = bitmapState ?: initialMemBitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = buttonText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = defaultDrawableRes),
                        contentDescription = buttonText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
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
