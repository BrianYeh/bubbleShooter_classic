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
            .background(Color(0xFFF7FAFF))
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
                    .clip(RoundedCornerShape(8.dp))
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
                drawGame(state)
            }

            if (state.phase == GamePhase.Paused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
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
private fun ScoreHeader(
    state: GameState,
    onPause: () -> Unit,
    onRestart: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatText(label = "Score", value = state.score.toString())
            StatText(label = "Level", value = state.level.toString())
            StatText(label = "Shots", value = state.shotsRemaining.toString())
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
private fun StatText(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            color = Color(0xFF63708A),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = value,
            color = Color(0xFF162033),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun NextBubbleChip(color: BubbleColor) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Next",
            color = Color(0xFF63708A),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
        )
        Canvas(modifier = Modifier.size(28.dp)) {
            drawBubble(
                center = Vec2(size.width / 2f, size.height / 2f),
                color = color,
                radius = size.minDimension * 0.42f,
            )
        }
    }
}

private fun DrawScope.drawGame(state: GameState) {
    val scale = GridMath.scaleFor(size.width, size.height)
    val left = (size.width - GridMath.LOGICAL_WIDTH * scale) / 2f
    val top = (size.height - GridMath.LOGICAL_HEIGHT * scale) / 2f

    drawRect(color = Color(0xFFE0E9F7))
    withTransform({
        translate(left, top)
        scale(scale, scale)
    }) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFEFF8FF), Color(0xFFEFFFF8), Color(0xFFFFF3D8)),
                startY = 0f,
                endY = GridMath.LOGICAL_HEIGHT,
            ),
            size = Size(GridMath.LOGICAL_WIDTH, GridMath.LOGICAL_HEIGHT),
            cornerRadius = CornerRadius(10f, 10f),
        )

        drawDangerLine()
        if (state.phase == GamePhase.Running && state.flyingBubble == null) {
            drawAimGuide(state.aimAngleDegrees)
        }
        drawShooter(state)
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
    }
}

private fun DrawScope.drawDangerLine() {
    drawLine(
        color = Color(0xFFFF5C77).copy(alpha = 0.72f),
        start = Offset(0f, GridMath.DANGER_LINE_Y),
        end = Offset(GridMath.LOGICAL_WIDTH, GridMath.DANGER_LINE_Y),
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
    )
}

private fun DrawScope.drawAimGuide(angleDegrees: Float) {
    aimGuideSegments(angleDegrees).forEach { (start, end) ->
        drawLine(
            color = Color(0xFF2E7BEF).copy(alpha = 0.38f),
            start = Offset(start.x, start.y),
            end = Offset(end.x, end.y),
            strokeWidth = 3f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 9f)),
        )
    }
}

private fun DrawScope.drawShooter(state: GameState) {
    val shooter = GridMath.shooterPosition
    val radians = state.aimAngleDegrees * PI.toFloat() / 180f
    val barrelEnd = Vec2(
        x = shooter.x + cos(radians) * 46f,
        y = shooter.y - sin(radians) * 46f,
    )

    drawCircle(
        color = Color(0xFF162033).copy(alpha = 0.10f),
        radius = 38f,
        center = Offset(shooter.x, shooter.y + 13f),
    )
    drawLine(
        color = Color(0xFF293246),
        start = Offset(shooter.x, shooter.y),
        end = Offset(barrelEnd.x, barrelEnd.y),
        strokeWidth = 14f,
        cap = StrokeCap.Round,
    )
    drawCircle(
        color = Color(0xFF293246),
        radius = 24f,
        center = Offset(shooter.x, shooter.y + 12f),
    )

    if (state.flyingBubble == null) {
        drawBubble(
            center = shooter,
            color = state.currentBubble,
            radius = GridMath.BUBBLE_RADIUS,
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
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.92f), base, shadow),
            center = offset - Offset(radius * 0.36f, radius * 0.42f),
            radius = radius * 1.55f,
        ),
        radius = radius,
        center = offset,
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.58f),
        radius = radius * 0.26f,
        center = offset - Offset(radius * 0.34f, radius * 0.36f),
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.28f),
        radius = radius,
        center = offset,
        style = Stroke(width = 1.2f),
    )
}

private fun aimGuideSegments(angleDegrees: Float): List<Pair<Vec2, Vec2>> {
    val segments = mutableListOf<Pair<Vec2, Vec2>>()
    var start = GridMath.shooterPosition
    var velocity = GridMath.velocityForAngle(angleDegrees)
    var remainingLength = 520f

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
