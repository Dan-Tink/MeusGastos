package com.meusgastos.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Bg = Color(0xFF0B0F1A)
private val SurfaceColor = Color(0xFF121A2B)
private val CardColor = Color(0xFF1A2338)
private val Cyan = Color(0xFF22D3EE)
private val Violet = Color(0xFF8B5CF6)

private val Scheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Color(0xFF00212B),
    secondary = Violet,
    onSecondary = Color.White,
    background = Bg,
    onBackground = Color(0xFFE6EAF2),
    surface = SurfaceColor,
    onSurface = Color(0xFFE6EAF2),
    surfaceVariant = CardColor,
    onSurfaceVariant = Color(0xFF9AA6BF),
    outline = Color(0xFF2C3753),
    error = Color(0xFFFF6B7A),
    onError = Color.White
)

private val TypePalette = listOf(
    Color(0xFF22D3EE),
    Color(0xFFA78BFA),
    Color(0xFF34D399),
    Color(0xFFFBBF24),
    Color(0xFFF472B6),
    Color(0xFF60A5FA),
    Color(0xFFFB923C)
)

fun typeColor(name: String): Color =
    TypePalette[(name.hashCode() and Int.MAX_VALUE) % TypePalette.size]

@Composable
fun MeusGastosTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
