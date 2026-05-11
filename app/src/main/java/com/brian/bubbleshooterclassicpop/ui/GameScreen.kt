package com.brian.bubbleshooterclassicpop.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.brian.bubbleshooterclassicpop.game.BubbleColor
import com.brian.bubbleshooterclassicpop.game.GamePhase
import com.brian.bubbleshooterclassicpop.game.GameState
import com.brian.bubbleshooterclassicpop.game.GridMath
import com.brian.bubbleshooterclassicpop.game.Vec2
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun GameScreen(
    state: GameState,
    onAim: (Vec2) -> Unit,
    onShoot: () -> Unit,
    onTick: (Float) -> Unit,
    onPause: () -> Unit,
    onRestart: () -> Unit,
) {
    val touchEnabled = state.phase == GamePhase.Running && state.flyingBubble == null
    val feedbackKey = feedbackKey(state)
    val feedbackText = feedbackMessage(state)
    var feedbackProgress by remember { mutableStateOf(1f) }

    LaunchedEffect(feedbackKey) {
        if (feedbackKey == null) {
            feedbackProgress = 1f
            return@LaunchedEffect
        }

        val durationNanos = FEEDBACK_DURATION_MILLIS * 1_000_000L
        val startNanos = withFrameNanos { it }
        feedbackProgress = 0f
        while (isActive && feedbackProgress < 1f) {
            withFrameNanos { now ->
                feedbackProgress = ((now - startNanos).toFloat() / durationNanos).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.Running) {
            var lastFrame = 0L
            while (isActive) {
                withFrameNanos { now ->
                    if (lastFrame != 0L) {
                        onTick((now - lastFrame) / 1_000_000_000f)
                    }
                    lastFrame = now
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF65C7FF), Color(0xFFBFEFFF), Color(0xFFFFE5AA)),
                ),
            )
            .statusBarsPadding()
            .padding(12.dp),
    ) {
        ScoreHeader(state = state, onPause = onPause, onRestart = onRestart)
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            var canvasSize by remember { mutableStateOf(IntSize.Zero) }
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(canvasSize, touchEnabled) {
                        if (touchEnabled && canvasSize.width > 0 && canvasSize.height > 0) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                onAim(down.position.toLogical(canvasSize))
                                var released = false
                                while (!released) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.first()
                                    onAim(change.position.toLogical(canvasSize))
                                    if (change.changedToUp()) {
                                        released = true
                                    }
                                }
                                onShoot()
                            }
                        }
                    },
            ) {
                drawGame(state, feedbackProgress)
            }

            FeedbackBanner(
                message = feedbackText,
                progress = feedbackProgress,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp),
            )

            if (state.phase == GamePhase.Paused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xAA162033)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Paused",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackBanner(
    message: String?,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    if (message == null || progress >= 1f) return
    val alpha = when {
        progress < 0.42f -> 1f
        else -> (1f - ((progress - 0.42f) / 0.58f)).coerceIn(0f, 1f)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xCC17315B).copy(alpha = 0.8f * alpha))
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = Color.White.copy(alpha = alpha),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
    }
}

private fun feedbackKey(state: GameState): String? {
    if (state.lastPopped > 0 || state.lastDropped > 0 || state.lastComboBonus > 0) {
        return "clear:${state.score}:${state.shotsRemaining}:${state.lastPopped}:${state.lastDropped}:${state.lastComboBonus}:${state.comboStreak}"
    }
    if (state.missStreak > 0 && state.flyingBubble == null && state.phase == GamePhase.Running) {
        return "miss:${state.shotsRemaining}:${state.missStreak}"
    }
    return null
}

private fun feedbackMessage(state: GameState): String? {
    val parts = buildList {
        if (state.lastPopped > 0) add("POP x${state.lastPopped}")
        if (state.lastDropped > 0) add("DROP x${state.lastDropped}")
        if (state.comboStreak > 1) add("COMBO x${state.comboStreak}")
        if (state.lastComboBonus > 0) add("+${state.lastComboBonus}")
    }
    if (parts.isNotEmpty()) return parts.joinToString("  •  ")
    if (state.missStreak > 0 && state.flyingBubble == null && state.phase == GamePhase.Running) {
        return "MISS ${state.missStreak}  •  MATCH 3+"
    }
    return null
}


