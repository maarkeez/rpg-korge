package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.ShopView
import com.mkz.rpg.screen.adapters.presentation.UiText
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.korge.view.View

/**
 * The stories of [ShopView], displayed as the shop presenter does: the hint until an item is selected, then its name,
 * its price and Buy or Sell, or why the buyer can not trade it. Every callback of the shop is logged in the actions, and
 * tapping a slot selects it, as the arrows switch the buyer, through their args.
 */
object ShopViewStories : StoryBinding {
    private const val BAG_SIZE = 12
    private const val PARTY_SIZE = 3
    private const val GOLD_LIMIT = 9999
    private const val BUY_PRICE = 50
    private const val SELL_PRICE = 25

    // The option of the selected slot control when no slot is selected, which no slot index is.
    private const val NO_SLOT = "none"

    private const val MODE = "mode"
    private const val BUYER = "buyer"
    private const val GOLD = "gold"
    private const val BAG_USED = "bagUsed"
    private const val BUYER_INDEX = "buyerIndex"
    private const val ITEMS = "items"
    private const val SELECTED_SLOT = "selectedSlot"
    private const val REFUSAL = "refusal"

    /** Why the buyer can not trade the items, which are then dimmed, and the text Buy or Sell shows instead. */
    private enum class Refusal(
        val text: UiText?,
    ) {
        NONE(null),
        NOT_ENOUGH_GOLD(UiText.SHOP_NOT_ENOUGH_GOLD),
        BAG_FULL(UiText.SHOP_NO_ROOM),
        EQUIPPED(UiText.SHOP_EQUIPPED),
        GOLD_FULL(UiText.SHOP_GOLD_FULL),
    }

