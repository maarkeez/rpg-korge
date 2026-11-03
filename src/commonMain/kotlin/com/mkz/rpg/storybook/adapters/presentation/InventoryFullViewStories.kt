package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.InventoryFullView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/** The stories of [InventoryFullView], at the width of the screen, with the texts of the language global. */
object InventoryFullViewStories : StoryBinding {
    private const val SELL_PRICE_LIMIT = 9999
    private const val DEFAULT_ITEM = "health-potion"

    private const val FOUND_ITEM = "foundItem"
    private const val FOUND_ITEM_NAME = "foundItemName"
    private const val FOUND_ITEM_SELL_PRICE = "foundItemSellPrice"

    private val default = StoryDto(id = "screen-inventory-full--default", name = "Default", description = "Asks to tap an item to swap it for the found one, or to leave it.", args = emptyMap())
    private val longName =
        StoryDto(
            id = "screen-inventory-full--long-name",
            name = "Long name",
            description = "A found item with a name longer than the line.",
            args = mapOf(FOUND_ITEM_NAME to ArgDto.text("Enchanted greatsword of the forgotten mountain king")),
        )
    private val expensive =
        StoryDto(
            id = "screen-inventory-full--expensive",
            name = "Expensive",
            description = "A found item with a sell price wider than any in the game.",
            args =
                mapOf(
                    FOUND_ITEM_SELL_PRICE to ArgDto.int(SELL_PRICE_LIMIT),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-inventory-full",
            group = "Screen",
            title = "Inventory full",
            description = "Asked when a party member finds an item with a full inventory: its icon, name and sell price, and Leave it. foundItem: the icon.",
            controls =
                listOf(
                    ControlDto.pick(FOUND_ITEM, context.lootableItemIds()),
                    ControlDto.text(FOUND_ITEM_NAME),
                    ControlDto.int(FOUND_ITEM_SELL_PRICE, minimum = 0, maximum = SELL_PRICE_LIMIT),
                ),
            args = mapOf(FOUND_ITEM to ArgDto.text(DEFAULT_ITEM), FOUND_ITEM_NAME to ArgDto.text("Health potion"), FOUND_ITEM_SELL_PRICE to ArgDto.int(25)),
            stories = listOf(default, longName, expensive),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        InventoryFullView(
            context.texts,
            foundItemName = args.text(FOUND_ITEM_NAME),
            foundItemSellPrice = args.int(FOUND_ITEM_SELL_PRICE),
            foundItemId = args.text(FOUND_ITEM),
            resourcesLoader = context.resourcesLoader,
            onLeft = { context.action("onLeft") },
        )
}
