package com.mkz.pixelart

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int

/** A command that draws on one layer. */
abstract class DrawCommand(
    name: String,
) : EditCommand(name) {
    val layerName by option("--layer", "-l", help = "The layer to draw on.").required()

    abstract fun draw(pixels: Pixels): String

    override fun change(sprite: Sprite): Change {
        val description = draw(sprite.layer(layerName).pixels)
        return Change("$description on layer '$layerName'", layerName)
    }

    protected fun pixels(changed: Int): String = "$changed px changed"
}

fun drawCommands() = listOf(SetCommand(), PaintCommand(), FillCommand(), RectCommand(), LineCommand(), ReplaceCommand(), FlipCommand(), MirrorCommand(), ShiftCommand(), CopyCommand())

class SetCommand : DrawCommand("set") {
    override fun help(context: Context) = "Sets single pixels, e.g. 'pixel set FILE --layer base 3,4=W 5,4=#151515 6,4=.'."

    private val colours by argument("X,Y=COLOUR", help = "A pixel and its colour.")
        .convert { value ->
            val colour = value.substringAfter("=", "")
            if (colour.isEmpty()) throw PixelArtException("'$value' is not a pixel and its colour like 3,4=W.")
            Point.parse(value.substringBefore("=")) to Palette.parse(colour)
        }.multiple(required = true)

    override fun draw(pixels: Pixels): String = "Set ${colours.size} px, ${pixels(Drawing.set(pixels, colours.toMap()))}"
}

class PaintCommand : DrawCommand("paint") {
    override fun help(context: Context) =
        """
        Paints a grid of colour characters, its top-left character at --at.

        '.' paints transparent and '-' leaves a pixel as it is, so only the drawn shape changes. Rows can have different lengths. The whitespace around each row and blank lines are ignored.

        The grid is read from GRID, or the standard input when it is '-' or not given, e.g.:

        ```
        pixel paint FILE --layer outline --at 4,2 <<'GRID'
        -WWW-
        W---W
        -WWW-
        GRID
        ```
        """.trimIndent()

    private val at by option("--at", help = "X,Y of the top-left character of the grid.").convert { Point.parse(it) }.required()
    private val grid by argument("GRID", help = "The file holding the grid (default: the standard input).").file().optional()

    override fun draw(pixels: Pixels): String {
        val cells = Grid.parse(workspace.readGrid(grid))
        return "Painted a ${cells.maxOf { it.size }}x${cells.size} grid at ${at.x},${at.y}, ${pixels(Drawing.paint(pixels, at, cells))}"
    }
}

class FillCommand : DrawCommand("fill") {
    override fun help(context: Context) = "Flood fills the area of X,Y's colour that X,Y belongs to, through the pixels next to each other."

    private val at by argument("X,Y", help = "A pixel of the area.").convert { Point.parse(it) }
    private val colour by argument("COLOUR", help = "The colour to fill with.").convert { Palette.parse(it) }
    private val diagonal by option("--diagonal", help = "Also go through the pixels next to each other diagonally.").flag()

    override fun draw(pixels: Pixels): String = "Filled from ${at.x},${at.y}, ${pixels(Drawing.fill(pixels, at, colour, diagonal))}"
}

class RectCommand : DrawCommand("rect") {
    override fun help(context: Context) = "Draws the border of a rectangle, or the whole rectangle with --filled."

    private val region by argument("REGION", help = "X,Y,WIDTHxHEIGHT of the rectangle.").convert { Region.parse(it) }
    private val colour by argument("COLOUR", help = "Its colour.").convert { Palette.parse(it) }
    private val filled by option("--filled", help = "Fill it.").flag()

    override fun draw(pixels: Pixels): String = "Drew ${if (filled) "a filled" else "the border of a"} rectangle at $region, ${pixels(Drawing.rect(pixels, region, colour, filled))}"
}

class LineCommand : DrawCommand("line") {
    override fun help(context: Context) = "Draws a line from X1,Y1 to X2,Y2, both included, one pixel thick."

    private val from by argument("X1,Y1", help = "Where it starts.").convert { Point.parse(it) }
    private val to by argument("X2,Y2", help = "Where it ends.").convert { Point.parse(it) }
    private val colour by argument("COLOUR", help = "Its colour.").convert { Palette.parse(it) }

    override fun draw(pixels: Pixels): String = "Drew a line from ${from.x},${from.y} to ${to.x},${to.y}, ${pixels(Drawing.line(pixels, from, to, colour))}"
}

