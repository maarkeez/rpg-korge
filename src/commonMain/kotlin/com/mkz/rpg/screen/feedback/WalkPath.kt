package com.mkz.rpg.screen.feedback

/** The tiles a unit visibly walks through. Used only for drawing, the domain decides where a unit may go. */
object WalkPath {
    const val MAX_HOPS = 12

    /**
     * Shortest walk from [from] to [to] (excluding [from], including [to]) over tiles for which [isWalkable] is true.
     * The destination is always allowed, because the unit already stands on it when the move is reported.
     * Falls back to a straight L-shaped walk (columns first) when no walkable route exists.
     */
    fun find(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
        isWalkable: (row: Int, column: Int) -> Boolean,
    ): List<Pair<Int, Int>> {
        if (from == to) return emptyList()
        val cameFrom = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
        val visited = mutableSetOf(from)
        val frontier = ArrayDeque<Pair<Int, Int>>().also { it.addLast(from) }
        while (frontier.isNotEmpty()) {
            val current = frontier.removeFirst()
            if (current == to) break
            for ((rowStep, columnStep) in STEPS) {
                val next = current.first + rowStep to current.second + columnStep
                if (next in visited) continue
                if (next != to && !isWalkable(next.first, next.second)) continue
                visited += next
                cameFrom[next] = current
                frontier.addLast(next)
            }
        }
        val path = if (to in cameFrom) rebuild(cameFrom, from, to) else straightPath(from, to)
        return if (path.size > MAX_HOPS) listOf(to) else path
    }

    private fun rebuild(
        cameFrom: Map<Pair<Int, Int>, Pair<Int, Int>>,
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
    ): List<Pair<Int, Int>> {
        val reversed = mutableListOf(to)
        var current = to
        while (cameFrom.getValue(current) != from) {
            current = cameFrom.getValue(current)
            reversed += current
        }
        return reversed.reversed()
    }

    private fun straightPath(
        from: Pair<Int, Int>,
        to: Pair<Int, Int>,
    ): List<Pair<Int, Int>> =
        buildList {
            var (row, column) = from
            while (column != to.second) {
                column += if (to.second > column) 1 else -1
                add(row to column)
            }
            while (row != to.first) {
                row += if (to.first > row) 1 else -1
                add(row to column)
            }
        }

    private val STEPS = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
}
