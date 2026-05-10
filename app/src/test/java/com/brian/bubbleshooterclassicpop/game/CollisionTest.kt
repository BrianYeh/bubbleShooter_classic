package com.brian.bubbleshooterclassicpop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CollisionTest {
    @Test
    fun detectsCollisionWithinBubbleDiameter() {
        val position = GridPosition(0, 3)
        val board = mapOf(position to Bubble(position, BubbleColor.Cherry))
        val center = GridMath.cellToCenter(position)
        val movingCenter = center.copy(x = center.x + GridMath.BUBBLE_DIAMETER - 2f)

        assertNotNull(Collision.findBubbleCollision(movingCenter, board))
    }

    @Test
    fun ignoresDistantBubble() {
        val position = GridPosition(0, 3)
        val board = mapOf(position to Bubble(position, BubbleColor.Cherry))
        val center = GridMath.cellToCenter(position)
        val movingCenter = center.copy(x = center.x + GridMath.BUBBLE_DIAMETER * 2f)

        assertNull(Collision.findBubbleCollision(movingCenter, board))
    }

    @Test
    fun bouncesOffLeftWall() {
        val step = Collision.advanceWithWallBounce(
            position = Vec2(GridMath.BUBBLE_RADIUS + 1f, 200f),
            velocity = Vec2(-120f, -20f),
            deltaSeconds = 0.05f,
        )

        assertTrue(step.position.x >= GridMath.BUBBLE_RADIUS)
        assertEquals(120f, step.velocity.x, 0.001f)
        assertEquals(-20f, step.velocity.y, 0.001f)
    }
}
