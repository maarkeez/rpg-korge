package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.GameButton
import com.mkz.tessavar.shared.adapters.presentation.gameButton
import korlibs.image.color.RGBA
import korlibs.korge.input.onClick
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIContainer
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.Container
import korlibs.math.geom.Size

class ItemButtonsView(
    messages: Messages,
    itemName: String,
    itemSellPrice: Int,
    actions: List<Button>,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val SPACING = 10.0
    }

    data class Button(
        val label: String,
        val isEnabled: Boolean = true,
        val color: RGBA = GameButton.GREY,
        val pressedColor: RGBA = GameButton.GREY_OVER,
        val onSelected: () -> Unit,
    )

    internal val itemNameView = UnitNameView(Size(WIDTH, 16))
    internal val itemSellPriceView = UnitNameView(Size(WIDTH, 16))
    internal var gameButtons: List<GameButton> = emptyList()

    init {
        uiVerticalStack(padding = 4.0) {
            val nameLine = UIContainer(Size(WIDTH, 16))
            nameLine.addChild(itemNameView)
            itemSellPriceView.alignTextToRight()
            nameLine.addChild(itemSellPriceView)
            addChild(nameLine)
            uiHorizontalStack(padding = SPACING) {
                val buttonWidth = (WIDTH - SPACING * (actions.size - 1)) / actions.size
                gameButtons =
                    actions.map { action ->
                        gameButton(action.label, Size(buttonWidth, UIButton.DEFAULT_SIZE.height), action.color, action.pressedColor) {
                            enabled = action.isEnabled
                            onClick { action.onSelected() }
                        }
                    }
            }
        }
        itemNameView.display(itemName)
        itemSellPriceView.display(messages.gold(itemSellPrice))
    }
}
