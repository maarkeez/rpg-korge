package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.PillView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object PillViewStories : StoryBinding {
    private const val TEXT = "text"
    private const val COLOR = "color"
    private const val GLYPH = "glyph"
    private const val SOLID = "solid"

    private const val MANA_COLOR = "0091FF"
    private const val COOLDOWN_COLOR = "FFBB31"
    private const val DEFEATED_COLOR = "FF375F"
    private const val GOLD_COLOR = "FFD84A"

    private val manaCost = StoryDto(id = "screen-pill--mana-cost", name = "Mana cost", description = "The mana an ability costs, after the orb of mana.", args = emptyMap())
    private val cooldown =
        StoryDto(
            id = "screen-pill--cooldown",
            name = "Cooldown",
            description = "The turns an ability waits after a cast, after a clock.",
            args = mapOf(TEXT to ValueDto.text("2"), COLOR to ValueDto.text(COOLDOWN_COLOR), GLYPH to ValueDto.text(PillView.Glyph.COOLDOWN.name)),
        )
    private val defeated =
        StoryDto(
            id = "screen-pill--defeated",
            name = "Defeated",
            description = "A recipient a cast would defeat: a solid pill reading KO, the text in white.",
            args = mapOf(TEXT to ValueDto.text("KO"), COLOR to ValueDto.text(DEFEATED_COLOR), GLYPH to ValueDto.text(PillView.Glyph.NONE.name), SOLID to ValueDto.boolean(true)),
        )
    private val gold =
        StoryDto(
            id = "screen-pill--gold",
            name = "Gold",
            description = "The gold of a quest reward, after a coin.",
            args = mapOf(TEXT to ValueDto.text("150 G"), COLOR to ValueDto.text(GOLD_COLOR), GLYPH to ValueDto.text(PillView.Glyph.GOLD.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-pill",
            group = "Screen",
            title = "Pill",
            description = "A short value in a capsule tinted with its colour, with an optional glyph drawn as a vector before it, e.g.: the costs of an ability.",
            controls =
                listOf(
                    ControlDto.text(TEXT),
                    ControlDto.color(COLOR),
                    ControlDto.enum(GLYPH, PillView.Glyph.entries.map { glyph -> glyph.name }),
                    ControlDto.boolean(SOLID),
                ),
            args = mapOf(TEXT to ValueDto.text("10"), COLOR to ValueDto.text(MANA_COLOR), GLYPH to ValueDto.text(PillView.Glyph.MANA.name), SOLID to ValueDto.boolean(false)),
            stories = listOf(manaCost, cooldown, defeated, gold),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        PillView(color = args.color(COLOR), glyph = PillView.Glyph.valueOf(args.text(GLYPH)), solid = args.boolean(SOLID)).apply {
            display(args.text(TEXT))
        }
}
