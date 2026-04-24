package com.dj2nazty.tileblast.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.cos
import kotlin.math.sin

/**
 * "NEW BEST SCORE!" celebration overlay. Shown in place of the normal game-over
 * dialog when the player ends a run with a new best. Features an animated
 * rotating golden star, sweeping light rays behind it, falling confetti,
 * bounce-scaling text, and a big green Play Again button.
 */
@Composable
fun NewBestCelebrationDialog(
    score: Int,
    onPlayAgain: () -> Unit,
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0f to Color(0xFF2A1F4A),
                        0.6f to Color(0xFF14142A),
                        1f to Color(0xFF0A0A14),
                    ),
                ),
        ) {
            // Confetti covers the full screen (behind card content)
            ConfettiField(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "GAME OVER",
                    fontSize = 22.sp,
                    color = Color(0xFFE8E8F8).copy(alpha = 0.75f),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                )

                AnimatedBigText(
                    lines = listOf("NEW", "BEST SCORE!"),
                )

                StarWithRays(modifier = Modifier.size(220.dp))

                ScoreCard(score = score)

                Spacer(modifier = Modifier.height(4.dp))

                PlayAgainButton(onClick = onPlayAgain)
            }
        }
    }
}

@Composable
private fun AnimatedBigText(lines: List<String>) {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1.15f, tween(260, easing = FastOutSlowInEasing))
        scale.animateTo(1f, tween(180, easing = FastOutSlowInEasing))
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
    ) {
        lines.forEach { line -> GoldOutlinedText(line) }
    }
}

@Composable
private fun GoldOutlinedText(text: String) {
    Box {
        // Thick dark outline — 8 offsets around
        val offsets = listOf(
            -3f to -3f, 3f to -3f, -3f to 3f, 3f to 3f,
            -3f to 0f, 3f to 0f, 0f to -3f, 0f to 3f,
        )
        offsets.forEach { (dx, dy) ->
            Text(
                text = text,
                fontSize = 54.sp,
                color = Color(0xFF3A1A66),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.graphicsLayer { translationX = dx; translationY = dy },
            )
        }
        // Gold gradient fill — simulate with two text layers
        Text(
            text = text,
            fontSize = 54.sp,
            color = Color(0xFFFFE45C),
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun StarWithRays(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "starRays")
    val rayRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rayRot",
    )
    val pulse by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val starPop = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        starPop.animateTo(1.2f, tween(260, easing = FastOutSlowInEasing))
        starPop.animateTo(1f, tween(180))
    }

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val starR = size.minDimension * 0.28f * starPop.value * pulse

        // 1. Soft warm halo behind everything
        drawCircle(
            brush = Brush.radialGradient(
                0f to Color(0xFFFFE45C).copy(alpha = 0.55f),
                0.5f to Color(0xFFFF9E2C).copy(alpha = 0.25f),
                1f to Color.Transparent,
                center = Offset(cx, cy),
                radius = size.minDimension * 0.55f,
            ),
            radius = size.minDimension * 0.55f,
            center = Offset(cx, cy),
        )

        // 2. Rotating long rays
        translate(cx, cy) {
            for (i in 0 until 12) {
                val angle = rayRotation + i * (360f / 12f)
                rotate(angle, Offset.Zero) {
                    val rayLen = size.minDimension * 0.48f
                    val rayWidth = size.minDimension * 0.06f
                    val path = Path().apply {
                        moveTo(0f, -rayWidth)
                        lineTo(rayLen, -rayWidth * 0.15f)
                        lineTo(rayLen, rayWidth * 0.15f)
                        lineTo(0f, rayWidth)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            0f to Color(0xFFFFF2B0).copy(alpha = 0.7f),
                            0.6f to Color(0xFFFF9E2C).copy(alpha = 0.3f),
                            1f to Color.Transparent,
                            startX = 0f,
                            endX = rayLen,
                        ),
                    )
                }
            }
        }

        // 3. Secondary short rays between (counter-rotating)
        translate(cx, cy) {
            for (i in 0 until 12) {
                val angle = -rayRotation * 0.7f + (i + 0.5f) * (360f / 12f)
                rotate(angle, Offset.Zero) {
                    val rayLen = size.minDimension * 0.32f
                    val rayWidth = size.minDimension * 0.035f
                    val path = Path().apply {
                        moveTo(0f, -rayWidth)
                        lineTo(rayLen, -rayWidth * 0.2f)
                        lineTo(rayLen, rayWidth * 0.2f)
                        lineTo(0f, rayWidth)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            0f to Color.White.copy(alpha = 0.6f),
                            1f to Color.Transparent,
                            startX = 0f,
                            endX = rayLen,
                        ),
                    )
                }
            }
        }

        // 4. The 5-pointed star (gold with highlight)
        val starPath = fivePointedStarPath(cx, cy, starR, starR * 0.45f)
        // Dark outline
        drawPath(
            path = fivePointedStarPath(cx, cy, starR * 1.1f, starR * 0.5f),
            color = Color(0xFF5A2F0A),
        )
        // Gold body with gradient
        drawPath(
            path = starPath,
            brush = Brush.verticalGradient(
                0f to Color(0xFFFFF2B0),
                0.5f to Color(0xFFFFD23A),
                1f to Color(0xFFE6A100),
                startY = cy - starR,
                endY = cy + starR,
            ),
        )
        // Inner shine on upper half of the star
        drawPath(
            path = starPath,
            brush = Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.55f),
                0.35f to Color.Transparent,
                startY = cy - starR,
                endY = cy + starR,
            ),
        )
    }
}

