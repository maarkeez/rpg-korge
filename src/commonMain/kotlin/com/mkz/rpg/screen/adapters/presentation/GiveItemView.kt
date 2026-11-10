package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class GiveItemView(
    messages: Messages,
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
                uiButton(messages[UiMessage.GIVE_ITEM_CANCEL]) {
                    width = WIDTH
                    bgColorOut = Colors.DIMGRAY
                    bgColorOver = RGBA(85, 85, 85)
                    background.borderColor = Colors.LIGHTGRAY
                    onClick { onCancelled() }
                }
        }
        messageView.display(messages[UiMessage.GIVE_ITEM_MESSAGE, "item" to itemName])
    }
}
