package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.image.bitmap.Bitmap
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Image

// Page object of [FormationView] and its [FormationRowView]s.

/** The names of the party members displayed, in party order. */
fun FormationView.names(): List<String> = rowViews.filter(FormationRowView::visible).map { rowView -> rowView.nameText.text }

/** The rows displaying the crown, as their party member leads. */
fun FormationView.crownedRowIndexes(): List<Int> = rowViews.indices.filter { index -> rowViews[index].visible && rowViews[index].isCrowned }

/** The crown of the leader's row, or `null` when no row displays it. */
fun FormationView.crownBitmap(): Bitmap? = rowViews.firstNotNullOfOrNull { rowView -> rowView.crownImage?.bitmap?.base }

/** The portrait displayed on each row, in party order. */
fun FormationView.portraitBitmaps(): List<Bitmap?> =
    rowViews.filter(FormationRowView::visible).map { rowView -> (rowView.portraitView.battleUnitPortrait.findViewByName(UnitPortraitView.PORTRAIT) as? Image)?.bitmap?.base }

/** The health and mana labels of each row, e.g.: `20 / 30` and `5 / 10`. */
fun FormationView.bars(): List<Pair<String, String>> = rowViews.filter(FormationRowView::visible).map { rowView -> rowView.healthBarView.label.text to rowView.manaBarView.label.text }

fun FormationView.selectedRowIndexes(): List<Int> = rowViews.indices.filter { index -> rowViews[index].isSelected }

fun FormationView.hint(): String = hintText.text

/** Whether the rows are stacked in order above the hint and Close, inside the panel. */
fun FormationView.isLaidOut(): Boolean {
    val rows = rowViews.map { rowView -> rowView.getGlobalBounds() }
    val footer = closeButton.getGlobalBounds()
    return rows.zipWithNext().all { (above, below) -> above.bottom <= below.top } &&
        rows.last().bottom <= footer.top &&
        hintText.getGlobalBounds().right <= footer.left
}

suspend fun ViewsForTesting.clickRow(
    formationView: FormationView,
    index: Int,
) = clickOn(formationView.rowViews[index].healthBarView)

suspend fun ViewsForTesting.clickClose(formationView: FormationView) = clickOn(formationView.closeButton)

suspend fun ViewsForTesting.clickPortrait(
    formationView: FormationView,
    index: Int,
) = clickOn(formationView.rowViews[index].portraitView)
