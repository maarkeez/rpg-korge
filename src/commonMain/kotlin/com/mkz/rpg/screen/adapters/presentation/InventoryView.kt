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
import korlibs.korge.view.position
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/**
 * The items stored by the selected battle unit, one slot per storage space, and its gold on the title line. Displayed in the HUD instead of the
 * battle unit info.
 *
 * While exploring, the title line names the party member instead, between the arrows that switch to the previous or
 * next member, as swiping the inventory left or right does: `< Knight ........ 20 G >`.
 */
open class InventoryView(
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
        private const val BAR_HEIGHT = 32.0
        private const val CLOSE_WIDTH = 80.0
        private const val ARROW_WIDTH = 40.0
        private const val TITLE_SPACING = 10.0
        private const val TITLE_LINE_WIDTH = WIDTH - CLOSE_WIDTH
        private const val TEXT_HEIGHT = 20.0

        // The title and the gold leave room for both arrows, spaced apart and from Close, which hide while their space is not needed.
        private const val TEXT_WIDTH = TITLE_LINE_WIDTH - 2 * ARROW_WIDTH - 3 * TITLE_SPACING
        private const val HEIGHT = BAR_HEIGHT + 2 * SLOT_SIZE + SLOT_SPACING + PADDING

        // How far the inventory is dragged left or right to switch the party member.
        const val SWIPE_THRESHOLD = 40.0
    }

    /** A stored item, and whether it is the equipped copy. */
    data class Slot(
        val lootableItemId: String,
        val isEquipped: Boolean,
    )

    /** The party member whose inventory is displayed while exploring, and its position in the party, from the leader. */
    data class Owner(
        val name: String,
        val partyIndex: Int,
        val partySize: Int,
    )

    interface Delegate {
        fun slotSelected(index: Int)

        /** The inventory was switched to the party member on its left ([step] -1) or right ([step] 1), with an arrow or a swipe. */
        fun inventoryOwnerStepped(step: Int)

        fun inventoryClosed()
    }

    private var delegate: Delegate? = null
    internal lateinit var previousOwnerButton: UIButton
    internal lateinit var nextOwnerButton: UIButton
    internal lateinit var titleView: UnitNameView
    internal lateinit var goldView: UnitNameView
    internal lateinit var closeButton: UIButton
    internal val slotViews: List<InventorySlotView> = List(SLOTS) { InventorySlotView(Size(SLOT_SIZE, SLOT_SIZE), resourcesLoader) }

    init {
        // The whole inventory can be swiped, also between its slots and buttons.
        solidRect(Size(WIDTH, HEIGHT), Colors.TRANSPARENT)
        uiVerticalStack(padding = PADDING) {
            uiHorizontalStack {
                addChild(titleLine())
                closeButton = button(texts[UiText.INVENTORY_CLOSE], CLOSE_WIDTH) { delegate?.inventoryClosed() }
            }
            slotViews.chunked(SLOTS_PER_ROW).forEachIndexed { rowIndex, rowSlots ->
                if (rowIndex > 0) uiSpacing(Size(0.0, SLOT_SPACING - PADDING))
                val row = UIContainer(Size(WIDTH, SLOT_SIZE))
                rowSlots.forEachIndexed { columnIndex, slotView -> slotView.x = columnIndex * (SLOT_SIZE + SLOT_SPACING) }
                rowSlots.forEach(row::addChild)
                addChild(row)
            }
        }
        slotViews.forEachIndexed { index, slotView -> slotView.setOnSelected { delegate?.slotSelected(index) } }
        onSwipe(threshold = SWIPE_THRESHOLD) { swipe ->
            when (swipe.direction) {
                SwipeDirection.LEFT -> swiped(-1)
                SwipeDirection.RIGHT -> swiped(1)
                else -> Unit
            }
        }
        displayTitle(owner = null)
    }

    open fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    internal fun swiped(step: Int) {
        delegate?.inventoryOwnerStepped(step)
    }

    /**
     * Displays the stored items in their order, followed by the empty storage spaces, and the gold. The [owner], while
     * exploring, is named between the arrows, which are disabled at the ends of the party. Clears the selected slot.
     */
    open fun display(
        slots: List<Slot>,
        gold: Int,
        owner: Owner? = null,
    ) {
        displayTitle(owner)
        goldView.display(texts.gold(gold))
        slotViews.forEachIndexed { index, slotView ->
            val slot = slots.getOrNull(index)
            slotView.display(lootableItemId = slot?.lootableItemId, isEquipped = slot?.isEquipped ?: false)
        }
    }

    /** Frames the selected slot, or none when [index] is `null`. */
    open fun selectSlot(index: Int?) {
        slotViews.forEachIndexed { slotIndex, slotView -> if (slotIndex == index) slotView.select() else slotView.unselect() }
    }

    // Inventory ........ 20 G, or < Knight ........ 20 G > while exploring. The gold stays right before Close, or before the next arrow.
    private fun displayTitle(owner: Owner?) {
        val hasArrows = owner != null
        previousOwnerButton.visible = hasArrows
        nextOwnerButton.visible = hasArrows
        previousOwnerButton.enabled = owner != null && owner.partyIndex > 0
        nextOwnerButton.enabled = owner != null && owner.partyIndex < owner.partySize - 1
        titleView.x = if (hasArrows) ARROW_WIDTH + TITLE_SPACING else 0.0
        goldView.x = if (hasArrows) ARROW_WIDTH + TITLE_SPACING else TITLE_LINE_WIDTH - TITLE_SPACING - TEXT_WIDTH
        titleView.display(owner?.name ?: texts[UiText.INVENTORY_TITLE])
    }

    private fun titleLine(): UIContainer =
        UIContainer(Size(TITLE_LINE_WIDTH, BAR_HEIGHT)).apply {
            previousOwnerButton = button("<", ARROW_WIDTH) { delegate?.inventoryOwnerStepped(-1) }
            val textY = (BAR_HEIGHT - TEXT_HEIGHT) / 2
            titleView = UnitNameView(Size(TEXT_WIDTH, TEXT_HEIGHT)).position(0.0, textY)
            addChild(titleView)
            goldView = UnitNameView(Size(TEXT_WIDTH, TEXT_HEIGHT)).position(0.0, textY)
            goldView.alignTextToRight()
            addChild(goldView)
            nextOwnerButton = button(">", ARROW_WIDTH) { delegate?.inventoryOwnerStepped(1) }.position(TITLE_LINE_WIDTH - TITLE_SPACING - ARROW_WIDTH, 0.0)
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
