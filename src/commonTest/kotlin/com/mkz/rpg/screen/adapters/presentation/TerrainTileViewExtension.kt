package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap

// Page object of [TerrainTileView].

/** The terrain image drawn, or the transition tile. */
fun TerrainTileView.drawnBitmap(): Bitmap = terrainImage.bitmap.base

/** Where the terrain is drawn inside the tile, and its size. */
fun TerrainTileView.placement(): TilePlacement = TilePlacement(terrainImage.x, terrainImage.y, terrainImage.scaledWidth, terrainImage.scaledHeight)

fun TerrainTileView.layerCount(): Int = children.size

fun TerrainTileView.isSmoothed(): Boolean = terrainImage.smoothing
