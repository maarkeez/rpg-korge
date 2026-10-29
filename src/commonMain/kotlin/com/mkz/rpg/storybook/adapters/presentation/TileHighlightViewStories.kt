package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.TileHighlightView
import com.mkz.rpg.screen.adapters.presentation.TileHighlightView.Kind
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [TileHighlightView], over a terrain tile as the battlefield draws it. */
object TileHighlightViewStories : StoryBinding {
    private const val DEFAULT_TERRAIN = "sand"
    private const val TERRAIN = StoryTiles.TERRAIN
    private const val KIND = "kind"

    private val moveRange =
        StoryDto(id = "screen-tile-highlight--move-range", name = "Move range", description = "A tile the selected battle unit can move to.", args = emptyMap())
    private val castRange =
        StoryDto(id = "screen-tile-highlight--cast-range", name = "Cast range", description = "A tile the selected ability can be cast on.", args = mapOf(KIND to ArgDto.text(Kind.CAST_RANGE.name)))
    private val selected =
        StoryDto(
            id = "screen-tile-highlight--selected-tile",
            name = "Selected tile",
            description = "The tile of the selected battle unit, or a vacant tile of the cast previewed.",
            args = mapOf(KIND to ArgDto.text(Kind.SELECTED.name)),
        )
    private val overWater =
        StoryDto(
            id = "screen-tile-highlight--over-water",
            name = "Over water",
            description = "The frame over another terrain: it must stand out on any of them.",
            args = mapOf(TERRAIN to ArgDto.text("water")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-tile-highlight",
            group = "Screen",
            title = "Tile highlight",
            description = "The frame drawn over a tile of the battlefield to point it out: where the selected battle unit can move or cast, and the tile selected.",
            controls = listOf(ControlDto.enum(KIND, Kind.entries.map { kind -> kind.name }), ControlDto.pick(TERRAIN, context.terrainIds())),
            args = mapOf(KIND to ArgDto.text(Kind.MOVE_RANGE.name), TERRAIN to ArgDto.text(DEFAULT_TERRAIN)),
            stories = listOf(moveRange, castRange, selected, overWater),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = StoryTiles.tile(args.text(TERRAIN), context, TileHighlightView(context.resourcesLoader, Kind.valueOf(args.text(KIND))))
}
