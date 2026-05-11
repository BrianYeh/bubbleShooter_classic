package com.brian.bubbleshooterclassicpop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
                    listOf(Color(0xFF54C6FF), Color(0xFFB9F3FF), Color(0xFFFFD979)),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(14.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLevelMapBackdrop()
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LevelMenuHud(onBack = onBack)
            LevelSelectCard(onLevelSelected = onLevelSelected, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LevelMenuHud(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xCC12346A))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFFF7B9B), Color(0xFFE33B63))))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Text("‹", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "BUBBLE JOURNEY",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "30 stages unlocked",
                color = Color(0xFFBDEEFF),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("★ 90", color = Color(0xFFFFE76A), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun LevelSelectCard(
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(34.dp), clip = false)
            .clip(RoundedCornerShape(34.dp))
            .background(Color(0xFFF6FBFF))
            .border(3.dp, Color.White.copy(alpha = 0.92f), RoundedCornerShape(34.dp)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .background(Brush.verticalGradient(listOf(Color(0xFF3DDCFF), Color(0xFF1F79E8))))
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawHeaderShine()
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CHOOSE LEVEL",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Clear bubbles, keep combos, collect stars",
                        color = Color(0xFFDDF8FF),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                WorldProgressPill()
                (1..TOTAL_LEVELS).chunked(5).forEachIndexed { rowIndex, rowLevels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        rowLevels.forEachIndexed { index, level ->
                            LevelBubbleTile(
                                level = level,
                                stars = starsForLevel(level),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(82.dp),
                                offsetDown = (rowIndex + index) % 2 == 1,
                                onClick = { onLevelSelected(level) },
                            )
                        }
                        repeat(5 - rowLevels.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 6.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFF2FCF86), Color(0xFF9BE354))))
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Tap a stage to start",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldProgressPill() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFEAF5FF))
            .border(1.dp, Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("World 1", color = Color(0xFF163A71), fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("Sunny Bubble Coast", color = Color(0xFF5E7396), style = MaterialTheme.typography.labelMedium)
        }
        Text("1–30", color = Color(0xFF2E7BEF), fontWeight = FontWeight.Black, fontSize = 20.sp)
    }
}

@Composable
private fun LevelBubbleTile(
    level: Int,
    stars: Int,
    modifier: Modifier = Modifier,
    offsetDown: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(top = if (offsetDown) 6.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(6.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = tileColors(level),
                        center = Offset(16f, 14f),
                        radius = 62f,
                    ),
                )
                .border(2.dp, Color.White.copy(alpha = 0.72f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(Color.White.copy(alpha = 0.42f), radius = size.minDimension * 0.14f, center = Offset(size.width * 0.34f, size.height * 0.28f))
                drawCircle(Color(0xFF143765).copy(alpha = 0.16f), radius = size.minDimension * 0.47f, center = Offset(size.width * 0.55f, size.height * 0.62f), style = Stroke(width = 2f))
            }
            Text(
                text = level.toString(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
            )
        }
        Text(
            text = buildString {
                repeat(stars) { append('★') }
                repeat(3 - stars) { append('☆') }
            },
            color = Color(0xFFFFB800),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            maxLines = 1,
            fontWeight = FontWeight.Black,
        )
    }
}

private fun tileColors(level: Int): List<Color> = when (level % 5) {
    0 -> listOf(Color.White, Color(0xFFFFD166), Color(0xFFE89E00))
    1 -> listOf(Color.White, Color(0xFF69D7FF), Color(0xFF2378E8))
    2 -> listOf(Color.White, Color(0xFFFF7DA0), Color(0xFFE44468))
    3 -> listOf(Color.White, Color(0xFF54E6AE), Color(0xFF189F78))
    else -> listOf(Color.White, Color(0xFFB491FF), Color(0xFF7555D8))
}

private fun starsForLevel(level: Int): Int = when {
    level <= 10 -> 3
    level <= 20 -> 2
    else -> 1
}

private fun DrawScope.drawLevelMapBackdrop() {
    drawCircle(Color.White.copy(alpha = 0.16f), radius = 74f, center = Offset(size.width * 0.14f, size.height * 0.18f))
    drawCircle(Color.White.copy(alpha = 0.12f), radius = 56f, center = Offset(size.width * 0.86f, size.height * 0.25f))
    drawCircle(Color.White.copy(alpha = 0.10f), radius = 92f, center = Offset(size.width * 0.20f, size.height * 0.82f))
    repeat(12) { index ->
        val x = size.width * ((index * 29 % 100) / 100f)
        val y = size.height * ((index * 41 % 100) / 100f)
        val radius = 13f + (index % 4) * 5f
        drawCircle(
            color = listOf(Color(0xFFFF6B8A), Color(0xFFFFD166), Color(0xFF55A7FF), Color(0xFF35C9A3))[index % 4].copy(alpha = 0.16f),
            radius = radius,
            center = Offset(x, y),
        )
        drawCircle(Color.White.copy(alpha = 0.40f), radius = radius * 0.24f, center = Offset(x - radius * 0.26f, y - radius * 0.30f))
    }
}

private fun DrawScope.drawHeaderShine() {
    val wave = Path().apply {
        moveTo(0f, size.height * 0.70f)
        quadraticTo(size.width * 0.32f, size.height * 0.96f, size.width * 0.68f, size.height * 0.76f)
        quadraticTo(size.width * 0.90f, size.height * 0.64f, size.width, size.height * 0.80f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(wave, Color(0xFF145BC7).copy(alpha = 0.22f))
    drawRoundRect(
        color = Color.White.copy(alpha = 0.20f),
        topLeft = Offset(size.width * 0.08f, size.height * 0.15f),
        size = Size(size.width * 0.84f, size.height * 0.18f),
        cornerRadius = CornerRadius(28f, 28f),
    )
    repeat(3) { index ->
        val center = Offset(size.width * (0.18f + index * 0.32f), size.height * 0.66f)
        drawStar(center = center, radius = 12f + index * 1.5f, color = Color.White.copy(alpha = 0.16f))
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    repeat(10) { index ->
        val angle = (-PI.toFloat() / 2f) + index * (PI.toFloat() / 5f)
        val r = if (index % 2 == 0) radius else radius * 0.46f
        val point = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r)
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    drawPath(path, color)
}
