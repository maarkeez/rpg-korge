package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ShopView
import com.mkz.rpg.screen.adapters.presentation.UiMessage
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.korge.view.View

/**
 * The stories of [ShopView], displayed as the shop presenter does: the hint until an item is selected, then its name and
 * price. Every callback of the shop is recorded in the actions, and tapping a slot selects it, as the arrows switch the
 * buyer, through their args. Back and Buy or Sell are in the row of the call to action.
 */
object ShopViewStories : StoryBinding {
    private const val INVENTORY_SIZE = 12
    private const val PARTY_SIZE = 3
    private const val GOLD_LIMIT = 9999
    private const val BUY_PRICE = 50
    private const val SELL_PRICE = 25

    // The option of the selected slot control when no slot is selected, which no slot index is.
    private const val NO_SLOT = "none"

    private const val MODE = "mode"
    private const val BUYER = "buyer"
    private const val GOLD = "gold"
    private const val INVENTORY_USED = "inventoryUsed"
    private const val BUYER_INDEX = "buyerIndex"
    private const val ITEMS = "items"
    private const val SELECTED_SLOT = "selectedSlot"
    private const val REFUSAL = "refusal"

    /** Why the buyer can not trade the items, which are then dimmed. */
    private enum class Refusal { NONE, NOT_ENOUGH_GOLD, INVENTORY_FULL, EQUIPPED, GOLD_FULL }

