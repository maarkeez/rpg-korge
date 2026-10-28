package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting

// Page object of [ControlsView].

fun ControlsView.titleText(): String = title.text

/** The names of the controls listed, in their order, e.g.: `remaining`. */
fun ControlsView.controlNames(): List<String> = controlRows.map { row -> row.nameText.text }

/** The value of the arg of the control, as displayed, e.g.: `15`, `true`, or the option highlighted, e.g.: `SELL`. */
fun ControlsView.displayedValue(controlName: String): String = rowOf(controlName).displayedValue()

/** The options of an enum control, in their order. */
fun ControlsView.optionNames(controlName: String): List<String> = rowOf(controlName).buttons.values.map { button -> button.label.text }

/** The color of the swatch of a color control, as its hex, e.g.: `FF375F`. */
fun ControlsView.swatchHex(controlName: String): String? =
    rowOf(controlName)
        .colorSwatch
        ?.color
        ?.hexStringNoAlpha
        ?.removePrefix("#")
        ?.uppercase()

/** Whether the rows are listed top down, in their order, all of them shown under the title. */
fun ControlsView.isListedTopDown(): Boolean =
    controlRows.zipWithNext().all { (above, below) -> above.globalPos.y < below.globalPos.y } &&
        controlRows.all { row -> row.isShown() && row.globalPos.y > title.globalPos.y }

suspend fun ViewsForTesting.clickReset(controlsView: ControlsView) = clickOn(controlsView.resetButton)

/** Clicks a button of the editor of the control: `toggle`, `decrease`, `increase`, `previous`, `next` or `option:<option>`. */
suspend fun ViewsForTesting.clickControlButton(
    controlsView: ControlsView,
    controlName: String,
    button: String,
) = clickOn(controlsView.rowOf(controlName).buttons.getValue(button))

/** Types the text in the input of a text or a color control, and presses return. */
fun ControlsView.enterText(
    controlName: String,
    text: String,
) {
    val input = requireNotNull(rowOf(controlName).textInput) { "The control '$controlName' has no text input" }
    input.text = text
    input.onReturnPressed(input.controller)
}

private fun ControlsView.rowOf(controlName: String): ControlsView.ControlRow = controlRows.first { row -> row.control.name == controlName }
