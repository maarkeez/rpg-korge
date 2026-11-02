package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.GraphicsRenderer
import korlibs.korge.view.ShapeView

// Page object of [QuestJournalView].

/** The ids of the quests of the rows, from the top. */
fun QuestJournalView.displayedRows(): List<String> = rows.map { row -> row.questId }

fun QuestJournalView.displayedSections(): List<String> = rows.map { row -> row.sectionText.text }

fun QuestJournalView.displayedNames(): List<String> = rows.map { row -> row.nameText.text }

fun QuestJournalView.displayedTasks(): List<String> = rows.map { row -> row.taskText.text }

fun QuestJournalView.displayedCounts(): List<String?> = rows.map { row -> row.countText?.text }

/** How many rows are in sight, as those out of the list are hidden. */
fun QuestJournalView.rowsInSight(): List<String> = rows.filter { row -> row.view.visible }.map { row -> row.questId }

/** How far the list is scrolled down. */
fun QuestJournalView.scrollPosition(): Double =
    -rows
        .first()
        .view.parent!!
        .y

/** Whether no shape of the rows is drawn straight to the GPU, past the clip of the list. */
fun QuestJournalView.rowsAreClippable(): Boolean =
    rows.all { row ->
        row.view
            .descendants()
            .filterIsInstance<ShapeView>()
            .all { shape -> shape.renderer == GraphicsRenderer.SYSTEM }
    }

fun QuestJournalView.isRepeatablePillShown(questId: String): Boolean = rowOf(questId).repeatablePillView != null

fun QuestJournalView.isProgressBarShown(questId: String): Boolean = rowOf(questId).progressBarView != null

fun QuestJournalView.isPinShown(questId: String): Boolean = rowOf(questId).pin != null

fun QuestJournalView.isPinnedRow(questId: String): Boolean = rowOf(questId).isPinned

suspend fun ViewsForTesting.clickRow(
    questJournalView: QuestJournalView,
    questId: String,
) = clickOn(questJournalView.rowOf(questId).nameText)

suspend fun ViewsForTesting.clickPin(
    questJournalView: QuestJournalView,
    questId: String,
) = clickOn(requireNotNull(questJournalView.rowOf(questId).pin) { "The row has no pin" })

/** Drags the list from a row in sight, by [pixels], negative to drag it up. The journal must be on the stage. */
suspend fun ViewsForTesting.dragJournal(
    questJournalView: QuestJournalView,
    pixels: Double,
) {
    val start = questJournalView.getGlobalBounds().center
    mouseMoveTo(start.x, start.y)
    mouseDown()
    mouseMoveTo(start.x, start.y + pixels)
    mouseUp()
}

private fun QuestJournalView.rowOf(questId: String) = rows.first { row -> row.questId == questId }
