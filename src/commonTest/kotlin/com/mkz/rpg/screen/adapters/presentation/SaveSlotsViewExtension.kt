package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

// Page object of [SaveSlotsView].

/** The save slots of the scene, or `null` when the scene has none. */
fun Stage.saveSlotsView(): SaveSlotsView? = descendants().filterIsInstance<SaveSlotsView>().firstOrNull()

fun SaveSlotsView.title(): String = titleText.text

/** What each slot displayed holds, from the first, e.g.: `Empty`. */
fun SaveSlotsView.slotLabels(): List<String> = slotButtons.filter { button -> button.visible }.map { button -> button.text }

/** Whether each slot displayed can be chosen, from the first. */
fun SaveSlotsView.enabledSlots(): List<Boolean> = slotButtons.filter { button -> button.visible }.map { button -> button.enabled }

/** The numbers of the slots displaying Delete, e.g.: `[2, 3]`. */
fun SaveSlotsView.deletableSlots(): List<Int> = deleteButtons.indices.filter { index -> deleteButtons[index].visible }.map { index -> index + 1 }

/** Whether the slots fit under the title, each with its Delete on its right, if any. */
fun SaveSlotsView.isLaidOut(): Boolean =
    slotButtons.indices.all { index ->
        val slot = slotButtons[index]
        val delete = deleteButtons[index]
        slot.x + slot.width <= (if (delete.visible) delete.x else titleText.x + titleText.width)
    }

suspend fun ViewsForTesting.clickSlot(
    saveSlotsView: SaveSlotsView,
    number: Int,
) = clickOn(saveSlotsView.slotButtons[number - 1])

suspend fun ViewsForTesting.clickDelete(
    saveSlotsView: SaveSlotsView,
    number: Int,
) = clickOn(saveSlotsView.deleteButtons[number - 1])

suspend fun ViewsForTesting.clickBack(saveSlotsView: SaveSlotsView) = clickOn(saveSlotsView.backButton)
