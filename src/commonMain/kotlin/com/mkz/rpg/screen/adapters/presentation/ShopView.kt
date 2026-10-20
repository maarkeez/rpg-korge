package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.SwipeDirection
import korlibs.korge.input.onClick
import korlibs.korge.input.onSwipe
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.circle
import korlibs.korge.view.container
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/**
 * A merchant's shop, displayed in place of its dialog once it is tapped, until it is closed. The Buy tab displays the
 * items the merchant sells, and the Sell tab the items of the buyer, one slot per item, as the inventory does. Under
 * them, the buyer's name, gold and bag, and one dot per party member, the buyer's one filled. Swiping the shop left or
 * right, or tapping a dot, switches the buyer.
 */
open class ShopView(
    resourcesLoader: BattleSceneResourcesLoader,
) : Container() {
    companion object {
        const val SLOTS = 12
        const val SLOTS_PER_ROW = 6
        private const val SLOT_SIZE = 60.0
        private const val SLOT_SPACING = 6.0
        private const val WIDTH = SLOTS_PER_ROW * SLOT_SIZE + (SLOTS_PER_ROW - 1) * SLOT_SPACING
        private const val HEIGHT = 300.0
        private const val TITLE_SPACING = 5.0
        private const val TAB_WIDTH = 60.0
        private const val CLOSE_WIDTH = 70.0
        private const val TITLE_WIDTH = WIDTH - CLOSE_WIDTH - 2 * TAB_WIDTH - 3 * TITLE_SPACING
        private const val DOT_RADIUS = 5.0
        private const val DOT_SIZE = 24.0

        // How far the shop is dragged left or right to switch the buyer.
        const val SWIPE_THRESHOLD = 40.0

        // FamiCube palette colours: blue like the human turn, white like the selection, and grey.
        val ACTIVE_TAB_COLOR = RGBA(0x00, 0x84, 0xFF)
        val BUYER_DOT_COLOR = Colors.WHITE
        val PARTY_MEMBER_DOT_COLOR = RGBA(0x7B, 0x7B, 0x7B)
    }

    enum class Tab { BUY, SELL }

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

        fun shopTabSelected(tab: Tab)

        /** The shop was swiped left ([step] -1) or right ([step] 1). */
        fun shopBuyerSwiped(step: Int)

        fun shopBuyerSelected(partyIndex: Int)

        fun shopClosed()
    }

    private var delegate: Delegate? = null
    internal lateinit var titleView: UnitNameView
    internal lateinit var buyTabButton: UIButton
    internal lateinit var sellTabButton: UIButton
    internal lateinit var closeButton: UIButton
    internal val slotViews: List<InventorySlotView> = List(SLOTS) { InventorySlotView(Size(SLOT_SIZE, SLOT_SIZE), resourcesLoader) }
    internal val buyerView = UnitNameView(Size(WIDTH, 20.0))
    internal val dotsRow = UIContainer(Size(WIDTH, DOT_SIZE))

    // The dot of each party member, in party order.
    internal var dots: List<Container> = emptyList()
        private set
    internal var activeTab: Tab = Tab.BUY
        private set

    init {
        // The whole shop can be swiped, also between its slots and buttons.
        solidRect(Size(WIDTH, HEIGHT), Colors.TRANSPARENT)
        uiVerticalStack(padding = 10.0) {
            uiHorizontalStack(padding = TITLE_SPACING) {
                titleView = UnitNameView(size = Size(width = TITLE_WIDTH, height = 20.0))
                addChild(titleView)
                buyTabButton = tabButton("Buy", Tab.BUY)
                sellTabButton = tabButton("Sell", Tab.SELL)
                closeButton =
                    uiButton("Close") {
                        width = CLOSE_WIDTH
                        bgColorOut = Colors.DIMGRAY
                        bgColorOver = RGBA(85, 85, 85)
                        background.borderColor = Colors.LIGHTGRAY
                        onClick { delegate?.shopClosed() }
                    }
            }
            slotViews.chunked(SLOTS_PER_ROW).forEachIndexed { rowIndex, rowSlots ->
                if (rowIndex > 0) uiSpacing(Size(0.0, SLOT_SPACING - 10.0))
                val row = UIContainer(Size(WIDTH, SLOT_SIZE))
                rowSlots.forEachIndexed { columnIndex, slotView -> slotView.x = columnIndex * (SLOT_SIZE + SLOT_SPACING) }
                rowSlots.forEach(row::addChild)
                addChild(row)
            }
            addChild(buyerView)
            addChild(dotsRow)
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
        delegate?.shopBuyerSwiped(step)
    }

    /**
     * Displays the merchant's name, the active tab, its items in their order followed by the empty slots, and the buyer.
     * Clears the selected slot.
     */
    open fun display(
        merchantName: String,
        tab: Tab,
        slots: List<InventoryView.Slot>,
        buyer: Buyer,
    ) {
        titleView.display(merchantName)
        activeTab = tab
        highlight(buyTabButton, tab == Tab.BUY)
        highlight(sellTabButton, tab == Tab.SELL)
        slotViews.forEachIndexed { index, slotView ->
            val slot = slots.getOrNull(index)
            slotView.display(lootableItemId = slot?.lootableItemId, isEquipped = slot?.isEquipped ?: false)
        }
        displayBuyer(buyer)
    }

    /** Frames the selected slot, or none when [index] is `null`. */
    open fun selectSlot(index: Int?) {
        slotViews.forEachIndexed { slotIndex, slotView -> if (slotIndex == index) slotView.select() else slotView.unselect() }
    }

    // E.g.: Knight · 20 G · Bag 11/12, or Bag full, and one dot per party member.
    private fun displayBuyer(buyer: Buyer) {
        val bag = if (buyer.usedStorageSpace >= buyer.maximumStorageSpace) "Bag full" else "Bag ${buyer.usedStorageSpace}/${buyer.maximumStorageSpace}"
        buyerView.display("${buyer.name} · ${goldText(buyer.gold)} · $bag")
        dots.forEach { dot -> dot.removeFromParent() }
        val dotsX = (WIDTH - buyer.partySize * DOT_SIZE) / 2
        dots =
            List(buyer.partySize) { partyIndex ->
                dotsRow.container {
                    x = dotsX + partyIndex * DOT_SIZE
                    // The whole dot area can be tapped, not only the dot.
                    val area = solidRect(Size(DOT_SIZE, DOT_SIZE), Colors.TRANSPARENT)
                    circle(radius = DOT_RADIUS, fill = if (partyIndex == buyer.partyIndex) BUYER_DOT_COLOR else PARTY_MEMBER_DOT_COLOR) {
                        centerOn(area)
                    }
                    onClick { delegate?.shopBuyerSelected(partyIndex) }
                }
            }
    }

    private fun Container.tabButton(
        label: String,
        tab: Tab,
    ): UIButton =
        uiButton(label) {
            width = TAB_WIDTH
            background.borderColor = Colors.LIGHTGRAY
            onClick { delegate?.shopTabSelected(tab) }
        }

    private fun highlight(
        tabButton: UIButton,
        isActive: Boolean,
    ) {
        tabButton.bgColorOut = if (isActive) ACTIVE_TAB_COLOR else Colors.DIMGRAY
        tabButton.bgColorOver = if (isActive) ACTIVE_TAB_COLOR else RGBA(85, 85, 85)
    }
}
