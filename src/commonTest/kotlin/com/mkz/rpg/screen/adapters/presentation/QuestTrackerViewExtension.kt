package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.descendants
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage

// Page object of [QuestTrackerView].

/** The tracker of the battle scene, or `null` when the scene is not displayed yet. */
fun Stage.questTrackerView(): QuestTrackerView? = descendants().filterIsInstance<QuestTrackerView>().firstOrNull()

/** The text it names, e.g.: `Defeat the Goat Wizard`, or `null` until it displays a quest. */
fun QuestTrackerView.displayedTask(): String? = taskText?.text

fun QuestTrackerView.displayedTaskWidth(): Double = taskText?.width ?: 0.0

/** The count of the task, e.g.: `3/5`, or `null` when it has none. */
fun QuestTrackerView.displayedCount(): String? = countText?.text

fun QuestTrackerView.isTaskGlyphShown(): Boolean = glyph != null

fun QuestTrackerView.isTickShown(): Boolean = tick != null

fun QuestTrackerView.isChevronShown(): Boolean = chevronView != null

/** Where the capsule ends on the right of the line, once it has settled. */
fun QuestTrackerView.capsuleRightEdge(): Double = requireNotNull(background) { "It displays no quest" }.let { capsuleBackground -> capsule.x + capsuleBackground.parent!!.x + capsuleBackground.width }

/** How big the capsule is displayed: 1.0 is its normal size. */
fun QuestTrackerView.capsuleScale(): Double = capsule.scaleX

suspend fun ViewsForTesting.clickCapsule(questTrackerView: QuestTrackerView) = clickOn(requireNotNull(questTrackerView.background) { "It displays no quest" })
