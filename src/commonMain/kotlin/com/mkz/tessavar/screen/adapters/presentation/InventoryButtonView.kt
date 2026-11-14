package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

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
