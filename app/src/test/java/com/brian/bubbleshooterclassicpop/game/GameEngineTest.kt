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
    fun canOpenLevelSelectAndStartChosenLevel() {
        val engine = GameEngine(Random(1))

        engine.showLevelSelect()
        assertEquals(GamePhase.LevelSelect, engine.state.phase)

        engine.start(level = 12)
        assertEquals(GamePhase.Running, engine.state.phase)
        assertEquals(12, engine.state.level)
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

    @Test
    fun attachingThirdMatchingBubbleUpdatesScoreAndWinsWhenBoardClears() {
        val placed = GridPosition(1, 0)
        val engine = GameEngine(
            random = Random(1),
            initialState = GameState(
                phase = GamePhase.Running,
                board = bubbleMap(
                    GridPosition(0, 0) to BubbleColor.Mint,
                    GridPosition(0, 1) to BubbleColor.Mint,
                ),
                currentBubble = BubbleColor.Mint,
                nextBubble = BubbleColor.Sky,
                flyingBubble = FlyingBubble(
                    position = collisionPointFor(placed),
                    velocity = Vec2(0f, 0f),
                    color = BubbleColor.Mint,
                ),
                shotsRemaining = 4,
            ),
        )

        engine.tick(1f / 120f)

        assertEquals(GamePhase.Won, engine.state.phase)
        assertEquals(30, engine.state.score)
        assertEquals(3, engine.state.lastPopped)
        assertEquals(0, engine.state.lastDropped)
        assertTrue(engine.state.board.isEmpty())
        assertEquals(BubbleColor.Sky, engine.state.currentBubble)
    }

    @Test
    fun droppedFloatingBubblesAddDropScore() {
        val placed = GridPosition(1, 0)
        val floating = GridPosition(2, 0)
        val survivor = GridPosition(0, 3)
        val engine = GameEngine(
            random = Random(1),
            initialState = GameState(
                phase = GamePhase.Running,
                board = bubbleMap(
                    GridPosition(0, 0) to BubbleColor.Cherry,
                    GridPosition(0, 1) to BubbleColor.Cherry,
                    floating to BubbleColor.Sky,
                    survivor to BubbleColor.Grape,
                ),
                currentBubble = BubbleColor.Cherry,
                nextBubble = BubbleColor.Sun,
                flyingBubble = FlyingBubble(
                    position = collisionPointFor(placed),
                    velocity = Vec2(0f, 0f),
                    color = BubbleColor.Cherry,
                ),
                shotsRemaining = 4,
            ),
        )

        engine.tick(1f / 120f)

        assertEquals(GamePhase.Running, engine.state.phase)
        assertEquals(50, engine.state.score)
        assertEquals(3, engine.state.lastPopped)
        assertEquals(1, engine.state.lastDropped)
        assertEquals(setOf(survivor), engine.state.board.keys)
    }

    @Test
    fun comboBonusRewardsConsecutiveClears() {
        val placed = GridPosition(1, 0)
        val engine = GameEngine(
            random = Random(1),
            initialState = GameState(
                phase = GamePhase.Running,
                board = bubbleMap(
                    GridPosition(0, 0) to BubbleColor.Mint,
                    GridPosition(0, 1) to BubbleColor.Mint,
                ),
                currentBubble = BubbleColor.Mint,
                nextBubble = BubbleColor.Sky,
                flyingBubble = FlyingBubble(
                    position = collisionPointFor(placed),
                    velocity = Vec2(0f, 0f),
                    color = BubbleColor.Mint,
                ),
                shotsRemaining = 4,
                comboStreak = 1,
            ),
        )

        engine.tick(1f / 120f)

        assertEquals(2, engine.state.comboStreak)
        assertEquals(GameEngine.COMBO_SCORE, engine.state.lastComboBonus)
        assertEquals(30 + GameEngine.COMBO_SCORE, engine.state.score)
        assertEquals(3, engine.state.lastClearedPositions.size)
    }

    @Test
    fun missResetsComboAndIncrementsMissStreak() {
        val placed = GridPosition(1, 0)
        val engine = GameEngine(
            random = Random(1),
            initialState = GameState(
                phase = GamePhase.Running,
                board = bubbleMap(GridPosition(0, 0) to BubbleColor.Cherry),
                currentBubble = BubbleColor.Sky,
                nextBubble = BubbleColor.Sun,
                flyingBubble = FlyingBubble(
                    position = collisionPointFor(placed),
                    velocity = Vec2(0f, 0f),
                    color = BubbleColor.Sky,
                ),
                shotsRemaining = 4,
                comboStreak = 2,
            ),
        )

        engine.tick(1f / 120f)

        assertEquals(GamePhase.Running, engine.state.phase)
        assertEquals(0, engine.state.comboStreak)
        assertEquals(1, engine.state.missStreak)
        assertEquals(0, engine.state.lastComboBonus)
        assertEquals(emptyList<GridPosition>(), engine.state.lastClearedPositions)
    }

    @Test
    fun nonClearingAttachmentWithNoShotsRemainingLoses() {
        val placed = GridPosition(1, 0)
        val engine = GameEngine(
            random = Random(1),
            initialState = GameState(
                phase = GamePhase.Running,
                board = bubbleMap(GridPosition(0, 0) to BubbleColor.Cherry),
                currentBubble = BubbleColor.Sky,
                nextBubble = BubbleColor.Sun,
                flyingBubble = FlyingBubble(
                    position = collisionPointFor(placed),
                    velocity = Vec2(0f, 0f),
                    color = BubbleColor.Sky,
                ),
                shotsRemaining = 0,
            ),
        )

        engine.tick(1f / 120f)

        assertEquals(GamePhase.Lost, engine.state.phase)
        assertEquals(0, engine.state.score)
        assertTrue(engine.state.board.isNotEmpty())
    }

    @Test
    fun pausePreventsShootingUntilResumed() {
        val engine = GameEngine(Random(1))
        engine.start(level = 1)
        engine.togglePause()

        engine.shoot()

        assertEquals(GamePhase.Paused, engine.state.phase)
        assertEquals(null, engine.state.flyingBubble)

        engine.togglePause()
        engine.shoot()

        assertEquals(GamePhase.Running, engine.state.phase)
        assertTrue(engine.state.flyingBubble != null)
    }

    private fun bubbleMap(vararg bubbles: Pair<GridPosition, BubbleColor>): Map<GridPosition, Bubble> =
        bubbles.associate { (position, color) -> position to Bubble(position, color) }

    private fun collisionPointFor(position: GridPosition): Vec2 =
        GridMath.cellToCenter(position).let { center -> center.copy(y = center.y - 2f) }
}
