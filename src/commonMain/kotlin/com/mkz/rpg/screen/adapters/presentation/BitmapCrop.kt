package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32

/** The part of the bitmap from ([x], [y]) that is [width] by [height] pixels, e.g.: a tile of a strip, or of a building. */
internal fun Bitmap.crop(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
): Bitmap {
    val croppedBitmap = Bitmap32(width, height, premultiplied = premultiplied)
    for (cropY in 0 until height) {
        for (cropX in 0 until width) {
            croppedBitmap.setRgba(cropX, cropY, getRgba(x + cropX, y + cropY))
        }
    }
    return croppedBitmap
}