private fun fivePointedStarPath(cx: Float, cy: Float, outerR: Float, innerR: Float): Path {
    val path = Path()
    // Start at the top point
    val points = 5
    val step = Math.PI / points
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val angle = -Math.PI / 2 + i * step
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

@Composable
private fun ScoreCard(score: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF26264A),
                    1f to Color(0xFF1A1A38),
                ),
            )
            .border(2.dp, Color(0xFFFFE45C).copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(horizontal = 32.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "%,d".format(score),
            fontSize = 46.sp,
            color = Color(0xFFFFE45C),
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "NEW BEST SCORE!",
            fontSize = 14.sp,
            color = Color(0xFFFFF2B0),
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
        )
    }
}

@Composable
private fun PlayAgainButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF6CE03A),
                    0.55f to Color(0xFF48B820),
                    1f to Color(0xFF2E8A10),
                ),
            )
            .border(2.dp, Color(0xFF9FF66F), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "START NEW GAME",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
        )
    }
}

/** Falling confetti — 40 particles in assorted colors drifting and rotating. */
@Composable
private fun ConfettiField(modifier: Modifier = Modifier) {
    val particles = remember {
        List(40) {
            ConfettiParticle(
                xFrac = Math.random().toFloat(),
                startYFrac = -(Math.random().toFloat() * 0.4f + 0.05f),
                endYFrac = 1.05f + Math.random().toFloat() * 0.2f,
                swayAmpFrac = (Math.random().toFloat() * 0.06f),
                swayPhase = (Math.random() * Math.PI * 2).toFloat(),
                hue = CONFETTI_COLORS.random(),
                rotationBase = (Math.random() * 360).toFloat(),
                rotationSpeed = ((Math.random() - 0.5) * 540).toFloat(),
                size = 8f + Math.random().toFloat() * 10f,
                duration = 3200 + (Math.random() * 1800).toInt(),
                startDelay = (Math.random() * 600).toInt(),
            )
        }
    }

    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        t.animateTo(
            1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        )
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val cycleT = ((t.value * 4200 + p.startDelay) % p.duration) / p.duration.toFloat()
            val y = p.startYFrac + (p.endYFrac - p.startYFrac) * cycleT
            val sway = sin(p.swayPhase + cycleT * 6f) * p.swayAmpFrac
            val xPx = (p.xFrac + sway) * w
            val yPx = y * h
            val rot = p.rotationBase + p.rotationSpeed * cycleT

            rotate(rot, Offset(xPx, yPx)) {
                drawRect(
                    color = p.hue,
                    topLeft = Offset(xPx - p.size / 2f, yPx - p.size * 0.35f),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.4f),
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val xFrac: Float,
    val startYFrac: Float,
    val endYFrac: Float,
    val swayAmpFrac: Float,
    val swayPhase: Float,
    val hue: Color,
    val rotationBase: Float,
    val rotationSpeed: Float,
    val size: Float,
    val duration: Int,
    val startDelay: Int,
)

private val CONFETTI_COLORS = listOf(
    Color(0xFFFF4FA8), // hot pink
    Color(0xFF7FE034), // lime
    Color(0xFF34D4F0), // cyan
    Color(0xFFFF9E2C), // orange
    Color(0xFF3A7FFF), // blue
    Color(0xFFB54FFF), // purple
    Color(0xFFFFD12E), // yellow
    Color(0xFFFFE45C), // gold
)
