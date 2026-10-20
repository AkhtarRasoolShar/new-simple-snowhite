package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R

/**
 * Premium full-screen laundry & dry-cleaning themed background.
 * Displays the high-resolution dry-cleaning boutique image showing freshly cleaned
 * suits, coats, and dresses in protective bags along a modern conveyor rack,
 * finished with a dark atmospheric overlay for crystal-clear readability.
 */
@Composable
fun LaundryBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040D1A))
    ) {
        // 1. Dry Clean Photographic/Graphic Background Image
        Image(
            painter = painterResource(id = R.drawable.img_dryclean_bg),
            contentDescription = "Dry cleaning boutique with freshly pressed garments",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Cinematic Dark Scrim Overlay for contrast & readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xBB040C18), // Top: allows title to pop
                            Color(0x66081B33), // Mid-top: reveals suits on conveyor
                            Color(0x77061426), // Center: reveals freshly cleaned clothes
                            Color(0xDD030812)  // Bottom: strong foundation for navigation/cues
                        )
                    )
                )
        )

        // 3. Foreground screen content
        content()
    }
}
