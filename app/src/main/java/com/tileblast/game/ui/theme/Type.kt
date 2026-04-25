package com.tileblast.game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun TileTypography(): Typography {
    val display = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
    )
    return Typography(
        displayLarge = display.copy(fontSize = 52.sp),
        displayMedium = display.copy(fontSize = 28.sp),
        headlineMedium = display.copy(fontSize = 24.sp),
        titleMedium = display.copy(fontSize = 18.sp),
        bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
        labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
    )
}
