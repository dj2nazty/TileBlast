package com.tileblast.game.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.tileblast.game.game.GameConstants
import com.tileblast.game.game.GameState
import com.tileblast.game.ui.theme.BorderC
import com.tileblast.game.ui.theme.Red
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
            delay(1500)
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
                val radius = CornerRadius(size.minDimension * 0.22f)
                // Empty-cell backing (subtle inset, slightly lighter at top for depth)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0f to Color(0xFF14142A),
                        1f to Color(0xFF0C0C1C),
                    ),
                    cornerRadius = radius,
                )
                // Inner dark rim
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.35f),
                    cornerRadius = radius,
                    style = Stroke(width = size.minDimension * 0.04f),
                )

                when {
                    color != null -> drawGlossyTile(color, radius)
                    ghost && ghostColor != null -> {
                        val a = if (ghostValid) 0.45f else 0.22f
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

/**
 * Burst overlay for a cleared row/column. For each cleared cell we render:
 *   1. A bright white-hot radial flash at the origin (peaks early, fades).
 *   2. A big colored radial bloom tinted with the tile color.
 *   3. 10 long thin light-rays emanating outward, each with a sharp core
 *      and a soft wider halo so they feel luminous rather than flat.
 *   4. Star-shaped sparkle particles drifting outward and rotating slightly.
 * Full animation is ~850ms; rays grow 3x cell size, sparkles fan out 2x.
 */
@Composable
private fun BurstOverlay(
    burst: BurstInstance,
    gapPx: Float,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(burst.id) {
        progress.animateTo(1f, tween(1400))
    }
    // Pre-compute per-cell randomized ray/sparkle layouts so each burst looks organic.
    val rays = remember(burst.id) {
        List(burst.cells.size) {
            RaySet(
                rayCount = 10,
                baseAngle = (Math.random() * 360.0).toFloat(),
                sparkles = List(7) {
                    SparkleInit(
                        dirX = (Math.random() * 2 - 1).toFloat(),
                        dirY = (Math.random() * 2 - 1).toFloat(),
                        sizeFactor = 0.5f + Math.random().toFloat(),
                        spin = (Math.random() * 360.0).toFloat(),
                    )
                },
            )
        }
    }

    Canvas(modifier = modifier) {
        val cellW = (size.width - gapPx * (GameConstants.COLS - 1)) / GameConstants.COLS
        val cellH = (size.height - gapPx * (GameConstants.ROWS - 1)) / GameConstants.ROWS
        val p = progress.value

        // Flash: peaks at ~15% then fades — the initial eye-catching pop.
        val flashPeakP = 0.15f
        val flashAlpha = if (p < flashPeakP) p / flashPeakP else (1f - (p - flashPeakP) / (1f - flashPeakP))
        val fadeOut = (1f - p).coerceIn(0f, 1f)

        val rayLenMax = cellW * 3.0f
        val rayLen = rayLenMax * easeOutCubic(p)
        val rayAlpha = fadeOut * fadeOut  // quadratic fade reads as brighter for longer
        val sparkleSpread = cellW * 2.0f * easeOutCubic(p)
        val sparkleSize = cellW * 0.22f * (1f - p * 0.6f)

        burst.cells.forEachIndexed { idx, (r, c) ->
            val cx = c * (cellW + gapPx) + cellW / 2f
            val cy = r * (cellH + gapPx) + cellH / 2f
            val tileColor = burst.colors[r to c] ?: Color.White
            val ray = rays[idx]

            // 1. Big colored bloom (behind everything)
            val bloomR = cellW * (1.4f + p * 0.6f)
            drawCircle(
                brush = Brush.radialGradient(
                    0f to tileColor.copy(alpha = 0.9f * rayAlpha),
                    0.45f to tileColor.copy(alpha = 0.35f * rayAlpha),
                    1f to Color.Transparent,
                    center = Offset(cx, cy),
                    radius = bloomR,
                ),
                radius = bloomR,
                center = Offset(cx, cy),
            )

            // 2. White-hot flash core (early peak, fades fast)
            val flashR = cellW * 0.9f * (1f + p * 0.5f)
            drawCircle(
                brush = Brush.radialGradient(
                    0f to Color.White.copy(alpha = flashAlpha),
                    0.5f to Color.White.copy(alpha = 0.45f * flashAlpha),
                    1f to Color.Transparent,
                    center = Offset(cx, cy),
                    radius = flashR,
                ),
                radius = flashR,
                center = Offset(cx, cy),
            )

            // 3. Long rays with sharp bright core + wide soft halo
            translate(cx, cy) {
                for (i in 0 until ray.rayCount) {
                    val angleDeg = ray.baseAngle + (360f / ray.rayCount) * i
                    rotate(angleDeg, Offset.Zero) {
                        // Soft wide halo ray
                        val haloWidth = cellW * 0.30f
                        val haloPath = Path().apply {
                            moveTo(0f, -haloWidth)
                            lineTo(rayLen, -haloWidth * 0.15f)
                            lineTo(rayLen, haloWidth * 0.15f)
                            lineTo(0f, haloWidth)
                            close()
                        }
                        drawPath(
                            path = haloPath,
                            brush = Brush.horizontalGradient(
                                0f to tileColor.copy(alpha = 0.7f * rayAlpha),
                                0.6f to tileColor.copy(alpha = 0.25f * rayAlpha),
                                1f to Color.Transparent,
                                startX = 0f,
                                endX = rayLen,
                            ),
                        )
                        // Bright thin core ray (white → tile color → transparent)
                        val coreWidth = cellW * 0.08f
                        val corePath = Path().apply {
                            moveTo(0f, -coreWidth)
                            lineTo(rayLen * 0.95f, -coreWidth * 0.2f)
                            lineTo(rayLen * 0.95f, coreWidth * 0.2f)
                            lineTo(0f, coreWidth)
                            close()
                        }
                        drawPath(
                            path = corePath,
                            brush = Brush.horizontalGradient(
                                0f to Color.White.copy(alpha = rayAlpha),
                                0.4f to tileColor.copy(alpha = 0.9f * rayAlpha),
                                1f to Color.Transparent,
                                startX = 0f,
                                endX = rayLen,
                            ),
                        )
                    }
                }
            }

            // 4. Star sparkles drifting outward
            for (sp in ray.sparkles) {
                val sx = cx + sp.dirX * sparkleSpread
                val sy = cy + sp.dirY * sparkleSpread
                val r2 = sparkleSize * sp.sizeFactor
                if (r2 <= 0f) continue
                rotate(sp.spin + p * 90f, Offset(sx, sy)) {
                    drawStarSparkle(
                        center = Offset(sx, sy),
                        radius = r2,
                        color = Color.White,
                        alpha = rayAlpha,
                    )
                }
            }
        }
    }
}

private data class RaySet(
    val rayCount: Int,
    val baseAngle: Float,
    val sparkles: List<SparkleInit>,
)

private data class SparkleInit(
    val dirX: Float,
    val dirY: Float,
    val sizeFactor: Float,
    val spin: Float,
)

private fun easeOutCubic(t: Float): Float {
    val inv = 1f - t
    return 1f - inv * inv * inv
}
