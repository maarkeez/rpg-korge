package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.EffectView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [EffectView], at the size the effects of a battle unit are displayed. */
object EffectViewStories : StoryBinding {
    private val SIZE = Size(16, 16)

    private const val EFFECT = "effect"
    private const val IS_NEW = "isNew"

    private val applied = StoryDto(id = "screen-effect--applied", name = "Applied", description = "An effect the battle unit has, as a small icon.", args = emptyMap())
    private val new =
        StoryDto(id = "screen-effect--new", name = "New", description = "An effect an ability cast would apply: its icon blinks.", args = mapOf(IS_NEW to ArgDto.boolean(true)))

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-effect",
            group = "Screen",
            title = "Effect",
            description = "The icon of an effect a battle unit has, in its info and in the previews. An effect an ability would apply blinks.",
            controls = listOf(ControlDto.pick(EFFECT, context.effectIds()), ControlDto.boolean(IS_NEW)),
            args = mapOf(EFFECT to ArgDto.text(context.effectIds().first()), IS_NEW to ArgDto.boolean(false)),
            stories = listOf(applied, new),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = EffectView(SIZE, context.resourcesLoader).apply { displayEffect(effectId = args.text(EFFECT), isNew = args.boolean(IS_NEW)) }
}
