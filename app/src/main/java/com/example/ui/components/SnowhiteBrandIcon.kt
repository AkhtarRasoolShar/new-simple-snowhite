package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun SnowhiteBrandIcon(
    logoUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val resolvedUrl = resolveLogoUrl(logoUrl)

    val bitmapState = produceState<ImageBitmap?>(initialValue = null, key1 = resolvedUrl) {
        value = if (!resolvedUrl.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                downloadBitmap(resolvedUrl)
            }
        } else {
            null
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
            val bitmap = bitmapState.value
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Snowhite Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((size.value * 0.08f).dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                SnowhiteDefaultAppIconBadge(size = size)
            }
        }
    }
}

@Composable
private fun SnowhiteDefaultAppIconBadge(size: Dp) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0061E0), Color(0xFF004CB3))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size((size.value * 0.45f).dp)) {
                val w = this.size.width
                val h = this.size.height

                // Sparkle / Star Emblem
                val path = Path().apply {
                    val cx = w * 0.5f
                    val cy = h * 0.38f
                    val rOuter = w * 0.28f
                    val rInner = w * 0.10f

                    for (i in 0..7) {
                        val angle = (i * Math.PI / 4).toFloat()
                        val r = if (i % 2 == 0) rOuter else rInner
                        val x = cx + r * kotlin.math.cos(angle)
                        val y = cy + r * kotlin.math.sin(angle)
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(path = path, color = Color.White)

                // Hanger Arc Curve below sparkle
                val hangerPath = Path().apply {
                    moveTo(w * 0.18f, h * 0.72f)
                    cubicTo(
                        w * 0.35f, h * 0.58f,
                        w * 0.65f, h * 0.58f,
                        w * 0.82f, h * 0.72f
                    )
                }
                drawPath(
                    path = hangerPath,
                    color = Color.White,
                    style = Stroke(width = w * 0.09f)
                )
            }

            if (size >= 32.dp) {
                Spacer(modifier = Modifier.height((size.value * 0.02f).dp))
                Text(
                    text = "SW",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = (size.value * 0.24f).sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private fun resolveLogoUrl(rawUrl: String?): String? {
    if (rawUrl.isNullOrBlank() || rawUrl == "null") return null
    return com.example.config.AppConfig.resolveImageUrl(rawUrl).ifBlank { null }
}

private fun downloadBitmap(urlString: String): ImageBitmap? {
    try {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        conn.connectTimeout = 6000
        conn.readTimeout = 6000
        conn.connect()
        if (conn.responseCode == HttpURLConnection.HTTP_OK) {
            conn.inputStream.use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                return bmp?.asImageBitmap()
            }
        }
    } catch (_: Exception) {
    }
    return null
}
