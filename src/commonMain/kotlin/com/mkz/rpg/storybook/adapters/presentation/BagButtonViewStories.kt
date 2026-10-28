package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BagButtonView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [BagButtonView], at the size the battle unit info displays it. */
object BagButtonViewStories : StoryBinding {
    private val SIZE = Size(29, 29)

    private val chest = StoryDto(id = "screen-bag-button--chest", name = "Chest", description = "The chest to tap to open the inventory of the selected battle unit.", args = emptyMap())

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-bag-button",
            group = "Screen",
            title = "Bag button",
            description = "Opens the inventory of the selected battle unit, in its info. Drawn with the chest the battlefield shows on the tiles with several items.",
            controls = emptyList(),
            args = emptyMap(),
            stories = listOf(chest),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = BagButtonView(SIZE, context.resourcesLoader).apply { display(onBagSelected = { context.action("onBagSelected") }) }
}
