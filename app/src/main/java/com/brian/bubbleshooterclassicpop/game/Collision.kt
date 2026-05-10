package com.brian.bubbleshooterclassicpop.game

object Collision {
    private const val COLLISION_SLOP = 1.5f

    fun findBubbleCollision(
        center: Vec2,
        board: Map<GridPosition, Bubble>,
    ): Bubble? {
        val collisionDistance = GridMath.BUBBLE_DIAMETER - COLLISION_SLOP
        return board.values
            .filter { bubble -> GridMath.cellToCenter(bubble.position).distanceTo(center) <= collisionDistance }
            .minByOrNull { bubble -> GridMath.cellToCenter(bubble.position).distanceTo(center) }
    }

    fun hitsTop(center: Vec2): Boolean =
        center.y <= GridMath.TOP_PADDING + GridMath.BUBBLE_RADIUS

    fun advanceWithWallBounce(
        position: Vec2,
        velocity: Vec2,
        deltaSeconds: Float,
    ): FlyingStep {
        var nextX = position.x + velocity.x * deltaSeconds
        val nextY = position.y + velocity.y * deltaSeconds
        var nextVelocityX = velocity.x
        val left = GridMath.BUBBLE_RADIUS
        val right = GridMath.LOGICAL_WIDTH - GridMath.BUBBLE_RADIUS

        if (nextX < left) {
            nextX = left + (left - nextX)
            nextVelocityX = -nextVelocityX
        } else if (nextX > right) {
            nextX = right - (nextX - right)
            nextVelocityX = -nextVelocityX
        }

        return FlyingStep(
            position = Vec2(nextX.coerceIn(left, right), nextY),
            velocity = Vec2(nextVelocityX, velocity.y),
        )
    }
}

data class FlyingStep(
    val position: Vec2,
    val velocity: Vec2,
)
