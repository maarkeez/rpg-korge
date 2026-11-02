package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting

// Page object of [PlayerCallToActionView]. Uses the page objects of the call to action it displays.

fun PlayerCallToActionView.isDisplayingFinishTurn(): Boolean = visible && callToAction is FinishTurnView

/** Whether the finish turn is displayed and can be clicked. */
fun PlayerCallToActionView.isFinishTurnEnabled(): Boolean = isDisplayingFinishTurn() && (callToAction as FinishTurnView).isButtonEnabled()

/** Whether the call to action is empty, e.g.: while exploring, when the player has nothing to choose. */
fun PlayerCallToActionView.isDisplayingNothing(): Boolean = callToAction == null

fun PlayerCallToActionView.isDisplayingCancelAndConfirm(): Boolean = visible && callToAction is CancelAndConfirmView

suspend fun ViewsForTesting.clickFinishTurn(playerCallToActionView: PlayerCallToActionView) = clickFinishTurn(playerCallToActionView.callToAction as FinishTurnView)

suspend fun ViewsForTesting.clickCancel(playerCallToActionView: PlayerCallToActionView) = clickCancel(playerCallToActionView.callToAction as CancelAndConfirmView)

suspend fun ViewsForTesting.clickConfirm(playerCallToActionView: PlayerCallToActionView) = clickConfirm(playerCallToActionView.callToAction as CancelAndConfirmView)

fun PlayerCallToActionView.isDisplayingItemActions(): Boolean = visible && callToAction is ItemActionsView

/** The name of the selected inventory item, followed by its actions, e.g.: Consume, Discard and Cancel. */
fun PlayerCallToActionView.itemName(): String = (callToAction as ItemActionsView).itemNameView.nameText()

/** The price of the selected inventory item, on the line of its name, e.g.: 50 G. */
fun PlayerCallToActionView.itemPrice(): String = (callToAction as ItemActionsView).itemPriceView.nameText()

fun PlayerCallToActionView.itemActionLabels(): List<String> = (callToAction as ItemActionsView).actionButtons.map { button -> button.text }

fun PlayerCallToActionView.enabledItemActionLabels(): List<String> = (callToAction as ItemActionsView).actionButtons.filter { button -> button.enabled }.map { button -> button.text }

/** The actions displayed faded, as disabled ones, e.g.: No one. */
fun PlayerCallToActionView.fadedItemActionLabels(): List<String> = (callToAction as ItemActionsView).actionButtons.filter { button -> button.isFaded() }.map { button -> button.text }

suspend fun ViewsForTesting.clickItemAction(
    playerCallToActionView: PlayerCallToActionView,
    label: String,
) = clickOn((playerCallToActionView.callToAction as ItemActionsView).actionButtons.single { button -> button.text == label })

fun PlayerCallToActionView.isDisplayingInventoryFull(): Boolean = visible && callToAction is InventoryFullView

fun PlayerCallToActionView.inventoryFullMessage(): String = (callToAction as InventoryFullView).messageView.nameText()

fun PlayerCallToActionView.isFoundItemIconDisplayed(): Boolean = (callToAction as InventoryFullView).foundItemIcon != null

suspend fun ViewsForTesting.clickLeaveIt(playerCallToActionView: PlayerCallToActionView) = clickOn((playerCallToActionView.callToAction as InventoryFullView).leaveButton)

fun PlayerCallToActionView.isDisplayingGiveItem(): Boolean = visible && callToAction is GiveItemView

fun PlayerCallToActionView.giveItemMessage(): String = (callToAction as GiveItemView).messageView.nameText()

suspend fun ViewsForTesting.clickCancelGiving(playerCallToActionView: PlayerCallToActionView) = clickOn((playerCallToActionView.callToAction as GiveItemView).cancelButton)

fun PlayerCallToActionView.isDisplayingGiveGold(): Boolean = visible && callToAction is GiveGoldActionsView

/** The label of Give, with the amount, e.g.: `Give 25 G`. */
fun PlayerCallToActionView.giveGoldLabel(): String = (callToAction as GiveGoldActionsView).giveButton.text

fun PlayerCallToActionView.isGiveAllEnabled(): Boolean = (callToAction as GiveGoldActionsView).allButton.enabled

suspend fun ViewsForTesting.clickCancelGivingGold(playerCallToActionView: PlayerCallToActionView) = clickOn((playerCallToActionView.callToAction as GiveGoldActionsView).cancelButton)

suspend fun ViewsForTesting.clickGiveAll(playerCallToActionView: PlayerCallToActionView) = clickOn((playerCallToActionView.callToAction as GiveGoldActionsView).allButton)

suspend fun ViewsForTesting.clickGiveGold(playerCallToActionView: PlayerCallToActionView) = clickOn((playerCallToActionView.callToAction as GiveGoldActionsView).giveButton)

fun PlayerCallToActionView.isDisplayingGiveGoldRefusal(): Boolean = visible && callToAction is GiveGoldRefusalView

fun PlayerCallToActionView.giveGoldRefusalMessage(): String = (callToAction as GiveGoldRefusalView).messageView.nameText()

suspend fun ViewsForTesting.clickGiveGoldRefusalOk(playerCallToActionView: PlayerCallToActionView) = clickOn((playerCallToActionView.callToAction as GiveGoldRefusalView).okButton)

fun PlayerCallToActionView.isDisplayingActions(): Boolean = visible && callToAction is ActionRowView

/** The labels of the buttons in the row, e.g.: Back and Abandon. */
fun PlayerCallToActionView.actionLabels(): List<String> = (callToAction as ActionRowView).buttons.map { button -> button.text }

/** The colour of the button of the row with the label, not pressed. */
fun PlayerCallToActionView.actionColor(label: String): RGBA = (callToAction as ActionRowView).buttons.single { button -> button.text == label }.bgColorOut

suspend fun ViewsForTesting.clickAction(
    playerCallToActionView: PlayerCallToActionView,
    label: String,
) = clickOn((playerCallToActionView.callToAction as ActionRowView).buttons.single { button -> button.text == label })

/** Chooses the secondary choice of a question, e.g.: Cancel. */
suspend fun ViewsForTesting.clickCancelConfirmation(playerCallToActionView: PlayerCallToActionView) = clickOn((playerCallToActionView.callToAction as ActionRowView).buttons.first())

/** Chooses the primary choice of a question, e.g.: Quit to menu. */
suspend fun ViewsForTesting.clickConfirmation(playerCallToActionView: PlayerCallToActionView) = clickOn((playerCallToActionView.callToAction as ActionRowView).buttons.last())
