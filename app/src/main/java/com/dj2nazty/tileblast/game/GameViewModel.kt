package com.dj2nazty.tileblast.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class GameEvent {
    data class Combo(val message: String) : GameEvent()
    data class Toast(val message: String) : GameEvent()
    data class ScorePop(val points: Int) : GameEvent()
    data object RequestInterstitial : GameEvent()
}

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(
        GameConstants.PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            delay(1500)
            if (shouldShowDaily()) {
                _state.update { it.copy(showDaily = true) }
            }
        }
    }

    private fun initialState(): GameState {
        val best = prefs.getInt(GameConstants.KEY_BEST, 0)
        return GameState(
            grid = emptyGrid().toImmutable(),
            pieces = listOf(randomPiece(), randomPiece(), randomPiece()),
            used = listOf(false, false, false),
            best = best,
        )
    }

    fun newGame() {
        val played = _state.value.score > 0
        _state.update {
            GameState(
                grid = emptyGrid().toImmutable(),
                pieces = listOf(randomPiece(), randomPiece(), randomPiece()),
                used = listOf(false, false, false),
                best = it.best,
            )
        }
        if (played) {
            viewModelScope.launch { _events.emit(GameEvent.RequestInterstitial) }
        }
    }

    fun startDrag(idx: Int) {
        val s = _state.value
        if (idx !in s.pieces.indices) return
        if (s.used[idx] || s.dragIdx >= 0) return
        _state.update { it.copy(dragIdx = idx, hoverIdx = -1, ghostRow = -1, ghostCol = -1) }
    }

    fun updateGhost(row: Int, col: Int) {
        _state.update { it.copy(ghostRow = row, ghostCol = col) }
    }

    fun selectHover(idx: Int) {
        val s = _state.value
        if (s.used.getOrNull(idx) == true || s.dragIdx >= 0) return
        val next = if (s.hoverIdx == idx) -1 else idx
        _state.update { it.copy(hoverIdx = next, ghostRow = -1, ghostCol = -1) }
    }

    fun endDrag(place: Boolean) {
        val s = _state.value
        val idx = s.dragIdx
        if (idx < 0) return
        if (place && s.ghostRow >= 0 && s.ghostCol >= 0) {
            val grid = s.grid.toMutableGrid()
            if (canPlace(grid, s.pieces[idx], s.ghostRow, s.ghostCol)) {
                placePiece(idx, s.ghostRow, s.ghostCol)
                _state.update { it.copy(dragIdx = -1, ghostRow = -1, ghostCol = -1) }
                return
            }
        }
        _state.update { it.copy(dragIdx = -1, ghostRow = -1, ghostCol = -1) }
    }

    fun cancelDrag() {
        _state.update { it.copy(dragIdx = -1, ghostRow = -1, ghostCol = -1) }
    }

    fun tryPlaceHover() {
        val s = _state.value
        val idx = s.hoverIdx
        if (idx < 0 || s.used[idx] || s.ghostRow < 0 || s.ghostCol < 0) return
        val grid = s.grid.toMutableGrid()
        if (!canPlace(grid, s.pieces[idx], s.ghostRow, s.ghostCol)) return
        placePiece(idx, s.ghostRow, s.ghostCol)
        _state.update { it.copy(hoverIdx = -1, ghostRow = -1, ghostCol = -1) }
    }

    private fun placePiece(idx: Int, row: Int, col: Int) {
        val s = _state.value
        val piece = s.pieces[idx]
        val grid = s.grid.toMutableGrid()
        val placedCells = mutableSetOf<Pair<Int, Int>>()
        for (dr in piece.shape.indices) {
            for (dc in piece.shape[dr].indices) {
                if (piece.shape[dr][dc] == 1) {
                    grid[row + dr][col + dc] = piece.color
                    placedCells.add((row + dr) to (col + dc))
                }
            }
        }

        // Clear lines
        val toFlash = mutableSetOf<Pair<Int, Int>>()
        var clearedRows = 0
        var clearedCols = 0
        for (r in 0 until GameConstants.ROWS) {
            if ((0 until GameConstants.COLS).all { grid[r][it] != null }) {
                clearedRows++
                for (c in 0 until GameConstants.COLS) toFlash.add(r to c)
            }
        }
        for (c in 0 until GameConstants.COLS) {
            if ((0 until GameConstants.ROWS).all { grid[it][c] != null }) {
                clearedCols++
                for (r in 0 until GameConstants.ROWS) toFlash.add(r to c)
            }
        }
        toFlash.forEach { (r, c) -> grid[r][c] = null }

        val cleared = clearedRows + clearedCols
        val cells = toFlash.size
        val pts = if (cleared > 0) {
            cells * 10 + if (cleared > 1) (cleared - 1) * 50 else 0
        } else 0

        val newUsed = s.used.toMutableList()
        newUsed[idx] = true

        // Regenerate all 3 pieces if they're all used
        val (newPieces, finalUsed) = if (newUsed.all { it }) {
            listOf(randomPiece(), randomPiece(), randomPiece()) to listOf(false, false, false)
        } else {
            s.pieces to newUsed.toList()
        }

        val newScore = s.score + pts
        var newBest = s.best
        if (newScore > newBest) {
            newBest = newScore
            prefs.edit().putInt(GameConstants.KEY_BEST, newBest).apply()
        }
        val newLevel = 1 + newScore / GameConstants.LEVEL_XP

        _state.update {
            it.copy(
                grid = grid.toImmutable(),
                pieces = newPieces,
                used = finalUsed,
                score = newScore,
                best = newBest,
                level = newLevel,
                justPlaced = placedCells,
                flashing = toFlash,
            )
        }

        // Fire transient events
        viewModelScope.launch {
            if (pts > 0) _events.emit(GameEvent.ScorePop(pts))
            if (cleared >= 2) _events.emit(GameEvent.Combo("COMBO ×$cleared!"))
        }

        // Clear just-placed / flashing after animation window
        viewModelScope.launch {
            delay(360)
            _state.update { it.copy(justPlaced = emptySet(), flashing = emptySet()) }
        }

        // Check for game over
        val stillHasMove = hasAnyMove(grid, finalUsed, newPieces)
        if (!stillHasMove) {
            viewModelScope.launch {
                delay(300)
                _state.update { it.copy(showGameOver = true) }
            }
        }
    }

    private fun hasAnyMove(
        grid: Array<Array<androidx.compose.ui.graphics.Color?>>,
        used: List<Boolean>,
        pieces: List<Piece>,
    ): Boolean {
        for (i in pieces.indices) {
            if (used[i]) continue
            for (r in 0 until GameConstants.ROWS) {
                for (c in 0 until GameConstants.COLS) {
                    if (canPlace(grid, pieces[i], r, c)) return true
                }
            }
        }
        return false
    }

    // ── Game Over actions ────────────────────────────
    /**
     * Call this after a rewarded ad is fully viewed (or skipped if no ad loaded).
     * Clears bottom 2 rows and deducts one life.
     */
    fun grantContinue() {
        val s = _state.value
        if (s.lives <= 0) return
        val grid = s.grid.toMutableGrid()
        for (r in GameConstants.ROWS - 2 until GameConstants.ROWS)
            for (c in 0 until GameConstants.COLS)
                grid[r][c] = null
        _state.update {
            it.copy(
                grid = grid.toImmutable(),
                lives = (it.lives - 1).coerceAtLeast(0),
                showGameOver = false,
            )
        }
        viewModelScope.launch { _events.emit(GameEvent.Toast("Continue granted!")) }
    }

    fun dismissGameOver() { _state.update { it.copy(showGameOver = false) } }

    // ── Daily reward ─────────────────────────────────
    private fun shouldShowDaily(): Boolean {
        val last = prefs.getLong(GameConstants.KEY_DAILY, 0L)
        if (last == 0L) return true
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return fmt.format(Date(last)) != fmt.format(Date())
    }

    /**
     * Call this after the daily rewarded ad is fully viewed (or skipped if no ad loaded).
     * Adds 500 points, persists the claim timestamp.
     */
    fun grantDaily() {
        val s = _state.value
        val newScore = s.score + 500
        var newBest = s.best
        if (newScore > newBest) {
            newBest = newScore
            prefs.edit().putInt(GameConstants.KEY_BEST, newBest).apply()
        }
        prefs.edit().putLong(GameConstants.KEY_DAILY, System.currentTimeMillis()).apply()
        val newLevel = 1 + newScore / GameConstants.LEVEL_XP
        _state.update {
            it.copy(
                score = newScore,
                best = newBest,
                level = newLevel,
                showDaily = false,
            )
        }
        viewModelScope.launch {
            _events.emit(GameEvent.ScorePop(500))
            _events.emit(GameEvent.Toast("+500 points! Daily reward claimed."))
        }
    }

    fun skipDaily() { _state.update { it.copy(showDaily = false) } }

    // ── Settings ─────────────────────────────────────
    fun openSettings() { _state.update { it.copy(showSettings = true) } }
    fun closeSettings() { _state.update { it.copy(showSettings = false) } }

    fun resetBest() {
        prefs.edit().remove(GameConstants.KEY_BEST).apply()
        _state.update { it.copy(best = 0, showSettings = false) }
        viewModelScope.launch { _events.emit(GameEvent.Toast("Best score reset.")) }
    }
}
