package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.TerrainBitmaps
import com.mkz.rpg.screen.adapters.presentation.TerrainTileView
import com.mkz.rpg.screen.adapters.presentation.UnitSpriteView
import korlibs.korge.view.Container
import korlibs.korge.view.View

/**
 * What the stories of the layers of a tile of the battlefield draw them on: the terrain of the tile, as in the game,
 * where they are drawn over it.
 */
internal object StoryTiles {
    /** The control choosing the terrain under the layer, and its arg. */
    const val TERRAIN = "terrain"

    /** The option of a pick control when none is meant, which no terrain nor unit is. */
    const val NONE = "none"

    /** A tile with the terrain, and the layers over it, from the bottom up. */
    fun tile(
        terrainId: String,
        context: StoryContext,
        vararg layers: View?,
    ): Container =
        Container().apply {
            addChild(terrainTile(terrainId, context))
            layers.filterNotNull().forEach(::addChild)
        }

    /** The pure terrain tile of the terrain. */
    fun terrainTile(
        terrainId: String,
        context: StoryContext,
    ): TerrainTileView =
        TerrainTileView(TerrainBitmaps(context.resourcesLoader)).apply {
            display(terrainId, transition = null)
        }

    /** The sprite of the unit, or `null` when it has no image. */
    fun unitSprite(
        unitId: String,
        context: StoryContext,
    ): UnitSpriteView? = context.resourcesLoader.bitmapOrNull("unit/$unitId.png")?.let(::UnitSpriteView)
}
