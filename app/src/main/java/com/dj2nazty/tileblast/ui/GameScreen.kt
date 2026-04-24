package com.dj2nazty.tileblast.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import com.dj2nazty.tileblast.ads.AdManager
import com.dj2nazty.tileblast.ads.BannerAdView
import com.dj2nazty.tileblast.game.GameConstants
import com.dj2nazty.tileblast.game.GameEvent
import com.dj2nazty.tileblast.game.GameViewModel
import com.dj2nazty.tileblast.game.Piece
import com.dj2nazty.tileblast.ui.dialogs.*
import com.dj2nazty.tileblast.ui.theme.*
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class ScorePopVisual(val id: Long, val points: Int)

@Composable
fun GameScreen(vm: GameViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val density = LocalDensity.current
    val gapPx = with(density) { 3.dp.toPx() }
    val context = LocalContext.current
    val activity = remember(context) {
        var c: android.content.Context? = context
        while (c is android.content.ContextWrapper) {
            if (c is Activity) return@remember c
            c = c.baseContext
        }
        null
    }

    var gridOffsetRoot by remember { mutableStateOf(Offset.Zero) }
    var gridSizePx by remember { mutableIntStateOf(0) }
    val slotRects = remember { mutableStateListOf(Rect.Zero, Rect.Zero, Rect.Zero) }

    var fingerPos by remember { mutableStateOf<Offset?>(null) }

    var comboText by remember { mutableStateOf<String?>(null) }
    var toastText by remember { mutableStateOf<String?>(null) }
    val scorePops = remember { mutableStateListOf<ScorePopVisual>() }
    val coroutineScope = rememberCoroutineScope()

    // Collect events → ephemeral visuals + ad triggers
    LaunchedEffect(activity) {
        vm.events.collect { event ->
            when (event) {
                is GameEvent.Combo -> {
                    comboText = event.message
                    coroutineScope.launch {
                        delay(900)
                        comboText = null
                    }
                }
                is GameEvent.Toast -> {
                    toastText = event.message
                    coroutineScope.launch {
                        delay(2400)
                        toastText = null
                    }
                }
                is GameEvent.ScorePop -> {
                    val pop = ScorePopVisual(System.nanoTime(), event.points)
                    scorePops += pop
                    coroutineScope.launch {
                        delay(800)
                        scorePops.remove(pop)
                    }
                }
                GameEvent.RequestInterstitial -> {
                    activity?.let { AdManager.showInterstitial(it) {} }
                }
            }
        }
    }

    // Compute ghost whenever drag position changes
    LaunchedEffect(fingerPos, state.dragIdx, gridSizePx) {
        val fp = fingerPos
        val idx = state.dragIdx
        if (fp == null || idx < 0 || gridSizePx == 0) {
            return@LaunchedEffect
        }
        val piece = state.pieces[idx]
        val csz = gridSizePx.toFloat() / GameConstants.COLS
        val pxW = piece.cols * csz + (piece.cols - 1) * gapPx
        val pxH = piece.rows * csz + (piece.rows - 1) * gapPx
        val lift = csz * 1.1f  // piece floats above finger on touch
        val visLeft = fp.x - pxW / 2f
        val visTop = fp.y - pxH - lift

        val fracC = (visLeft - gridOffsetRoot.x) / csz
        val fracR = (visTop - gridOffsetRoot.y) / csz

        val overGrid = (visLeft + pxW) > gridOffsetRoot.x - csz &&
                visLeft < gridOffsetRoot.x + gridSizePx + csz &&
                (visTop + pxH) > gridOffsetRoot.y - csz &&
                visTop < gridOffsetRoot.y + gridSizePx + csz

        if (overGrid) {
            val newR = fracR.roundToInt().coerceIn(0, GameConstants.ROWS - piece.rows)
            val newC = fracC.roundToInt().coerceIn(0, GameConstants.COLS - piece.cols)
            vm.updateGhost(newR, newC)
        } else {
            vm.updateGhost(-1, -1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .pointerInput(state.used) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = true,
                        pass = PointerEventPass.Main,
                    )
                    // Hit-test: is the initial down inside any tray slot?
                    val slotIdx = slotRects.indexOfFirst {
                        it.contains(down.position)
                    }
                    if (slotIdx < 0 || slotIdx >= state.used.size || state.used[slotIdx]) {
                        // Not a tray drag — let children (buttons, etc.) handle it.
                        return@awaitEachGesture
                    }
                    down.consume()
                    vm.startDrag(slotIdx)
                    fingerPos = down.position

                    // Track movement until up
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: break
                        if (change.changedToUp()) {
                            change.consume()
                            val hadGhost = vm.hadValidGhost()
                            vm.endDrag(hadGhost)
                            fingerPos = null
                            break
                        }
                        fingerPos = change.position
                        change.consume()
                    }
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TopBar(
                score = state.score,
                best = state.best,
                onSettings = vm::openSettings,
            )
            LevelBar(
                level = state.level,
                xpInLevel = state.xpInLevel,
                xpPerLevel = GameConstants.LEVEL_XP,
                xpTarget = state.xpTarget,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                GameGrid(
                    state = state,
                    modifier = Modifier.fillMaxWidth(),
                    onPositioned = { x, y, s ->
                        gridOffsetRoot = Offset(x, y)
                        gridSizePx = s
                    },
                )
            }
            PieceTray(
                pieces = state.pieces,
                used = state.used,
                dragIdx = state.dragIdx,
                hoverIdx = state.hoverIdx,
                onSlotPositioned = { i, rect ->
                    if (i in slotRects.indices) slotRects[i] = rect
                },
            )
            BannerAdView()
        }

        // Floating dragged piece
        val dragIdx = state.dragIdx
        val fp = fingerPos
        if (dragIdx >= 0 && fp != null && gridSizePx > 0) {
            val piece = state.pieces[dragIdx]
            val csz = gridSizePx.toFloat() / GameConstants.COLS
            val pxW = piece.cols * csz + (piece.cols - 1) * gapPx
            val pxH = piece.rows * csz + (piece.rows - 1) * gapPx
            val lift = csz * 1.1f
            val x = fp.x - pxW / 2f
            val y = fp.y - pxH - lift
            FloatingDragPiece(
                piece = piece,
                cellSizePx = csz,
                gapPx = gapPx,
                offsetX = x,
                offsetY = y,
            )
        }

        // Combo banner
        AnimatedVisibility(
            visible = comboText != null,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(250)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(
                text = comboText.orEmpty(),
                fontSize = 34.sp,
                color = Accent,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        // Score pops
        scorePops.forEach { pop ->
            ScorePop(points = pop.points, key = pop.id, modifier = Modifier.align(Alignment.TopCenter))
        }

        // Toast
        AnimatedVisibility(
            visible = toastText != null,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surf2)
                    .border(1.5.dp, Border2, RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text(text = toastText.orEmpty(), color = TextC, fontSize = 13.sp)
            }
        }
    }

    // Dialogs
    if (state.showGameOver) {
        GameOverDialog(
            score = state.score,
            isNewBest = state.score >= state.best && state.score > 0,
            lives = state.lives,
            onContinue = {
                if (activity != null) {
                    AdManager.showRewarded(
                        activity = activity,
                        onReward = { vm.grantContinue() },
                    )
                } else {
                    vm.grantContinue()
                }
            },
            onNewGame = {
                vm.dismissGameOver()
                vm.newGame()
            },
        )
    }
    if (state.showDaily) {
        DailyRewardDialog(
            onClaim = {
                if (activity != null) {
                    AdManager.showRewarded(
                        activity = activity,
                        onReward = { vm.grantDaily() },
                    )
                } else {
                    vm.grantDaily()
                }
            },
            onSkip = vm::skipDaily,
        )
    }
    if (state.showSettings) {
        SettingsDialog(
            best = state.best,
            onClose = vm::closeSettings,
            onReset = vm::resetBest,
        )
    }
}

