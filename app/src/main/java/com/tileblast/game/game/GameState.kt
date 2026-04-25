package com.tileblast.game.game

import androidx.compose.ui.graphics.Color

data class GameState(
    val grid: List<List<Color?>>,
    val pieces: List<Piece>,
    val used: List<Boolean>,
    val score: Int = 0,
    val best: Int = 0,
    val level: Int = 1,
    val lives: Int = 3,
    val ghostRow: Int = -1,
    val ghostCol: Int = -1,
    val dragIdx: Int = -1,
    val hoverIdx: Int = -1,
    val justPlaced: Set<Pair<Int, Int>> = emptySet(),
    val flashing: Set<Pair<Int, Int>> = emptySet(),
    val showGameOver: Boolean = false,
    val showDaily: Boolean = false,
    val showSettings: Boolean = false,
) {
    val activeIdx: Int get() = if (dragIdx >= 0) dragIdx else hoverIdx

    val xpInLevel: Int get() = score - (level - 1) * GameConstants.LEVEL_XP
    val xpTarget: Int get() = level * GameConstants.LEVEL_XP

    fun isGhostCell(r: Int, c: Int): Boolean {
        if (activeIdx < 0 || ghostRow < 0 || ghostCol < 0) return false
        val p = pieces[activeIdx]
        for (dr in p.shape.indices)
            for (dc in p.shape[dr].indices)
                if (p.shape[dr][dc] == 1 &&
                    r == ghostRow + dr && c == ghostCol + dc
                ) return true
        return false
    }

    val ghostValid: Boolean
        get() = activeIdx >= 0 && ghostRow >= 0 && ghostCol >= 0 &&
                canPlace(grid.toMutableGrid(), pieces[activeIdx], ghostRow, ghostCol)
}

fun List<List<Color?>>.toMutableGrid(): Array<Array<Color?>> =
    Array(GameConstants.ROWS) { r -> Array(GameConstants.COLS) { c -> this[r][c] } }

fun emptyGrid(): Array<Array<Color?>> =
    Array(GameConstants.ROWS) { arrayOfNulls(GameConstants.COLS) }

fun Array<Array<Color?>>.toImmutable(): List<List<Color?>> = map { it.toList() }
