package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.isShown
import com.mkz.rpg.shared.adapters.presentation.nextFrame
import korlibs.event.MouseEvent
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

/** Taps the story by its id, when several components have a story of the same name, e.g.: `Empty`. */
suspend fun ViewsForTesting.clickStoryId(
    sidebarView: SidebarView,
    storyId: String,
) = clickOn(sidebarView.storyRows.first { row -> row.storyId == storyId })

/** How far the list is scrolled: 0 at the top, and less than 0 once it is scrolled down. */
fun SidebarView.scrollOffset(): Double = catalogue.y

/** The names of the stories in sight, in their order: the ones inside the list, with none of them cut. */
fun SidebarView.visibleStoryNames(): List<String> {
    val bounds = list.getGlobalBounds()
    return storyRows.filter { row -> row.getGlobalBounds().let { rowBounds -> rowBounds.top >= bounds.top && rowBounds.bottom <= bounds.bottom } }.map { row -> row.label.text }
}

/** Turns the mouse wheel by [pixels] over the sidebar, positive to scroll down. The sidebar must be on the stage. */
@Suppress("DEPRECATION")
suspend fun ViewsForTesting.scrollSidebar(
    sidebarView: SidebarView,
    pixels: Double,
) {
    val center = sidebarView.getGlobalBounds().center
    mouseMoveTo(center.x, center.y)
    val mousePosition = views.windowMousePos
    gameWindow.dispatch(
        MouseEvent(
            type = MouseEvent.Type.SCROLL,
            x = mousePosition.x.toInt(),
            y = mousePosition.y.toInt(),
            scrollDeltaY = pixels.toFloat(),
            scrollDeltaMode = MouseEvent.ScrollDeltaMode.PIXEL,
        ),
    )
    nextFrame()
}

/** Drags the list from the center of the sidebar down by [pixels], negative to drag it up. The sidebar must be on the stage. */
suspend fun ViewsForTesting.dragSidebar(
    sidebarView: SidebarView,
    pixels: Double,
) {
    val start = sidebarView.getGlobalBounds().center
    mouseMoveTo(start.x, start.y)
    mouseDown()
    mouseMoveTo(start.x, start.y + pixels)
    mouseUp()
}
