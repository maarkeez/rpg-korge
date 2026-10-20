package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

// Page object of [ShopView] and its [InventorySlotView]s.

fun ShopView.titleText(): String = titleView.nameText()

fun ShopView.slotCount(): Int = slotViews.size

/** The image of the item sold in each slot, or `null` for the empty slots. */
fun ShopView.slotBitmaps(): List<Bitmap?> = slotViews.map { slotView -> (slotView.findViewByName(InventorySlotView.ITEM) as? Image)?.bitmap?.base }

/** The slots framed in white, as their item is selected. */
fun ShopView.selectedSlotIndexes(): List<Int> = slotViews.indices.filter { index -> slotViews[index].findViewByName(InventorySlotView.SELECTION_FRAME) != null }

suspend fun ViewsForTesting.clickSlot(
    shopView: ShopView,
    index: Int,
) = clickOn(shopView.slotViews[index])

suspend fun ViewsForTesting.clickClose(shopView: ShopView) = clickOn(shopView.closeButton)
