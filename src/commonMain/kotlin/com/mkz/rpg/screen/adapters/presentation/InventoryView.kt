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
 * The items stored by the selected battle unit, one slot per storage space, and its gold on the title line. Displayed in the HUD instead of the
 * battle unit info.
 */
open class InventoryView(
    resourcesLoader: BattleSceneResourcesLoader,
) : Container() {
    companion object {
        const val SLOTS = 12
        const val SLOTS_PER_ROW = 6
        private const val SLOT_SIZE = 60.0
        private const val SLOT_SPACING = 6.0
        private const val WIDTH = SLOTS_PER_ROW * SLOT_SIZE + (SLOTS_PER_ROW - 1) * SLOT_SPACING
        private const val CLOSE_WIDTH = 80.0
        private const val TITLE_SPACING = 10.0
    }

    /** A stored item, and whether it is the equipped copy. */
    data class Slot(
        val lootableItemId: String,
        val isEquipped: Boolean,
    )

    interface Delegate {
        fun slotSelected(index: Int)

        fun inventoryClosed()
    }

    private var delegate: Delegate? = null
    internal lateinit var titleView: UnitNameView
    internal lateinit var goldView: UnitNameView
    internal lateinit var closeButton: UIButton
    internal val slotViews: List<InventorySlotView> = List(SLOTS) { InventorySlotView(Size(SLOT_SIZE, SLOT_SIZE), resourcesLoader) }

    init {
        uiVerticalStack(padding = 10.0) {
            uiHorizontalStack {
                // The gold shares the line of the title, aligned to the right, before Close.
                val titleSize = Size(width = WIDTH - CLOSE_WIDTH - TITLE_SPACING, height = 20.0)
                val titleLine = UIContainer(Size(WIDTH - CLOSE_WIDTH, 20.0))
                titleView = UnitNameView(size = titleSize)
                titleLine.addChild(titleView)
                goldView = UnitNameView(size = titleSize)
                goldView.alignTextToRight()
                titleLine.addChild(goldView)
                addChild(titleLine)
                closeButton =
                    uiButton("Close") {
                        width = CLOSE_WIDTH
                        bgColorOut = Colors.DIMGRAY
                        bgColorOver = RGBA(85, 85, 85)
                        background.borderColor = Colors.LIGHTGRAY
                        onClick { delegate?.inventoryClosed() }
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
        slotViews.forEachIndexed { index, slotView -> slotView.setOnSelected { delegate?.slotSelected(index) } }
        titleView.display("Inventory")
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    /** Displays the stored items in their order, followed by the empty storage spaces, and the gold. Clears the selected slot. */
    open fun display(
        slots: List<Slot>,
        gold: Int,
    ) {
        goldView.display(goldText(gold))
        slotViews.forEachIndexed { index, slotView ->
            val slot = slots.getOrNull(index)
            slotView.display(lootableItemId = slot?.lootableItemId, isEquipped = slot?.isEquipped ?: false)
        }
    }

    /** Frames the selected slot, or none when [index] is `null`. */
    open fun selectSlot(index: Int?) {
        slotViews.forEachIndexed { slotIndex, slotView -> if (slotIndex == index) slotView.select() else slotView.unselect() }
    }
}
