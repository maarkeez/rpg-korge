package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Text
import korlibs.korge.view.View

// Page object of [QuestAnnouncementView].

fun QuestAnnouncementView.isAnnouncementShown(): Boolean = isShown()

fun QuestAnnouncementView.displayedTitle(): String = linesOf(titleText).joinToString(" ")

fun QuestAnnouncementView.displayedName(): String = linesOf(nameView).joinToString(" ")

fun QuestAnnouncementView.displayedDescription(): String = linesOf(descriptionView).joinToString(" ")

/** The current task, or `null` when it announces the completion. */
fun QuestAnnouncementView.displayedTask(): String? = taskText?.text

/** The gold of the reward, e.g.: `150 G`, or none when it pays none or it announces the start. */
fun QuestAnnouncementView.displayedGold(): List<String> = linesOf(rewardView)

/** The names of the items of the reward. */
fun QuestAnnouncementView.displayedItems(): List<String> = itemNames.map { name -> name.text }

fun QuestAnnouncementView.isTargetTicked(): Boolean = targetTick != null

/** The middle of the card, on the screen, as a pair of x and y. */
fun QuestAnnouncementView.cardCentre(): Pair<Double, Double> {
    val card = requireNotNull(card) { "It announces nothing" }
    val background = requireNotNull(background) { "It announces nothing" }
    return card.x + background.width / 2 to card.y + background.height / 2
}

fun QuestAnnouncementView.cardHeight(): Double = requireNotNull(background) { "It announces nothing" }.height

suspend fun ViewsForTesting.clickContinue(questAnnouncementView: QuestAnnouncementView) = clickConfirm(requireNotNull(questAnnouncementView.continueButton) { "It announces nothing" })

private fun linesOf(view: View?): List<String> =
    view
        ?.descendants()
        .orEmpty()
        .filterIsInstance<Text>()
        .map { line -> line.text }
