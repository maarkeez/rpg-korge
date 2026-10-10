package com.mkz.rpg.assets

import korlibs.image.bitmap.Bitmap32
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs

internal const val TILE_PIXEL_SIZE: Int = 16

internal const val UNIT_FRAME_SIZE: Int = 32

/** [width] x [height]; a strip of frames sets [frameWidth] and may hold any number of them. */
internal data class ArtSize(
    val width: Int,
    val height: Int,
    val frameWidth: Int? = null,
) {
    fun matches(
        actualWidth: Int,
        actualHeight: Int,
    ): Boolean =
        actualHeight == height &&
            if (frameWidth == null) actualWidth == width else actualWidth > 0 && actualWidth % frameWidth == 0

    override fun toString(): String = if (frameWidth == null) "${width}x$height" else "N x ${frameWidth}x$height"
}

// Exact frame or strip size per family, from §6.1 of docs/design/asset-redesign-plan.md.
// Null means the folder does not belong to any family yet: add the family to the plan before adding files there.
internal fun expectedArtSize(path: String): ArtSize? =
    when {
        path == "effect/fx_spread.png" -> ArtSize(width = 2 * TILE_PIXEL_SIZE, height = TILE_PIXEL_SIZE)
        path.startsWith("effect/fx_") -> ArtSize(width = 3 * TILE_PIXEL_SIZE, height = TILE_PIXEL_SIZE)
        path.startsWith("unit/") && (path.endsWith("_idle.png") || path.endsWith("_walk.png")) ->
            ArtSize(width = UNIT_FRAME_SIZE, height = UNIT_FRAME_SIZE, frameWidth = UNIT_FRAME_SIZE)
        path.startsWith("terrain/variants/") -> ArtSize(width = 3 * TILE_PIXEL_SIZE, height = TILE_PIXEL_SIZE)
        path.startsWith("terrain/transitions/") -> ArtSize(width = 16 * TILE_PIXEL_SIZE, height = TILE_PIXEL_SIZE)
        path.startsWith("unit/") && path.endsWith("_portrait.png") -> ArtSize(width = 32, height = 32)
        path.startsWith("unit/") -> ArtSize(width = UNIT_FRAME_SIZE, height = UNIT_FRAME_SIZE)
        path.startsWith("ability/") ||
            path.startsWith("battlefield/") ||
            path.startsWith("effect/") -> ArtSize(width = TILE_PIXEL_SIZE, height = TILE_PIXEL_SIZE)
        else -> null
    }

internal suspend fun loadBitmap32(path: String): Bitmap32 = resourcesVfs[path].readBitmap().toBMP32()

internal fun Bitmap32.tile(index: Int): Bitmap32 {
    val tile = Bitmap32(TILE_PIXEL_SIZE, TILE_PIXEL_SIZE)
    for (y in 0 until TILE_PIXEL_SIZE) {
        for (x in 0 until TILE_PIXEL_SIZE) {
            tile[x, y] = this[index * TILE_PIXEL_SIZE + x, y]
        }
    }
    return tile
}

internal fun Bitmap32.isPixelIdentical(other: Bitmap32): Boolean {
    if (width != other.width || height != other.height) return false
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (this[x, y].value != other[x, y].value) return false
        }
    }
    return true
}

internal fun Bitmap32.hasPartialAlpha(): Boolean {
    for (y in 0 until height) {
        for (x in 0 until width) {
            val alpha = this[x, y].a
            if (alpha != 0 && alpha != 255) return true
        }
    }
    return false
}

internal fun Bitmap32.isFullyOpaque(): Boolean {
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (this[x, y].a != 255) return false
        }
    }
    return true
}

/** Frame [index] of a horizontal strip of square frames of [size]. */
internal fun Bitmap32.frame(
    index: Int,
    size: Int,
): Bitmap32 {
    val frame = Bitmap32(size, size)
    for (y in 0 until size) {
        for (x in 0 until size) {
            frame[x, y] = this[index * size + x, y]
        }
    }
    return frame
}
