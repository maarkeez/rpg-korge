package com.mkz.pixelart

import kotlin.math.pow

/**
 * The FamiCube palette, in the order of `resources/famicube-palette.png`.
 *
 * Pixels are stored as palette indexes: 0 is transparent and 1 to 64 are the colours. Every colour has a fixed
 * character, the same in every file, so grids can be read, written and pasted between sprites.
 */
object Palette {
    const val TRANSPARENT = 0
    const val TRANSPARENT_CHAR = '.'

    /** A grid character that leaves a pixel as it is, when painting. */
    const val KEEP_CHAR = '-'

    val colours: List<Int> =
        listOf(
            0x000000,
            0x00177d,
            0x024aca,
            0x0084ff,
            0x5ba8ff,
            0x98dcff,
            0x9ba0ef,
            0x6264dc,
            0x3d34a5,
            0x211640,
            0x5a1991,
            0x6a31ca,
            0xa675fe,
            0xe2c9ff,
            0xfec9ed,
            0xd59cfc,
            0xcc69e4,
            0xa328b3,
            0x871646,
            0xcf3c71,
            0xff82ce,
            0xffe9c5,
            0xf5b784,
            0xe18289,
            0xda655e,
            0x823c3d,
            0x4f1507,
            0xe03c28,
            0xe2d7b5,
            0xc59782,
            0xae6c37,
            0x5c3c0d,
            0x231712,
            0xad4e1a,
            0xf68f37,
            0xffe737,
            0xffbb31,
            0xcc8f15,
            0x939717,
            0xb6c121,
            0xeeffa9,
            0xbeeb71,
            0x8cd612,
            0x6ab417,
            0x376d03,
            0x172808,
            0x004e00,
            0x139d08,
            0x58d332,
            0x20b562,
            0x00604b,
            0x005280,
            0x0a98ac,
            0x25e2cd,
            0xbdffca,
            0x71a6a1,
            0x415d66,
            0x0d2030,
            0x151515,
            0x343434,
            0x7b7b7b,
            0xa8a8a8,
            0xd7d7d7,
            0xffffff,
        )

    private const val CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+="

    /** The number of palette entries, transparent included. */
    val size: Int = colours.size + 1

    private val indexByRgb: Map<Int, Int> = colours.withIndex().associate { (position, rgb) -> rgb to position + 1 }

    fun char(index: Int): Char = if (index == TRANSPARENT) TRANSPARENT_CHAR else CHARS[index - 1]

    fun rgb(index: Int): Int? = if (index == TRANSPARENT) null else colours[index - 1]

    /** The hex of a palette index, or `transparent`. */
    fun describe(index: Int): String = rgb(index)?.let(::hex) ?: "transparent"

    fun hex(rgb: Int): String = "#%06x".format(rgb)

    fun indexOfChar(char: Char): Int? =
        when (char) {
            TRANSPARENT_CHAR -> TRANSPARENT
            else -> CHARS.indexOf(char).takeIf { it >= 0 }?.plus(1)
        }

    fun indexOfRgb(rgb: Int): Int? = indexByRgb[rgb and 0xffffff]

    /** Reads a colour given as its character, its hex (e.g.: `#151515`), or `.` for transparent. */
    fun parse(colour: String): Int {
        if (colour.length == 1) {
            return indexOfChar(colour[0]) ?: throw PixelArtException("'$colour' is not the character of a colour. Run 'pixel palette' to list them.")
        }
        val hex = colour.removePrefix("#")
        val rgb = hex.takeIf { it.length == 6 }?.toIntOrNull(16) ?: throw PixelArtException("'$colour' is neither a colour character nor a hex colour like #151515.")
        return indexOfRgb(rgb) ?: throw PixelArtException("$colour is not a FamiCube colour, its closest one is ${describe(nearest(rgb))} '${char(nearest(rgb))}'.")
    }

    /** The colour that looks the closest to [rgb], comparing them in the CIELAB colour space. */
    fun nearest(rgb: Int): Int {
        val lab = lab(rgb)
        return (1 until size).minBy { index ->
            val other = lab(colours[index - 1])
            lab.indices.sumOf { (lab[it] - other[it]).pow(2) }
        }
    }

    private fun lab(rgb: Int): DoubleArray {
        fun linear(channel: Int): Double {
            val value = channel / 255.0
            return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
        }

        fun f(value: Double): Double = if (value > 0.008856) value.pow(1.0 / 3) else 7.787 * value + 16.0 / 116

        val r = linear((rgb shr 16) and 0xff)
        val g = linear((rgb shr 8) and 0xff)
        val b = linear(rgb and 0xff)
        val x = f((0.4124 * r + 0.3576 * g + 0.1805 * b) / 0.95047)
        val y = f(0.2126 * r + 0.7152 * g + 0.0722 * b)
        val z = f((0.0193 * r + 0.1192 * g + 0.9505 * b) / 1.08883)
        return doubleArrayOf(116 * y - 16, 500 * (x - y), 200 * (y - z))
    }
}

/** A failure explained to the agent, printed without a stack trace. */
class PixelArtException(
    message: String,
) : RuntimeException(message)
