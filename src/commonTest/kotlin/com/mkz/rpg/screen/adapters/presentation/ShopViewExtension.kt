package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.image.bitmap.Bitmap
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Circle
import korlibs.korge.view.Image

// Page object of [ShopView] and its [InventorySlotView]s.

fun ShopView.titleText(): String = titleView.nameText()

fun ShopView.slotCount(): Int = slotViews.size

/** The image of the item in each slot, or `null` for the empty slots. */
fun ShopView.slotBitmaps(): List<Bitmap?> = slotViews.map { slotView -> (slotView.findViewByName(InventorySlotView.ITEM) as? Image)?.bitmap?.base }

/** The slots framed in gold, as their item is equipped. */
fun ShopView.equippedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.EQUIPPED_FRAME) != null }

/** The slots framed in white, as their item is selected. */
fun ShopView.selectedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.SELECTION_FRAME) != null }

/** Whether the buyer buys or sells. */
fun ShopView.mode(): ShopView.Mode = mode

/** The buyer's name, gold and bag, e.g.: Knight · 20 G · Bag 11/12. */
fun ShopView.buyerText(): String = buyerView.nameText()

/** The colour of each dot, in party order. */
fun ShopView.dotColors(): List<RGBA> = dots.map { dot -> (dot.children.last() as Circle).fill as RGBA }

suspend fun ViewsForTesting.clickSlot(
    shopView: ShopView,
    index: Int,
) = clickOn(shopView.slotViews[index])

suspend fun ViewsForTesting.clickDot(
    shopView: ShopView,
    partyIndex: Int,
) = clickOn(shopView.dots[partyIndex])

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