@Composable
private fun ScoreHeader(
    state: GameState,
    onPause: () -> Unit,
    onRestart: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xAA17315B))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ArcadeMenuButton()
            StarProgressBar(
                progress = (state.score / 1_500f).coerceIn(0f, 1f),
                modifier = Modifier.weight(1f),
            )
            ScorePill(score = state.score)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatPill(label = "Level", value = state.level.toString(), modifier = Modifier.weight(1f))
            StatPill(label = "Shots", value = state.shotsRemaining.toString(), modifier = Modifier.weight(1f))
            NextBubbleChip(color = state.nextBubble)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        ) {
            OutlinedButton(onClick = onRestart) {
                Text(text = "Restart")
            }
            Button(onClick = onPause) {
                Text(text = if (state.phase == GamePhase.Paused) "Resume" else "Pause")
            }
        }
    }
}

@Composable
private fun ArcadeMenuButton() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF6BE4FF), Color(0xFF2B77D9)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            repeat(3) { index ->
                drawLine(
                    color = Color.White,
                    start = Offset(size.width * 0.18f, size.height * (0.28f + index * 0.22f)),
                    end = Offset(size.width * 0.82f, size.height * (0.28f + index * 0.22f)),
                    strokeWidth = 3.2f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun StarProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(18.dp)),
    ) {
        val trackHeight = size.height * 0.34f
        val trackTop = size.height * 0.34f
        val trackSize = Size(size.width, trackHeight)
        drawRoundRect(
            color = Color(0xFF0E2448).copy(alpha = 0.72f),
            topLeft = Offset(0f, trackTop),
            size = trackSize,
            cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f),
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFFFFE66B), Color(0xFFFF9D42))),
            topLeft = Offset(0f, trackTop),
            size = Size(size.width * progress, trackHeight),
            cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f),
        )
        listOf(0.22f, 0.50f, 0.78f).forEachIndexed { index, xFraction ->
            val center = Offset(size.width * xFraction, size.height * 0.5f)
            val filled = progress >= xFraction
            drawPath(
                path = starPath(center, size.height * 0.22f),
                color = if (filled) Color(0xFFFFF06A) else Color.White.copy(alpha = 0.65f),
            )
            drawPath(
                path = starPath(center, size.height * 0.22f),
                color = Color(0xFF6B4B00).copy(alpha = 0.35f),
                style = Stroke(width = 1.2f),
            )
        }
    }
}

@Composable
private fun ScorePill(score: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF9C7DFF), Color(0xFF3D73E6)),
                ),
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = "SCORE",
            color = Color.White.copy(alpha = 0.78f),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = score.toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun StatPill(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label.uppercase(),
            color = Color(0xFF45618C),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            color = Color(0xFF17315B),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun NextBubbleChip(color: BubbleColor) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "NEXT",
            color = Color(0xFF45618C),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
        Canvas(modifier = Modifier.size(30.dp)) {
            drawBubble(
                center = Vec2(size.width / 2f, size.height / 2f),
                color = color,
                radius = size.minDimension * 0.42f,
            )
        }
    }
}

private fun starPath(center: Offset, radius: Float): Path {
    val path = Path()
    val innerRadius = radius * 0.46f
    repeat(10) { index ->
        val angle = (-PI.toFloat() / 2f) + index * (PI.toFloat() / 5f)
        val r = if (index % 2 == 0) radius else innerRadius
        val point = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r)
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    return path
}

