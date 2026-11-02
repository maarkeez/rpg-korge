package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.korge.tests.ViewsForTesting

// Page object of [GiveGoldView].

/** The name of the giver, and what it would carry, e.g.: `Knight` and `120 G > 95 G`. */
fun GiveGoldView.giverTexts(): Pair<String, String> = giverNameText.text to giverGoldText.text

/** The name of the receiver, and what it would carry, e.g.: `Viking` and `100 G > 125 G`. */
fun GiveGoldView.receiverTexts(): Pair<String, String> = receiverNameText.text to receiverGoldText.text

fun GiveGoldView.amountLabel(): String = amountText.text

/** Whether the arrows to switch the receiver are displayed, and whether each one is enabled. */
fun GiveGoldView.arrowsVisible(): Boolean = previousReceiverButton.visible && nextReceiverButton.visible

fun GiveGoldView.arrowsEnabled(): Pair<Boolean, Boolean> = previousReceiverButton.enabled to nextReceiverButton.enabled

fun GiveGoldView.arrowsFaded(): Pair<Boolean, Boolean> = previousReceiverButton.isFaded() to nextReceiverButton.isFaded()

suspend fun ViewsForTesting.clickPreviousReceiver(giveGoldView: GiveGoldView) = clickOn(giveGoldView.previousReceiverButton)

suspend fun ViewsForTesting.clickNextReceiver(giveGoldView: GiveGoldView) = clickOn(giveGoldView.nextReceiverButton)

suspend fun ViewsForTesting.clickMinus(giveGoldView: GiveGoldView) = clickOn(giveGoldView.minusButton)

suspend fun ViewsForTesting.clickPlus(giveGoldView: GiveGoldView) = clickOn(giveGoldView.plusButton)

/** Drags the mouse along the slider, starting on its center, [dx] pixels to the right (negative to the left). */
suspend fun ViewsForTesting.dragSlider(
    giveGoldView: GiveGoldView,
    dx: Double,
) {
    val start = giveGoldView.slider.getGlobalBounds().center
    mouseMoveTo(start.x, start.y)
    mouseDown()
    val steps = 5
    for (step in 1..steps) mouseMoveTo(start.x + dx * step / steps, start.y)
    mouseUp()
}
