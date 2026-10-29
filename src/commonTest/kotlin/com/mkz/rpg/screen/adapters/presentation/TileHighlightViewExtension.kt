package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap

// Page object of [TileHighlightView].

/** The frame image drawn. */
fun TileHighlightView.drawnBitmap(): Bitmap = frameImage.bitmap.base

/** Where the frame is drawn inside the tile, and its size. */
fun TileHighlightView.placement(): TilePlacement = TilePlacement(frameImage.x, frameImage.y, frameImage.scaledWidth, frameImage.scaledHeight)
