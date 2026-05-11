package com.brian.bubbleshooterclassicpop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TOTAL_LEVELS = 30

@Composable
fun LevelSelectScreen(
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF66C9FF), Color(0xFFC8F7FF), Color(0xFFFFE1A0)),
                ),
            )
            .statusBarsPadding()
            .padding(18.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(12) { index ->
                val x = size.width * ((index * 37 % 100) / 100f)
                val y = size.height * ((index * 23 % 100) / 100f)
                val radius = 18f + (index % 4) * 6f
                drawCircle(
                    color = listOf(
                        Color(0xFFFF6B8A),
                        Color(0xFFFFD166),
                        Color(0xFF55A7FF),
                        Color(0xFF35C9A3),
                    )[index % 4].copy(alpha = 0.16f),
                    radius = radius,
                    center = Offset(x, y),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = radius * 0.22f,
                    center = Offset(x - radius * 0.28f, y - radius * 0.32f),
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButton(onClick = onBack) { Text("Back") }
                Text(
                    text = "SELECT LEVEL",
                    color = Color(0xFF17315B),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.size(76.dp, 1.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.72f))
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "Pick any stage. Higher levels start with more rows and tighter shots.",
                    color = Color(0xFF45618C),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                (1..TOTAL_LEVELS).chunked(4).forEach { rowLevels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowLevels.forEach { level ->
                            LevelTile(
                                level = level,
                                stars = starsForLevel(level),
                                modifier = Modifier.weight(1f),
                                onClick = { onLevelSelected(level) },
                            )
                        }
                        repeat(4 - rowLevels.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelTile(
    level: Int,
    stars: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(74.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = level.toString(),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
            )
            Text(
                text = buildString {
                    repeat(stars) { append('★') }
                    repeat(3 - stars) { append('☆') }
                },
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
    }
}

private fun starsForLevel(level: Int): Int = when {
    level <= 10 -> 3
    level <= 20 -> 2
    else -> 1
}
