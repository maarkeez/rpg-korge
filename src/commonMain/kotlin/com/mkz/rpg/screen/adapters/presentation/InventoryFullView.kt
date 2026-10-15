package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.korge.view.Image
import korlibs.korge.view.image
import korlibs.math.geom.Size

/** Asks the player to tap a stored item to discard it for the found one, or to leave the found one on its tile. */
class InventoryFullView(
    foundItemName: String,
    foundItemId: String,
    resourcesLoader: BattleSceneResourcesLoader,
    onLeft: () -> Unit,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val ICON_SIZE = 32.0
        private const val ICON_SPACING = 8.0
    }

    internal val titleView = UnitNameView(Size(WIDTH - ICON_SIZE - ICON_SPACING, 14))
    internal val messageView = UnitNameView(Size(WIDTH - ICON_SIZE - ICON_SPACING, 14))
    internal var foundItemIcon: Image? = null
    internal lateinit var leaveButton: UIButton

    init {
        uiVerticalStack(padding = 6.0) {
            uiHorizontalStack(padding = ICON_SPACING) {
                // A fixed size box, as the stack would resize the icon.
                val iconBox = UIContainer(Size(ICON_SIZE, ICON_SIZE))
                resourcesLoader.bitmapOrNull("lootableItem/$foundItemId.png")?.let { itemBitmap ->
                    foundItemIcon =
                        iconBox.image(itemBitmap) {
                            smoothing = false
                            scale = ICON_SIZE / itemBitmap.width
                        }
                }
                addChild(iconBox)
                uiVerticalStack(padding = 2.0) {
                    addChild(titleView)
                    addChild(messageView)
                }
            }
            leaveButton =
                uiButton("Leave it") {
                    width = WIDTH
                    bgColorOut = Colors.DIMGRAY
                    bgColorOver = RGBA(85, 85, 85)
                    background.borderColor = Colors.LIGHTGRAY
                    onClick { onLeft() }
                }
        }
        titleView.display("Inventory full")
        messageView.display("Tap an item to swap it for the $foundItemName")
    }
}
