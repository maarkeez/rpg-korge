package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.EffectView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [EffectView], at the size the effects of a battle unit are displayed. */
object EffectViewStories : StoryBinding {
    private val SIZE = Size(20, 20)

    private const val EFFECT = "effect"
    private const val IS_NEW = "isNew"

    private val applied = StoryDto(id = "screen-effect--applied", name = "Applied", description = "An effect the battle unit has, as a small icon in a chip.", args = emptyMap())
    private val new =
        StoryDto(
            id = "screen-effect--new",
            name = "New",
            description = "An effect an ability cast would apply: its chip is outlined in gold and blinks.",
            args = mapOf(IS_NEW to ValueDto.boolean(true)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-effect",
            group = "Screen",
            title = "Effect",
            description = "The icon of an effect a battle unit has, at 1x in a rounded chip, in its info and in the previews. An effect an ability would apply is outlined in gold and blinks.",
            controls = listOf(ControlDto.pick(EFFECT, context.effectIds()), ControlDto.boolean(IS_NEW)),
            args = mapOf(EFFECT to ValueDto.text(context.effectIds().first()), IS_NEW to ValueDto.boolean(false)),
            stories = listOf(applied, new),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = EffectView(SIZE, context.resourcesLoader).apply { displayEffect(effectId = args.text(EFFECT), isNew = args.boolean(IS_NEW)) }
}
