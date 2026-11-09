package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.View

open class CallToActionView(
    private val messages: Messages,
    private val resourcesLoader: GameSceneResourcesLoader = GameSceneResourcesLoader(),
) : Container() {
    companion object {
        const val CALL_TO_ACTION = "CALL_TO_ACTION"
    }

    // The finish turn, displayed when the player has nothing else to choose. `null` out of the battles (e.g.: while
    // exploring), when nothing is displayed then.
    private var onPlayerTurnFinished: (() -> Unit)? = null
    private var isFinishPlayerTurnEnabled = true

    open fun hide() {
        visible = false
    }

    /** Displays the finish turn, which is displayed again once the player has chosen, e.g.: cancelled a cast. */
    open fun displayFinishPlayerTurn(onPlayerTurnFinished: () -> Unit) {
        this.onPlayerTurnFinished = onPlayerTurnFinished
        val finishPlayerTurnView = FinishPlayerTurnView(messages, onPlayerTurnFinished = onPlayerTurnFinished)
        finishPlayerTurnView.isEnabled = isFinishPlayerTurnEnabled
        display(finishPlayerTurnView)
    }

    /** Forgets the finish turn, e.g.: once the battle finishes, so nothing is displayed once the player has chosen. */
    open fun hideFinishPlayerTurn() {
        onPlayerTurnFinished = null
        (callToAction as? FinishPlayerTurnView)?.removeFromParent()
    }

    /** Enables or disables the finish turn, also when it is displayed again later (e.g.: after cancelling a cast). */
    open fun setFinishPlayerTurnEnabled(enabled: Boolean) {
        isFinishPlayerTurnEnabled = enabled
        (callToAction as? FinishPlayerTurnView)?.isEnabled = enabled
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
                cancelLabel = messages[UiMessage.CALL_TO_ACTION_CANCEL],
                confirmLabel = messages[UiMessage.CALL_TO_ACTION_CONFIRM],
            )
        display(cancelAndConfirmView)
    }

    /**
     * Displays the finish turn again once the player is done choosing, e.g.: with an inventory item. Nothing is displayed
     * out of the battles.
     */
    open fun dismiss() {
        val onPlayerTurnFinished = onPlayerTurnFinished
        if (onPlayerTurnFinished == null) {
            callToAction?.removeFromParent()
        } else {
            displayFinishPlayerTurn(onPlayerTurnFinished = onPlayerTurnFinished)
        }
    }

    /**
     * Displays the name and sell price of the selected item and its actions, e.g.: Consume. It is dismissed once an action is
     * selected, unless the action displayed something else instead, e.g.: Give asks for the receiver.
     */
    open fun displayItemButtons(
        itemName: String,
        itemSellPrice: Int,
        actions: List<ItemButtonsView.Button>,
    ) {
        lateinit var itemButtonsView: ItemButtonsView
        val itemButtons =
            actions.map { action ->
                action.copy(
                    onSelected = {
                        action.onSelected()
                        if (callToAction === itemButtonsView) dismiss()
                    },
                )
            }
        itemButtonsView = ItemButtonsView(messages, itemName = itemName, itemSellPrice = itemSellPrice, actions = itemButtons)
        display(itemButtonsView)
    }

    /** Asks the player whether to discard the item. A tap does not dismiss it: the presenter displays what follows. */
    open fun displayDiscardQuestion(
        itemName: String,
        onBack: () -> Unit,
        onDiscarded: () -> Unit,
    ) {
        display(DiscardQuestionView(messages, itemName = itemName, onBack = onBack, onDiscarded = onDiscarded))
    }

    /** Asks the player to tap the party member who receives the item, with Cancel. */
    open fun displayGiveItem(
        itemName: String,
        onCancelled: () -> Unit,
    ) {
        display(GiveItemView(messages, itemName = itemName, onCancelled = onCancelled))
    }

    /** Displays Cancel, All and Give, for the amount of the panel. They stay until something else is displayed. */
    open fun displayGiveGold(
        amount: Int,
        maximum: Int,
        onCancelled: () -> Unit,
        onAll: () -> Unit,
        onGiven: () -> Unit,
    ) {
        display(GiveGoldActionsView(messages, onCancelled = onCancelled, onAll = onAll, onGiven = onGiven).apply { displayAmount(amount, maximum) })
    }

    /** Writes the amount on Give again, without displaying the buttons again, as the amount changes on every step of the slider. */
    open fun updateGiveGold(
        amount: Int,
        maximum: Int,
    ) {
        (callToAction as? GiveGoldActionsView)?.displayAmount(amount, maximum)
    }

    /** Displays why no gold can be given, with OK, which dismisses it. */
    open fun displayGiveGoldRefusal(
        message: String,
        onDismissed: () -> Unit,
    ) {
        display(
            GiveGoldRefusalView(message, messages[UiMessage.GIVE_GOLD_OK]) {
                onDismissed()
                dismiss()
            },
        )
    }

    /**
     * Displays the buttons of a panel in the row, also when the pad had hidden the call to action. Unlike the item buttons,
     * a tap does not dismiss them: the presenter displays what follows.
     */
    open fun displayButtons(actions: List<ButtonRowView.Action>) {
        display(ButtonRowView(actions))
        visible = true
    }

    /** Displays both choices of a question in the row, the one that can not be undone in red, e.g.: Cancel and Quit to menu. */
    open fun displayConfirmation(
        cancelLabel: String,
        confirmLabel: String,
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        displayButtons(
            listOf(
                ButtonRowView.Action(cancelLabel, onSelected = onCancelled),
                ButtonRowView.Action(confirmLabel, HudStyle.DANGER, HudStyle.DANGER_PRESSED, onSelected = onConfirmed),
            ),
        )
    }

    /** Removes the row and nothing else, e.g.: an item found with a full inventory stays. The finish turn returns when the battle has one. */
    open fun hideButtons() {
        if (callToAction is ButtonRowView) dismiss()
    }

    /** Asks the player what to do with an item found while the inventory is full. */
    open fun displayInventoryFull(
        foundItemName: String,
        foundItemSellPrice: Int,
        foundItemId: String,
        onLeft: () -> Unit,
    ) {
        display(InventoryFullView(messages, foundItemName = foundItemName, foundItemSellPrice = foundItemSellPrice, foundItemId = foundItemId, resourcesLoader = resourcesLoader, onLeft = onLeft))
    }

    internal val callToAction: View?
        get() = findViewByName(CALL_TO_ACTION)

    private fun display(view: View) {
        view.name = CALL_TO_ACTION
        callToAction?.removeFromParent()
        addChild(view)
    }
}
