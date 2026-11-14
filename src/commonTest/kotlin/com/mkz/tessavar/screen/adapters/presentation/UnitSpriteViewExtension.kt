package com.mkz.tessavar.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap

fun UnitSpriteView.drawnBitmap(): Bitmap = spriteImage.bitmap.base

fun UnitSpriteView.placement(): TilePlacement = TilePlacement(spriteImage.x, spriteImage.y, spriteImage.scaledWidth, spriteImage.scaledHeight)

fun UnitSpriteView.scale(): Double = spriteImage.scale

fun UnitSpriteView.isSmoothed(): Boolean = spriteImage.smoothing
