package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.bitmap.Bitmap

fun TerrainTileView.drawnBitmap(): Bitmap = terrainImage.bitmap.base

fun TerrainTileView.placement(): TilePlacement = TilePlacement(terrainImage.x, terrainImage.y, terrainImage.scaledWidth, terrainImage.scaledHeight)

fun TerrainTileView.layerCount(): Int = children.size

fun TerrainTileView.isSmoothed(): Boolean = terrainImage.smoothing
