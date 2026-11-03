package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the call to action view to display, instead of drawing it.
// The player actions are simulated by calling the callbacks it was given (e.g.: `onTurnFinished`).
class FakePlayerCallToActionView : PlayerCallToActionView(TextsMother.texts()) {
    // The finish turn the presenters displayed, `null` while it is hidden (e.g.: while exploring).
    var onTurnFinished: (() -> Unit)? = null
        private set
    var onCancelled: (() -> Unit)? = null
        private set
    var onConfirmed: (() -> Unit)? = null
        private set
    val finishTurnEnabledChanges = mutableListOf<Boolean>()

    data class DisplayedItemActions(
        val itemName: String,
        val itemSellPrice: Int,
        val actions: List<ItemActionsView.Action>,
    )

    data class DisplayedDiscardQuestion(
        val itemName: String,
        val onBack: () -> Unit,
        val onDiscarded: () -> Unit,
    )

    data class DisplayedInventoryFull(
        val foundItemName: String,
        val foundItemSellPrice: Int,
        val foundItemId: String,
        val onLeft: () -> Unit,
    )

    data class DisplayedGiveItem(
        val itemName: String,
        val onCancelled: () -> Unit,
    )

    data class DisplayedGiveGold(
        val amount: Int,
        val maximum: Int,
        val onCancelled: () -> Unit,
        val onAll: () -> Unit,
        val onGiven: () -> Unit,
    )

    data class DisplayedGiveGoldRefusal(
        val message: String,
        val onDismissed: () -> Unit,
    )

    // What the call to action displays last, e.g.: the item actions. `null` once dismissed.
    var displayedItemActions: DisplayedItemActions? = null
        private set
    var displayedDiscardQuestion: DisplayedDiscardQuestion? = null
        private set
    var displayedInventoryFull: DisplayedInventoryFull? = null
        private set
    var displayedGiveItem: DisplayedGiveItem? = null
        private set
    var displayedGiveGold: DisplayedGiveGold? = null
        private set
    var displayedGiveGoldRefusal: DisplayedGiveGoldRefusal? = null
        private set
    var displayedActions: List<ActionRowView.Action>? = null
        private set
    var hideCount = 0
        private set

    val isFinishTurnEnabled: Boolean
        get() = finishTurnEnabledChanges.lastOrNull() ?: false

    /** The labels of the buttons in the row, e.g.: Back and Abandon. Empty when no row is displayed. */
    fun actionLabels(): List<String> = displayedActions.orEmpty().map { action -> action.label }

    /** Taps the button of the row with the label. */
    fun chooseAction(label: String) = requireNotNull(displayedActions) { "It displays no row" }.single { action -> action.label == label }.onSelected()

    override fun hide() {
        hideCount++
    }

    override fun displayFinishTurn(onTurnFinished: () -> Unit) {
        this.onTurnFinished = onTurnFinished
    }

    override fun hideFinishTurn() {
        onTurnFinished = null
    }

    override fun dismiss() {
        displayedItemActions = null
        displayedDiscardQuestion = null
        displayedInventoryFull = null
        displayedGiveItem = null
        displayedGiveGold = null
        displayedGiveGoldRefusal = null
        displayedActions = null
    }

    // As the real view, a tap does not dismiss it, and it is displayed also when the pad had hidden the call to action.
    override fun displayActions(actions: List<ActionRowView.Action>) {
        dismiss()
        displayedActions = actions
        visible = true
    }

    override fun hideActions() {
        displayedActions = null
    }

    override fun displayGiveGold(
        amount: Int,
        maximum: Int,
        onCancelled: () -> Unit,
        onAll: () -> Unit,
        onGiven: () -> Unit,
    ) {
        dismiss()
        displayedGiveGold = DisplayedGiveGold(amount, maximum, onCancelled, onAll, onGiven)
    }

    override fun updateGiveGold(
        amount: Int,
        maximum: Int,
    ) {
        displayedGiveGold = displayedGiveGold?.copy(amount = amount, maximum = maximum)
    }

    // As the real view, OK dismisses it.
    override fun displayGiveGoldRefusal(
        message: String,
        onDismissed: () -> Unit,
    ) {
        dismiss()
        displayedGiveGoldRefusal =
            DisplayedGiveGoldRefusal(message) {
                onDismissed()
                dismiss()
            }
    }

    // As the real view, it is dismissed once an action is selected, unless the action displayed something else (e.g.: Give).
    override fun displayItemActions(
        itemName: String,
        itemSellPrice: Int,
        actions: List<ItemActionsView.Action>,
    ) {
        displayedDiscardQuestion = null
        displayedInventoryFull = null
        displayedGiveItem = null
        displayedGiveGoldRefusal = null
        lateinit var itemActions: DisplayedItemActions
        itemActions =
            DisplayedItemActions(
                itemName = itemName,
                itemSellPrice = itemSellPrice,
                actions =
                    actions.map { action ->
                        action.copy(onSelected = {
                            action.onSelected()
                            if (displayedItemActions === itemActions) dismiss()
                        })
                    },
            )
        displayedItemActions = itemActions
    }

    override fun displayDiscardQuestion(
        itemName: String,
        onBack: () -> Unit,
        onDiscarded: () -> Unit,
    ) {
        displayedItemActions = null
        displayedInventoryFull = null
        displayedDiscardQuestion = DisplayedDiscardQuestion(itemName, onBack, onDiscarded)
    }

    override fun displayGiveItem(
        itemName: String,
        onCancelled: () -> Unit,
    ) {
        displayedItemActions = null
        displayedInventoryFull = null
        displayedGiveItem = DisplayedGiveItem(itemName, onCancelled)
    }

    override fun displayInventoryFull(
        foundItemName: String,
        foundItemSellPrice: Int,
        foundItemId: String,
        onLeft: () -> Unit,
    ) {
        displayedItemActions = null
        displayedDiscardQuestion = null
        displayedGiveItem = null
        displayedInventoryFull = DisplayedInventoryFull(foundItemName, foundItemSellPrice, foundItemId, onLeft)
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
