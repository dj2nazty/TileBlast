package com.dj2nazty.tileblast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Bg = Color(0xFF12121F)
val Surface = Color(0xFF1C1C30)
val Surf2 = Color(0xFF242440)
val BorderC = Color(0x17FFFFFF)
val Border2 = Color(0x2BFFFFFF)
val TextC = Color(0xFFE8E8F8)
val Muted = Color(0x73E8E8F8)
val EmptyCell = Color(0xFF191928)
val Accent = Color(0xFF5B8FFF)
val AccentSecondary = Color(0xFFA78BFF)
val Red = Color(0xFFE84A6A)

private val TileDarkScheme = darkColorScheme(
    background = Bg,
    surface = Surface,
    primary = Accent,
    onBackground = TextC,
    onSurface = TextC,
    onPrimary = Color.White,
)

@Composable
fun TileBlastTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TileDarkScheme,
        typography = TileTypography(),
        content = content,
    )
}
