package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.TileFlashView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [TileFlashView], over a terrain tile and the battle unit standing on it, as the battlefield draws it. */
object TileFlashViewStories : StoryBinding {
    private const val DEFAULT_TERRAIN = "sand"
    private const val DEFAULT_UNIT = "knight"
    private const val TERRAIN = StoryTiles.TERRAIN
    private const val UNIT = "unit"
    private const val FLASHING = "flashing"

    private val flashing =
        StoryDto(id = "screen-tile-flash--flashing", name = "Flashing", description = "The frame of the blink where the battle unit is a white silhouette, hiding it.", args = emptyMap())
    private val notFlashing =
        StoryDto(
            id = "screen-tile-flash--not-flashing",
            name = "Not flashing",
            description = "The other frame of the blink: the battle unit alone, as the flash comes off.",
            args = mapOf(FLASHING to ArgDto.boolean(false)),
        )
    private val smallUnit =
        StoryDto(
            id = "screen-tile-flash--small-unit",
            name = "Small unit",
            description = "A battle unit with 16x16 art: the silhouette fills the tile the same.",
            args =
                mapOf(
                    UNIT to ArgDto.text("rat"),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-tile-flash",
            group = "Screen",
            title = "Tile flash",
            description =
                "The white silhouette of a battle unit that blinks when it receives an effect: the battlefield draws it over the battle unit and removes it, three times. " +
                    "flashing: whether the silhouette is drawn.",
            controls = listOf(ControlDto.pick(UNIT, context.unitIds()), ControlDto.pick(TERRAIN, context.terrainIds()), ControlDto.boolean(FLASHING)),
            args = mapOf(UNIT to ArgDto.text(DEFAULT_UNIT), TERRAIN to ArgDto.text(DEFAULT_TERRAIN), FLASHING to ArgDto.boolean(true)),
            stories = listOf(flashing, notFlashing, smallUnit),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val unitId = args.text(UNIT)
        val unitBitmap = context.resourcesLoader.bitmapOrNull("unit/$unitId.png")
        return StoryTiles.tile(
            args.text(TERRAIN),
            context,
            StoryTiles.unitSprite(unitId, context),
            unitBitmap?.takeIf { args.boolean(FLASHING) }?.let(::TileFlashView),
        )
    }
}