    private val buy = StoryDto(id = "screen-shop--buy", name = "Buy", description = "The merchant's items, before one is tapped: the hint tells what tapping one does.", args = emptyMap())
    private val buyItemSelected =
        StoryDto(
            id = "screen-shop--buy-item-selected",
            name = "Buy, item selected",
            description = "An item tapped, framed in white, with its name, its price and Buy.",
            args = mapOf(SELECTED_SLOT to ArgDto.text("1")),
        )
    private val buyNotEnoughGold =
        StoryDto(
            id = "screen-shop--buy-not-enough-gold",
            name = "Buy, not enough gold",
            description = "A buyer that can not afford the items: they are dimmed, and Buy tells why, disabled.",
            args = mapOf(GOLD to ArgDto.int(20), REFUSAL to ArgDto.text(Refusal.NOT_ENOUGH_GOLD.name), SELECTED_SLOT to ArgDto.text("0")),
        )
    private val buyBagFull =
        StoryDto(
            id = "screen-shop--buy-bag-full",
            name = "Buy, bag full",
            description = "A buyer with no room left in its bag: the items are dimmed, and Buy tells why, disabled.",
            args = mapOf(BAG_USED to ArgDto.int(BAG_SIZE), REFUSAL to ArgDto.text(Refusal.BAG_FULL.name), SELECTED_SLOT to ArgDto.text("0")),
        )
    private val sell = StoryDto(id = "screen-shop--sell", name = "Sell", description = "The buyer's own items, before one is tapped.", args = mapOf(MODE to ArgDto.text(ShopView.Mode.SELL.name)))
    private val sellEquipped =
        StoryDto(
            id = "screen-shop--sell-equipped",
            name = "Sell, equipped",
            description = "Items the buyer wears, framed in gold: they are dimmed, and Sell tells why, disabled.",
            args = mapOf(MODE to ArgDto.text(ShopView.Mode.SELL.name), REFUSAL to ArgDto.text(Refusal.EQUIPPED.name), SELECTED_SLOT to ArgDto.text("0")),
        )
    private val sellBagFull =
        StoryDto(
            id = "screen-shop--sell-bag-full",
            name = "Sell, bag full",
            description = "A full bag can still sell: the last item tapped, with Sell enabled.",
            args = mapOf(MODE to ArgDto.text(ShopView.Mode.SELL.name), ITEMS to ArgDto.int(BAG_SIZE), BAG_USED to ArgDto.int(BAG_SIZE), SELECTED_SLOT to ArgDto.text("11")),
        )
    private val sellNothing =
        StoryDto(
            id = "screen-shop--sell-nothing",
            name = "Sell, nothing to sell",
            description = "A buyer with an empty bag: the hint tells it has nothing to sell.",
            args = mapOf(MODE to ArgDto.text(ShopView.Mode.SELL.name), ITEMS to ArgDto.int(0), BAG_USED to ArgDto.int(0)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-shop",
            group = "Screen",
            title = "Shop",
            description =
                "A merchant's shop: the party member buying or selling on top, with arrows to switch it, then the items, " +
                    "then Back, the item tapped and Buy or Sell. refusal: why the items can not be traded.",
            controls =
                listOf(
                    ControlDto.enum(MODE, ShopView.Mode.entries.map { mode -> mode.name }),
                    ControlDto.text(BUYER),
                    ControlDto.int(GOLD, minimum = 0, maximum = GOLD_LIMIT),
                    ControlDto.int(BAG_USED, minimum = 0, maximum = BAG_SIZE),
                    ControlDto.int(BUYER_INDEX, minimum = 0, maximum = PARTY_SIZE - 1),
                    ControlDto.int(ITEMS, minimum = 0, maximum = ShopView.SLOTS),
                    ControlDto.pick(SELECTED_SLOT, listOf(NO_SLOT) + (0 until ShopView.SLOTS).map { index -> index.toString() }),
                    ControlDto.enum(REFUSAL, Refusal.entries.map { refusal -> refusal.name }),
                ),
            args =
                mapOf(
                    MODE to ArgDto.text(ShopView.Mode.BUY.name),
                    BUYER to ArgDto.text("Knight"),
                    GOLD to ArgDto.int(120),
                    BAG_USED to ArgDto.int(4),
                    BUYER_INDEX to ArgDto.int(0),
                    ITEMS to ArgDto.int(4),
                    SELECTED_SLOT to ArgDto.text(NO_SLOT),
                    REFUSAL to ArgDto.text(Refusal.NONE.name),
                ),
            stories = listOf(buy, buyItemSelected, buyNotEnoughGold, buyBagFull, sell, sellEquipped, sellBagFull, sellNothing),
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
        return ShopView(context.resourcesLoader, context.texts).apply {
            setDelegate(
                object : ShopView.Delegate {
                    // As in the game, tapping the selected item, or an empty slot, clears the selection.
                    override fun shopSlotSelected(index: Int) {
                        val selected = index.takeIf { index in items.indices && index != selectedSlot }
                        context.action("shopSlotSelected", index, setArg = SELECTED_SLOT to ArgDto.text(selected?.toString() ?: NO_SLOT))
                    }

                    // The buyer stays at the ends of the party.
                    override fun shopBuyerStepped(step: Int) {
                        val stepped = (buyerIndex + step).takeIf { index -> index in 0 until PARTY_SIZE }
                        context.action("shopBuyerStepped", step, setArg = stepped?.let { index -> BUYER_INDEX to ArgDto.int(index) })
                    }

                    override fun shopActionSelected() = context.action("shopActionSelected")

                    override fun shopBackSelected() = context.action("shopBackSelected")
                },
            )
            display(
                slots = items.map { item -> ShopView.Slot(item, isEquipped = refusal == Refusal.EQUIPPED, isTradeable = refusal == Refusal.NONE) },
                buyer =
                    ShopView.Buyer(
                        name = args.text(BUYER),
                        gold = args.int(GOLD),
                        usedStorageSpace = args.int(BAG_USED),
                        maximumStorageSpace = BAG_SIZE,
                        partyIndex = buyerIndex,
                        partySize = PARTY_SIZE,
                    ),
            )
            if (selectedSlot == null) {
                displayHint(context.texts[hintOf(mode, items)])
            } else {
                selectSlot(selectedSlot)
                val (price, actionLabel) = if (mode == ShopView.Mode.BUY) BUY_PRICE to UiText.SHOP_BUY else SELL_PRICE to UiText.SHOP_SELL
                displayItem(
                    itemName = context.texts.lootableItemName(items[selectedSlot]),
                    itemPrice = price,
                    actionLabel = context.texts[refusal.text ?: actionLabel],
                    isActionEnabled = refusal == Refusal.NONE,
                )
            }
        }
    }

    // What tapping an item does, or that the buyer has nothing to sell.
    private fun hintOf(
        mode: ShopView.Mode,
        items: List<String>,
    ): UiText =
        when {
            mode == ShopView.Mode.BUY -> UiText.SHOP_TAP_TO_BUY
            items.isEmpty() -> UiText.SHOP_NOTHING_TO_SELL
            else -> UiText.SHOP_TAP_TO_SELL
        }
}
