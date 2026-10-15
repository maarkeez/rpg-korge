package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.image
import korlibs.math.geom.Size

/** Opens the inventory of the selected battle unit. Displayed with the chest drawn on the tiles with several items. */
class BagButtonView(
    size: Size,
    private val resourcesLoader: BattleSceneResourcesLoader,
) : UIButton(size) {
    companion object {
        const val BAG = "BAG"
    }

    private var onBagSelected: (() -> Unit)? = null

    init {
        bgColorOut = Colors.TRANSPARENT
        bgColorOver = Colors.TRANSPARENT
        background.borderColor = Colors.TRANSPARENT
        background.bgColor = Colors.TRANSPARENT
        background.shadowColor = Colors.TRANSPARENT
        visible = false
        onClick { onBagSelected?.invoke() }
    }

    fun display(onBagSelected: () -> Unit) {
        this.onBagSelected = onBagSelected
        // Drawn once displayed, as the images are loaded after the views are created.
        if (findViewByName(BAG) == null) {
            val bagButton = this
            resourcesLoader.bitmapOrNull(BattlefieldView.CHEST_PATH)?.let { chestBitmap ->
                bagButton.image(chestBitmap) {
                    name = BAG
                    smoothing = false
                    scale = 3.0
                    centerOn(bagButton)
                }
            }
        }
        visible = true
    }

    fun hide() {
        onBagSelected = null
        visible = false
    }
}
