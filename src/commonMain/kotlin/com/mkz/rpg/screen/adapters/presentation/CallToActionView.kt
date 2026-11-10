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

    private var onPlayerTurnFinished: (() -> Unit)? = null
    private var isFinishPlayerTurnEnabled = true

    open fun hide() {
        visible = false
    }

    open fun displayFinishPlayerTurn(onPlayerTurnFinished: () -> Unit) {
        this.onPlayerTurnFinished = onPlayerTurnFinished
        val finishPlayerTurnView = FinishPlayerTurnView(messages, onPlayerTurnFinished = onPlayerTurnFinished)
        finishPlayerTurnView.isEnabled = isFinishPlayerTurnEnabled
        display(finishPlayerTurnView)
    }

    open fun hideFinishPlayerTurn() {
        onPlayerTurnFinished = null
        (callToAction as? FinishPlayerTurnView)?.removeFromParent()
    }

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

    open fun dismiss() {
        val onPlayerTurnFinished = onPlayerTurnFinished
        if (onPlayerTurnFinished == null) {
            callToAction?.removeFromParent()
        } else {
            displayFinishPlayerTurn(onPlayerTurnFinished = onPlayerTurnFinished)
        }
    }

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

    open fun displayDiscardQuestion(
        itemName: String,
        onBack: () -> Unit,
        onDiscarded: () -> Unit,
    ) {
        display(DiscardQuestionView(messages, itemName = itemName, onBack = onBack, onDiscarded = onDiscarded))
    }

    open fun displayGiveItem(
        itemName: String,
        onCancelled: () -> Unit,
    ) {
        display(GiveItemView(messages, itemName = itemName, onCancelled = onCancelled))
    }

    open fun displayGiveGold(
        amount: Int,
        maximum: Int,
        onCancelled: () -> Unit,
        onAll: () -> Unit,
        onGiven: () -> Unit,
    ) {
        display(GiveGoldActionsView(messages, onCancelled = onCancelled, onAll = onAll, onGiven = onGiven).apply { displayAmount(amount, maximum) })
    }

    open fun updateGiveGold(
        amount: Int,
        maximum: Int,
    ) {
        (callToAction as? GiveGoldActionsView)?.displayAmount(amount, maximum)
    }

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

    open fun displayButtons(actions: List<ButtonRowView.Action>) {
        display(ButtonRowView(actions))
        visible = true
    }

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

    open fun hideButtons() {
        if (callToAction is ButtonRowView) dismiss()
    }

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
