package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.InventoryButtonView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [InventoryButtonView], at the size the battle unit info displays it. */
object InventoryButtonViewStories : StoryBinding {
    private val SIZE = Size(40, 40)

    private val chest = StoryDto(id = "screen-inventory-button--chest", name = "Chest", description = "The chest to tap to open the inventory of the selected battle unit.", args = emptyMap())

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-inventory-button",
            group = "Screen",
            title = "Inventory button",
            description = "Opens the inventory of the selected battle unit, in its info. Drawn with the chest the battlefield shows on the tiles with several items.",
            controls = emptyList(),
            args = emptyMap(),
            stories = listOf(chest),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View = InventoryButtonView(SIZE, context.resourcesLoader).apply { display(onInventoryButtonSelected = { context.action("onInventoryButtonSelected") }) }
}
