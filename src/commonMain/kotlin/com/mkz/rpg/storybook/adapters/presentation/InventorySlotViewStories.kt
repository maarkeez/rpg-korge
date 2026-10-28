package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.InventorySlotView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View
import korlibs.math.geom.Size

/** The stories of [InventorySlotView], at the size the inventory and the shop display it. */
object InventorySlotViewStories : StoryBinding {
    private val SIZE = Size(60, 60)
    private const val ITEMS_DIRECTORY = "lootableItem"
    private const val IMAGE_EXTENSION = ".png"

    // The option of the item control for an empty slot, which no lootable item id is.
    private const val NO_ITEM = "none"
    private const val DEFAULT_ITEM = "health-potion"

    private const val ITEM = "item"
    private const val EQUIPPED = "equipped"
    private const val DIMMED = "dimmed"
    private const val SELECTED = "selected"

    private val empty = StoryDto(id = "screen-inventory-slot--empty", name = "Empty", description = "A storage space with no item.", args = mapOf(ITEM to ArgDto.text(NO_ITEM)))
    private val stored = StoryDto(id = "screen-inventory-slot--stored", name = "Stored", description = "A storage space holding an item.", args = emptyMap())
    private val equipped =
        StoryDto(id = "screen-inventory-slot--equipped", name = "Equipped", description = "An item the battle unit wears, framed in gold.", args = mapOf(EQUIPPED to ArgDto.boolean(true)))
    private val selected =
        StoryDto(id = "screen-inventory-slot--selected", name = "Selected", description = "The slot tapped last, framed in white.", args = mapOf(SELECTED to ArgDto.boolean(true)))
    private val dimmed =
        StoryDto(
            id = "screen-inventory-slot--dimmed",
            name = "Dimmed",
            description = "An item that can not be traded, e.g.: at a merchant that does not buy it.",
            args =
                mapOf(
                    DIMMED to ArgDto.boolean(true),
                ),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-inventory-slot",
            group = "Screen",
            title = "Inventory slot",
            description = "One storage space of the inventory and the shop: empty, or the icon of the item it holds.",
            controls =
                listOf(
                    ControlDto.pick(ITEM, listOf(NO_ITEM) + lootableItemIds(context)),
                    ControlDto.boolean(EQUIPPED),
                    ControlDto.boolean(DIMMED),
                    ControlDto.boolean(SELECTED),
                ),
            args = mapOf(ITEM to ArgDto.text(DEFAULT_ITEM), EQUIPPED to ArgDto.boolean(false), DIMMED to ArgDto.boolean(false), SELECTED to ArgDto.boolean(false)),
            stories = listOf(empty, stored, equipped, selected, dimmed),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View =
        InventorySlotView(SIZE, context.resourcesLoader).apply {
            display(lootableItemId = args.text(ITEM).takeUnless { it == NO_ITEM }, isEquipped = args.boolean(EQUIPPED), isDimmed = args.boolean(DIMMED))
            if (args.boolean(SELECTED)) select()
        }

    // Every lootable item that has an icon, in alphabetical order.
    private fun lootableItemIds(context: StoryContext): List<String> =
        context.resourcesLoader
            .bitmaps(ITEMS_DIRECTORY)
            .keys
            .filter { name -> name.endsWith(IMAGE_EXTENSION) }
            .map { name -> name.removeSuffix(IMAGE_EXTENSION) }
            .sorted()
}