private fun DrawScope.drawGame(state: GameState, feedbackProgress: Float) {
    val scale = GridMath.scaleFor(size.width, size.height)
    val left = (size.width - GridMath.LOGICAL_WIDTH * scale) / 2f
    val top = (size.height - GridMath.LOGICAL_HEIGHT * scale) / 2f

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF79D8FF), Color(0xFFC7F4FF), Color(0xFFFFD98A)),
        ),
    )
    withTransform({
        translate(left, top)
        scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
    }) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF62C8FF), Color(0xFFC8F7FF), Color(0xFFFFD98A)),
                startY = 0f,
                endY = GridMath.LOGICAL_HEIGHT,
            ),
            size = Size(GridMath.LOGICAL_WIDTH, GridMath.LOGICAL_HEIGHT),
            cornerRadius = CornerRadius(24f, 24f),
        )
        drawSkyDecorations()
        drawBoardGlow()

        drawDangerLine()
        if (state.phase == GamePhase.Running && state.flyingBubble == null) {
            drawAimGuide(state.aimAngleDegrees)
        }
        state.board.values.forEach { bubble ->
            drawBubble(
                center = GridMath.cellToCenter(bubble.position),
                color = bubble.color,
                radius = GridMath.BUBBLE_RADIUS,
            )
        }
        state.flyingBubble?.let { flying ->
            drawBubble(
                center = flying.position,
                color = flying.color,
                radius = GridMath.BUBBLE_RADIUS,
            )
        }
        drawClearBursts(state, feedbackProgress)
        drawPowerUpDock()
        drawShooter(state)
    }
}

private fun DrawScope.drawSkyDecorations() {
    drawCircle(Color.White.copy(alpha = 0.10f), radius = 42f, center = Offset(312f, 118f))
    drawCircle(Color.White.copy(alpha = 0.08f), radius = 30f, center = Offset(48f, 168f))
    drawCloud(Offset(58f, 92f), 0.78f)
    drawCloud(Offset(286f, 182f), 0.58f)
}

private fun DrawScope.drawCloud(origin: Offset, scale: Float) {
    val color = Color.White.copy(alpha = 0.36f)
    drawCircle(color, radius = 18f * scale, center = origin)
    drawCircle(color, radius = 24f * scale, center = origin + Offset(22f * scale, -6f * scale))
    drawCircle(color, radius = 17f * scale, center = origin + Offset(46f * scale, 2f * scale))
    drawRoundRect(
        color = color,
        topLeft = origin + Offset(-12f * scale, 2f * scale),
        size = Size(72f * scale, 22f * scale),
        cornerRadius = CornerRadius(12f * scale, 12f * scale),
    )
}

private fun DrawScope.drawBoardGlow() {
    drawRoundRect(
        color = Color.White.copy(alpha = 0.17f),
        topLeft = Offset(8f, 16f),
        size = Size(GridMath.LOGICAL_WIDTH - 16f, 402f),
        cornerRadius = CornerRadius(24f, 24f),
    )
    drawRoundRect(
        color = Color(0xFF1259A8).copy(alpha = 0.08f),
        topLeft = Offset(8f, 16f),
        size = Size(GridMath.LOGICAL_WIDTH - 16f, 402f),
        cornerRadius = CornerRadius(24f, 24f),
        style = Stroke(width = 2f),
    )
}

private fun DrawScope.drawDangerLine() {
    drawLine(
        color = Color(0xFFFF4F70).copy(alpha = 0.55f),
        start = Offset(18f, GridMath.DANGER_LINE_Y),
        end = Offset(GridMath.LOGICAL_WIDTH - 18f, GridMath.DANGER_LINE_Y),
        strokeWidth = 3f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
    )
}

private fun DrawScope.drawAimGuide(angleDegrees: Float) {
    aimGuideSegments(angleDegrees).forEach { (start, end) ->
        drawLine(
            color = Color.White.copy(alpha = 0.78f),
            start = Offset(start.x, start.y),
            end = Offset(end.x, end.y),
            strokeWidth = 7f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f)),
        )
        drawLine(
            color = Color(0xFF2577FF).copy(alpha = 0.62f),
            start = Offset(start.x, start.y),
            end = Offset(end.x, end.y),
            strokeWidth = 3.4f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f)),
        )
    }
}


private fun DrawScope.drawClearBursts(state: GameState, progress: Float) {
    if (progress >= 1f) return
    val alpha = (1f - progress).coerceIn(0f, 1f)
    state.lastClearedPositions.forEachIndexed { index, position ->
        val center = GridMath.cellToCenter(position)
        val offset = Offset(center.x, center.y)
        val radius = GridMath.BUBBLE_RADIUS * (0.82f + progress * 1.25f + (index % 3) * 0.08f)
        drawCircle(
            color = Color.White.copy(alpha = 0.72f * alpha),
            radius = radius,
            center = offset,
            style = Stroke(width = 2.2f),
        )
        drawCircle(
            color = Color(0xFFFFF06A).copy(alpha = 0.50f * alpha),
            radius = radius * 0.72f,
            center = offset,
            style = Stroke(width = 1.4f),
        )
        repeat(4) { spark ->
            val angle = spark * (PI.toFloat() / 2f) + (index * 0.35f)
            val sparkCenter = offset + Offset(cos(angle) * radius, sin(angle) * radius)
            drawCircle(
                color = Color.White.copy(alpha = 0.78f * alpha),
                radius = 2.2f + progress * 1.4f,
                center = sparkCenter,
            )
        }
    }
}

