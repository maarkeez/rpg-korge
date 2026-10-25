package com.mkz.pixelart

import kotlin.math.abs

/** The drawing operations on the pixels of a layer. Each one returns how many pixels it changed. */
object Drawing {
    private fun Pixels.paint(
        x: Int,
        y: Int,
        index: Int,
    ): Int {
        if (this[x, y] == index) return 0
        this[x, y] = index
        return 1
    }

    private fun Pixels.requireInside(point: Point) {
        if (!contains(point.x, point.y)) throw PixelArtException("${point.x},${point.y} is outside of the ${width}x$height sprite.")
    }

    fun set(
        pixels: Pixels,
        colours: Map<Point, Int>,
    ): Int {
        colours.keys.forEach { pixels.requireInside(it) }
        return colours.entries.sumOf { (point, index) -> pixels.paint(point.x, point.y, index) }
    }

    /** Paints [grid] with its top-left pixel at [at]. `null` cells leave their pixel as it is. */
    fun paint(
        pixels: Pixels,
        at: Point,
        grid: List<List<Int?>>,
    ): Int {
        val width = grid.maxOf { it.size }
        Region(at.x, at.y, width, grid.size).requireInside(pixels)
        return grid.withIndex().sumOf { (row, cells) ->
            cells.withIndex().sumOf { (column, index) -> index?.let { pixels.paint(at.x + column, at.y + row, it) } ?: 0 }
        }
    }

    /** Fills the area of [at]'s colour that [at] belongs to, through the pixels next to each other (and diagonally, if [diagonal]). */
    fun fill(
        pixels: Pixels,
        at: Point,
        index: Int,
        diagonal: Boolean,
    ): Int {
        pixels.requireInside(at)
        val target = pixels[at.x, at.y]
        if (target == index) return 0
        val neighbours = if (diagonal) (-1..1).flatMap { dy -> (-1..1).map { dx -> dx to dy } } - (0 to 0) else listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
        val pending = ArrayDeque(listOf(at))
        var changed = 0
        while (pending.isNotEmpty()) {
            val point = pending.removeFirst()
            if (!pixels.contains(point.x, point.y) || pixels[point.x, point.y] != target) continue
            changed += pixels.paint(point.x, point.y, index)
            neighbours.forEach { (dx, dy) -> pending.addLast(Point(point.x + dx, point.y + dy)) }
        }
        return changed
    }

    fun rect(
        pixels: Pixels,
        region: Region,
        index: Int,
        filled: Boolean,
    ): Int {
        region.requireInside(pixels)
        return region.points().filter { filled || it.x == region.x || it.x == region.right || it.y == region.y || it.y == region.bottom }.sumOf { pixels.paint(it.x, it.y, index) }
    }

    /** A line between [start] and [end], both included, with Bresenham's algorithm. It is the same whichever end it starts from. */
    fun line(
        pixels: Pixels,
        start: Point,
        end: Point,
        index: Int,
    ): Int {
        pixels.requireInside(start)
        pixels.requireInside(end)
        val (from, to) = if (start.x < end.x || (start.x == end.x && start.y <= end.y)) start to end else end to start
        val dx = abs(to.x - from.x)
        val dy = -abs(to.y - from.y)
        val stepX = if (from.x < to.x) 1 else -1
        val stepY = if (from.y < to.y) 1 else -1
        var error = dx + dy
        var x = from.x
        var y = from.y
        var changed = pixels.paint(x, y, index)
        while (x != to.x || y != to.y) {
            val double = 2 * error
            if (double >= dy) {
                error += dy
                x += stepX
            }
            if (double <= dx) {
                error += dx
                y += stepY
            }
            changed += pixels.paint(x, y, index)
        }
        return changed
    }

    fun replace(
        pixels: Pixels,
        region: Region,
        from: Int,
        to: Int,
    ): Int {
        region.requireInside(pixels)
        return region.points().filter { pixels[it.x, it.y] == from }.sumOf { pixels.paint(it.x, it.y, to) }
    }

    enum class Direction { HORIZONTAL, VERTICAL }

    /** Flips the region: [Direction.HORIZONTAL] swaps its left and right, [Direction.VERTICAL] its top and bottom. */
    fun flip(
        pixels: Pixels,
        region: Region,
        direction: Direction,
    ): Int {
        region.requireInside(pixels)
        val source = pixels.copy()
        return region.points().sumOf { point ->
            val sourceX = if (direction == Direction.HORIZONTAL) region.x + region.right - point.x else point.x
            val sourceY = if (direction == Direction.VERTICAL) region.y + region.bottom - point.y else point.y
            pixels.paint(point.x, point.y, source[sourceX, sourceY])
        }
    }

    enum class Half { LEFT, RIGHT, TOP, BOTTOM }

    /** Copies the [from] half of the region onto the other half, mirrored, so the region is symmetric. */
    fun mirror(
        pixels: Pixels,
        region: Region,
        from: Half,
    ): Int {
        region.requireInside(pixels)
        val changed =
            region.points().filter { point ->
                when (from) {
                    Half.LEFT -> point.x - region.x >= (region.width + 1) / 2
                    Half.RIGHT -> point.x - region.x < region.width / 2
                    Half.TOP -> point.y - region.y >= (region.height + 1) / 2
                    Half.BOTTOM -> point.y - region.y < region.height / 2
                }
            }
        return changed.sumOf { point ->
            val sourceX = if (from == Half.LEFT || from == Half.RIGHT) region.x + region.right - point.x else point.x
            val sourceY = if (from == Half.TOP || from == Half.BOTTOM) region.y + region.bottom - point.y else point.y
            pixels.paint(point.x, point.y, pixels[sourceX, sourceY])
        }
    }

    /** Moves every pixel by [dx], [dy]. Pixels moved out of the sprite are lost, unless [wrap] brings them in from the other side. */
    fun shift(
        pixels: Pixels,
        dx: Int,
        dy: Int,
        wrap: Boolean,
    ): Int {
        val source = pixels.copy()
        return Region.whole(pixels).points().sumOf { point ->
            var sourceX = point.x - dx
            var sourceY = point.y - dy
            if (wrap) {
                sourceX = Math.floorMod(sourceX, pixels.width)
                sourceY = Math.floorMod(sourceY, pixels.height)
            }
            val index = if (source.contains(sourceX, sourceY)) source[sourceX, sourceY] else Palette.TRANSPARENT
            pixels.paint(point.x, point.y, index)
        }
    }

    /** Copies [region] of [source] to [at], leaving the pixels under transparent ones as they are when [skipTransparent]. */
    fun copy(
        source: Pixels,
        region: Region,
        target: Pixels,
        at: Point,
        skipTransparent: Boolean,
    ): Int {
        region.requireInside(source)
        Region(at.x, at.y, region.width, region.height).requireInside(target)
        val copied = source.copy()
        return region.points().sumOf { point ->
            val index = copied[point.x, point.y]
            if (skipTransparent && index == Palette.TRANSPARENT) 0 else target.paint(at.x + point.x - region.x, at.y + point.y - region.y, index)
        }
    }
}
