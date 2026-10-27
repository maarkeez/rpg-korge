package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting

// Page object of [SidebarView].

fun SidebarView.titleText(): String = title.text

/** The groups listed, in their order, as displayed, e.g.: `SCREEN`. */
fun SidebarView.groupNames(): List<String> = groupTexts.map { text -> text.text }

/** The titles of the components listed, in their order, e.g.: `Health bar`. */
fun SidebarView.componentTitles(): List<String> = componentTexts.map { text -> text.text }

/** The names of the stories listed, in their order, e.g.: `Full`. */
fun SidebarView.storyNames(): List<String> = storyRows.map { row -> row.label.text }

/** The names of the stories highlighted as on show. */
fun SidebarView.selectedStoryNames(): List<String> = storyRows.filter { row -> row.background.color == SidebarView.SELECTED_COLOR }.map { row -> row.label.text }

/** Whether the title is on top, and the groups, components and stories are listed top down, each in its order, all of them shown. */
fun SidebarView.isListedTopDown(): Boolean {
    val lists = listOf(groupTexts, componentTexts, storyRows)
    return lists.all { views -> views.zipWithNext().all { (above, below) -> above.globalPos.y < below.globalPos.y } } &&
        lists.flatten().all { view -> view.isShown() && view.globalPos.y > title.globalPos.y }
}

suspend fun ViewsForTesting.clickStory(
    sidebarView: SidebarView,
    storyName: String,
) = clickOn(sidebarView.storyRows.first { row -> row.label.text == storyName })
