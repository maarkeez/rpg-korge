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
import korlibs.math.geom.Size

/**
 * The name and price of the selected item and what the player can do with it, e.g.: Consume, Discard or Cancel. Without
 * actions (e.g.: an item a merchant sells), only its name and price are displayed.
 */
class ItemActionsView(
    itemName: String,
    itemPrice: Int,
    actions: List<Action>,
) : Container() {
    companion object {
        private const val WIDTH = 390.0
        private const val SPACING = 10.0
    }

    /** A disabled action is still displayed, greyed out, but can not be clicked. */
    data class Action(
        val label: String,
        val isEnabled: Boolean = true,
        val onSelected: () -> Unit,
    )

    internal val itemNameView = UnitNameView(Size(WIDTH, 16))
    internal val itemPriceView = UnitNameView(Size(WIDTH, 16))
    internal var actionButtons: List<UIButton> = emptyList()

    init {
        uiVerticalStack(padding = 4.0) {
            // The price shares the line of the name, aligned to the right.
            val nameLine = UIContainer(Size(WIDTH, 16))
            nameLine.addChild(itemNameView)
            itemPriceView.alignTextToRight()
            nameLine.addChild(itemPriceView)
            addChild(nameLine)
            if (actions.isNotEmpty()) {
                uiHorizontalStack(padding = SPACING) {
                    val buttonWidth = (WIDTH - SPACING * (actions.size - 1)) / actions.size
                    actionButtons =
                        actions.map { action ->
                            uiButton(action.label) {
                                width = buttonWidth
                                bgColorOut = Colors.DIMGRAY
                                bgColorOver = RGBA(85, 85, 85)
                                background.borderColor = Colors.LIGHTGRAY
                                enabled = action.isEnabled
                                onClick { action.onSelected() }
                            }
                        }
                }
            }
        }
        itemNameView.display(itemName)
        itemPriceView.display(goldText(itemPrice))
    }
}
