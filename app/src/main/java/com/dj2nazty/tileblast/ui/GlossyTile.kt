package com.dj2nazty.tileblast.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * A glossy, slightly beveled 3D-look rounded tile. Used for board cells,
 * tray preview pieces, and the floating dragged piece so everything
 * looks consistent with the Tile Blast art style.
 */
@Composable
fun GlossyTile(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.drawBehind {
            val radius = CornerRadius(size.minDimension * 0.18f)
            drawGlossyTile(color, radius)
        },
    )
}

/** Paint a glossy tile into the current DrawScope at the full `size`. */
fun DrawScope.drawGlossyTile(base: Color, radius: CornerRadius) {
    val brighter = base.lighten(0.30f)
    val darker = base.darken(0.28f)

    // Base vertical gradient: brighter top → base → darker bottom for depth
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to brighter,
            0.55f to base,
            1f to darker,
        ),
        cornerRadius = radius,
    )

    // Glass-sheen overlay on the top half
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.38f),
            0.55f to Color.Transparent,
        ),
        cornerRadius = radius,
    )

    // Inner bottom shadow
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.75f to Color.Transparent,
            1f to Color.Black.copy(alpha = 0.22f),
        ),
        cornerRadius = radius,
    )

    // Corner sparkle highlight
    val sparkleR = size.minDimension * 0.07f
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = sparkleR,
        center = Offset(size.width * 0.28f, size.height * 0.22f),
    )
}

fun Color.lighten(amount: Float): Color = Color(
    red = (red + (1f - red) * amount).coerceIn(0f, 1f),
    green = (green + (1f - green) * amount).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * amount).coerceIn(0f, 1f),
    alpha = alpha,
)

fun Color.darken(amount: Float): Color = Color(
    red = (red * (1f - amount)).coerceIn(0f, 1f),
    green = (green * (1f - amount)).coerceIn(0f, 1f),
    blue = (blue * (1f - amount)).coerceIn(0f, 1f),
    alpha = alpha,
)
