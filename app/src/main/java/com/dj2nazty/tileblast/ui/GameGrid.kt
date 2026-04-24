package com.dj2nazty.tileblast.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.dj2nazty.tileblast.game.GameConstants
import com.dj2nazty.tileblast.game.GameState
import com.dj2nazty.tileblast.ui.theme.BorderC
import com.dj2nazty.tileblast.ui.theme.EmptyCell
import com.dj2nazty.tileblast.ui.theme.Red

@Composable
fun GameGrid(
    state: GameState,
    modifier: Modifier = Modifier,
    onPositioned: (rootOffsetX: Float, rootOffsetY: Float, sizePx: Int) -> Unit,
) {
    val density = LocalDensity.current
    val gapDp = 3.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(2.dp, BorderC, RoundedCornerShape(16.dp))
            .padding(3.dp)
            .onGloballyPositioned { coords ->
                val root = coords.positionInRoot()
                // Include internal 3dp padding offset so gridOrigin maps to cell grid area
                val pad = with(density) { 3.dp.toPx() }
                onPositioned(root.x + pad, root.y + pad, (coords.size.width - 2 * pad.toInt()))
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
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(justPlaced) {
        if (justPlaced) {
            scale.snapTo(0.5f)
            scale.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(flashing) {
        if (flashing) {
            alpha.snapTo(1f)
            scale.snapTo(1.1f)
            alpha.animateTo(0f, tween(350))
            scale.animateTo(0.8f, tween(350))
        } else {
            alpha.snapTo(1f)
            if (!justPlaced) scale.snapTo(1f)
        }
    }

    val cellColor = when {
        color != null -> color
        ghost && ghostColor != null -> {
            val a = if (ghostValid) 0.38f else 0.19f
            ghostColor.copy(alpha = a)
        }
        else -> EmptyCell
    }
    val outlineColor = if (ghost && ghostColor != null) {
        if (ghostValid) ghostColor else Red
    } else null

    Box(
        modifier = modifier
            .scale(scale.value)
            .alpha(alpha.value)
            .clip(RoundedCornerShape(4.dp))
            .background(cellColor)
            .then(
                if (outlineColor != null)
                    Modifier.border(2.dp, outlineColor, RoundedCornerShape(4.dp))
                else Modifier
            ),
    )
}
