package com.mkz.tessavar.screen.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.descendants
import com.mkz.tessavar.shared.adapters.presentation.isShown
import korlibs.korge.view.Text
import korlibs.korge.view.View

fun QuestDetailView.isDetailShown(): Boolean = isShown()

fun QuestDetailView.displayedCaption(): String? = captionText?.text

fun QuestDetailView.displayedName(): List<String> = linesOf(nameView)

fun QuestDetailView.isRepeatablePillShown(): Boolean = repeatablePillView != null

fun QuestDetailView.rewardBottom(): Double = requireNotNull(rewardView) { "It displays no quest" }.getBounds(this).bottom

fun QuestDetailView.displayedDescription(): List<String> = linesOf(descriptionView)

fun QuestDetailView.displayedTask(): String? = taskText?.text

fun QuestDetailView.displayedCount(): String? = countText?.text

fun QuestDetailView.isProgressBarShown(): Boolean = progressBarView != null

fun QuestDetailView.displayedTasksDone(): String? = tasksDoneText?.text

fun QuestDetailView.isTargetTicked(): Boolean = targetTick != null

fun QuestDetailView.displayedReward(): List<String> = linesOf(rewardView)

private fun linesOf(view: View?): List<String> =
    view
        ?.descendants()
        .orEmpty()
        .filterIsInstance<Text>()
        .map { line -> line.text }
