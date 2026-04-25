package com.tileblast.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Premium glossy tile with a soft top-sheen and bevel.
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
 * Paint a glossy 3D-beveled tile filling the DrawScope. Pure gradient-based
 * rendering — no pasted-on "sparkle dot" artifacts. The illusion of depth
 * comes from:
 *   1. A bottom-offset dark silhouette (ground shadow / side thickness)
 *   2. Base vertical gradient (light top → base → dark bottom)
 *   3. A soft ambient radial highlight in the upper half (reflected light)
 *   4. A crisp top-edge bevel (very thin bright line across the top)
 *   5. A subtle bottom inner shadow for contact weight
 *   6. Darker inner stroke to define the silhouette
 */
fun DrawScope.drawGlossyTile(base: Color, radius: CornerRadius) {
    val w = size.width
    val h = size.height

    val topLight = base.lighten(0.45f)
    val topMid   = base.lighten(0.15f)
    val bottomDark = base.darken(0.35f)
    val bottomDarker = base.darken(0.55f)

    // 1. Silhouette/drop suggestion — offset darker tile so the bottom edge
    //    reads as the side of the plastic tile peeking out.
    drawRoundRect(
        color = bottomDarker,
        topLeft = Offset(0f, h * 0.04f),
        size = Size(w, h),
        cornerRadius = radius,
    )

    // 2. Main body: smooth vertical gradient (brighter top → base → darker)
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to topLight,
            0.40f to topMid,
            0.70f to base,
            1f to bottomDark,
        ),
        cornerRadius = radius,
    )

    // 3. Ambient top-sheen (soft radial highlight from upper-center). This is
    //    what sells the "glossy plastic" look without any fake sparkle dots.
    drawRoundRect(
        brush = Brush.radialGradient(
            0f to Color.White.copy(alpha = 0.30f),
            0.6f to Color.White.copy(alpha = 0.06f),
            1f to Color.Transparent,
            center = Offset(w * 0.5f, h * 0.05f),
            radius = w * 0.9f,
        ),
        cornerRadius = radius,
    )

    // 4. Crisp top-edge bevel line
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.White.copy(alpha = 0.55f),
            0.35f to Color.Transparent,
        ),
        topLeft = Offset(w * 0.08f, h * 0.04f),
        size = Size(w * 0.84f, h * 0.18f),
        cornerRadius = CornerRadius(radius.x * 0.7f),
    )

    // 5. Bottom inner shadow for weight
    drawRoundRect(
        brush = Brush.verticalGradient(
            0f to Color.Transparent,
            0.72f to Color.Transparent,
            1f to Color.Black.copy(alpha = 0.28f),
        ),
        cornerRadius = radius,
    )

    // 6. Thin inner stroke so the silhouette reads clean on dark bg
    drawRoundRect(
        color = bottomDark.copy(alpha = 0.85f),
        cornerRadius = radius,
        style = Stroke(width = w * 0.02f),
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