@Composable
private fun FloatingDragPiece(
    piece: Piece,
    cellSizePx: Float,
    gapPx: Float,
    offsetX: Float,
    offsetY: Float,
) {
    val density = LocalDensity.current
    val cellDp = with(density) { cellSizePx.toDp() }
    val gapDp = with(density) { gapPx.toDp() }

    Column(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer {
                shadowElevation = 24f
            },
        verticalArrangement = Arrangement.spacedBy(gapDp),
    ) {
        for (r in piece.shape.indices) {
            Row(horizontalArrangement = Arrangement.spacedBy(gapDp)) {
                for (c in 0 until piece.cols) {
                    val on = c < piece.shape[r].size && piece.shape[r][c] == 1
                    Box(
                        modifier = Modifier
                            .size(cellDp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (on) piece.color else Color.Transparent),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScorePop(points: Int, key: Long, modifier: Modifier = Modifier) {
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(key) {
        launch { offsetY.animateTo(-40f, tween(750)) }
        alpha.animateTo(0f, tween(750))
    }
    Text(
        text = "+%,d".format(points),
        fontSize = 20.sp,
        color = Accent,
        fontWeight = FontWeight.ExtraBold,
        modifier = modifier
            .padding(top = 80.dp)
            .graphicsLayer {
                translationY = offsetY.value
                this.alpha = alpha.value
            },
    )
}

// Extension on VM so UI doesn't touch internal state math.
private fun GameViewModel.hadValidGhost(): Boolean {
    val s = this.state.value
    return s.ghostRow >= 0 && s.ghostCol >= 0
}
