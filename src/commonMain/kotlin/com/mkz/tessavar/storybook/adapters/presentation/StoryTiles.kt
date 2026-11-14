package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.TerrainBitmaps
import com.mkz.tessavar.screen.adapters.presentation.TerrainTileView
import com.mkz.tessavar.screen.adapters.presentation.UnitSpriteView
import korlibs.korge.view.Container
import korlibs.korge.view.View

internal object StoryTiles {
    const val TERRAIN = "terrain"

    const val NONE = "none"

    fun tile(
        terrainId: String,
        context: StoryContext,
        vararg layers: View?,
    ): Container =
        Container().apply {
            addChild(terrainTile(terrainId, context))
            layers.filterNotNull().forEach(::addChild)
        }

    fun terrainTile(
        terrainId: String,
        context: StoryContext,
    ): TerrainTileView =
        TerrainTileView(TerrainBitmaps(context.resourcesLoader)).apply {
            display(terrainId, transition = null)
        }

    fun unitSprite(
        unitId: String,
        context: StoryContext,
    ): UnitSpriteView? = context.resourcesLoader.bitmapOrNull("unit/$unitId.png")?.let(::UnitSpriteView)
}
