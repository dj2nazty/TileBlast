package com.dj2nazty.tileblast.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dj2nazty.tileblast.ui.theme.*

@Composable
private fun Modal(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(22.dp))
            .background(Surface)
            .border(1.5.dp, Border2, RoundedCornerShape(22.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
fun ModalOverlay(
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(
            dismissOnBackPress = onDismiss != null,
            dismissOnClickOutside = onDismiss != null,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f)),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun PrimaryButton(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) Accent else Color(0xFF333333))
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (enabled) Color.White else Color(0xFF666666),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun GhostButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Transparent)
            .border(1.5.dp, BorderC, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Muted,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun AdBadge(label: String = "Rewarded Video Ad") {
    Text(
        text = label,
        color = Accent,
        fontSize = 10.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Accent.copy(alpha = 0.12f))
            .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
fun GameOverDialog(
    score: Int,
    onNewGame: () -> Unit,
) {
    ModalOverlay {
        Modal {
            Text("Game Over", fontSize = 28.sp, color = TextC, fontWeight = FontWeight.ExtraBold)
            Text(
                text = "%,d".format(score),
                fontSize = 52.sp,
                color = Accent,
                fontWeight = FontWeight.ExtraBold,
            )
            PrimaryButton(label = "Start New Game", onClick = onNewGame)
        }
    }
}

@Composable
fun DailyRewardDialog(onClaim: () -> Unit, onSkip: () -> Unit) {
    ModalOverlay(onDismiss = onSkip) {
        Modal {
            Text("\uD83C\uDF81", fontSize = 46.sp)
            Text("Daily Reward", fontSize = 28.sp, color = TextC, fontWeight = FontWeight.ExtraBold)
            Text(
                "Watch a short ad and earn +500 bonus points!",
                color = Muted,
                fontSize = 13.sp,
            )
            AdBadge()
            PrimaryButton("\u25B6  Watch Ad — Claim", onClick = onClaim)
            GhostButton("Not now", onClick = onSkip)
        }
    }
}

@Composable
fun SettingsDialog(best: Int, onClose: () -> Unit, onReset: () -> Unit) {
    ModalOverlay(onDismiss = onClose) {
        Modal {
            Text("Settings", fontSize = 28.sp, color = TextC, fontWeight = FontWeight.ExtraBold)
            SettingRow(label = "Best Score", value = "%,d".format(best))
            SettingRow(label = "Version", value = "1.0")
            PrimaryButton("Done", onClick = onClose)
            GhostButton("Reset Best Score", onClick = onReset)
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = TextC, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        Text(value, color = Muted, fontSize = 13.sp)
    }
}

@Composable
fun InterstitialDialog(timer: Int, onDismiss: () -> Unit) {
    ModalOverlay(onDismiss = if (timer <= 0) onDismiss else null) {
        Modal {
            AdBadge(label = "Interstitial Ad")
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(2.dp, Accent.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    timer.toString(),
                    fontSize = 24.sp,
                    color = Accent,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Text(
                "Level Up!",
                color = Accent,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "[ Ad renders here ]",
                    color = Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.alpha(0.9f),
                )
            }
        }
    }
}
