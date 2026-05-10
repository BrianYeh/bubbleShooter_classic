package com.brian.bubbleshooterclassicpop.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchDetectorTest {
    @Test
    fun removesThreeConnectedSameColorBubbles() {
        val placed = GridPosition(1, 0)
        val board = mapOf(
            GridPosition(0, 0) to Bubble(GridPosition(0, 0), BubbleColor.Mint),
            GridPosition(0, 1) to Bubble(GridPosition(0, 1), BubbleColor.Mint),
            placed to Bubble(placed, BubbleColor.Mint),
            GridPosition(0, 2) to Bubble(GridPosition(0, 2), BubbleColor.Sky),
        )

        val result = MatchDetector.removeMatchesAndFloating(placed, board)

        assertEquals(3, result.popped.size)
        assertTrue(placed in result.popped)
        assertEquals(setOf(GridPosition(0, 2)), result.board.keys)
    }

    @Test
    fun doesNotRemovePair() {
        val placed = GridPosition(1, 0)
        val board = mapOf(
            GridPosition(0, 0) to Bubble(GridPosition(0, 0), BubbleColor.Sun),
            placed to Bubble(placed, BubbleColor.Sun),
        )

        val result = MatchDetector.removeMatchesAndFloating(placed, board)

        assertEquals(0, result.popped.size)
        assertEquals(board, result.board)
    }

    @Test
    fun detectsFloatingBubblesAfterPop() {
        val topLeft = GridPosition(0, 0)
        val topRight = GridPosition(0, 3)
        val bridge = GridPosition(1, 0)
        val floating = GridPosition(2, 0)
        val board = mapOf(
            topLeft to Bubble(topLeft, BubbleColor.Cherry),
            bridge to Bubble(bridge, BubbleColor.Cherry),
            GridPosition(0, 1) to Bubble(GridPosition(0, 1), BubbleColor.Cherry),
            floating to Bubble(floating, BubbleColor.Sky),
            topRight to Bubble(topRight, BubbleColor.Mint),
        )

        val result = MatchDetector.removeMatchesAndFloating(bridge, board)

        assertEquals(setOf(floating), result.dropped)
        assertEquals(setOf(topRight), result.board.keys)
    }
}
