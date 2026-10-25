package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

/** Asks the player to tap the party member who receives the item, or to cancel giving it. */
class GiveItemView(
    itemName: String,
    onCancelled: () -> Unit,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
    }

    internal val messageView = UnitNameView(Size(WIDTH, 16))
    internal lateinit var cancelButton: UIButton

    init {
        uiVerticalStack(padding = 4.0) {
            addChild(messageView)
            cancelButton =
                uiButton("Cancel") {
                    width = WIDTH
                    bgColorOut = Colors.DIMGRAY
                    bgColorOver = RGBA(85, 85, 85)
                    background.borderColor = Colors.LIGHTGRAY
                    onClick { onCancelled() }
                }
        }
        messageView.display("Tap a party member to give the $itemName")
    }
}
