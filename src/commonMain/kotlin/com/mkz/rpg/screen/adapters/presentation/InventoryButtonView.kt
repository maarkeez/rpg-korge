package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

/** Opens the inventory of the selected battle unit. Displayed with the chest drawn on the tiles with several items, at 2x in a slot. */
class InventoryButtonView(
    size: Size,
    private val resourcesLoader: GameSceneResourcesLoader,
) : UIButton(size) {
    companion object {
        const val CHEST = "CHEST"
    }

    private var onInventoryButtonSelected: (() -> Unit)? = null

    init {
        bgColorOut = Colors.TRANSPARENT
        bgColorOver = Colors.TRANSPARENT
        background.borderColor = Colors.TRANSPARENT
        background.bgColor = Colors.TRANSPARENT
        background.shadowColor = Colors.TRANSPARENT
        panel(size, fill = HudStyle.PANEL_RAISED, radius = HudStyle.SLOT_RADIUS)
        visible = false
        onClick { onInventoryButtonSelected?.invoke() }
    }

    fun display(onInventoryButtonSelected: () -> Unit) {
        this.onInventoryButtonSelected = onInventoryButtonSelected
        // Drawn once displayed, as the images are loaded after the views are created.
        if (findViewByName(CHEST) == null) {
            val inventoryButton = this
            resourcesLoader.bitmapOrNull(BattlefieldView.CHEST_PATH)?.let { chestBitmap ->
                inventoryButton.image(chestBitmap) {
                    name = CHEST
                    smoothing = false
                    scale = 2.0
                    centerOn(inventoryButton)
                }
            }
        }
        visible = true
    }

    fun hide() {
        onInventoryButtonSelected = null
        visible = false
    }
}
