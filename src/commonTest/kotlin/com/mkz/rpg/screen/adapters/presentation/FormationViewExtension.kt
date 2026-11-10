package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

fun FormationView.names(): List<String> = rowViews.filter(FormationRowView::visible).map { rowView -> rowView.nameText.text }

fun FormationView.crownedRowIndexes(): List<Int> = rowViews.indices.filter { index -> rowViews[index].visible && rowViews[index].isCrowned }

fun FormationView.crownBitmap(): Bitmap? = rowViews.firstNotNullOfOrNull { rowView -> rowView.crownImage?.bitmap?.base }

fun FormationView.portraitBitmaps(): List<Bitmap?> =
    rowViews.filter(FormationRowView::visible).map { rowView -> (rowView.portraitView.slot.findViewByName(UnitPortraitView.PORTRAIT) as? Image)?.bitmap?.base }

fun FormationView.bars(): List<Pair<String, String>> = rowViews.filter(FormationRowView::visible).map { rowView -> rowView.healthBarView.label.text to rowView.manaBarView.label.text }

fun FormationView.selectedRowIndexes(): List<Int> = rowViews.indices.filter { index -> rowViews[index].isSelected }

fun FormationView.hint(): String = hintText.text

fun FormationView.isLaidOut(): Boolean {
    val rows = rowViews.map { rowView -> rowView.getGlobalBounds() }
    return rows.zipWithNext().all { (above, below) -> above.bottom <= below.top } && rows.last().bottom <= hintText.getGlobalBounds().top
}

fun FormationView.hintWidth(): Double = hintText.width

suspend fun ViewsForTesting.clickRow(
    formationView: FormationView,
    index: Int,
) = clickOn(formationView.rowViews[index].healthBarView)

suspend fun ViewsForTesting.clickPortrait(
    formationView: FormationView,
    index: Int,
) = clickOn(formationView.rowViews[index].portraitView)
