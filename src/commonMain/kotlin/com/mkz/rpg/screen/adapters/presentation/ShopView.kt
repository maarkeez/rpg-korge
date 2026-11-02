package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.GameButton
import com.mkz.rpg.shared.adapters.presentation.gameButton
import korlibs.image.color.Colors
import korlibs.image.text.TextAlignment
import korlibs.korge.input.SwipeDirection
import korlibs.korge.input.onClick
import korlibs.korge.input.onSwipe
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiText
import korlibs.korge.view.Container
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/**
 * A merchant's shop, displayed in place of its dialog once the player chooses to buy or sell, until Back. On top, the
 * buyer's name, gold and bag, between the arrows that switch the buyer, as swiping the shop left or right does. Under
 * it, the items to buy or to sell, one slot per item, as the inventory does. At the bottom, a hint until an item is
 * selected, then its name and price. Back and Buy or Sell are in the row of the call to action.
 */
open class ShopView(
    resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : Container() {
    companion object {
        const val SLOTS = 12
        const val SLOTS_PER_ROW = 6
        private const val SLOT_SIZE = 60.0
        private const val SLOT_SPACING = 6.0
        private const val WIDTH = SLOTS_PER_ROW * SLOT_SIZE + (SLOTS_PER_ROW - 1) * SLOT_SPACING
        private const val BAR_HEIGHT = 32.0
        private const val ARROW_WIDTH = 40.0
        private const val HEIGHT = 300.0

        // The even gap around the header, the slots and the line of the item.
        private const val GAP = 27.0
        private const val SLOTS_Y = GAP + BAR_HEIGHT + GAP
        private const val ITEM_Y = SLOTS_Y + 2 * SLOT_SIZE + SLOT_SPACING + GAP

        // How far the shop is dragged left or right to switch the buyer.
        const val SWIPE_THRESHOLD = 40.0
    }

    /** Whether the buyer buys the merchant's items, or sells its own. */
    enum class Mode { BUY, SELL }

    /** An item to buy or to sell, dimmed when the buyer can not trade it, and whether it is the buyer's equipped copy. */
    data class Slot(
        val lootableItemId: String,
        val isEquipped: Boolean,
        val isTradeable: Boolean,
    )

    /** The party member trading: its position in the party, from the leader, and its gold and bag. */
    data class Buyer(
        val name: String,
        val gold: Int,
        val usedStorageSpace: Int,
        val maximumStorageSpace: Int,
        val partyIndex: Int,
        val partySize: Int,
    )

    interface Delegate {
        fun shopSlotSelected(index: Int)

        /** The buyer was switched to the party member on its left ([step] -1) or right ([step] 1), with an arrow or a swipe. */
        fun shopBuyerStepped(step: Int)
    }

    private var delegate: Delegate? = null
    internal lateinit var previousBuyerButton: GameButton
    internal lateinit var nextBuyerButton: GameButton
    internal lateinit var buyerText: UIText
    internal val slotViews: List<InventorySlotView> = List(SLOTS) { InventorySlotView(Size(SLOT_SIZE, SLOT_SIZE), resourcesLoader) }
    internal lateinit var itemNameText: UIText
    internal lateinit var itemPriceText: UIText

    init {
        // The whole shop can be swiped, also between its slots and buttons.
        solidRect(Size(WIDTH, HEIGHT), Colors.TRANSPARENT)
        addChild(header().position(0.0, GAP))
        slotViews.chunked(SLOTS_PER_ROW).forEachIndexed { rowIndex, rowSlots ->
            rowSlots.forEachIndexed { columnIndex, slotView ->
                addChild(slotView)
                slotView.position(columnIndex * (SLOT_SIZE + SLOT_SPACING), SLOTS_Y + rowIndex * (SLOT_SIZE + SLOT_SPACING))
            }
        }
        itemNameText =
            uiText("", size = Size(WIDTH, BAR_HEIGHT)) {
                styles.textAlignment = TextAlignment.MIDDLE_LEFT
                position(0.0, ITEM_Y)
            }
        itemPriceText =
            uiText("", size = Size(WIDTH, BAR_HEIGHT)) {
                styles.textAlignment = TextAlignment.MIDDLE_RIGHT
                position(0.0, ITEM_Y)
            }
        slotViews.forEachIndexed { index, slotView -> slotView.setOnSelected { delegate?.shopSlotSelected(index) } }
        onSwipe(threshold = SWIPE_THRESHOLD) { swipe ->
            when (swipe.direction) {
                SwipeDirection.LEFT -> swiped(-1)
                SwipeDirection.RIGHT -> swiped(1)
                else -> Unit
            }
        }
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    internal fun swiped(step: Int) {
        delegate?.shopBuyerStepped(step)
    }

    /**
     * Displays the items in their order followed by the empty slots, the ones the buyer can not trade dimmed, and the buyer, e.g.: Knight · 20 G · Bag 11/12, or
     * Bag full. The arrows are disabled at the ends of the party. Clears the selected slot.
     */
    open fun display(
        slots: List<Slot>,
        buyer: Buyer,
    ) {
        slotViews.forEachIndexed { index, slotView ->
            val slot = slots.getOrNull(index)
            slotView.display(lootableItemId = slot?.lootableItemId, isEquipped = slot?.isEquipped ?: false, isDimmed = slot?.isTradeable == false)
        }
        val bag =
            if (buyer.usedStorageSpace >= buyer.maximumStorageSpace) {
                texts[UiText.SHOP_BAG_FULL]
            } else {
                texts[UiText.SHOP_BAG, "used" to buyer.usedStorageSpace, "maximum" to buyer.maximumStorageSpace]
            }
        buyerText.text = texts[UiText.SHOP_BUYER, "name" to buyer.name, "gold" to texts.gold(buyer.gold), "bag" to bag]
        previousBuyerButton.enabled = buyer.partyIndex > 0
        nextBuyerButton.enabled = buyer.partyIndex < buyer.partySize - 1
        selectSlot(null)
    }

    /** Frames the selected slot, or none when [index] is `null`. */
    open fun selectSlot(index: Int?) {
        slotViews.forEachIndexed { slotIndex, slotView -> if (slotIndex == index) slotView.select() else slotView.unselect() }
    }

    /** Displays the [hint] under the slots, e.g.: Tap an item to buy. */
    open fun displayHint(hint: String) {
        itemNameText.text = hint
        itemPriceText.text = ""
    }

    /** Displays the selected item's name and price under the slots. */
    open fun displayItem(
        itemName: String,
        itemPrice: Int,
    ) {
        itemNameText.text = itemName
        itemPriceText.text = texts.gold(itemPrice)
    }

    // < Knight · 20 G · Bag 11/12 >
    private fun header(): UIContainer =
        UIContainer(Size(WIDTH, BAR_HEIGHT)).apply {
            previousBuyerButton = button("<", ARROW_WIDTH) { delegate?.shopBuyerStepped(-1) }
            buyerText =
                uiText("", size = Size(WIDTH - 2 * ARROW_WIDTH, BAR_HEIGHT)) {
                    styles.textAlignment = TextAlignment.MIDDLE_CENTER
                    position(ARROW_WIDTH, 0.0)
                }
            nextBuyerButton = button(">", ARROW_WIDTH) { delegate?.shopBuyerStepped(1) }.position(WIDTH - ARROW_WIDTH, 0.0)
        }

    private fun Container.button(
        label: String,
        width: Double,
        onSelected: () -> Unit,
    ): GameButton =
        gameButton(label, Size(width, BAR_HEIGHT)) {
            onClick { onSelected() }
        }
}
