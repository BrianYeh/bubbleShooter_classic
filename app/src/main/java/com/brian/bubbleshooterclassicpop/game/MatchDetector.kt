package com.brian.bubbleshooterclassicpop.game

data class MatchResult(
    val board: Map<GridPosition, Bubble>,
    val popped: Set<GridPosition>,
    val dropped: Set<GridPosition>,
)

object MatchDetector {
    fun connectedSameColor(
        start: GridPosition,
        board: Map<GridPosition, Bubble>,
    ): Set<GridPosition> {
        val startBubble = board[start] ?: return emptySet()
        val visited = linkedSetOf<GridPosition>()
        val pending = ArrayDeque<GridPosition>()
        pending.add(start)

        while (pending.isNotEmpty()) {
            val current = pending.removeFirst()
            if (!visited.add(current)) continue

            GridMath.neighborsOf(current)
                .filter { neighbor -> board[neighbor]?.color == startBubble.color }
                .filterNot { neighbor -> neighbor in visited }
                .forEach(pending::add)
        }

        return visited
    }

    fun floatingBubbles(board: Map<GridPosition, Bubble>): Set<GridPosition> {
        val connectedToTop = linkedSetOf<GridPosition>()
        val pending = ArrayDeque<GridPosition>()
        board.keys.filter { it.row == 0 }.forEach(pending::add)

        while (pending.isNotEmpty()) {
            val current = pending.removeFirst()
            if (!connectedToTop.add(current)) continue

            GridMath.neighborsOf(current)
                .filter { neighbor -> neighbor in board }
                .filterNot { neighbor -> neighbor in connectedToTop }
                .forEach(pending::add)
        }

        return board.keys - connectedToTop
    }

    fun removeMatchesAndFloating(
        placed: GridPosition,
        board: Map<GridPosition, Bubble>,
    ): MatchResult {
        val matching = connectedSameColor(placed, board)
        if (matching.size < 3) {
            return MatchResult(board = board, popped = emptySet(), dropped = emptySet())
        }

        val afterPop = board - matching
        val dropped = floatingBubbles(afterPop)
        return MatchResult(
            board = afterPop - dropped,
            popped = matching,
            dropped = dropped,
        )
    }
}
