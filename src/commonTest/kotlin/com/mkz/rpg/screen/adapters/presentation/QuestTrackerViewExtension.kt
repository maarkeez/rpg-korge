package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import korlibs.korge.tests.ViewsForTesting

fun QuestTrackerView.displayedTask(): String? = taskText?.text

fun QuestTrackerView.displayedTaskWidth(): Double = taskText?.width ?: 0.0

fun QuestTrackerView.displayedCount(): String? = countText?.text

fun QuestTrackerView.isPinShown(): Boolean = pin != null

fun QuestTrackerView.isTaskGlyphShown(): Boolean = glyph != null

fun QuestTrackerView.isTickShown(): Boolean = tick != null

fun QuestTrackerView.isChevronShown(): Boolean = chevronView != null

fun QuestTrackerView.capsuleRightEdge(): Double = requireNotNull(background) { "It displays no quest" }.let { capsuleBackground -> capsule.x + capsuleBackground.parent!!.x + capsuleBackground.width }

fun QuestTrackerView.capsuleScale(): Double = capsule.scaleX

suspend fun ViewsForTesting.clickCapsule(questTrackerView: QuestTrackerView) = clickOn(requireNotNull(questTrackerView.background) { "It displays no quest" })
