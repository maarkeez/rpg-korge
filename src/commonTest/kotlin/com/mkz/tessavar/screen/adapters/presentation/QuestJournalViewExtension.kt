package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.GraphicsRenderer
import korlibs.korge.view.ShapeView

fun QuestJournalView.displayedRows(): List<String> = rows.map { row -> row.questId }

fun QuestJournalView.displayedSections(): List<String> = rows.map { row -> row.sectionText.text }

fun QuestJournalView.displayedNames(): List<String> = rows.map { row -> row.nameText.text }

fun QuestJournalView.displayedTasks(): List<String> = rows.map { row -> row.taskText.text }

fun QuestJournalView.displayedCounts(): List<String?> = rows.map { row -> row.countText?.text }

fun QuestJournalView.rowsInSight(): List<String> = rows.filter { row -> row.view.visible }.map { row -> row.questId }

fun QuestJournalView.scrollPosition(): Double =
    -rows
        .first()
        .view.parent!!
        .y

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
