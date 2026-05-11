package com.brian.bubbleshooterclassicpop.game

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object GridMath {
    const val LOGICAL_WIDTH = 360f
    const val LOGICAL_HEIGHT = 640f
    const val BUBBLE_RADIUS = 15f
    const val BUBBLE_DIAMETER = BUBBLE_RADIUS * 2f
    const val TOP_PADDING = 34f
    const val MAX_ROWS = 18
    const val SHOOT_SPEED = 720f
    const val DANGER_LINE_Y = 500f
    const val MIN_AIM_DEGREES = 12f
    const val MAX_AIM_DEGREES = 168f

    val rowSpacing: Float = (BUBBLE_RADIUS * sqrt(3f))
    val shooterPosition: Vec2 = Vec2(LOGICAL_WIDTH / 2f, LOGICAL_HEIGHT - 48f)

    fun columnsForRow(row: Int): Int = if (row % 2 == 0) 12 else 11

    fun isValidCell(position: GridPosition): Boolean =
        position.row in 0 until MAX_ROWS && position.col in 0 until columnsForRow(position.row)

    fun cellToCenter(position: GridPosition): Vec2 {
        require(isValidCell(position)) { "Invalid grid position: $position" }
        val offset = if (position.row % 2 == 0) 0f else BUBBLE_RADIUS
        return Vec2(
            x = BUBBLE_RADIUS + offset + position.col * BUBBLE_DIAMETER,
            y = TOP_PADDING + BUBBLE_RADIUS + position.row * rowSpacing,
        )
    }

    fun nearestCell(point: Vec2): GridPosition {
        val approximateRow = ((point.y - TOP_PADDING - BUBBLE_RADIUS) / rowSpacing).roundToInt()
        val rowsToCheck = ((approximateRow - 2)..(approximateRow + 2)).filter { it in 0 until MAX_ROWS }
        return rowsToCheck
            .flatMap { row -> (0 until columnsForRow(row)).map { col -> GridPosition(row, col) } }
            .minBy { cellToCenter(it).distanceTo(point) }
    }

    fun nearestAttachableCell(
        point: Vec2,
        board: Map<GridPosition, Bubble>,
    ): GridPosition? {
        val occupied = board.keys
        return (0 until MAX_ROWS)
            .flatMap { row -> (0 until columnsForRow(row)).map { col -> GridPosition(row, col) } }
            .asSequence()
            .filterNot { it in occupied }
            .filter { it.row == 0 || neighborsOf(it).any { neighbor -> neighbor in occupied } }
            .minByOrNull { cellToCenter(it).distanceTo(point) }
    }

    fun neighborsOf(position: GridPosition): List<GridPosition> {
        val row = position.row
        val col = position.col
        val diagonalCols = if (row % 2 == 0) {
            listOf(col - 1, col)
        } else {
            listOf(col, col + 1)
        }

        return buildList {
            add(GridPosition(row, col - 1))
            add(GridPosition(row, col + 1))
            diagonalCols.forEach { diagonalCol ->
                add(GridPosition(row - 1, diagonalCol))
                add(GridPosition(row + 1, diagonalCol))
            }
        }.filter(::isValidCell)
    }

    fun aimAngleForTarget(target: Vec2): Float {
        val dx = target.x - shooterPosition.x
        val dy = shooterPosition.y - target.y
        val rawAngle = if (dy <= 0f) {
            if (dx < 0f) MAX_AIM_DEGREES else MIN_AIM_DEGREES
        } else {
            (atan2(dy, dx) * 180f / PI.toFloat())
        }
        return rawAngle.coerceIn(MIN_AIM_DEGREES, MAX_AIM_DEGREES)
    }

    fun velocityForAngle(angleDegrees: Float): Vec2 {
        val radians = angleDegrees * PI.toFloat() / 180f
        return Vec2(
            x = cos(radians) * SHOOT_SPEED,
            y = -sin(radians) * SHOOT_SPEED,
        )
    }

    fun isBeyondDangerLine(board: Map<GridPosition, Bubble>): Boolean =
        board.keys.any { cellToCenter(it).y + BUBBLE_RADIUS >= DANGER_LINE_Y }

    fun scaleFor(canvasWidth: Float, canvasHeight: Float): Float =
        min(canvasWidth / LOGICAL_WIDTH, canvasHeight / LOGICAL_HEIGHT)
}
