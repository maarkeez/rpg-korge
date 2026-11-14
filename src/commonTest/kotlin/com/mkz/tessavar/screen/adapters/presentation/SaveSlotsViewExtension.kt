package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

fun Stage.saveSlotsView(): SaveSlotsView? = descendants().filterIsInstance<SaveSlotsView>().firstOrNull()

fun SaveSlotsView.title(): String = titleText.text

fun SaveSlotsView.slotLabels(): List<String> = slotButtons.filter { button -> button.visible }.map { button -> button.text }

fun SaveSlotsView.enabledSlots(): List<Boolean> = slotButtons.filter { button -> button.visible }.map { button -> button.enabled }

fun SaveSlotsView.deletableSlots(): List<Int> = deleteButtons.indices.filter { index -> deleteButtons[index].visible }.map { index -> index + 1 }

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

fun SaveSlotsView.rowTops(): List<Double> = slotButtons.map { button -> button.y }

fun SaveSlotsView.rowsBottom(): Double = slotButtons.last().let { button -> button.y + button.height }
