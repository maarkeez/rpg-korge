package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.InventoryView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

object InventoryViewStories : StoryBinding {
    private const val PARTY_LIMIT = 3
    private const val GOLD_LIMIT = 9999

    private const val NO_SLOT = "none"

    private const val ITEMS = "items"
    private const val GOLD = "gold"
    private const val EQUIPPED_SLOT = "equippedSlot"
    private const val SELECTED_SLOT = "selectedSlot"
    private const val EXPLORING = "exploring"
    private const val OWNER = "owner"
    private const val PARTY_INDEX = "partyIndex"
    private const val PARTY_SIZE = "partySize"

    private val empty = StoryDto(id = "screen-inventory--empty", name = "Empty", description = "A party member who carries nothing.", args = mapOf(ITEMS to ValueDto.int(0), GOLD to ValueDto.int(0)))
    private val stored = StoryDto(id = "screen-inventory--stored", name = "Stored", description = "Some items stored, and the gold on the title line.", args = emptyMap())
    private val full = StoryDto(id = "screen-inventory--full", name = "Full", description = "All the storage spaces taken.", args = mapOf(ITEMS to ValueDto.int(InventoryView.SLOTS)))
    private val equipped =
        StoryDto(id = "screen-inventory--equipped", name = "Equipped", description = "An item the battle unit wears, framed in gold.", args = mapOf(EQUIPPED_SLOT to ValueDto.text("0")))
    private val selected = StoryDto(id = "screen-inventory--selected", name = "Selected", description = "The slot tapped last, framed in white.", args = mapOf(SELECTED_SLOT to ValueDto.text("1")))
    private val exploring =
        StoryDto(
            id = "screen-inventory--exploring",
            name = "Exploring",
            description = "While exploring the title names the party member between two arrows, which switch to the previous or next one. The first has no previous.",
            args = mapOf(EXPLORING to ValueDto.boolean(true)),
        )
    private val exploringLastMember =
        StoryDto(
            id = "screen-inventory--exploring-last-member",
            name = "Exploring, last member",
            description = "The last party member has no next one: its arrow is disabled.",
            args = mapOf(EXPLORING to ValueDto.boolean(true), PARTY_INDEX to ValueDto.int(PARTY_LIMIT - 1)),
        )
    private val exploringOnlyMember =
        StoryDto(
            id = "screen-inventory--exploring-only-member",
            name = "Exploring, only member",
            description = "A party of one has no other member to switch to: both arrows are disabled.",
            args = mapOf(EXPLORING to ValueDto.boolean(true), PARTY_SIZE to ValueDto.int(1)),
        )

    private val exploringNoGold =
        StoryDto(
            id = "screen-inventory--exploring-no-gold",
            name = "Exploring, no gold",
            description = "A party member who carries no gold: its gold button is faded, and can still be tapped, to read why.",
            args = mapOf(EXPLORING to ValueDto.boolean(true), GOLD to ValueDto.int(0)),
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
                    ControlDto.int(PARTY_INDEX, minimum = 0, maximum = PARTY_LIMIT - 1),
                    ControlDto.int(PARTY_SIZE, minimum = 1, maximum = PARTY_LIMIT),
                ),
            args =
                mapOf(
                    ITEMS to ValueDto.int(4),
                    GOLD to ValueDto.int(120),
                    EQUIPPED_SLOT to ValueDto.text(NO_SLOT),
                    SELECTED_SLOT to ValueDto.text(NO_SLOT),
                    EXPLORING to ValueDto.boolean(false),
                    OWNER to ValueDto.text("Knight"),
                    PARTY_INDEX to ValueDto.int(0),
                    PARTY_SIZE to ValueDto.int(PARTY_LIMIT),
                ),
            stories = listOf(empty, stored, full, equipped, selected, exploring, exploringLastMember, exploringOnlyMember, exploringNoGold),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val itemIds = context.lootableItemIds()
        val equippedSlot = args.text(EQUIPPED_SLOT).toIntOrNull()
        val items = args.int(ITEMS)
        val selectedSlot = args.text(SELECTED_SLOT).toIntOrNull()?.takeIf { index -> index < items }
        val partySize = args.int(PARTY_SIZE)
        val partyIndex = args.int(PARTY_INDEX).coerceAtMost(partySize - 1)
        return InventoryView(context.resourcesLoader, context.messages).apply {
            setDelegate(
                object : InventoryView.Delegate {
                    override fun slotSelected(index: Int) {
                        val selected = index.takeIf { index < items && index != selectedSlot }
                        context.action("slotSelected", index, setArg = SELECTED_SLOT to ValueDto.text(selected?.toString() ?: NO_SLOT))
                    }

                    override fun inventoryOwnerStepped(step: Int) {
                        val stepped = (partyIndex + step).takeIf { index -> index in 0 until partySize }
                        context.action("inventoryOwnerStepped", step, setArg = stepped?.let { index -> PARTY_INDEX to ValueDto.int(index) })
                    }

                    override fun inventoryClosed() = context.action("inventoryClosed")

                    override fun goldSelected() = context.action("goldSelected")
                },
            )
            display(
                slots = List(items) { index -> InventoryView.Slot(itemIds[index % itemIds.size], isEquipped = index == equippedSlot) },
                gold = args.int(GOLD),
                owner = if (args.boolean(EXPLORING)) InventoryView.Owner(args.text(OWNER), partyIndex, partySize, canGiveGold = args.int(GOLD) > 0 && partySize > 1) else null,
            )
            selectSlot(selectedSlot)
        }
    }

    private fun slots(): List<String> = listOf(NO_SLOT) + (0 until InventoryView.SLOTS).map { index -> index.toString() }
}
