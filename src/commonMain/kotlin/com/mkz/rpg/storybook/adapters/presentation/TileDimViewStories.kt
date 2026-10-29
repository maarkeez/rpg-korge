package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.TileDimView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [TileDimView], over a terrain tile and the battle unit standing on it, as the battlefield draws it. */
object TileDimViewStories : StoryBinding {
    private const val DEFAULT_TERRAIN = "sand"
    private const val DEFAULT_UNIT = "knight"
    private const val TERRAIN = StoryTiles.TERRAIN
    private const val UNIT = "unit"
    private const val NONE = StoryTiles.NONE

    private val overTerrain =
        StoryDto(
            id = "screen-tile-dim--over-terrain",
            name = "Over terrain",
            description = "A tile that can not be selected, with no one on it: its terrain is darkened.",
            args =
                mapOf(
                    UNIT to ArgDto.text(NONE),
                ),
        )
    private val overBattleUnit =
        StoryDto(id = "screen-tile-dim--over-battle-unit", name = "Over a battle unit", description = "The mask darkens the battle unit on the tile too.", args = emptyMap())
    private val overLava =
        StoryDto(
            id = "screen-tile-dim--over-lava",
            name = "Over lava",
            description = "The mask over another terrain.",
            args = mapOf(TERRAIN to ArgDto.text("lava"), UNIT to ArgDto.text("rat")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-tile-dim",
            group = "Screen",
            title = "Tile dim",
            description = "The black mask over the tiles of the battlefield that can not be selected, e.g.: the ones the selected battle unit can not move to, so the others stand out.",
            controls = listOf(ControlDto.pick(TERRAIN, context.terrainIds()), ControlDto.pick(UNIT, listOf(NONE) + context.unitIds())),
            args = mapOf(TERRAIN to ArgDto.text(DEFAULT_TERRAIN), UNIT to ArgDto.text(DEFAULT_UNIT)),
            stories = listOf(overTerrain, overBattleUnit, overLava),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        StoryTiles.tile(
            args.text(TERRAIN),
            context,
            args.text(UNIT).takeUnless { unitId -> unitId == NONE }?.let { unitId -> StoryTiles.unitSprite(unitId, context) },
            TileDimView(),
        )
}
