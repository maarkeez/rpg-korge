package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.BattlefieldView
import com.mkz.tessavar.screen.adapters.presentation.WorldBuilderHudView
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object WorldBuilderHudViewStories : StoryBinding {
    private const val MINIMUM_SIZE = 1
    private const val MAXIMUM_SIZE = 128
    private const val DEFAULT_TERRAIN = "grass"

    private const val ROWS = "rows"
    private const val COLUMNS = "columns"
    private const val SELECTED_TERRAIN = "selectedTerrain"

    private val default = StoryDto(id = "screen-world-builder-hud--default", name = "Default", description = "A new world: 16 x 16 tiles, with the grass selected.", args = emptyMap())
    private val smallestWorld =
        StoryDto(
            id = "screen-world-builder-hud--smallest-world",
            name = "Smallest world",
            description = "The steps that would make the world smaller are disabled.",
            args = mapOf(ROWS to ValueDto.int(MINIMUM_SIZE), COLUMNS to ValueDto.int(MINIMUM_SIZE)),
        )
    private val largestWorld =
        StoryDto(
            id = "screen-world-builder-hud--largest-world",
            name = "Largest world",
            description = "The steps that would make the world larger are disabled.",
            args = mapOf(ROWS to ValueDto.int(MAXIMUM_SIZE), COLUMNS to ValueDto.int(MAXIMUM_SIZE)),
        )
    private val otherTerrain =
        StoryDto(
            id = "screen-world-builder-hud--other-terrain",
            name = "Other terrain",
            description = "Another terrain selected, framed in white.",
            args = mapOf(SELECTED_TERRAIN to ValueDto.text("water")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-world-builder-hud",
            group = "Screen",
            title = "World builder HUD",
            description = "Under the world in the world builder: the rows and columns of the world, with steps to change them, and the terrains the tiles are painted with.",
            controls =
                listOf(
                    ControlDto.int(ROWS, minimum = MINIMUM_SIZE, maximum = MAXIMUM_SIZE),
                    ControlDto.int(COLUMNS, minimum = MINIMUM_SIZE, maximum = MAXIMUM_SIZE),
                    ControlDto.pick(SELECTED_TERRAIN, context.terrainIds()),
                ),
            args = mapOf(ROWS to ValueDto.int(16), COLUMNS to ValueDto.int(16), SELECTED_TERRAIN to ValueDto.text(DEFAULT_TERRAIN)),
            stories = listOf(default, smallestWorld, largestWorld, otherTerrain),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val rows = args.int(ROWS)
        val columns = args.int(COLUMNS)
        val battlefieldView = BattlefieldView(context.resourcesLoader)
        val terrains =
            context.terrainIds().map { terrainId ->
                WorldBuilderHudView.TerrainOption(terrainId = terrainId, bitmap = battlefieldView.terrainTileBitmap(terrainId, terrainTransition = null))
            }
        return WorldBuilderHudView(context.messages).apply {
            setDelegate(
                object : WorldBuilderHudView.Delegate {
                    override fun sizeChanged(
                        rows: Int,
                        columns: Int,
                    ) = context.action("sizeChanged", rows, columns, setArg = if (rows != args.int(ROWS)) ROWS to ValueDto.int(rows) else COLUMNS to ValueDto.int(columns))

                    override fun terrainSelected(terrainId: String) = context.action("terrainSelected", terrainId, setArg = SELECTED_TERRAIN to ValueDto.text(terrainId))
                },
            )
            displaySize(rows = rows, columns = columns, allowedSizes = MINIMUM_SIZE..MAXIMUM_SIZE)
            displayTerrains(terrains, selectedTerrainId = args.text(SELECTED_TERRAIN))
        }
    }
}
