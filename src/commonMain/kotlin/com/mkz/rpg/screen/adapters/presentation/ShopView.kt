package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiSpacing
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

/**
 * The items a merchant sells, one slot per item, as the inventory displays the stored ones. Displayed in place of the
 * merchant's dialog once it is tapped, until it is closed.
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
        private const val CLOSE_WIDTH = 80.0
    }

    interface Delegate {
        fun shopSlotSelected(index: Int)

        fun shopClosed()
    }

    private var delegate: Delegate? = null
    internal lateinit var titleView: UnitNameView
    internal lateinit var closeButton: UIButton
    internal val slotViews: List<InventorySlotView> = List(SLOTS) { InventorySlotView(Size(SLOT_SIZE, SLOT_SIZE), resourcesLoader) }

    init {
        uiVerticalStack(padding = 10.0) {
            uiHorizontalStack {
                titleView = UnitNameView(size = Size(width = WIDTH - CLOSE_WIDTH, height = 20))
                addChild(titleView)
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
        }
        slotViews.forEachIndexed { index, slotView -> slotView.setOnSelected { delegate?.shopSlotSelected(index) } }
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Displays the merchant's name and the items it sells in their order, followed by the empty slots. Clears the selected slot. */
    open fun display(
        merchantName: String,
        lootableItemIds: List<String>,
    ) {
        titleView.display(merchantName)
        slotViews.forEachIndexed { index, slotView -> slotView.display(lootableItemId = lootableItemIds.getOrNull(index), isEquipped = false) }
    }

    /** Frames the selected slot, or none when [index] is `null`. */
    open fun selectSlot(index: Int?) {
        slotViews.forEachIndexed { slotIndex, slotView -> if (slotIndex == index) slotView.select() else slotView.unselect() }
    }
}
