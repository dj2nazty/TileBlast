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
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Premium glossy tile with pronounced bevel, top highlight strip, and corner sparkles.
 * Shared between the board, tray preview, and the floating dragged piece.
 */
@Composable
fun GlossyTile(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.drawBehind {
            val radius = CornerRadius(size.minDimension * 0.22f)
            drawGlossyTile(color, radius)
        },
    )
}

/**
 * Paint a glossy 3D-beveled tile filling the DrawScope.
 *
 * Stacking order (bottom → top):
 *   1. Outer drop-shadow pad (very subtle black)
 *   2. Dark rim behind the tile (fake bottom bevel edge)
 *   3. Base color fill
 *   4. Vertical dual-gradient: bright top half → base mid → dark bottom
 *   5. Top bevel highlight strip (the bright "plastic keycap" ridge)
 *   6. Bottom inner shadow
 *   7. Thin inner stroke (slightly darker than the base) to crisp the edge
 *   8. Corner sparkle dots
 */
fun DrawScope.drawGlossyTile(base: Color, radius: CornerRadius) {
    val w = size.width
    val h = size.height

    val bright = base.lighten(0.55f)
    val mid = base.lighten(0.12f)
    val darker = base.darken(0.40f)
    val darkest = base.darken(0.62f)

    // 1. Soft drop-shadow suggestion (bottom dark edge) — rendered as a slightly
    //    offset darker round-rect so the tile reads as floating above the grid.
    drawRoundRect(
        color = darkest.copy(alpha = 0.85f),
        topLeft = Offset(0f, h * 0.03f),
        size = androidx.compose.ui.geometry.Size(w, h),
        cornerRadius = radius,
    )

    // 2. Base body
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to bright,
            0.30f to mid,
            0.68f to base,
            1f to darker,
        ),
        cornerRadius = radius,
    )

    // 3. Top glossy highlight band — bright white arc that fades down
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.55f),
            0.35f to Color.White.copy(alpha = 0.12f),
            0.55f to Color.Transparent,
        ),
        cornerRadius = radius,
    )

    // 4. Bottom inner darkening for extra body
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.70f to Color.Transparent,
            1f to Color.Black.copy(alpha = 0.32f),
        ),
        cornerRadius = radius,
    )

    // 5. Crisp inner stroke so the tile edges read clean on dark bg
    drawRoundRect(
        color = darker.copy(alpha = 0.9f),
        cornerRadius = radius,
        style = Stroke(width = w * 0.025f),
    )

    // 6. Top rim highlight (very thin, just below the top edge)
    val rimInset = w * 0.10f
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.65f),
            0.35f to Color.Transparent,
            startY = h * 0.04f,
            endY = h * 0.28f,
        ),
        topLeft = Offset(rimInset, h * 0.08f),
        size = androidx.compose.ui.geometry.Size(w - rimInset * 2f, h * 0.26f),
        cornerRadius = CornerRadius(radius.x * 0.6f),
    )

    // 7. Corner star-sparkle (two overlapping circles for a glowing dot)
    val sparkleX = w * 0.28f
    val sparkleY = h * 0.22f
    drawCircle(
        color = Color.White.copy(alpha = 0.45f),
        radius = w * 0.13f,
        center = Offset(sparkleX, sparkleY),
    )
    drawCircle(
        color = Color.White,
        radius = w * 0.055f,
        center = Offset(sparkleX, sparkleY),
    )

    // 8. Tiny secondary sparkle lower right
    drawCircle(
        color = Color.White.copy(alpha = 0.75f),
        radius = w * 0.035f,
        center = Offset(w * 0.75f, h * 0.62f),
    )
}

/** Draw a 4-point star sparkle (two crossed thin diamonds). Useful for burst effects. */
fun DrawScope.drawStarSparkle(
    center: Offset,
    radius: Float,
    color: Color = Color.White,
    alpha: Float = 1f,
) {
    val col = color.copy(alpha = alpha.coerceIn(0f, 1f))
    // Soft bloom glow underneath
    drawCircle(
        color = col.copy(alpha = alpha * 0.35f),
        radius = radius * 1.6f,
        center = center,
    )
    // Horizontal beam
    val thin = radius * 0.32f
    val path1 = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x - radius, center.y)
        lineTo(center.x, center.y - thin)
        lineTo(center.x + radius, center.y)
        lineTo(center.x, center.y + thin)
        close()
    }
    // Vertical beam
    val path2 = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x - thin, center.y)
        lineTo(center.x, center.y + radius)
        lineTo(center.x + thin, center.y)
        close()
    }
    drawPath(path1, col)
    drawPath(path2, col)
    // Bright white hot core
    drawCircle(
        color = Color.White.copy(alpha = alpha),
        radius = radius * 0.22f,
        center = center,
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
