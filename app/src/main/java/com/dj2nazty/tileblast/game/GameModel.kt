package com.dj2nazty.tileblast.game

import androidx.compose.ui.graphics.Color

object GameConstants {
    const val ROWS = 8
    const val COLS = 8
    const val LEVEL_XP = 500
    const val INT_EVERY = 3

    const val PREFS_NAME = "tileblast_prefs"
    const val KEY_BEST = "tb_best"
    const val KEY_DAILY = "tb_daily"
}

val PieceColors = listOf(
    Color(0xFF22D9A0),
    Color(0xFF4FA8F5),
    Color(0xFFF06060),
    Color(0xFF9B80F0),
    Color(0xFFF0A030),
    Color(0xFF50D0E0),
)

// All 23 shapes from the original HTML — 1 = filled, 0 = empty.
val PieceShapes: List<List<List<Int>>> = listOf(
    listOf(listOf(1)),
    listOf(listOf(1, 1)),
    listOf(listOf(1), listOf(1)),
    listOf(listOf(1, 1, 1)),
    listOf(listOf(1), listOf(1), listOf(1)),
    listOf(listOf(1, 0), listOf(1, 0), listOf(1, 1)),
    listOf(listOf(0, 1), listOf(0, 1), listOf(1, 1)),
    listOf(listOf(1, 1), listOf(1, 0), listOf(1, 0)),
    listOf(listOf(1, 1), listOf(0, 1), listOf(0, 1)),
    listOf(listOf(1, 1, 1), listOf(0, 1, 0)),
    listOf(listOf(0, 1), listOf(1, 1), listOf(0, 1)),
    listOf(listOf(1, 1), listOf(1, 1)),
    listOf(listOf(1, 1, 1, 1)),
    listOf(listOf(1), listOf(1), listOf(1), listOf(1)),
    listOf(listOf(0, 1, 1), listOf(1, 1, 0)),
    listOf(listOf(1, 1, 0), listOf(0, 1, 1)),
    listOf(listOf(1, 0), listOf(1, 0), listOf(1, 0), listOf(1, 1)),
    listOf(listOf(0, 1), listOf(0, 1), listOf(0, 1), listOf(1, 1)),
    listOf(listOf(1, 1, 1), listOf(1, 0, 0)),
    listOf(listOf(1, 1, 1), listOf(0, 0, 1)),
    listOf(listOf(1, 1, 1), listOf(1, 1, 1), listOf(1, 1, 1)),
    listOf(listOf(1, 1), listOf(1, 1), listOf(1, 1)),
    listOf(listOf(1, 1, 1), listOf(1, 1, 1)),
)

data class Piece(
    val shape: List<List<Int>>,
    val color: Color,
) {
    val rows: Int get() = shape.size
    val cols: Int get() = shape.maxOf { it.size }
}

fun randomPiece(): Piece = Piece(
    shape = PieceShapes.random().map { it.toList() },
    color = PieceColors.random(),
)

fun canPlace(grid: Array<Array<Color?>>, piece: Piece, row: Int, col: Int): Boolean {
    for (r in piece.shape.indices) {
        for (c in piece.shape[r].indices) {
            if (piece.shape[r][c] == 0) continue
            val gr = row + r
            val gc = col + c
            if (gr !in 0 until GameConstants.ROWS) return false
            if (gc !in 0 until GameConstants.COLS) return false
            if (grid[gr][gc] != null) return false
        }
    }
    return true
}
