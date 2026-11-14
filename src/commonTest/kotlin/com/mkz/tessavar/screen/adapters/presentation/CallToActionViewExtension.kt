package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.isFaded
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting

fun CallToActionView.isDisplayingFinishPlayerTurn(): Boolean = visible && callToAction is FinishPlayerTurnView

fun CallToActionView.isFinishPlayerTurnEnabled(): Boolean = isDisplayingFinishPlayerTurn() && (callToAction as FinishPlayerTurnView).isButtonEnabled()

fun CallToActionView.isDisplayingNothing(): Boolean = callToAction == null

fun CallToActionView.isDisplayingCancelAndConfirm(): Boolean = visible && callToAction is CancelAndConfirmView

suspend fun ViewsForTesting.clickFinishPlayerTurn(callToActionView: CallToActionView) = clickFinishPlayerTurn(callToActionView.callToAction as FinishPlayerTurnView)

suspend fun ViewsForTesting.clickCancel(callToActionView: CallToActionView) = clickCancel(callToActionView.callToAction as CancelAndConfirmView)

suspend fun ViewsForTesting.clickConfirm(callToActionView: CallToActionView) = clickConfirm(callToActionView.callToAction as CancelAndConfirmView)

fun CallToActionView.isDisplayingItemButtons(): Boolean = visible && callToAction is ItemButtonsView

fun CallToActionView.itemName(): String = (callToAction as ItemButtonsView).itemNameView.nameText()

fun CallToActionView.itemSellPrice(): String = (callToAction as ItemButtonsView).itemSellPriceView.nameText()

fun CallToActionView.itemButtonLabels(): List<String> = (callToAction as ItemButtonsView).gameButtons.map { button -> button.text }

fun CallToActionView.enabledItemButtonLabels(): List<String> = (callToAction as ItemButtonsView).gameButtons.filter { button -> button.enabled }.map { button -> button.text }

fun CallToActionView.fadedItemButtonLabels(): List<String> = (callToAction as ItemButtonsView).gameButtons.filter { button -> button.isFaded() }.map { button -> button.text }

suspend fun ViewsForTesting.clickItemAction(
    callToActionView: CallToActionView,
    label: String,
) = clickOn((callToActionView.callToAction as ItemButtonsView).gameButtons.single { button -> button.text == label })

fun CallToActionView.itemButtonColor(label: String): RGBA = (callToAction as ItemButtonsView).gameButtons.single { button -> button.text == label }.bgColorOut

fun CallToActionView.discardQuestion(): String = (callToAction as DiscardQuestionView).questionView.nameText()

fun CallToActionView.discardQuestionLabels(): List<String> = (callToAction as DiscardQuestionView).rowView.buttons.map { button -> button.text }

fun CallToActionView.discardQuestionColors(): List<RGBA> = (callToAction as DiscardQuestionView).rowView.buttons.map { button -> button.bgColorOut }

suspend fun ViewsForTesting.clickDiscardQuestionBack(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as DiscardQuestionView).rowView.buttons.first())

suspend fun ViewsForTesting.clickDiscardQuestionDiscard(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as DiscardQuestionView).rowView.buttons.last())

fun CallToActionView.isDisplayingInventoryFull(): Boolean = visible && callToAction is InventoryFullView

fun CallToActionView.inventoryFullMessage(): String = (callToAction as InventoryFullView).messageView.nameText()

fun CallToActionView.isFoundItemIconDisplayed(): Boolean = (callToAction as InventoryFullView).foundItemIcon != null

suspend fun ViewsForTesting.clickLeaveIt(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as InventoryFullView).leaveButton)

fun CallToActionView.isDisplayingGiveItem(): Boolean = visible && callToAction is GiveItemView

fun CallToActionView.giveItemMessage(): String = (callToAction as GiveItemView).messageView.nameText()

suspend fun ViewsForTesting.clickCancelGiving(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as GiveItemView).cancelButton)

fun CallToActionView.isDisplayingGiveGold(): Boolean = visible && callToAction is GiveGoldActionsView

fun CallToActionView.giveGoldLabel(): String = (callToAction as GiveGoldActionsView).giveButton.text

fun CallToActionView.isGiveAllEnabled(): Boolean = (callToAction as GiveGoldActionsView).allButton.enabled

suspend fun ViewsForTesting.clickCancelGivingGold(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as GiveGoldActionsView).cancelButton)

suspend fun ViewsForTesting.clickGiveAll(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as GiveGoldActionsView).allButton)

suspend fun ViewsForTesting.clickGiveGold(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as GiveGoldActionsView).giveButton)

fun CallToActionView.isDisplayingGiveGoldRefusal(): Boolean = visible && callToAction is GiveGoldRefusalView

fun CallToActionView.giveGoldRefusalMessage(): String = (callToAction as GiveGoldRefusalView).messageView.nameText()

suspend fun ViewsForTesting.clickGiveGoldRefusalOk(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as GiveGoldRefusalView).okButton)

fun CallToActionView.isDisplayingActions(): Boolean = visible && callToAction is ButtonRowView

fun CallToActionView.actionLabels(): List<String> = (callToAction as ButtonRowView).buttons.map { button -> button.text }

fun CallToActionView.actionColor(label: String): RGBA = (callToAction as ButtonRowView).buttons.single { button -> button.text == label }.bgColorOut

suspend fun ViewsForTesting.clickAction(
    callToActionView: CallToActionView,
    label: String,
) = clickOn((callToActionView.callToAction as ButtonRowView).buttons.single { button -> button.text == label })

suspend fun ViewsForTesting.clickCancelConfirmation(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as ButtonRowView).buttons.first())

suspend fun ViewsForTesting.clickConfirmation(callToActionView: CallToActionView) = clickOn((callToActionView.callToAction as ButtonRowView).buttons.last())
