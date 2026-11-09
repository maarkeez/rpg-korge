package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.TileBlinkView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

/** The stories of [TileBlinkView], over a terrain tile and the battle unit standing on it, as the battlefield draws it. */
object TileBlinkViewStories : StoryBinding {
    private const val DEFAULT_TERRAIN = "sand"
    private const val DEFAULT_UNIT = "knight"
    private const val TERRAIN = StoryTiles.TERRAIN
    private const val UNIT = "unit"
    private const val BLINKING = "blinking"

    private val blinking =
        StoryDto(id = "screen-tile-blink--blinking", name = "Blinking", description = "The frame of the blink where the battle unit is a white silhouette, hiding it.", args = emptyMap())
    private val notBlinking =
        StoryDto(
            id = "screen-tile-blink--not-blinking",
            name = "Not blinking",
            description = "The other frame of the blink: the battle unit alone, as the silhouette comes off.",
            args = mapOf(BLINKING to ValueDto.boolean(false)),
        )
    private val smallUnit =
        StoryDto(
            id = "screen-tile-blink--small-unit",
            name = "Small unit",
            description = "A battle unit with 16x16 art: the silhouette fills the tile the same.",
            args =
                mapOf(
                    UNIT to ValueDto.text("rat"),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-tile-blink",
            group = "Screen",
            title = "Tile blink",
            description =
                "The white silhouette of a battle unit that blinks when it receives an effect: the battlefield draws it over the battle unit and removes it, three times. " +
                    "blinking: whether the silhouette is drawn.",
            controls = listOf(ControlDto.pick(UNIT, context.unitIds()), ControlDto.pick(TERRAIN, context.terrainIds()), ControlDto.boolean(BLINKING)),
            args = mapOf(UNIT to ValueDto.text(DEFAULT_UNIT), TERRAIN to ValueDto.text(DEFAULT_TERRAIN), BLINKING to ValueDto.boolean(true)),
            stories = listOf(blinking, notBlinking, smallUnit),
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
            unitBitmap?.takeIf { args.boolean(BLINKING) }?.let(::TileBlinkView),
        )
    }
}
