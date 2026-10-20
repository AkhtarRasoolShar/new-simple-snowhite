package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SnowhiteBlue = Color(0xFF0061E0)
val SnowhiteDarkBlue = Color(0xFF0043A4)
val SnowhiteIceBlue = Color(0xFFF0F7FF)
val SnowhiteLightBorder = Color(0xFFBAE6FD)
val SnowhiteCardBorder = Color(0xFFCBD5E1)

private val LightColorScheme = lightColorScheme(
    primary = SnowhiteBlue,
    onPrimary = Color.White,
    primaryContainer = SnowhiteIceBlue,
    onPrimaryContainer = SnowhiteDarkBlue,
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    outline = SnowhiteCardBorder
)

@Composable
fun SnowhiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
