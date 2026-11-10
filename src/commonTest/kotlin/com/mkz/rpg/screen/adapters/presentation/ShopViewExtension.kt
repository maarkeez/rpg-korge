package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isFaded
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

fun ShopView.slotCount(): Int = slotViews.size

fun ShopView.slotBitmaps(): List<Bitmap?> = slotViews.map { slotView -> (slotView.findViewByName(InventorySlotView.ITEM) as? Image)?.bitmap?.base }

fun ShopView.equippedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.EQUIPPED_FRAME) != null }

fun ShopView.dimmedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].isDimmed }

fun ShopView.itemAlphas(): List<Double?> = slotViews.map { slotView -> (slotView.findViewByName(InventorySlotView.ITEM) as? Image)?.alpha }

fun ShopView.selectedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.SELECTION_FRAME) != null }

fun ShopView.buyerText(): String = buyerText.text

fun ShopView.arrowsEnabled(): Pair<Boolean, Boolean> = previousBuyerButton.enabled to nextBuyerButton.enabled

fun ShopView.arrowsFaded(): Pair<Boolean, Boolean> = previousBuyerButton.isFaded() to nextBuyerButton.isFaded()

fun ShopView.footerText(): String = itemNameText.text

fun ShopView.footerPrice(): String = itemPriceText.text

fun ShopView.footerBottom(): Double = itemNameText.y + itemNameText.height

fun ShopView.isLaidOut(): Boolean {
    val header = buyerText.getGlobalBounds()
    val slots = slotViews.first().getGlobalBounds()
    val footer = itemNameText.getGlobalBounds()
    return header.bottom <= slots.top &&
        slotViews.last().getGlobalBounds().bottom <= footer.top &&
        previousBuyerButton.getGlobalBounds().right <= header.left &&
        nextBuyerButton.getGlobalBounds().left >= header.right
}

suspend fun ViewsForTesting.clickSlot(
    shopView: ShopView,
    index: Int,
) = clickOn(shopView.slotViews[index])

suspend fun ViewsForTesting.clickPreviousBuyer(shopView: ShopView) = clickOn(shopView.previousBuyerButton)

suspend fun ViewsForTesting.clickNextBuyer(shopView: ShopView) = clickOn(shopView.nextBuyerButton)

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
