package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image

open class UnitSpriteView(
    bitmap: Bitmap,
) : Container() {
    internal val spriteImage: Image =
        image(bitmap) {
            scale = BattlefieldView.tileFillingScale(bitmap)
            smoothing = false
        }
}
