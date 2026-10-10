package com.mkz.rpg.screen

import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.math.geom.Size

/** Procedural pixel-art glyphs drawn with palette colors, one [SolidRect] per art pixel. */
object PixelGlyphs {
    val MANA_DROP =
        listOf(
            "  #  ",
            " ### ",
            " ### ",
            "#####",
            "#####",
            "#####",
            " ### ",
        )

    val LOCK =
        listOf(
            "  ###  ",
            " #   # ",
            " #   # ",
            "#######",
            "#######",
            "### ###",
            "#######",
        )

    val SKULL = listOf("#####", "# # #", "#####", " ### ", " # # ")

    val TIMES = listOf("# #", " # ", "# #")

    private val DIGITS =
        listOf(
            listOf("###", "# #", "# #", "# #", "###"),
            listOf(" # ", "## ", " # ", " # ", "###"),
            listOf("###", "  #", "###", "#  ", "###"),
            listOf("###", "  #", "###", "  #", "###"),
            listOf("# #", "# #", "###", "  #", "  #"),
            listOf("###", "#  ", "###", "  #", "###"),
            listOf("###", "#  ", "###", "# #", "###"),
            listOf("###", "  #", "  #", " # ", " # "),
            listOf("###", "# #", "###", "# #", "###"),
            listOf("###", "# #", "###", "  #", "###"),
        )

    /** Draws [rows] (`#` is a filled art pixel) at [scale] points per art pixel. */
    fun glyph(
        rows: List<String>,
        color: RGBA,
        scale: Int,
        name: String? = null,
    ): Container =
        Container().also { container ->
            name?.let { container.name = it }
            rows.forEachIndexed { y, row ->
                row.forEachIndexed { x, char ->
                    if (char == '#') {
                        container.addChild(
                            SolidRect(Size(scale, scale), color).also {
                                it.x = (x * scale).toDouble()
                                it.y = (y * scale).toDouble()
                            },
                        )
                    }
                }
            }
        }

    /** A number in a 3x5 pixel font with a one-art-pixel dark shadow so it reads over any icon. */
    fun number(
        value: Int,
        color: RGBA,
        scale: Int,
        name: String,
    ): PixelNumberView = PixelNumberView(value.coerceIn(0, MAX_NUMBER), color, scale).also { it.name = name }

    class PixelNumberView(
        val value: Int,
        color: RGBA,
        scale: Int,
    ) : Container() {
        val pixelWidth: Int
        val pixelHeight: Int = 5 * scale

        init {
            val digits = value.toString().map { it - '0' }
            val rows = List(5) { row -> digits.joinToString(" ") { digit -> DIGITS[digit][row] } }
            pixelWidth = rows.first().length * scale
            addChild(
                glyph(rows, UiPalette.panel, scale).also {
                    it.x = scale.toDouble()
                    it.y = scale.toDouble()
                },
            )
            addChild(glyph(rows, color, scale))
        }
    }

    private const val MAX_NUMBER = 99
}
