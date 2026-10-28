package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.InventoryView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [InventoryView], with the items of the game, in alphabetical order, as many times as needed. Tapping a
 * slot selects it, and the arrows switch the party member, as the inventory presenter does.
 */
object InventoryViewStories : StoryBinding {
    private const val PARTY_SIZE = 3
    private const val GOLD_LIMIT = 9999

    // The option of the slot controls when no slot is meant, which no slot index is.
    private const val NO_SLOT = "none"

    private const val ITEMS = "items"
    private const val GOLD = "gold"
    private const val EQUIPPED_SLOT = "equippedSlot"
    private const val SELECTED_SLOT = "selectedSlot"
    private const val EXPLORING = "exploring"
    private const val OWNER = "owner"
    private const val PARTY_INDEX = "partyIndex"

    private val empty = StoryDto(id = "screen-inventory--empty", name = "Empty", description = "A party member who carries nothing.", args = mapOf(ITEMS to ArgDto.int(0), GOLD to ArgDto.int(0)))
    private val stored = StoryDto(id = "screen-inventory--stored", name = "Stored", description = "Some items stored, and the gold on the title line.", args = emptyMap())
    private val full = StoryDto(id = "screen-inventory--full", name = "Full", description = "All the storage spaces taken.", args = mapOf(ITEMS to ArgDto.int(InventoryView.SLOTS)))
    private val equipped =
        StoryDto(id = "screen-inventory--equipped", name = "Equipped", description = "An item the battle unit wears, framed in gold.", args = mapOf(EQUIPPED_SLOT to ArgDto.text("0")))
    private val selected = StoryDto(id = "screen-inventory--selected", name = "Selected", description = "The slot tapped last, framed in white.", args = mapOf(SELECTED_SLOT to ArgDto.text("1")))
    private val exploring =
        StoryDto(
            id = "screen-inventory--exploring",
            name = "Exploring",
            description = "While exploring the title names the party member between two arrows, which switch to the previous or next one. The first has no previous.",
            args = mapOf(EXPLORING to ArgDto.boolean(true)),
        )
    private val exploringLastMember =
        StoryDto(
            id = "screen-inventory--exploring-last-member",
            name = "Exploring, last member",
            description = "The last party member has no next one: its arrow is disabled.",
            args = mapOf(EXPLORING to ArgDto.boolean(true), PARTY_INDEX to ArgDto.int(PARTY_SIZE - 1)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-inventory",
            group = "Screen",
            title = "Inventory",
            description = "The items stored by a party member, one slot per storage space, and its gold. It takes the place of the battle unit info in the HUD.",
            controls =
                listOf(
                    ControlDto.int(ITEMS, minimum = 0, maximum = InventoryView.SLOTS),
                    ControlDto.int(GOLD, minimum = 0, maximum = GOLD_LIMIT),
                    ControlDto.pick(EQUIPPED_SLOT, slots()),
                    ControlDto.pick(SELECTED_SLOT, slots()),
                    ControlDto.boolean(EXPLORING),
                    ControlDto.text(OWNER),
                    ControlDto.int(PARTY_INDEX, minimum = 0, maximum = PARTY_SIZE - 1),
                ),
            args =
                mapOf(
                    ITEMS to ArgDto.int(4),
                    GOLD to ArgDto.int(120),
                    EQUIPPED_SLOT to ArgDto.text(NO_SLOT),
                    SELECTED_SLOT to ArgDto.text(NO_SLOT),
                    EXPLORING to ArgDto.boolean(false),
                    OWNER to ArgDto.text("Knight"),
                    PARTY_INDEX to ArgDto.int(0),
                ),
            stories = listOf(empty, stored, full, equipped, selected, exploring, exploringLastMember),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val itemIds = context.lootableItemIds()
        val equippedSlot = args.text(EQUIPPED_SLOT).toIntOrNull()
        val items = args.int(ITEMS)
        // A slot with no item is neither equipped nor selected.
        val selectedSlot = args.text(SELECTED_SLOT).toIntOrNull()?.takeIf { index -> index < items }
        val partyIndex = args.int(PARTY_INDEX)
        return InventoryView(context.resourcesLoader, context.texts).apply {
            setDelegate(
                object : InventoryView.Delegate {
                    // Tapping the selected item, or an empty slot, clears the selection.
                    override fun slotSelected(index: Int) {
                        val selected = index.takeIf { index < items && index != selectedSlot }
                        context.action("slotSelected", index, setArg = SELECTED_SLOT to ArgDto.text(selected?.toString() ?: NO_SLOT))
                    }

                    // The party member stays at the ends of the party.
                    override fun inventoryOwnerStepped(step: Int) {
                        val stepped = (partyIndex + step).takeIf { index -> index in 0 until PARTY_SIZE }
                        context.action("inventoryOwnerStepped", step, setArg = stepped?.let { index -> PARTY_INDEX to ArgDto.int(index) })
                    }

                    override fun inventoryClosed() = context.action("inventoryClosed")
                },
            )
            display(
                // The first lootable items, in alphabetical order, as many times as needed.
                slots = List(items) { index -> InventoryView.Slot(itemIds[index % itemIds.size], isEquipped = index == equippedSlot) },
                gold = args.int(GOLD),
                owner = if (args.boolean(EXPLORING)) InventoryView.Owner(args.text(OWNER), partyIndex, PARTY_SIZE) else null,
            )
            selectSlot(selectedSlot)
        }
    }

    private fun slots(): List<String> = listOf(NO_SLOT) + (0 until InventoryView.SLOTS).map { index -> index.toString() }
}
