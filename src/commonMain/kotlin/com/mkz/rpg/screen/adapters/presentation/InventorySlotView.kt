package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.Container
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size

/** One storage space of the inventory: empty, or the icon of the stored item, framed in gold while it is equipped. */
class InventorySlotView(
    private val slotSize: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIButton(slotSize) {
    companion object {
        const val ITEM = "ITEM"
        const val EQUIPPED_FRAME = "EQUIPPED_FRAME"
        const val SELECTION_FRAME = "SELECTION_FRAME"

        // FamiCube palette colours: gold like the cast points, and white like the steps.
        val EQUIPPED_COLOR = RGBA(0xFF, 0xBB, 0x31)
        val SELECTION_COLOR = Colors.WHITE
        val EMPTY_COLOR = RGBA(0x34, 0x34, 0x34)

        private const val FRAME_THICKNESS = 3.0

        // How visible a dimmed item is, e.g.: an item the buyer can not trade at a merchant.
        const val DIMMED_ALPHA = 0.35
    }

    private var onSelected: (() -> Unit)? = null
    internal var isDimmed = false
        private set

    init {
        bgColorOut = EMPTY_COLOR
        bgColorOver = EMPTY_COLOR
        background.borderColor = Colors.TRANSPARENT
        onClick { onSelected?.invoke() }
    }

    fun setOnSelected(onSelected: () -> Unit) {
        this.onSelected = onSelected
    }

    /** Displays the stored item, dimmed or not, or an empty space when [lootableItemId] is `null`. */
    fun display(
        lootableItemId: String?,
        isEquipped: Boolean,
        isDimmed: Boolean = false,
    ) {
        findViewByName(ITEM)?.removeFromParent()
        findViewByName(EQUIPPED_FRAME)?.removeFromParent()
        unselect()
        this.isDimmed = lootableItemId != null && isDimmed
        val slot = this
        lootableItemId?.let { resourcesLoader.bitmapOrNull("lootableItem/$it.png") }?.let { itemBitmap ->
            slot.image(itemBitmap) {
                name = ITEM
                smoothing = false
                scale = 3.0
                alpha = if (isDimmed) DIMMED_ALPHA else 1.0
                centerOn(slot)
            }
        }
        if (isEquipped) addFrame(EQUIPPED_FRAME, EQUIPPED_COLOR, inset = 0.0)
    }

    fun select() {
        if (findViewByName(SELECTION_FRAME) == null) addFrame(SELECTION_FRAME, SELECTION_COLOR, inset = FRAME_THICKNESS)
    }

    fun unselect() {
        findViewByName(SELECTION_FRAME)?.removeFromParent()
    }

    // A hollow square, drawn [inset] pixels inside the slot edges.
    private fun addFrame(
        frameName: String,
        color: RGBA,
        inset: Double,
    ) {
        val width = slotSize.width - 2 * inset
        val height = slotSize.height - 2 * inset
        container {
            name = frameName
            x = inset
            y = inset
            addSide(width, FRAME_THICKNESS, 0.0, 0.0, color)
            addSide(width, FRAME_THICKNESS, 0.0, height - FRAME_THICKNESS, color)
            addSide(FRAME_THICKNESS, height, 0.0, 0.0, color)
            addSide(FRAME_THICKNESS, height, width - FRAME_THICKNESS, 0.0, color)
        }
    }

    private fun Container.addSide(
        width: Double,
        height: Double,
        x: Double,
        y: Double,
        color: RGBA,
    ) {
        solidRect(Size(width, height), color) {
            this.x = x
            this.y = y
        }
    }
}
