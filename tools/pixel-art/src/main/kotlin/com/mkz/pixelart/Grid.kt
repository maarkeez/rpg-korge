package com.mkz.pixelart

data class Point(
    val x: Int,
    val y: Int,
) {
    companion object {
        fun parse(value: String): Point {
            val parts = value.split(",").map { it.trim().toIntOrNull() }
            if (parts.size != 2 || parts.any { it == null }) throw PixelArtException("'$value' is not a point like 3,4 (x,y).")
            return Point(parts[0]!!, parts[1]!!)
        }
    }
}

data class Size(
    val width: Int,
    val height: Int,
) {
    companion object {
        fun parse(value: String): Size {
            val parts = value.lowercase().split("x").map { it.trim().toIntOrNull() }
            if (parts.size != 2 || parts.any { it == null || it < 1 }) throw PixelArtException("'$value' is not a size like 16x16 (width x height).")
            return Size(parts[0]!!, parts[1]!!)
        }
    }
}

data class Region(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
) {
    val right: Int get() = x + width - 1
    val bottom: Int get() = y + height - 1

    fun points(): List<Point> = (y..bottom).flatMap { pointY -> (x..right).map { pointX -> Point(pointX, pointY) } }

    fun requireInside(pixels: Pixels) {
        if (x < 0 || y < 0 || right >= pixels.width || bottom >= pixels.height) {
            throw PixelArtException("Region $this is outside of the ${pixels.width}x${pixels.height} sprite.")
        }
    }

    override fun toString(): String = "$x,$y,${width}x$height"

    companion object {
        fun whole(pixels: Pixels): Region = Region(0, 0, pixels.width, pixels.height)

        fun parse(value: String): Region {
            val parts = value.split(",")
            if (parts.size != 3) throw PixelArtException("'$value' is not a region like 2,3,8x4 (x,y,width x height).")
            val point = Point.parse("${parts[0]},${parts[1]}")
            val size = Size.parse(parts[2])
            return Region(point.x, point.y, size.width, size.height)
        }
    }
}

/** Prints pixels as characters, one per pixel, and reads them back. */
object Grid {
    /** The grid alone, as [parse] reads it. */
    fun plain(
        pixels: Pixels,
        region: Region = Region.whole(pixels),
    ): String =
        (region.y..region.bottom).joinToString("\n") { y ->
            (region.x..region.right).map { x -> Palette.char(pixels[x, y]) }.joinToString("")
        }

    /**
     * The grid, with the x coordinates above it, the y coordinates on its left, and the legend of its colours below it.
     * The pixels [shown] rejects are printed as [Palette.KEEP_CHAR] and left out of the legend.
     */
    fun annotated(
        pixels: Pixels,
        region: Region = Region.whole(pixels),
        title: String,
        shown: (Point) -> Boolean = { true },
    ): String {
        fun char(point: Point): Char = if (shown(point)) Palette.char(pixels[point.x, point.y]) else Palette.KEEP_CHAR

        val margin = region.bottom.toString().length + 1
        val columns = region.x..region.right
        val xAxis =
            if (region.right >= 10) {
                listOf(
                    " ".repeat(margin) + columns.map { if (it >= 10) (it / 10 % 10).digitToChar() else ' ' }.joinToString(""),
                    " ".repeat(margin) + columns.map { (it % 10).digitToChar() }.joinToString(""),
                )
            } else {
                listOf(" ".repeat(margin) + columns.map { it.digitToChar() }.joinToString(""))
            }
        val rows =
            (region.y..region.bottom).map { y ->
                y.toString().padStart(margin - 1) + " " + columns.map { x -> char(Point(x, y)) }.joinToString("")
            }
        val counts =
            region
                .points()
                .filter(shown)
                .map { pixels[it.x, it.y] }
                .groupingBy { it }
                .eachCount()
        val legend = legend(counts)
        return (listOf(title) + xAxis + rows + listOf("") + legend).joinToString("\n")
    }

    fun legend(counts: Map<Int, Int>): List<String> =
        listOf("legend:") +
            counts.keys.sorted().map { index ->
                val count = counts.getValue(index)
                "  ${Palette.char(index)} ${Palette.describe(index)} ($count px)"
            }

    /**
     * Reads a grid of colour characters, where [Palette.KEEP_CHAR] leaves a pixel as it is (`null`). Blank lines and
     * the whitespace around each line are ignored.
     */
    fun parse(text: String): List<List<Int?>> {
        val rows = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (rows.isEmpty()) throw PixelArtException("The grid is empty.")
        return rows.mapIndexed { row, line ->
            line.mapIndexed { column, char ->
                if (char == Palette.KEEP_CHAR) {
                    null
                } else {
                    Palette.indexOfChar(char) ?: throw PixelArtException(
                        "Grid row $row, column $column: '$char' is not the character of a colour. Use '${Palette.TRANSPARENT_CHAR}' for transparent and '${Palette.KEEP_CHAR}' to keep a pixel. Run 'pixel palette' to list them.",
                    )
                }
            }
        }
    }
}
