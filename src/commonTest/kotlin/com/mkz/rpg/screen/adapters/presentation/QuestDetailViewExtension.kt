package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.image.color.RGBA
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Text
import korlibs.korge.view.View

// Page object of [QuestDetailView].

fun QuestDetailView.isDetailShown(): Boolean = isShown()

fun QuestDetailView.displayedCaption(): String? = captionText?.text

/** The lines of the name, from the top. */
fun QuestDetailView.displayedName(): List<String> = linesOf(nameView)

fun QuestDetailView.isRepeatablePillShown(): Boolean = repeatablePillView != null

/** Where the reward row ends, within the view. */
fun QuestDetailView.rewardBottom(): Double = requireNotNull(rewardView) { "It displays no quest" }.getBounds(this).bottom

/** Where the buttons start, within the view. */
fun QuestDetailView.buttonsTop(): Double = requireNotNull(backButton) { "It displays no quest" }.y

/** The lines of the description, from the top. */
fun QuestDetailView.displayedDescription(): List<String> = linesOf(descriptionView)

fun QuestDetailView.displayedTask(): String? = taskText?.text

/** The count of the task, e.g.: `3/5`, or `null` when it has none. */
fun QuestDetailView.displayedCount(): String? = countText?.text

fun QuestDetailView.isProgressBarShown(): Boolean = progressBarView != null

/** How many tasks are done, e.g.: `2 tasks done`, or `null` when it is not displayed. */
fun QuestDetailView.displayedTasksDone(): String? = tasksDoneText?.text

fun QuestDetailView.isTargetTicked(): Boolean = targetTick != null

/** The texts of the reward row, e.g.: `150 G`. */
fun QuestDetailView.displayedReward(): List<String> = linesOf(rewardView)

fun QuestDetailView.isAbandonShown(): Boolean = abandonButton != null

/** The colour of Abandon, not pressed. */
fun QuestDetailView.abandonColor(): RGBA = requireNotNull(abandonButton) { "It displays no Abandon" }.confirmButton.bgColorOut

suspend fun ViewsForTesting.clickAbandon(questDetailView: QuestDetailView) = clickConfirm(requireNotNull(questDetailView.abandonButton) { "It displays no Abandon" })

suspend fun ViewsForTesting.clickBack(questDetailView: QuestDetailView) = clickCancel(requireNotNull(questDetailView.backButton) { "It displays no quest" })

private fun linesOf(view: View?): List<String> =
    view
        ?.descendants()
        .orEmpty()
        .filterIsInstance<Text>()
        .map { line -> line.text }
