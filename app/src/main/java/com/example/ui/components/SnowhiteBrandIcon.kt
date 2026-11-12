package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SnowhiteBrandIcon(
    logoUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val resolvedUrl = remember(logoUrl) { resolveLogoUrl(logoUrl) }

    val initialCachedBitmap = remember(resolvedUrl) {
        resolvedUrl?.let { SnowhiteImageCache.getMemoryBitmap(it) }
    }
    var bitmapState by remember(resolvedUrl) { mutableStateOf(initialCachedBitmap) }

    if (bitmapState == null && !resolvedUrl.isNullOrBlank()) {
        LaunchedEffect(resolvedUrl) {
            val bmp = withContext(Dispatchers.IO) {
                SnowhiteImageCache.loadFromDisk(context, resolvedUrl)
                    ?: SnowhiteImageCache.downloadAndCache(context, resolvedUrl)
            }
            if (bmp != null) {
                bitmapState = bmp
            }
        }
    }

    val cornerRadius = (size.value * 0.28f).dp
    val cornerShape = RoundedCornerShape(cornerRadius)

    Surface(
        modifier = modifier
            .size(size)
            .shadow(4.dp, shape = cornerShape),
        shape = cornerShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF0061E0))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = bitmapState ?: initialCachedBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Snowhite Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((size.value * 0.08f).dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_snowhite_logo),
                    contentDescription = "Snowhite Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((size.value * 0.06f).dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

private fun resolveLogoUrl(rawUrl: String?): String? {
    if (rawUrl.isNullOrBlank() || rawUrl == "null") return null
    return AppConfig.resolveImageUrl(rawUrl).ifBlank { null }
}
