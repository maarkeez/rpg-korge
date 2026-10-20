package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

// Page object of [InventoryView] and its [InventorySlotView]s.

fun InventoryView.titleText(): String = titleView.nameText()

/** The gold of the battle unit, on the title line, e.g.: 120 G. */
fun InventoryView.goldText(): String = goldView.nameText()

fun InventoryView.slotCount(): Int = slotViews.size

/** The image of the item stored in each slot, or `null` for the empty slots. */
fun InventoryView.slotBitmaps(): List<Bitmap?> = slotViews.map { slotView -> (slotView.findViewByName(InventorySlotView.ITEM) as? Image)?.bitmap?.base }

/** The slots framed in gold, as their item is equipped. */
fun InventoryView.equippedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.EQUIPPED_FRAME) != null }

/** The slots framed in white, as their item is selected. */
fun InventoryView.selectedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.SELECTION_FRAME) != null }

suspend fun ViewsForTesting.clickSlot(
    inventoryView: InventoryView,
    index: Int,
) = clickOn(inventoryView.slotViews[index])

suspend fun ViewsForTesting.clickClose(inventoryView: InventoryView) = clickOn(inventoryView.closeButton)
