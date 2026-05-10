package com.brian.bubbleshooterclassicpop.game

import kotlin.math.hypot

data class GridPosition(
    val row: Int,
    val col: Int,
)

data class Vec2(
    val x: Float,
    val y: Float,
) {
    operator fun plus(other: Vec2): Vec2 = Vec2(x + other.x, y + other.y)

    operator fun times(scale: Float): Vec2 = Vec2(x * scale, y * scale)

    fun distanceTo(other: Vec2): Float = hypot(x - other.x, y - other.y)
}

data class Bubble(
    val position: GridPosition,
    val color: BubbleColor,
)

data class FlyingBubble(
    val position: Vec2,
    val velocity: Vec2,
    val color: BubbleColor,
)
