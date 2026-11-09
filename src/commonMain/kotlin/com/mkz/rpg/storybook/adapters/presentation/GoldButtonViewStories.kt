package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.GoldButtonView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

/** The stories of [GoldButtonView], with the texts of the language global. */
object GoldButtonViewStories : StoryBinding {
    private const val GOLD_LIMIT = 99_999

    private const val GOLD = "gold"
    private const val FADED = "faded"

    private val default = StoryDto(id = "screen-gold-button--default", name = "Default", description = "The gold of a party member who can give some.", args = emptyMap())
    private val faded =
        StoryDto(
            id = "screen-gold-button--faded",
            name = "Faded",
            description = "A party member who can not give gold, e.g.: it carries none. It can still be tapped, to read why.",
            args = mapOf(GOLD to ValueDto.int(0), FADED to ValueDto.boolean(true)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-gold-button",
            group = "Screen",
            title = "Gold button",
            description = "The gold on the title line of the inventory while exploring, looking like a button, to give some to another party member.",
            controls = listOf(ControlDto.int(GOLD, minimum = 0, maximum = GOLD_LIMIT), ControlDto.boolean(FADED)),
            args = mapOf(GOLD to ValueDto.int(120), FADED to ValueDto.boolean(false)),
            stories = listOf(default, faded),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = GoldButtonView(onSelected = { context.action("onSelected") }).apply { display(context.messages.gold(args.int(GOLD)), isFaded = args.boolean(FADED)) }
}
