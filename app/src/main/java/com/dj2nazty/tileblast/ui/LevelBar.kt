package com.dj2nazty.tileblast.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dj2nazty.tileblast.ui.theme.*

@Composable
fun LevelBar(
    level: Int,
    xpInLevel: Int,
    xpPerLevel: Int,
    xpTarget: Int,
    modifier: Modifier = Modifier,
) {
    val targetFrac = (xpInLevel.toFloat() / xpPerLevel).coerceIn(0f, 1f)
    val frac by animateFloatAsState(
        targetValue = targetFrac,
        animationSpec = tween(durationMillis = 400),
        label = "xp",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier.widthIn(min = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Level",
                fontSize = 11.sp,
                color = Muted,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = level.toString(),
                fontSize = 18.sp,
                color = TextC,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Surface)
                .border(1.dp, BorderC, RoundedCornerShape(6.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(frac)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Accent, AccentSecondary))
                    ),
            )
        }
        Text(
            text = "%,d".format(xpTarget),
            fontSize = 10.sp,
            color = Muted,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.widthIn(min = 36.dp),
        )
    }
}
