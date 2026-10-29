package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors

/**
 * A battle unit blinking in white, e.g.: when it receives an effect: the silhouette of its image over its tile. The
 * battlefield view draws it and removes it again to make it blink.
 */
class TileFlashView(
    unitBitmap: Bitmap,
) : UnitSpriteView(whiteSilhouette(unitBitmap)) {
    private companion object {
        fun whiteSilhouette(bitmap: Bitmap): Bitmap {
            val silhouette = Bitmap32(bitmap.width, bitmap.height, premultiplied = bitmap.premultiplied)
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    if (bitmap.getRgba(x, y).a > 0) silhouette.setRgba(x, y, Colors.WHITE)
                }
            }
            return silhouette
        }
    }
}
