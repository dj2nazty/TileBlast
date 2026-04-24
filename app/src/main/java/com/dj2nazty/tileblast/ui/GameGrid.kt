package com.dj2nazty.tileblast.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.dj2nazty.tileblast.game.GameConstants
import com.dj2nazty.tileblast.game.GameState
import com.dj2nazty.tileblast.ui.theme.BorderC
import com.dj2nazty.tileblast.ui.theme.EmptyCell
import com.dj2nazty.tileblast.ui.theme.Red
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private data class BurstInstance(
    val id: Long,
    val cells: Set<Pair<Int, Int>>,
    val colors: Map<Pair<Int, Int>, Color>,
)

@Composable
fun GameGrid(
    state: GameState,
    modifier: Modifier = Modifier,
    onPositioned: (rootOffsetX: Float, rootOffsetY: Float, sizePx: Int) -> Unit,
) {
    val density = LocalDensity.current
    val gapDp = 3.dp
    val gapPx = with(density) { gapDp.toPx() }
    val padPx = with(density) { 3.dp.toPx() }

    // Track burst animations for cells that just cleared so we can overlay rays/sparkles
    // even after state.flashing is cleared back to an empty set by the ViewModel.
    val bursts = remember { mutableStateListOf<BurstInstance>() }
    LaunchedEffect(state.flashing) {
        if (state.flashing.isNotEmpty()) {
            // Snapshot the cleared cells + their previous tile colors so we can render
            // a colorful burst even after the grid repaints them empty.
            val colorMap = state.flashing.associateWith { (r, c) ->
                state.grid.getOrNull(r)?.getOrNull(c) ?: Color.White
            }
            val instance = BurstInstance(
                id = System.nanoTime(),
                cells = state.flashing.toSet(),
                colors = colorMap,
            )
            bursts += instance
            delay(700)
            bursts.remove(instance)
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    0f to Color(0xFF0E0E1A),
                    1f to Color(0xFF16162A),
                ),
            )
            .border(2.dp, BorderC, RoundedCornerShape(16.dp))
            .padding(3.dp)
            .onGloballyPositioned { coords ->
                val root = coords.positionInRoot()
                onPositioned(root.x + padPx, root.y + padPx, (coords.size.width - 2 * padPx.toInt()))
            },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(gapDp),
        ) {
            for (r in 0 until GameConstants.ROWS) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gapDp),
                ) {
                    for (c in 0 until GameConstants.COLS) {
                        val color = state.grid[r][c]
                        val ghost = color == null && state.isGhostCell(r, c)
                        val ghostColor = state.pieces.getOrNull(state.activeIdx)?.color
                        val ghostValid = state.ghostValid
                        val isJustPlaced = (r to c) in state.justPlaced
                        val isFlashing = (r to c) in state.flashing

                        Cell(
                            color = color,
                            ghost = ghost,
                            ghostColor = ghostColor,
                            ghostValid = ghostValid,
                            justPlaced = isJustPlaced,
                            flashing = isFlashing,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }

        // Burst overlay — draw rays + sparkles on top of the grid for each active burst.
        // Sized to the grid interior (matches the cell layout above).
        bursts.forEach { burst ->
            key(burst.id) {
                BurstOverlay(
                    burst = burst,
                    gapPx = gapPx,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}

@Composable
private fun Cell(
    color: Color?,
    ghost: Boolean,
    ghostColor: Color?,
    ghostValid: Boolean,
    justPlaced: Boolean,
    flashing: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(justPlaced) {
        if (justPlaced) {
            scale.snapTo(0.4f)
            scale.animateTo(1.08f, tween(200, easing = FastOutSlowInEasing))
            scale.animateTo(1f, tween(140, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(flashing) {
        if (flashing) {
            scale.snapTo(1f)
            scale.animateTo(1.15f, tween(160))
            scale.animateTo(0f, tween(260))
        } else if (!justPlaced) {
            scale.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .scale(scale.value)
            .drawBehind {
                val radius = CornerRadius(size.minDimension * 0.14f)
                // Empty cell backing (so partial-transparency ghosts don't show the grid bg).
                drawRoundRect(color = EmptyCell, cornerRadius = radius)

                when {
                    color != null -> drawGlossyTile(color, radius)
                    ghost && ghostColor != null -> {
                        val a = if (ghostValid) 0.40f else 0.20f
                        drawGlossyTile(ghostColor.copy(alpha = a), radius)
                        drawRoundRect(
                            color = if (ghostValid) ghostColor else Red,
                            cornerRadius = radius,
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                }
            },
    )
}

/** Burst overlay: draws radial rays + sparkle particles from each cleared cell, fading over ~700ms. */
@Composable
private fun BurstOverlay(
    burst: BurstInstance,
    gapPx: Float,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(burst.id) {
        progress.animateTo(1f, tween(700))
    }
    // Pre-compute randomized ray offsets so rays don't all look identical.
    val rays = remember(burst.id) {
        List(burst.cells.size) {
            RaySet(
                rayCount = 8,
                baseAngle = (Math.random() * 360.0).toFloat(),
                sparkleOffsets = List(5) {
                    Pair(
                        (Math.random() * 2 - 1).toFloat(),
                        (Math.random() * 2 - 1).toFloat(),
                    )
                },
            )
        }
    }

    Canvas(modifier = modifier) {
        val cellW = (size.width - gapPx * (GameConstants.COLS - 1)) / GameConstants.COLS
        val cellH = (size.height - gapPx * (GameConstants.ROWS - 1)) / GameConstants.ROWS
        val p = progress.value
        // Ray length grows, alpha fades out over the animation.
        val rayLenMax = cellW * 1.6f
        val rayLen = rayLenMax * easeOutCubic(p)
        val rayAlpha = (1f - p).coerceIn(0f, 1f)
        val sparkleR = cellW * 0.12f * (1f - p)
        val sparkleSpread = cellW * 1.1f * easeOutCubic(p)

        burst.cells.forEachIndexed { idx, (r, c) ->
            val cx = c * (cellW + gapPx) + cellW / 2f
            val cy = r * (cellH + gapPx) + cellH / 2f
            val tileColor = burst.colors[r to c] ?: Color.White
            val ray = rays[idx]

            // Soft white flash glow under the rays
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color.White.copy(alpha = 0.55f * rayAlpha),
                    0.3f to tileColor.copy(alpha = 0.45f * rayAlpha),
                    1f to Color.Transparent,
                    center = Offset(cx, cy),
                    radius = cellW * 1.2f,
                ),
                radius = cellW * 1.2f,
                center = Offset(cx, cy),
            )

            // Radial rays
            translate(cx, cy) {
                for (i in 0 until ray.rayCount) {
                    val angleDeg = ray.baseAngle + (360f / ray.rayCount) * i
                    rotate(angleDeg, Offset.Zero) {
                        val path = Path().apply {
                            moveTo(0f, -cellW * 0.18f)
                            lineTo(rayLen, 0f)
                            lineTo(0f, cellW * 0.18f)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                0f to tileColor.copy(alpha = 0.85f * rayAlpha),
                                1f to Color.Transparent,
                                startX = 0f,
                                endX = rayLen,
                            ),
                        )
                    }
                }
            }

            // Sparkle particles drifting outward
            for ((sx, sy) in ray.sparkleOffsets) {
                drawCircle(
                    color = Color.White.copy(alpha = rayAlpha),
                    radius = sparkleR.coerceAtLeast(0.5f),
                    center = Offset(
                        cx + sx * sparkleSpread,
                        cy + sy * sparkleSpread,
                    ),
                )
            }
        }
    }
}

private data class RaySet(
    val rayCount: Int,
    val baseAngle: Float,
    val sparkleOffsets: List<Pair<Float, Float>>,
)

private fun easeOutCubic(t: Float): Float {
    val inv = 1f - t
    return 1f - inv * inv * inv
}
