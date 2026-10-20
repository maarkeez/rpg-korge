package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the call to action view to display, instead of drawing it.
// The player actions are simulated by calling the callbacks it was given (e.g.: `onTurnFinished`).
class FakePlayerCallToActionView : PlayerCallToActionView() {
    var onTurnFinished: (() -> Unit)? = null
        private set
    var onCancelled: (() -> Unit)? = null
        private set
    var onConfirmed: (() -> Unit)? = null
        private set
    val finishTurnEnabledChanges = mutableListOf<Boolean>()

    data class DisplayedItemActions(
        val itemName: String,
        val itemPrice: Int,
        val actions: List<ItemActionsView.Action>,
    )

    data class DisplayedInventoryFull(
        val foundItemName: String,
        val foundItemPrice: Int,
        val foundItemId: String,
        val onLeft: () -> Unit,
    )

    // What the call to action displays last, e.g.: the item actions. `null` while it displays the finish turn.
    var displayedItemActions: DisplayedItemActions? = null
        private set
    var displayedInventoryFull: DisplayedInventoryFull? = null
        private set
    var hideCount = 0
        private set

    val isFinishTurnEnabled: Boolean
        get() = finishTurnEnabledChanges.lastOrNull() ?: false

    override fun hide() {
        hideCount++
    }

    override fun displayFinishTurn(onTurnFinished: () -> Unit) {
        this.onTurnFinished = onTurnFinished
    }

    override fun redisplayFinishTurn() {
        displayedItemActions = null
        displayedInventoryFull = null
    }

    // As the real view, the finish turn is displayed again once an action is selected.
    override fun displayItemActions(
        itemName: String,
        itemPrice: Int,
        actions: List<ItemActionsView.Action>,
        redisplaysFinishTurn: Boolean,
    ) {
        displayedInventoryFull = null
        displayedItemActions =
            DisplayedItemActions(
                itemName = itemName,
                itemPrice = itemPrice,
                actions =
                    actions.map { action ->
                        action.copy(onSelected = {
                            action.onSelected()
                            if (redisplaysFinishTurn) redisplayFinishTurn()
                        })
                    },
            )
    }

    override fun displayInventoryFull(
        foundItemName: String,
        foundItemPrice: Int,
        foundItemId: String,
        onLeft: () -> Unit,
    ) {
        displayedItemActions = null
        displayedInventoryFull = DisplayedInventoryFull(foundItemName, foundItemPrice, foundItemId, onLeft)
    }

    override fun setFinishTurnEnabled(enabled: Boolean) {
        finishTurnEnabledChanges += enabled
    }

    override fun displayCancelAndConfirm(
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        this.onCancelled = onCancelled
        this.onConfirmed = onConfirmed
    }
}
