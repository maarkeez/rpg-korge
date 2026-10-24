package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.View

open class PlayerCallToActionView(
    private val resourcesLoader: BattleSceneResourcesLoader = BattleSceneResourcesLoader(),
) : Container() {
    companion object {
        const val CALL_TO_ACTION = "CALL_TO_ACTION"
    }

    // The finish turn, displayed when the player has nothing else to choose. `null` out of the battles (e.g.: while
    // exploring), when nothing is displayed then.
    private var onTurnFinished: (() -> Unit)? = null
    private var isFinishTurnEnabled = true

    open fun hide() {
        visible = false
    }

    /** Displays the finish turn, which is displayed again once the player has chosen, e.g.: cancelled a cast. */
    open fun displayFinishTurn(onTurnFinished: () -> Unit) {
        this.onTurnFinished = onTurnFinished
        val finishTurnView = FinishTurnView(onTurnFinished = onTurnFinished)
        finishTurnView.isEnabled = isFinishTurnEnabled
        display(finishTurnView)
    }

    /** Forgets the finish turn, e.g.: once the battle finishes, so nothing is displayed once the player has chosen. */
    open fun hideFinishTurn() {
        onTurnFinished = null
        (callToAction as? FinishTurnView)?.removeFromParent()
    }

    /** Enables or disables the finish turn, also when it is displayed again later (e.g.: after cancelling a cast). */
    open fun setFinishTurnEnabled(enabled: Boolean) {
        isFinishTurnEnabled = enabled
        (callToAction as? FinishTurnView)?.isEnabled = enabled
    }

    open fun displayCancelAndConfirm(
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        val cancelAndConfirmView =
            CancelAndConfirmView(
                onCancelled = {
                    onCancelled()
                    dismiss()
                },
                onConfirmed = {
                    onConfirmed()
                    dismiss()
                },
            )
        display(cancelAndConfirmView)
    }

    /**
     * Displays the finish turn again once the player is done choosing, e.g.: with an inventory item. Nothing is displayed
     * out of the battles.
     */
    open fun dismiss() {
        val onTurnFinished = onTurnFinished
        if (onTurnFinished == null) {
            callToAction?.removeFromParent()
        } else {
            displayFinishTurn(onTurnFinished = onTurnFinished)
        }
    }

    /**
     * Displays the name and price of the selected item and its actions, e.g.: Consume. It is dismissed once an action is
     * selected.
     */
    open fun displayItemActions(
        itemName: String,
        itemPrice: Int,
        actions: List<ItemActionsView.Action>,
    ) {
        val itemActions =
            actions.map { action ->
                action.copy(
                    onSelected = {
                        action.onSelected()
                        dismiss()
                    },
                )
            }
        display(ItemActionsView(itemName = itemName, itemPrice = itemPrice, actions = itemActions))
    }

    /** Asks the player what to do with an item found while the inventory is full. */
    open fun displayInventoryFull(
        foundItemName: String,
        foundItemPrice: Int,
        foundItemId: String,
        onLeft: () -> Unit,
    ) {
        display(InventoryFullView(foundItemName = foundItemName, foundItemPrice = foundItemPrice, foundItemId = foundItemId, resourcesLoader = resourcesLoader, onLeft = onLeft))
    }

    internal val callToAction: View?
        get() = findViewByName(CALL_TO_ACTION)

    private fun display(view: View) {
        view.name = CALL_TO_ACTION
        callToAction?.removeFromParent()
        addChild(view)
    }
}
