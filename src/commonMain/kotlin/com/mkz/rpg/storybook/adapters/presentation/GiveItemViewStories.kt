package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.GiveItemView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [GiveItemView], at the width of the screen, with the texts of the language global. */
object GiveItemViewStories : StoryBinding {
    private const val ITEM_NAME = "itemName"

    private val default = StoryDto(id = "screen-give-item--default", name = "Default", description = "Asks to tap the party member who receives the item, or to cancel.", args = emptyMap())
    private val longName =
        StoryDto(
            id = "screen-give-item--long-name",
            name = "Long name",
            description = "An item with a name longer than the line.",
            args = mapOf(ITEM_NAME to ArgDto.text("Enchanted greatsword of the forgotten mountain king")),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-give-item",
            group = "Screen",
            title = "Give item",
            description = "Asks the player, while exploring, to tap the party member who receives the item chosen in the inventory, with Cancel.",
            controls = listOf(ControlDto.text(ITEM_NAME)),
            args = mapOf(ITEM_NAME to ArgDto.text("Health potion")),
            stories = listOf(default, longName),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = GiveItemView(context.texts, itemName = args.text(ITEM_NAME), onCancelled = { context.action("onCancelled") })
}
