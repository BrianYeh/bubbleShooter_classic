package com.brian.bubbleshooterclassicpop.game

import kotlin.math.min
import kotlin.random.Random

class GameEngine(
    private val random: Random = Random(7),
    initialState: GameState = GameState(),
) {
    var state: GameState = initialState
        private set

    fun start(level: Int = 1) {
        state = createInitialState(level.coerceAtLeast(1))
    }

    fun restart() {
        start(state.level.coerceAtLeast(1))
    }

    fun nextLevel() {
        start(state.level + 1)
    }

    fun setAimTarget(target: Vec2) {
        if (state.phase != GamePhase.Running || state.flyingBubble != null) return
        state = state.copy(aimAngleDegrees = GridMath.aimAngleForTarget(target))
    }

    fun togglePause() {
        state = when (state.phase) {
            GamePhase.Running -> state.copy(phase = GamePhase.Paused)
            GamePhase.Paused -> state.copy(phase = GamePhase.Running)
            else -> state
        }
    }

    fun shoot() {
        if (state.phase != GamePhase.Running || state.flyingBubble != null) return
        if (state.shotsRemaining <= 0) {
            state = state.copy(phase = GamePhase.Lost)
            return
        }

        val velocity = GridMath.velocityForAngle(state.aimAngleDegrees)
        state = state.copy(
            flyingBubble = FlyingBubble(
                position = GridMath.shooterPosition,
                velocity = velocity,
                color = state.currentBubble,
            ),
            shotsRemaining = state.shotsRemaining - 1,
            lastPopped = 0,
            lastDropped = 0,
        )
    }

    fun tick(deltaSeconds: Float) {
        val flying = state.flyingBubble ?: return
        if (state.phase != GamePhase.Running) return

        var remaining = deltaSeconds.coerceIn(0f, 0.08f)
        var activeFlying = flying
        while (remaining > 0f && state.flyingBubble != null) {
            val stepSeconds = min(remaining, 1f / 120f)
            activeFlying = advanceFlying(activeFlying, stepSeconds) ?: return
            remaining -= stepSeconds
        }
    }

    fun hasWon(board: Map<GridPosition, Bubble> = state.board): Boolean = board.isEmpty()

    fun hasLost(
        board: Map<GridPosition, Bubble> = state.board,
        shotsRemaining: Int = state.shotsRemaining,
    ): Boolean = board.isNotEmpty() && (GridMath.isBeyondDangerLine(board) || shotsRemaining <= 0)

    private fun advanceFlying(
        flying: FlyingBubble,
        deltaSeconds: Float,
    ): FlyingBubble? {
        val step = Collision.advanceWithWallBounce(flying.position, flying.velocity, deltaSeconds)
        val moved = flying.copy(position = step.position, velocity = step.velocity)
        val hitBubble = Collision.findBubbleCollision(moved.position, state.board)

        if (Collision.hitsTop(moved.position) || hitBubble != null) {
            attachBubble(moved.position, moved.color)
            return null
        }

        state = state.copy(flyingBubble = moved)
        return moved
    }

    private fun attachBubble(point: Vec2, color: BubbleColor) {
        val snap = GridMath.nearestAttachableCell(point, state.board)
        if (snap == null) {
            state = state.copy(flyingBubble = null, phase = GamePhase.Lost)
            return
        }

        val placedBoard = state.board + (snap to Bubble(position = snap, color = color))
        val result = MatchDetector.removeMatchesAndFloating(snap, placedBoard)
        val gained = result.popped.size * POP_SCORE + result.dropped.size * DROP_SCORE
        val won = hasWon(result.board)
        val lost = !won && hasLost(result.board, state.shotsRemaining)

        state = state.copy(
            board = result.board,
            flyingBubble = null,
            currentBubble = state.nextBubble,
            nextBubble = randomColor(),
            score = state.score + gained,
            phase = when {
                won -> GamePhase.Won
                lost -> GamePhase.Lost
                else -> GamePhase.Running
            },
            lastPopped = result.popped.size,
            lastDropped = result.dropped.size,
        )
    }

    private fun createInitialState(level: Int): GameState {
        val rows = (5 + level).coerceAtMost(9)
        val colors = BubbleColor.entries
        val board = buildMap {
            repeat(rows) { row ->
                repeat(GridMath.columnsForRow(row)) { col ->
                    val position = GridPosition(row, col)
                    val colorIndex = (row * 2 + col + random.nextInt(colors.size)) % colors.size
                    put(position, Bubble(position = position, color = colors[colorIndex]))
                }
            }
        }

        return GameState(
            phase = GamePhase.Running,
            board = board,
            currentBubble = randomColor(),
            nextBubble = randomColor(),
            aimAngleDegrees = 90f,
            score = 0,
            level = level,
            shotsRemaining = 46 + level * 4,
        )
    }

    private fun randomColor(): BubbleColor = BubbleColor.entries[random.nextInt(BubbleColor.entries.size)]

    companion object {
        const val POP_SCORE = 10
        const val DROP_SCORE = 20
    }
}
