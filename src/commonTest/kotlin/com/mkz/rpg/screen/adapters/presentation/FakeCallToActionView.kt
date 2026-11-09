package com.mkz.rpg.screen.adapters.presentation

// Records what the presenters ask the call to action view to display, instead of drawing it.
// The player actions are simulated by calling the callbacks it was given (e.g.: `onPlayerTurnFinished`).
class FakeCallToActionView : CallToActionView(MessagesMother.messages()) {
    // The finish turn the presenters displayed, `null` while it is hidden (e.g.: while exploring).
    var onPlayerTurnFinished: (() -> Unit)? = null
        private set
    var onCancelled: (() -> Unit)? = null
        private set
    var onConfirmed: (() -> Unit)? = null
        private set
    val finishPlayerTurnEnabledChanges = mutableListOf<Boolean>()

    data class DisplayedItemButtons(
        val itemName: String,
        val itemSellPrice: Int,
        val actions: List<ItemButtonsView.Button>,
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

    // What the call to action displays last, e.g.: the item buttons. `null` once dismissed.
    var displayedItemButtons: DisplayedItemButtons? = null
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
    var displayedActions: List<ButtonRowView.Action>? = null
        private set
    var hideCount = 0
        private set

    val isFinishPlayerTurnEnabled: Boolean
        get() = finishPlayerTurnEnabledChanges.lastOrNull() ?: false

    /** The labels of the buttons in the row, e.g.: Back and Abandon. Empty when no row is displayed. */
    fun actionLabels(): List<String> = displayedActions.orEmpty().map { action -> action.label }

    /** Taps the button of the row with the label. */
    fun chooseAction(label: String) = requireNotNull(displayedActions) { "It displays no row" }.single { action -> action.label == label }.onSelected()

    override fun hide() {
        hideCount++
    }

    override fun displayFinishPlayerTurn(onPlayerTurnFinished: () -> Unit) {
        this.onPlayerTurnFinished = onPlayerTurnFinished
    }

    override fun hideFinishPlayerTurn() {
        onPlayerTurnFinished = null
    }

    override fun dismiss() {
        displayedItemButtons = null
        displayedDiscardQuestion = null
        displayedInventoryFull = null
        displayedGiveItem = null
        displayedGiveGold = null
        displayedGiveGoldRefusal = null
        displayedActions = null
    }

    // As the real view, a tap does not dismiss it, and it is displayed also when the pad had hidden the call to action.
    override fun displayButtons(actions: List<ButtonRowView.Action>) {
        dismiss()
        displayedActions = actions
        visible = true
    }

    override fun hideButtons() {
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
    override fun displayItemButtons(
        itemName: String,
        itemSellPrice: Int,
        actions: List<ItemButtonsView.Button>,
    ) {
        displayedDiscardQuestion = null
        displayedInventoryFull = null
        displayedGiveItem = null
        displayedGiveGoldRefusal = null
        lateinit var itemButtons: DisplayedItemButtons
        itemButtons =
            DisplayedItemButtons(
                itemName = itemName,
                itemSellPrice = itemSellPrice,
                actions =
                    actions.map { action ->
                        action.copy(onSelected = {
                            action.onSelected()
                            if (displayedItemButtons === itemButtons) dismiss()
                        })
                    },
            )
        displayedItemButtons = itemButtons
    }

    override fun displayDiscardQuestion(
        itemName: String,
        onBack: () -> Unit,
        onDiscarded: () -> Unit,
    ) {
        displayedItemButtons = null
        displayedInventoryFull = null
        displayedDiscardQuestion = DisplayedDiscardQuestion(itemName, onBack, onDiscarded)
    }

    override fun displayGiveItem(
        itemName: String,
        onCancelled: () -> Unit,
    ) {
        displayedItemButtons = null
        displayedInventoryFull = null
        displayedGiveItem = DisplayedGiveItem(itemName, onCancelled)
    }

    override fun displayInventoryFull(
        foundItemName: String,
        foundItemSellPrice: Int,
        foundItemId: String,
        onLeft: () -> Unit,
    ) {
        displayedItemButtons = null
        displayedDiscardQuestion = null
        displayedGiveItem = null
        displayedInventoryFull = DisplayedInventoryFull(foundItemName, foundItemSellPrice, foundItemId, onLeft)
    }

    override fun setFinishPlayerTurnEnabled(enabled: Boolean) {
        finishPlayerTurnEnabledChanges += enabled
    }

    override fun displayCancelAndConfirm(
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        this.onCancelled = onCancelled
        this.onConfirmed = onConfirmed
    }
}
