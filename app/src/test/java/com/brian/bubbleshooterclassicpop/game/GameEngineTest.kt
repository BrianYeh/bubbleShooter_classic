package com.brian.bubbleshooterclassicpop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {
    @Test
    fun startCreatesPlayableBoard() {
        val engine = GameEngine(Random(1))

        engine.start(level = 1)

        assertEquals(GamePhase.Running, engine.state.phase)
        assertTrue(engine.state.board.isNotEmpty())
        assertTrue(engine.state.shotsRemaining > 0)
    }

    @Test
    fun emptyBoardIsWinCondition() {
        val engine = GameEngine(Random(1))

        assertTrue(engine.hasWon(emptyMap()))
        assertFalse(engine.hasLost(emptyMap(), shotsRemaining = 0))
    }

    @Test
    fun bubbleCrossingDangerLineIsLoseCondition() {
        val dangerCell = GridPosition(GridMath.MAX_ROWS - 1, 0)
        val board = mapOf(dangerCell to Bubble(dangerCell, BubbleColor.Grape))
        val engine = GameEngine(Random(1))

        assertTrue(GridMath.cellToCenter(dangerCell).y + GridMath.BUBBLE_RADIUS >= GridMath.DANGER_LINE_Y)
        assertTrue(engine.hasLost(board, shotsRemaining = 12))
    }

    @Test
    fun shootingConsumesOneShotAndCreatesFlyingBubble() {
        val engine = GameEngine(Random(1))
        engine.start(level = 1)
        val shots = engine.state.shotsRemaining

        engine.shoot()

        assertEquals(shots - 1, engine.state.shotsRemaining)
        assertTrue(engine.state.flyingBubble != null)
    }
}
