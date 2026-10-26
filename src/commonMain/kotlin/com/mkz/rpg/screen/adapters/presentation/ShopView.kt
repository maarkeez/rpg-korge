package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.korge.input.SwipeDirection
import korlibs.korge.input.onClick
import korlibs.korge.input.onSwipe
import korlibs.korge.style.styles
import korlibs.korge.style.textAlignment
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/**
 * A merchant's shop, displayed in place of its dialog once the player chooses to buy or sell, until Back. On top, the
 * buyer's name, gold and bag, between the arrows that switch the buyer, as swiping the shop left or right does. Under
 * it, the items to buy or to sell, one slot per item, as the inventory does. At the bottom, every button the player
 * taps: Back, and a hint until an item is selected, then its name and price with Buy or Sell.
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
        private const val PADDING = 10.0
        private const val SPACING = 8.0
        private const val BAR_HEIGHT = 32.0
        private const val ARROW_WIDTH = 40.0
        private const val BACK_WIDTH = 64.0
        private const val ACTION_WIDTH = 130.0
        private const val ITEM_WIDTH = WIDTH - BACK_WIDTH - ACTION_WIDTH - 2 * SPACING
        private const val HEIGHT = 2 * BAR_HEIGHT + 2 * SLOT_SIZE + SLOT_SPACING + 3 * PADDING

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

        /** Buy or Sell was tapped. */
        fun shopActionSelected()

        fun shopBackSelected()
    }

    private var delegate: Delegate? = null
    internal lateinit var previousBuyerButton: UIButton
    internal lateinit var nextBuyerButton: UIButton
    internal lateinit var buyerText: UIText
    internal val slotViews: List<InventorySlotView> = List(SLOTS) { InventorySlotView(Size(SLOT_SIZE, SLOT_SIZE), resourcesLoader) }
    internal lateinit var backButton: UIButton
    internal lateinit var itemNameText: UIText
    internal lateinit var itemPriceText: UIText
    internal lateinit var actionButton: UIButton

    init {
        // The whole shop can be swiped, also between its slots and buttons.
        solidRect(Size(WIDTH, HEIGHT), Colors.TRANSPARENT)
        uiVerticalStack(padding = PADDING) {
            addChild(header())
            slotViews.chunked(SLOTS_PER_ROW).forEachIndexed { rowIndex, rowSlots ->
                if (rowIndex > 0) uiSpacing(Size(0.0, SLOT_SPACING - PADDING))
                val row = UIContainer(Size(WIDTH, SLOT_SIZE))
                rowSlots.forEachIndexed { columnIndex, slotView -> slotView.x = columnIndex * (SLOT_SIZE + SLOT_SPACING) }
                rowSlots.forEach(row::addChild)
                addChild(row)
            }
            addChild(footer())
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

    /** Displays the [hint] next to Back, e.g.: Tap an item to buy, without Buy nor Sell. */
    open fun displayHint(hint: String) {
        itemNameText.text = hint
        itemPriceText.text = ""
        actionButton.visible = false
    }

    /**
     * Displays the selected item's name and price next to Back, and its action, e.g.: Buy, or why it can not be done
     * instead, disabled, e.g.: Not enough gold.
     */
    open fun displayItem(
        itemName: String,
        itemPrice: Int,
        actionLabel: String,
        isActionEnabled: Boolean,
    ) {
        itemNameText.text = itemName
        itemPriceText.text = texts.gold(itemPrice)
        actionButton.text = actionLabel
        actionButton.enabled = isActionEnabled
        actionButton.visible = true
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

    // Back  Hammer ....... 50 G  Buy
    private fun footer(): UIContainer =
        UIContainer(Size(WIDTH, BAR_HEIGHT)).apply {
            backButton = button(texts[UiText.SHOP_BACK], BACK_WIDTH) { delegate?.shopBackSelected() }
            val itemX = BACK_WIDTH + SPACING
            itemNameText =
                uiText("", size = Size(ITEM_WIDTH, BAR_HEIGHT)) {
                    styles.textAlignment = TextAlignment.MIDDLE_LEFT
                    position(itemX, 0.0)
                }
            itemPriceText =
                uiText("", size = Size(ITEM_WIDTH, BAR_HEIGHT)) {
                    styles.textAlignment = TextAlignment.MIDDLE_RIGHT
                    position(itemX, 0.0)
                }
            actionButton = button("", ACTION_WIDTH) { delegate?.shopActionSelected() }.position(WIDTH - ACTION_WIDTH, 0.0)
            actionButton.visible = false
        }

    private fun Container.button(
        label: String,
        width: Double,
        onSelected: () -> Unit,
    ): UIButton =
        uiButton(label, size = Size(width, BAR_HEIGHT)) {
            bgColorOut = Colors.DIMGRAY
            bgColorOver = RGBA(85, 85, 85)
            background.borderColor = Colors.LIGHTGRAY
            onClick { onSelected() }
        }
}