class ReplaceCommand : DrawCommand("replace") {
    override fun help(context: Context) = "Replaces every pixel of a colour by another one, e.g. to recolour a shade."

    private val from by argument("FROM", help = "The colour to replace.").convert { Palette.parse(it) }
    private val to by argument("TO", help = "Its replacement.").convert { Palette.parse(it) }
    private val region by option("--region", help = "Only in this region, X,Y,WIDTHxHEIGHT (default: the whole layer).").convert { Region.parse(it) }

    override fun draw(pixels: Pixels): String = "Replaced ${Palette.char(from)} by ${Palette.char(to)}, ${pixels(Drawing.replace(pixels, region ?: Region.whole(pixels), from, to))}"
}

class FlipCommand : DrawCommand("flip") {
    override fun help(context: Context) = "Flips a layer, or a region: horizontal swaps its left and right, vertical its top and bottom."

    private val direction by argument("DIRECTION", help = "horizontal or vertical.").choice("horizontal" to Drawing.Direction.HORIZONTAL, "vertical" to Drawing.Direction.VERTICAL)
    private val region by option("--region", help = "Only this region, X,Y,WIDTHxHEIGHT (default: the whole layer).").convert { Region.parse(it) }

    override fun draw(pixels: Pixels): String = "Flipped ${direction.name.lowercase()}ly, ${pixels(Drawing.flip(pixels, region ?: Region.whole(pixels), direction))}"
}

class MirrorCommand : DrawCommand("mirror") {
    override fun help(context: Context) =
        "Copies a half of a layer, or of a region, onto the other half, mirrored, so it is symmetric. E.g. draw the left half of a front facing character, then mirror it from the left. On an odd width, the middle column stays as it is."

    private val from by option("--from", help = "The half to copy: left, right, top or bottom.")
        .choice("left" to Drawing.Half.LEFT, "right" to Drawing.Half.RIGHT, "top" to Drawing.Half.TOP, "bottom" to Drawing.Half.BOTTOM)
        .required()
    private val region by option("--region", help = "Only this region, X,Y,WIDTHxHEIGHT (default: the whole layer).").convert { Region.parse(it) }

    override fun draw(pixels: Pixels): String = "Mirrored from the ${from.name.lowercase()}, ${pixels(Drawing.mirror(pixels, region ?: Region.whole(pixels), from))}"
}

class ShiftCommand : DrawCommand("shift") {
    override fun help(context: Context) = "Moves every pixel of a layer by --dx and --dy, e.g. '--dx -1' moves it one pixel left. Pixels moved out of the sprite are lost, unless --wrap."

    private val dx by option("--dx", help = "Pixels to the right (negative: to the left).").int().default(0)
    private val dy by option("--dy", help = "Pixels down (negative: up).").int().default(0)
    private val wrap by option("--wrap", help = "Bring the pixels moved out of the sprite in from the other side.").flag()

    override fun draw(pixels: Pixels): String = "Shifted by $dx,$dy, ${pixels(Drawing.shift(pixels, dx, dy, wrap))}"
}

class CopyCommand : DrawCommand("copy") {
    override fun help(context: Context) = "Copies a region onto --layer at --at. The region is read from --from-layer (default: --layer) of --from (default: FILE), before anything is drawn."

    private val region by option("--region", help = "X,Y,WIDTHxHEIGHT of the region to copy.").convert { Region.parse(it) }.required()
    private val at by option("--at", help = "X,Y where its top-left pixel goes.").convert { Point.parse(it) }.required()
    private val fromLayer by option("--from-layer", help = "The layer to copy from (default: --layer).")
    private val from by option("--from", help = "The .aseprite file to copy from (default: FILE).").file()
    private val skipTransparent by option("--skip-transparent", help = "Leave the pixels under the transparent ones as they are.").flag()

    private lateinit var source: Pixels

    override fun change(sprite: Sprite): Change {
        val sourceSprite = from?.let { SpriteFiles.readSprite(it) } ?: sprite
        source = sourceSprite.layer(fromLayer ?: layerName).pixels.copy()
        return super.change(sprite)
    }

    override fun draw(pixels: Pixels): String {
        val origin = listOfNotNull(from?.path, fromLayer?.let { "layer '$it'" }).joinToString(", ").ifEmpty { "itself" }
        return "Copied $region of $origin to ${at.x},${at.y}, ${pixels(Drawing.copy(source, region, pixels, at, skipTransparent))}"
    }
}
