package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

/**
 * The image of a battle unit, or of an npc standing as one, on its tile of the battlefield: scaled to fill the tile
 * whatever the size of its art (16x16 or 32x32), at a whole scale so it stays sharp.
 */
open class UnitSpriteView(
    bitmap: Bitmap,
) : Container() {
    internal val spriteImage: Image =
        image(bitmap) {
            scale = BattlefieldView.tileFillingScale(bitmap)
            smoothing = false
        }
}
