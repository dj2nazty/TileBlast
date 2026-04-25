package com.tileblast.game.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tileblast.game.ui.theme.*

@Composable
fun TopBar(
    score: Int,
    best: Int,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        // Score pill
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Surface)
                .border(1.5.dp, Border2, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Dot()
            Spacer(Modifier.width(8.dp))
            Text(
                text = "%,d".format(score),
                fontSize = 24.sp,
                color = TextC,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.width(8.dp))
            Dot()
        }
        // Best pill
        Column(
            modifier = Modifier
                .widthIn(min = 82.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Surface)
                .border(1.5.dp, BorderC, RoundedCornerShape(14.dp))
                .padding(horizontal = 11.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "ALL-TIME",
                fontSize = 8.sp,
                color = Muted,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.08.sp,
            )
            Text(
                text = "%,d".format(best),
                fontSize = 18.sp,
                color = TextC,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        // Settings button
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .border(1.5.dp, BorderC, RoundedCornerShape(12.dp))
                .clickable { onSettings() },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "\u2699", fontSize = 22.sp, color = TextC)
        }
    }
}

@Composable
private fun Dot() {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(Accent.copy(alpha = 0.55f)),
    )
}
