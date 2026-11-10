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

open class ShopView(
    resourcesLoader: GameSceneResourcesLoader,
    private val messages: Messages,
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

        private const val GAP = 27.0
        private const val SLOTS_Y = GAP + BAR_HEIGHT + GAP
        private const val ITEM_Y = SLOTS_Y + 2 * SLOT_SIZE + SLOT_SPACING + GAP

        const val SWIPE_THRESHOLD = 40.0
    }

    enum class Mode { BUY, SELL }

    data class Slot(
        val lootableItemId: String,
        val isEquipped: Boolean,
        val isTradeable: Boolean,
    )

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

    open fun display(
        slots: List<Slot>,
        buyer: Buyer,
    ) {
        slotViews.forEachIndexed { index, slotView ->
            val slot = slots.getOrNull(index)
            slotView.display(lootableItemId = slot?.lootableItemId, isEquipped = slot?.isEquipped ?: false, isDimmed = slot?.isTradeable == false)
        }
        val inventory =
            if (buyer.usedStorageSpace >= buyer.maximumStorageSpace) {
                messages[UiMessage.SHOP_INVENTORY_FULL]
            } else {
                messages[UiMessage.SHOP_INVENTORY, "used" to buyer.usedStorageSpace, "maximum" to buyer.maximumStorageSpace]
            }
        buyerText.text = messages[UiMessage.SHOP_BUYER, "name" to buyer.name, "gold" to messages.gold(buyer.gold), "inventory" to inventory]
        previousBuyerButton.enabled = buyer.partyIndex > 0
        nextBuyerButton.enabled = buyer.partyIndex < buyer.partySize - 1
        selectSlot(null)
    }

    open fun selectSlot(index: Int?) {
        slotViews.forEachIndexed { slotIndex, slotView -> if (slotIndex == index) slotView.select() else slotView.unselect() }
    }

    open fun displayHint(hint: String) {
        itemNameText.text = hint
        itemPriceText.text = ""
    }

    open fun displayItem(
        itemName: String,
        itemPrice: Int,
    ) {
        itemNameText.text = itemName
        itemPriceText.text = messages.gold(itemPrice)
    }

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
