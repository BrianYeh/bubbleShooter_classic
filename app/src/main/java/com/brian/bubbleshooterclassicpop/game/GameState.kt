package com.brian.bubbleshooterclassicpop.game

enum class GamePhase {
    Start,
    Running,
    Paused,
    Won,
    Lost,
}

data class GameState(
    val phase: GamePhase = GamePhase.Start,
    val board: Map<GridPosition, Bubble> = emptyMap(),
    val currentBubble: BubbleColor = BubbleColor.Cherry,
    val nextBubble: BubbleColor = BubbleColor.Sun,
    val flyingBubble: FlyingBubble? = null,
    val aimAngleDegrees: Float = 90f,
    val score: Int = 0,
    val level: Int = 1,
    val shotsRemaining: Int = 0,
    val lastPopped: Int = 0,
    val lastDropped: Int = 0,
    val lastComboBonus: Int = 0,
    val comboStreak: Int = 0,
    val missStreak: Int = 0,
    val lastClearedPositions: List<GridPosition> = emptyList(),
)
