package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.View

open class PlayerCallToActionView(
    private val resourcesLoader: BattleSceneResourcesLoader = BattleSceneResourcesLoader(),
) : Container() {
    companion object {
        const val CALL_TO_ACTION = "CALL_TO_ACTION"
    }

    private var onTurnFinished: (() -> Unit)? = null
    private var isFinishTurnEnabled = true

    open fun hide() {
        visible = false
    }

    open fun displayFinishTurn(onTurnFinished: () -> Unit) {
        this.onTurnFinished = onTurnFinished
        val finishTurnView = FinishTurnView(onTurnFinished = onTurnFinished)
        finishTurnView.isEnabled = isFinishTurnEnabled
        display(finishTurnView)
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
                    displayFinishTurn(onTurnFinished = onTurnFinished!!)
                },
                onConfirmed = {
                    onConfirmed()
                    displayFinishTurn(onTurnFinished = onTurnFinished!!)
                },
            )
        display(cancelAndConfirmView)
    }

    /** Displays the finish turn again, e.g.: once the player is done with an inventory item. */
    open fun redisplayFinishTurn() {
        displayFinishTurn(onTurnFinished = onTurnFinished!!)
    }

    /**
     * Displays the name and price of the selected item and its actions, if any (e.g.: none for an item a merchant sells).
     * The finish turn is displayed again once an action is selected.
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
                        redisplayFinishTurn()
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
