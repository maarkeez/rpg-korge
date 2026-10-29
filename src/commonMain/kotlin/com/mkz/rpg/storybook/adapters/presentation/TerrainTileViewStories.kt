package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.TerrainBitmaps
import com.mkz.rpg.screen.adapters.presentation.TerrainTileView
import com.mkz.rpg.screen.adapters.presentation.TerrainTransition
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [TerrainTileView], at the size the battlefield displays it: a tile of 64 pixels. */
object TerrainTileViewStories : StoryBinding {
    private const val TRANSITION_NAME_SEPARATOR = "_to_"

    // Each strip of transitions holds 16 tiles, the first one the pure terrain and the last one the pure target terrain.
    private const val MAXIMUM_WANG_INDEX = 15

    private const val DEFAULT_TERRAIN = "sand"
    private const val TERRAIN = StoryTiles.TERRAIN
    private const val TRANSITION = "transition"
    private const val WANG_INDEX = "wangIndex"

    private val sand =
        StoryDto(id = "screen-terrain-tile--sand", name = "Sand", description = "A pure terrain tile: the terrain fills the whole tile.", args = emptyMap())
    private val water =
        StoryDto(id = "screen-terrain-tile--water", name = "Water", description = "Another terrain: any terrain with a tile can be picked.", args = mapOf(TERRAIN to ArgDto.text("water")))
    private val sandToGrass =
        StoryDto(
            id = "screen-terrain-tile--sand-to-grass",
            name = "Sand to grass",
            description = "A tile of sand next to grass: its transition draws the grass over the edges and corners that touch it.",
            args = mapOf(TRANSITION to ArgDto.text("sand_to_grass"), WANG_INDEX to ArgDto.int(9)),
        )
    private val sandToVoid =
        StoryDto(
            id = "screen-terrain-tile--sand-to-void",
            name = "Sand to void",
            description = "A tile of sand on the edge of the map: the void, where nothing walks, takes one corner.",
            args = mapOf(TRANSITION to ArgDto.text("sand_to_void"), WANG_INDEX to ArgDto.int(3)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-terrain-tile",
            group = "Screen",
            title = "Terrain tile",
            description =
                "The terrain of a tile of the battlefield, the bottom layer of the tile. " +
                    "transition: the strip of tiles from a terrain to another, or none. wangIndex: which tile of the strip, by the edges and corners that touch the target terrain.",
            controls =
                listOf(
                    ControlDto.pick(TERRAIN, context.terrainIds()),
                    ControlDto.pick(TRANSITION, listOf(StoryTiles.NONE) + context.terrainTransitionIds()),
                    ControlDto.int(WANG_INDEX, minimum = 0, maximum = MAXIMUM_WANG_INDEX),
                ),
            args = mapOf(TERRAIN to ArgDto.text(DEFAULT_TERRAIN), TRANSITION to ArgDto.text(StoryTiles.NONE), WANG_INDEX to ArgDto.int(0)),
            stories = listOf(sand, water, sandToGrass, sandToVoid),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val transition =
            args.text(TRANSITION).takeUnless { transitionId -> transitionId == StoryTiles.NONE }?.let { transitionId ->
                val (fromTerrainId, toTerrainId) = transitionId.split(TRANSITION_NAME_SEPARATOR)
                TerrainTransition(fromTerrainId = fromTerrainId, toTerrainId = toTerrainId, wangIndex = args.int(WANG_INDEX))
            }
        return TerrainTileView(TerrainBitmaps(context.resourcesLoader)).apply { display(terrainId = args.text(TERRAIN), transition = transition) }
    }
}
