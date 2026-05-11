package com.brian.bubbleshooterclassicpop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GridMathTest {
    @Test
    fun nearestCellReturnsOriginalCellCenter() {
        val cells = listOf(
            GridPosition(0, 0),
            GridPosition(0, 7),
            GridPosition(1, 3),
            GridPosition(5, 8),
        )

        cells.forEach { cell ->
            assertEquals(cell, GridMath.nearestCell(GridMath.cellToCenter(cell)))
        }
    }

    @Test
    fun oddRowsAreOffsetFromEvenRows() {
        val even = GridMath.cellToCenter(GridPosition(0, 0))
        val odd = GridMath.cellToCenter(GridPosition(1, 0))

        assertEquals(GridMath.BUBBLE_RADIUS, odd.x - even.x, 0.001f)
        assertEquals(GridMath.rowSpacing, odd.y - even.y, 0.001f)
    }

    @Test
    fun shotSpeedFeelsArcadeFast() {
        assertTrue(GridMath.SHOOT_SPEED >= 700f)
    }

    @Test
    fun aimAngleCannotPointDownward() {
        val targetBelowRight = Vec2(GridMath.shooterPosition.x + 40f, GridMath.shooterPosition.y + 60f)
        val targetBelowLeft = Vec2(GridMath.shooterPosition.x - 40f, GridMath.shooterPosition.y + 60f)

        assertEquals(GridMath.MIN_AIM_DEGREES, GridMath.aimAngleForTarget(targetBelowRight), 0.001f)
        assertEquals(GridMath.MAX_AIM_DEGREES, GridMath.aimAngleForTarget(targetBelowLeft), 0.001f)
    }

    @Test
    fun neighborsRespectHexOffsetRows() {
        val evenNeighbors = GridMath.neighborsOf(GridPosition(2, 3))
        val oddNeighbors = GridMath.neighborsOf(GridPosition(3, 3))

        assertTrue(GridPosition(1, 2) in evenNeighbors)
        assertTrue(GridPosition(1, 3) in evenNeighbors)
        assertTrue(GridPosition(2, 2) in evenNeighbors)
        assertTrue(GridPosition(2, 4) in evenNeighbors)
        assertTrue(GridPosition(2, 3) in oddNeighbors)
        assertTrue(GridPosition(2, 4) in oddNeighbors)
        assertTrue(GridPosition(3, 2) in oddNeighbors)
        assertTrue(GridPosition(3, 4) in oddNeighbors)
    }
}