    private val buy = StoryDto(id = "screen-shop--buy", name = "Buy", description = "The merchant's items, before one is tapped: the hint tells what tapping one does.", args = emptyMap())
    private val buyItemSelected =
        StoryDto(
            id = "screen-shop--buy-item-selected",
            name = "Buy, item selected",
            description = "An item tapped, framed in white, with its name and its price.",
            args = mapOf(SELECTED_SLOT to ValueDto.text("1")),
        )
    private val buyNotEnoughGold =
        StoryDto(
            id = "screen-shop--buy-not-enough-gold",
            name = "Buy, not enough gold",
            description = "A buyer that can not afford the items: they are dimmed.",
            args = mapOf(GOLD to ValueDto.int(20), REFUSAL to ValueDto.text(Refusal.NOT_ENOUGH_GOLD.name), SELECTED_SLOT to ValueDto.text("0")),
        )
    private val buyInventoryFull =
        StoryDto(
            id = "screen-shop--buy-inventory-full",
            name = "Buy, inventory full",
            description = "A buyer with no storage space left in its inventory: the items are dimmed.",
            args = mapOf(INVENTORY_USED to ValueDto.int(INVENTORY_SIZE), REFUSAL to ValueDto.text(Refusal.INVENTORY_FULL.name), SELECTED_SLOT to ValueDto.text("0")),
        )
    private val sell = StoryDto(id = "screen-shop--sell", name = "Sell", description = "The buyer's own items, before one is tapped.", args = mapOf(MODE to ValueDto.text(ShopView.Mode.SELL.name)))
    private val sellEquipped =
        StoryDto(
            id = "screen-shop--sell-equipped",
            name = "Sell, equipped",
            description = "Items the buyer wears, framed in gold: they are dimmed.",
            args = mapOf(MODE to ValueDto.text(ShopView.Mode.SELL.name), REFUSAL to ValueDto.text(Refusal.EQUIPPED.name), SELECTED_SLOT to ValueDto.text("0")),
        )
    private val sellInventoryFull =
        StoryDto(
            id = "screen-shop--sell-inventory-full",
            name = "Sell, inventory full",
            description = "A full inventory can still sell: the last item tapped, its sell price under the slots.",
            args = mapOf(MODE to ValueDto.text(ShopView.Mode.SELL.name), ITEMS to ValueDto.int(INVENTORY_SIZE), INVENTORY_USED to ValueDto.int(INVENTORY_SIZE), SELECTED_SLOT to ValueDto.text("11")),
        )
    private val sellNothing =
        StoryDto(
            id = "screen-shop--sell-nothing",
            name = "Sell, nothing to sell",
            description = "A buyer with an empty inventory: the hint tells it has nothing to sell.",
            args = mapOf(MODE to ValueDto.text(ShopView.Mode.SELL.name), ITEMS to ValueDto.int(0), INVENTORY_USED to ValueDto.int(0)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-shop",
            group = "Screen",
            title = "Shop",
            description =
                "A merchant's shop: the party member buying or selling on top, with arrows to switch it, then the items, " +
                    "then the item tapped, evenly apart. Back and Buy or Sell are in the row of the call to action. refusal: why the items can not be traded.",
            controls =
                listOf(
                    ControlDto.enum(MODE, ShopView.Mode.entries.map { mode -> mode.name }),
                    ControlDto.text(BUYER),
                    ControlDto.int(GOLD, minimum = 0, maximum = GOLD_LIMIT),
                    ControlDto.int(INVENTORY_USED, minimum = 0, maximum = INVENTORY_SIZE),
                    ControlDto.int(BUYER_INDEX, minimum = 0, maximum = PARTY_SIZE - 1),
                    ControlDto.int(ITEMS, minimum = 0, maximum = ShopView.SLOTS),
                    ControlDto.pick(SELECTED_SLOT, listOf(NO_SLOT) + (0 until ShopView.SLOTS).map { index -> index.toString() }),
                    ControlDto.pick(REFUSAL, Refusal.entries.map { refusal -> refusal.name }),
                ),
            args =
                mapOf(
                    MODE to ValueDto.text(ShopView.Mode.BUY.name),
                    BUYER to ValueDto.text("Knight"),
                    GOLD to ValueDto.int(120),
                    INVENTORY_USED to ValueDto.int(4),
                    BUYER_INDEX to ValueDto.int(0),
                    ITEMS to ValueDto.int(4),
                    SELECTED_SLOT to ValueDto.text(NO_SLOT),
                    REFUSAL to ValueDto.text(Refusal.NONE.name),
                ),
            stories = listOf(buy, buyItemSelected, buyNotEnoughGold, buyInventoryFull, sell, sellEquipped, sellInventoryFull, sellNothing),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val mode = ShopView.Mode.valueOf(args.text(MODE))
        val refusal = Refusal.valueOf(args.text(REFUSAL))
        val itemIds = context.lootableItemIds()
        // The first lootable items, in alphabetical order, as many times as needed.
        val items = List(args.int(ITEMS)) { index -> itemIds[index % itemIds.size] }
        // A selected slot with no item selects none.
        val selectedSlot = args.text(SELECTED_SLOT).toIntOrNull()?.takeIf { index -> index in items.indices }
        val buyerIndex = args.int(BUYER_INDEX)
        return ShopView(context.resourcesLoader, context.messages).apply {
            setDelegate(
                object : ShopView.Delegate {
                    // As in the game, tapping the selected item, or an empty slot, clears the selection.
                    override fun shopSlotSelected(index: Int) {
                        val selected = index.takeIf { index in items.indices && index != selectedSlot }
                        context.action("shopSlotSelected", index, setArg = SELECTED_SLOT to ValueDto.text(selected?.toString() ?: NO_SLOT))
                    }

                    // The buyer stays at the ends of the party.
                    override fun shopBuyerStepped(step: Int) {
                        val stepped = (buyerIndex + step).takeIf { index -> index in 0 until PARTY_SIZE }
                        context.action("shopBuyerStepped", step, setArg = stepped?.let { index -> BUYER_INDEX to ValueDto.int(index) })
                    }
                },
            )
            display(
                slots = items.map { item -> ShopView.Slot(item, isEquipped = refusal == Refusal.EQUIPPED, isTradeable = refusal == Refusal.NONE) },
                buyer =
                    ShopView.Buyer(
                        name = args.text(BUYER),
                        gold = args.int(GOLD),
                        usedStorageSpace = args.int(INVENTORY_USED),
                        maximumStorageSpace = INVENTORY_SIZE,
                        partyIndex = buyerIndex,
                        partySize = PARTY_SIZE,
                    ),
            )
            if (selectedSlot == null) {
                displayHint(context.messages[hintOf(mode, items)])
            } else {
                selectSlot(selectedSlot)
                displayItem(itemName = context.messages.lootableItemName(items[selectedSlot]), itemPrice = if (mode == ShopView.Mode.BUY) BUY_PRICE else SELL_PRICE)
            }
        }
    }

    // What tapping an item does, or that the buyer has nothing to sell.
    private fun hintOf(
        mode: ShopView.Mode,
        items: List<String>,
    ): UiMessage =
        when {
            mode == ShopView.Mode.BUY -> UiMessage.SHOP_TAP_TO_BUY
            items.isEmpty() -> UiMessage.SHOP_NOTHING_TO_SELL
            else -> UiMessage.SHOP_TAP_TO_SELL
        }
}
