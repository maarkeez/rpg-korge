package com.mkz.pixelart

import java.io.File
import kotlin.io.path.createTempDirectory

/** Pixels from grid rows, e.g. `pixelsOf("W..", ".W.")`. */
fun pixelsOf(vararg rows: String): Pixels {
    val grid = Grid.parse(rows.joinToString("\n"))
    val pixels = Pixels(grid[0].size, grid.size)
    grid.forEachIndexed { y, row -> row.forEachIndexed { x, index -> pixels[x, y] = index ?: Palette.TRANSPARENT } }
    return pixels
}

/** A sprite from its layers, given from the bottom one. */
fun spriteOf(vararg layers: Layer): Sprite = Sprite(layers[0].pixels.width, layers[0].pixels.height, layers.toMutableList())

fun layerOf(
    name: String,
    vararg rows: String,
    visible: Boolean = true,
    opacity: Int = 255,
): Layer = Layer(name, pixelsOf(*rows), visible, opacity)

/** A temporary folder holding an `art` folder, deleted after [block]. */
fun withProject(block: (File) -> Unit) {
    val root = createTempDirectory("pixel-art").toFile()
    try {
        File(root, "art").mkdirs()
        block(root)
    } finally {
        root.deleteRecursively()
    }
}

const val OUTLINE = 'W'
const val BLACK = '0'
