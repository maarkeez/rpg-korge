package com.mkz.rpg.screen.adapters.presentation

import korlibs.korge.view.Container
import korlibs.korge.view.View

open class PlayerCallToActionView(
    private val texts: Texts,
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
        val finishTurnView = FinishTurnView(texts, onTurnFinished = onTurnFinished)
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
                cancelLabel = texts[UiText.CALL_TO_ACTION_CANCEL],
                confirmLabel = texts[UiText.CALL_TO_ACTION_CONFIRM],
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
     * selected, unless the action displayed something else instead, e.g.: Give asks for the receiver.
     */
    open fun displayItemActions(
        itemName: String,
        itemPrice: Int,
        actions: List<ItemActionsView.Action>,
    ) {
        lateinit var itemActionsView: ItemActionsView
        val itemActions =
            actions.map { action ->
                action.copy(
                    onSelected = {
                        action.onSelected()
                        if (callToAction === itemActionsView) dismiss()
                    },
                )
            }
        itemActionsView = ItemActionsView(texts, itemName = itemName, itemPrice = itemPrice, actions = itemActions)
        display(itemActionsView)
    }

    /** Asks the player to tap the party member who receives the item, with Cancel. */
    open fun displayGiveItem(
        itemName: String,
        onCancelled: () -> Unit,
    ) {
        display(GiveItemView(texts, itemName = itemName, onCancelled = onCancelled))
    }

    /** Displays Cancel, All and Give, for the amount of the panel. They stay until something else is displayed. */
    open fun displayGiveGold(
        amount: Int,
        maximum: Int,
        onCancelled: () -> Unit,
        onAll: () -> Unit,
        onGiven: () -> Unit,
    ) {
        display(GiveGoldActionsView(texts, onCancelled = onCancelled, onAll = onAll, onGiven = onGiven).apply { displayAmount(amount, maximum) })
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
            GiveGoldRefusalView(message, texts[UiText.GIVE_GOLD_OK]) {
                onDismissed()
                dismiss()
            },
        )
    }

    /**
     * Displays the buttons of a panel in the row, also when the pad had hidden the call to action. Unlike the item actions,
     * a tap does not dismiss them: the presenter displays what follows.
     */
    open fun displayActions(actions: List<ActionRowView.Action>) {
        display(ActionRowView(actions))
        visible = true
    }

    /** Displays both choices of a question in the row, the one that can not be undone in red, e.g.: Cancel and Quit to menu. */
    open fun displayConfirmation(
        cancelLabel: String,
        confirmLabel: String,
        onCancelled: () -> Unit,
        onConfirmed: () -> Unit,
    ) {
        displayActions(
            listOf(
                ActionRowView.Action(cancelLabel, onSelected = onCancelled),
                ActionRowView.Action(confirmLabel, HudStyle.DANGER, HudStyle.DANGER_PRESSED, onSelected = onConfirmed),
            ),
        )
    }

    /** Removes the row and nothing else, e.g.: an item found with a full inventory stays. The finish turn returns when the battle has one. */
    open fun hideActions() {
        if (callToAction is ActionRowView) dismiss()
    }

    /** Asks the player what to do with an item found while the inventory is full. */
    open fun displayInventoryFull(
        foundItemName: String,
        foundItemPrice: Int,
        foundItemId: String,
        onLeft: () -> Unit,
    ) {
        display(InventoryFullView(texts, foundItemName = foundItemName, foundItemPrice = foundItemPrice, foundItemId = foundItemId, resourcesLoader = resourcesLoader, onLeft = onLeft))
    }

    internal val callToAction: View?
        get() = findViewByName(CALL_TO_ACTION)

    private fun display(view: View) {
        view.name = CALL_TO_ACTION
        callToAction?.removeFromParent()
        addChild(view)
    }
}
