package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting

fun ControlsView.titleText(): String = title.text

fun ControlsView.controlNames(): List<String> = controlRows.map { row -> row.nameText.text }

fun ControlsView.displayedValue(controlName: String): String = rowOf(controlName).displayedValue()

fun ControlsView.optionNames(controlName: String): List<String> = rowOf(controlName).buttons.values.map { button -> button.label.text }

fun ControlsView.swatchHex(controlName: String): String? =
    rowOf(controlName)
        .colorSwatch
        ?.color
        ?.hexStringNoAlpha
        ?.removePrefix("#")
        ?.uppercase()

fun ControlsView.isListedTopDown(): Boolean =
    controlRows.zipWithNext().all { (above, below) -> above.globalPos.y < below.globalPos.y } &&
        controlRows.all { row -> row.isShown() && row.globalPos.y > title.globalPos.y }

suspend fun ViewsForTesting.clickReset(controlsView: ControlsView) = clickOn(controlsView.resetButton)

suspend fun ViewsForTesting.clickControlButton(
    controlsView: ControlsView,
    controlName: String,
    button: String,
) = clickOn(controlsView.rowOf(controlName).buttons.getValue(button))

fun ControlsView.enterText(
    controlName: String,
    text: String,
) {
    val input = requireNotNull(rowOf(controlName).textInput) { "The control '$controlName' has no text input" }
    input.text = text
    input.onReturnPressed(input.controller)
}

private fun ControlsView.rowOf(controlName: String): ControlsView.ControlRow = controlRows.first { row -> row.control.name == controlName }

fun ControlsView.fitsItsMaximumHeight(): Boolean = rows.y + rows.height <= ControlsView.MAXIMUM_HEIGHT
