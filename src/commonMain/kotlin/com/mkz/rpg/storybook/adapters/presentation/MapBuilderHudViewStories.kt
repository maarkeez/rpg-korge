package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattlefieldView
import com.mkz.rpg.screen.adapters.presentation.MapBuilderHudView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [MapBuilderHudView], with the tile of each terrain the game has. As in the map builder, a step changes
 * the size and tapping a terrain selects it, through the args.
 */
object MapBuilderHudViewStories : StoryBinding {
    // The sizes the map builder allows, which are in its domain.
    private const val MINIMUM_SIZE = 1
    private const val MAXIMUM_SIZE = 128
    private const val DEFAULT_TERRAIN = "grass"

    private const val ROWS = "rows"
    private const val COLUMNS = "columns"
    private const val SELECTED_TERRAIN = "selectedTerrain"

    private val default = StoryDto(id = "screen-map-builder-hud--default", name = "Default", description = "A new map: 16 x 16 tiles, with the grass selected.", args = emptyMap())
    private val smallestMap =
        StoryDto(
            id = "screen-map-builder-hud--smallest-map",
            name = "Smallest map",
            description = "The steps that would make the map smaller are disabled.",
            args = mapOf(ROWS to ArgDto.int(MINIMUM_SIZE), COLUMNS to ArgDto.int(MINIMUM_SIZE)),
        )
    private val largestMap =
        StoryDto(
            id = "screen-map-builder-hud--largest-map",
            name = "Largest map",
            description = "The steps that would make the map larger are disabled.",
            args = mapOf(ROWS to ArgDto.int(MAXIMUM_SIZE), COLUMNS to ArgDto.int(MAXIMUM_SIZE)),
        )
    private val otherTerrain =
        StoryDto(
            id = "screen-map-builder-hud--other-terrain",
            name = "Other terrain",
            description = "Another terrain selected, framed in white.",
            args = mapOf(SELECTED_TERRAIN to ArgDto.text("water")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-map-builder-hud",
            group = "Screen",
            title = "Map builder HUD",
            description = "Under the map in the map builder: the rows and columns of the map, with steps to change them, and the terrains the tiles are painted with.",
            controls =
                listOf(
                    ControlDto.int(ROWS, minimum = MINIMUM_SIZE, maximum = MAXIMUM_SIZE),
                    ControlDto.int(COLUMNS, minimum = MINIMUM_SIZE, maximum = MAXIMUM_SIZE),
                    ControlDto.pick(SELECTED_TERRAIN, context.terrainIds()),
                ),
            args = mapOf(ROWS to ArgDto.int(16), COLUMNS to ArgDto.int(16), SELECTED_TERRAIN to ArgDto.text(DEFAULT_TERRAIN)),
            stories = listOf(default, smallestMap, largestMap, otherTerrain),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val rows = args.int(ROWS)
        val columns = args.int(COLUMNS)
        // The tiles come from the battlefield, as the presenter takes them.
        val battlefieldView = BattlefieldView(context.resourcesLoader)
        val terrains =
            context.terrainIds().map { terrainId ->
                MapBuilderHudView.TerrainOption(terrainId = terrainId, bitmap = battlefieldView.terrainTileBitmap(terrainId, terrainTransition = null))
            }
        return MapBuilderHudView(context.texts).apply {
            setDelegate(
                object : MapBuilderHudView.Delegate {
                    // A step changes the rows or the columns, which the map builder allows within its sizes.
                    override fun sizeChanged(
                        rows: Int,
                        columns: Int,
                    ) = context.action("sizeChanged", rows, columns, setArg = if (rows != args.int(ROWS)) ROWS to ArgDto.int(rows) else COLUMNS to ArgDto.int(columns))

                    override fun terrainSelected(terrainId: String) = context.action("terrainSelected", terrainId, setArg = SELECTED_TERRAIN to ArgDto.text(terrainId))
                },
            )
            displaySize(rows = rows, columns = columns, allowedSizes = MINIMUM_SIZE..MAXIMUM_SIZE)
            displayTerrains(terrains, selectedTerrainId = args.text(SELECTED_TERRAIN))
        }
    }
}
