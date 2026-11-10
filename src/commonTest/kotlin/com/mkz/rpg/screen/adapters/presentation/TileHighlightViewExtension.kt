package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap

fun TileHighlightView.drawnBitmap(): Bitmap = frameImage.bitmap.base

fun TileHighlightView.placement(): TilePlacement = TilePlacement(frameImage.x, frameImage.y, frameImage.scaledWidth, frameImage.scaledHeight)
