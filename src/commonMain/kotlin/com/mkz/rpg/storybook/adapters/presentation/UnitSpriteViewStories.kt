package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.UnitSpriteView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [UnitSpriteView], over a terrain tile as the battlefield draws it. */
object UnitSpriteViewStories : StoryBinding {
    private const val DEFAULT_TERRAIN = "sand"
    private const val DEFAULT_UNIT = "knight"
    private const val TERRAIN = StoryTiles.TERRAIN
    private const val UNIT = "unit"
    private const val NPC = "npc"
    private const val NONE = StoryTiles.NONE

    private val knight =
        StoryDto(
            id = "screen-unit-sprite--knight",
            name = "Knight",
            description = "A battle unit with 32x32 art, drawn at twice its size to fill the tile.",
            args = emptyMap(),
        )
    private val rat =
        StoryDto(
            id = "screen-unit-sprite--rat",
            name = "Rat",
            description = "A battle unit with 16x16 art, drawn at four times its size to fill the tile.",
            args = mapOf(UNIT to ArgDto.text("rat")),
        )
    private val npc =
        StoryDto(
            id = "screen-unit-sprite--npc",
            name = "Npc",
            description = "An npc stands on its tile as a battle unit does.",
            args = mapOf(NPC to ArgDto.text("alchemist")),
        )
    private val onLava =
        StoryDto(
            id = "screen-unit-sprite--on-lava",
            name = "On lava",
            description = "A battle unit over another terrain.",
            args = mapOf(UNIT to ArgDto.text("goat"), TERRAIN to ArgDto.text("lava")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-unit-sprite",
            group = "Screen",
            title = "Unit sprite",
            description =
                "The image of a battle unit, or of an npc standing as one, on its tile of the battlefield. " +
                    "unit: the battle unit. npc: the npc standing instead of the battle unit, or none.",
            controls =
                listOf(
                    ControlDto.pick(UNIT, context.unitIds()),
                    ControlDto.pick(NPC, listOf(NONE) + context.npcIds()),
                    ControlDto.pick(TERRAIN, context.terrainIds()),
                ),
            args = mapOf(UNIT to ArgDto.text(DEFAULT_UNIT), NPC to ArgDto.text(NONE), TERRAIN to ArgDto.text(DEFAULT_TERRAIN)),
            stories = listOf(knight, rat, npc, onLava),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val npcId = args.text(NPC)
        val sprite =
            if (npcId == NONE) {
                StoryTiles.unitSprite(args.text(UNIT), context)
            } else {
                context.resourcesLoader.bitmapOrNull("npc/$npcId.png")?.let(::UnitSpriteView)
            }
        return StoryTiles.tile(args.text(TERRAIN), context, sprite)
    }
}
