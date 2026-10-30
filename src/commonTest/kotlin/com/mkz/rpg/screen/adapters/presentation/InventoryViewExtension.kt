package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

// Page object of [InventoryView] and its [InventorySlotView]s.

fun InventoryView.titleText(): String = titleView.nameText()

/** The gold of the battle unit, on the title line, e.g.: 120 G. */
fun InventoryView.goldText(): String = goldView.nameText()

/** Whether the arrows to the previous and the next party member are displayed, and whether each one is enabled. */
fun InventoryView.arrowsVisible(): Boolean = previousOwnerButton.visible && nextOwnerButton.visible

fun InventoryView.arrowsEnabled(): Pair<Boolean, Boolean> = previousOwnerButton.enabled to nextOwnerButton.enabled

/** Whether each arrow is displayed faded, as a disabled one. */
fun InventoryView.arrowsFaded(): Pair<Boolean, Boolean> = previousOwnerButton.isFaded() to nextOwnerButton.isFaded()

/** Whether the arrows are on each side of the title and the gold, before Close. */
fun InventoryView.isTitleLaidOut(): Boolean {
    val title = titleView.getGlobalBounds()
    val gold = goldView.getGlobalBounds()
    return previousOwnerButton.getGlobalBounds().right <= title.left &&
        nextOwnerButton.getGlobalBounds().left >= gold.right &&
        nextOwnerButton.getGlobalBounds().right < closeButton.getGlobalBounds().left
}

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

suspend fun ViewsForTesting.clickPreviousOwner(inventoryView: InventoryView) = clickOn(inventoryView.previousOwnerButton)

suspend fun ViewsForTesting.clickNextOwner(inventoryView: InventoryView) = clickOn(inventoryView.nextOwnerButton)

/** Drags the mouse across the inventory, starting on its first slot, [dx] pixels to the right (negative to the left). */
suspend fun ViewsForTesting.swipeInventory(
    inventoryView: InventoryView,
    dx: Double,
) {
    val start =
        inventoryView.slotViews
            .first()
            .getGlobalBounds()
            .center
    mouseMoveTo(start.x, start.y)
    mouseDown()
    val steps = 5
    for (step in 1..steps) mouseMoveTo(start.x + dx * step / steps, start.y)
    mouseUp()
}
