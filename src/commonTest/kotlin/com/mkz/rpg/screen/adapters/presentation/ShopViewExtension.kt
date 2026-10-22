package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

// Page object of [ShopView] and its [InventorySlotView]s.

fun ShopView.slotCount(): Int = slotViews.size

/** The image of the item in each slot, or `null` for the empty slots. */
fun ShopView.slotBitmaps(): List<Bitmap?> = slotViews.map { slotView -> (slotView.findViewByName(InventorySlotView.ITEM) as? Image)?.bitmap?.base }

/** The slots framed in gold, as their item is equipped. */
fun ShopView.equippedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.EQUIPPED_FRAME) != null }

/** The slots whose item is dimmed, as the buyer can not trade it. */
fun ShopView.dimmedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].isDimmed }

/** How visible the item of each slot is, or `null` for the empty slots. */
fun ShopView.itemAlphas(): List<Double?> = slotViews.map { slotView -> (slotView.findViewByName(InventorySlotView.ITEM) as? Image)?.alpha }

/** The slots framed in white, as their item is selected. */
fun ShopView.selectedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.SELECTION_FRAME) != null }

/** The buyer's name, gold and bag, e.g.: Knight · 20 G · Bag 11/12. */
fun ShopView.buyerText(): String = buyerText.text

/** Whether the arrows to the previous and the next party member are enabled. */
fun ShopView.arrowsEnabled(): Pair<Boolean, Boolean> = previousBuyerButton.enabled to nextBuyerButton.enabled

/** What the footer displays next to Back: the hint, or the selected item's name, and its price. */
fun ShopView.footerText(): String = itemNameText.text

fun ShopView.footerPrice(): String = itemPriceText.text

/** The label of Buy or Sell, and whether it is enabled, or `null` while it is hidden. */
fun ShopView.action(): Pair<String, Boolean>? = if (actionButton.visible) actionButton.text to actionButton.enabled else null

/** Whether the header, the slots and the footer are stacked in this order, the arrows on each side of the buyer. */
fun ShopView.isLaidOut(): Boolean {
    val header = buyerText.getGlobalBounds()
    val slots = slotViews.first().getGlobalBounds()
    val footer = backButton.getGlobalBounds()
    return header.bottom <= slots.top &&
        slotViews.last().getGlobalBounds().bottom <= footer.top &&
        previousBuyerButton.getGlobalBounds().right <= header.left &&
        nextBuyerButton.getGlobalBounds().left >= header.right &&
        backButton.getGlobalBounds().top == actionButton.getGlobalBounds().top
}

suspend fun ViewsForTesting.clickSlot(
    shopView: ShopView,
    index: Int,
) = clickOn(shopView.slotViews[index])

suspend fun ViewsForTesting.clickPreviousBuyer(shopView: ShopView) = clickOn(shopView.previousBuyerButton)

suspend fun ViewsForTesting.clickNextBuyer(shopView: ShopView) = clickOn(shopView.nextBuyerButton)

suspend fun ViewsForTesting.clickAction(shopView: ShopView) = clickOn(shopView.actionButton)

suspend fun ViewsForTesting.clickBack(shopView: ShopView) = clickOn(shopView.backButton)

/** Drags the mouse across the shop, starting on its first slot, [dx] pixels to the right (negative to the left). */
suspend fun ViewsForTesting.swipeShop(
    shopView: ShopView,
    dx: Double,
) {
    val start =
        shopView.slotViews
            .first()
            .getGlobalBounds()
            .center
    mouseMoveTo(start.x, start.y)
    mouseDown()
    val steps = 5
    for (step in 1..steps) mouseMoveTo(start.x + dx * step / steps, start.y)
    mouseUp()
}