private const val FEEDBACK_DURATION_MILLIS = 520L

private fun DrawScope.drawShooter(state: GameState) {
    val shooter = GridMath.shooterPosition
    val radians = state.aimAngleDegrees * PI.toFloat() / 180f
    val barrelEnd = Vec2(
        x = shooter.x + cos(radians) * 56f,
        y = shooter.y - sin(radians) * 56f,
    )

    drawCircle(
        color = Color(0xFF0B2A56).copy(alpha = 0.24f),
        radius = 44f,
        center = Offset(shooter.x, shooter.y + 17f),
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.75f), Color(0xFF51B5FF), Color(0xFF2469D7)),
            center = Offset(shooter.x - 14f, shooter.y - 10f),
            radius = 62f,
        ),
        radius = 33f,
        center = Offset(shooter.x, shooter.y + 12f),
    )
    drawLine(
        color = Color(0xFF0F3269).copy(alpha = 0.82f),
        start = Offset(shooter.x, shooter.y + 6f),
        end = Offset(barrelEnd.x, barrelEnd.y),
        strokeWidth = 19f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = 0.55f),
        start = Offset(shooter.x - 2f, shooter.y + 2f),
        end = Offset(barrelEnd.x - 2f, barrelEnd.y - 2f),
        strokeWidth = 7f,
        cap = StrokeCap.Round,
    )

    if (state.flyingBubble == null) {
        drawBubble(
            center = shooter,
            color = state.currentBubble,
            radius = GridMath.BUBBLE_RADIUS * 1.18f,
        )
    }
}

private fun DrawScope.drawPowerUpDock() {
    val dockTop = GridMath.LOGICAL_HEIGHT - 82f
    val buttonSize = Size(50f, 36f)
    val starts = listOf(34f, 91f, 219f, 276f)
    val iconColors = listOf(Color(0xFFFF5C77), Color(0xFFFFC53D), Color(0xFF57D7FF), Color(0xFF9D7CFF))

    drawRoundRect(
        color = Color(0xFF14366F).copy(alpha = 0.24f),
        topLeft = Offset(20f, dockTop - 6f),
        size = Size(320f, 68f),
        cornerRadius = CornerRadius(26f, 26f),
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.18f),
        radius = 43f,
        center = Offset(GridMath.shooterPosition.x, GridMath.shooterPosition.y + 10f),
    )

    starts.forEachIndexed { index, x ->
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.94f), Color(0xFFBFE9FF).copy(alpha = 0.86f))),
            topLeft = Offset(x, dockTop + 9f),
            size = buttonSize,
            cornerRadius = CornerRadius(17f, 17f),
        )
        drawRoundRect(
            color = Color(0xFF266BD8).copy(alpha = 0.36f),
            topLeft = Offset(x, dockTop + 9f),
            size = buttonSize,
            cornerRadius = CornerRadius(17f, 17f),
            style = Stroke(width = 2f),
        )
        val center = Offset(x + 20f, dockTop + 27f)
        drawCircle(iconColors[index], radius = 11f, center = center)
        drawCircle(Color.White.copy(alpha = 0.72f), radius = 3.8f, center = center - Offset(3.8f, 4.2f))
        drawCircle(Color(0xFF1E4E9B), radius = 8f, center = Offset(x + 40f, dockTop + 17f))
        drawLine(
            color = Color.White,
            start = Offset(x + 36.5f, dockTop + 17f),
            end = Offset(x + 43.5f, dockTop + 17f),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color.White,
            start = Offset(x + 40f, dockTop + 13.5f),
            end = Offset(x + 40f, dockTop + 20.5f),
            strokeWidth = 2f,
            cap = StrokeCap.Round,
        )
    }
}


