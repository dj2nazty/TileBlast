package com.dj2nazty.tileblast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dj2nazty.tileblast.game.Piece
import com.dj2nazty.tileblast.ui.theme.*

@Composable
fun PieceTray(
    pieces: List<Piece>,
    used: List<Boolean>,
    dragIdx: Int,
    hoverIdx: Int,
    onSlotPositioned: (index: Int, rectInRoot: Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // "Pieces" tab label
        Text(
            text = "PIECES",
            fontSize = 12.sp,
            color = Muted,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = Modifier
                .padding(start = 6.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(Surface)
                .border(
                    1.5.dp,
                    BorderC,
                    RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                )
                .padding(horizontal = 14.dp, vertical = 5.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp, topEnd = 14.dp,
                        bottomStart = 14.dp, bottomEnd = 14.dp,
                    )
                )
                .background(Surface)
                .border(
                    1.5.dp,
                    BorderC,
                    RoundedCornerShape(
                        topStart = 0.dp, topEnd = 14.dp,
                        bottomStart = 14.dp, bottomEnd = 14.dp,
                    ),
                )
                .heightIn(min = 90.dp, max = 120.dp)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in pieces.indices) {
                SlotCell(
                    piece = pieces[i],
                    isUsed = used[i],
                    isSelected = hoverIdx == i && dragIdx < 0,
                    isLifting = dragIdx == i,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 3.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            onSlotPositioned(
                                i,
                                Rect(
                                    pos.x, pos.y,
                                    pos.x + coords.size.width,
                                    pos.y + coords.size.height,
                                ),
                            )
                        },
                )
            }
        }
    }
}

@Composable
private fun SlotCell(
    piece: Piece,
    isUsed: Boolean,
    isSelected: Boolean,
    isLifting: Boolean,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isSelected) Accent else Color.Transparent
    val bgColor = if (isSelected) Accent.copy(alpha = 0.12f) else Color.Transparent
    val alpha = when {
        isLifting -> 0.22f
        isUsed -> 0.15f
        else -> 1f
    }
    val scale = if (isLifting) 0.88f else 1f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp))
            .alpha(alpha)
            .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        PiecePreview(piece = piece, maxCellDp = 20)
    }
}

@Composable
private fun PiecePreview(piece: Piece, maxCellDp: Int) {
    val cols = piece.cols
    val rows = piece.rows
    val maxDim = maxOf(cols, rows)
    val cellPx = (82 / maxDim).coerceAtMost(maxCellDp)
    Column {
        for (r in piece.shape.indices) {
            Row {
                for (c in 0 until cols) {
                    val on = c < piece.shape[r].size && piece.shape[r][c] == 1
                    if (on) {
                        GlossyTile(
                            color = piece.color,
                            modifier = Modifier
                                .size(cellPx.dp)
                                .padding(1.dp),
                        )
                    } else {
                        Box(modifier = Modifier.size(cellPx.dp))
                    }
                }
            }
        }
    }
}
