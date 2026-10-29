package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap

// Page object of [UnitSpriteView].

/** The image drawn. */
fun UnitSpriteView.drawnBitmap(): Bitmap = spriteImage.bitmap.base

/** Where the image is drawn inside the tile, and its size. */
fun UnitSpriteView.placement(): TilePlacement = TilePlacement(spriteImage.x, spriteImage.y, spriteImage.scaledWidth, spriteImage.scaledHeight)

/** How many times each pixel of the art is drawn, in both directions. */
fun UnitSpriteView.scale(): Double = spriteImage.scale

fun UnitSpriteView.isSmoothed(): Boolean = spriteImage.smoothing