private fun DrawScope.drawBubble(
    center: Vec2,
    color: BubbleColor,
    radius: Float,
) {
    val base = color.baseColor()
    val shadow = color.shadowColor()
    val offset = Offset(center.x, center.y)
    drawCircle(
        color = Color(0xFF0F3568).copy(alpha = 0.22f),
        radius = radius * 1.05f,
        center = offset + Offset(radius * 0.16f, radius * 0.24f),
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.98f), base, shadow),
            center = offset - Offset(radius * 0.40f, radius * 0.48f),
            radius = radius * 1.78f,
        ),
        radius = radius,
        center = offset,
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.74f),
        radius = radius * 0.27f,
        center = offset - Offset(radius * 0.34f, radius * 0.38f),
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.25f),
        radius = radius * 0.16f,
        center = offset + Offset(radius * 0.34f, radius * 0.32f),
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.48f),
        radius = radius,
        center = offset,
        style = Stroke(width = 1.7f),
    )
    drawCircle(
        color = shadow.copy(alpha = 0.45f),
        radius = radius * 0.98f,
        center = offset,
        style = Stroke(width = 0.9f),
    )
}

private fun aimGuideSegments(angleDegrees: Float): List<Pair<Vec2, Vec2>> {
    val segments = mutableListOf<Pair<Vec2, Vec2>>()
    var velocity = GridMath.velocityForAngle(angleDegrees)
    var start = GridMath.shooterPosition + velocity * (46f / GridMath.SHOOT_SPEED)
    var remainingLength = 474f

    repeat(3) {
        val distanceToTop = if (velocity.y < 0f) {
            (GridMath.TOP_PADDING + GridMath.BUBBLE_RADIUS - start.y) / velocity.y
        } else {
            Float.POSITIVE_INFINITY
        }
        val distanceToWall = when {
            velocity.x < 0f -> (GridMath.BUBBLE_RADIUS - start.x) / velocity.x
            velocity.x > 0f -> (GridMath.LOGICAL_WIDTH - GridMath.BUBBLE_RADIUS - start.x) / velocity.x
            else -> Float.POSITIVE_INFINITY
        }
        val maxStepTime = remainingLength / GridMath.SHOOT_SPEED
        val stepTime = min(maxStepTime, min(distanceToTop, distanceToWall))
        if (!stepTime.isFinite() || stepTime <= 0f) return@repeat

        val end = start + velocity * stepTime
        segments += start to end
        remainingLength -= GridMath.SHOOT_SPEED * stepTime
        if (remainingLength <= 0f || distanceToTop <= distanceToWall) return segments

        start = end
        velocity = velocity.copy(x = -velocity.x)
    }

    return segments
}

private fun Offset.toLogical(size: IntSize): Vec2 {
    val scale = GridMath.scaleFor(size.width.toFloat(), size.height.toFloat()).takeIf { it > 0f } ?: 1f
    val left = (size.width - GridMath.LOGICAL_WIDTH * scale) / 2f
    val top = (size.height - GridMath.LOGICAL_HEIGHT * scale) / 2f
    return Vec2(
        x = ((x - left) / scale).coerceIn(0f, GridMath.LOGICAL_WIDTH),
        y = ((y - top) / scale).coerceIn(0f, GridMath.LOGICAL_HEIGHT),
    )
}

private fun BubbleColor.baseColor(): Color = when (this) {
    BubbleColor.Cherry -> Color(0xFFFF5C77)
    BubbleColor.Sun -> Color(0xFFFFC53D)
    BubbleColor.Mint -> Color(0xFF35C9A3)
    BubbleColor.Sky -> Color(0xFF4D9DFF)
    BubbleColor.Grape -> Color(0xFF9D7CFF)
    BubbleColor.Coral -> Color(0xFFFF8E5E)
}

private fun BubbleColor.shadowColor(): Color = when (this) {
    BubbleColor.Cherry -> Color(0xFFC8294C)
    BubbleColor.Sun -> Color(0xFFE19A00)
    BubbleColor.Mint -> Color(0xFF159072)
    BubbleColor.Sky -> Color(0xFF176FC9)
    BubbleColor.Grape -> Color(0xFF6543CF)
    BubbleColor.Coral -> Color(0xFFD95C31)
}
