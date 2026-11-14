package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.clickOn
import com.mkz.tessavar.shared.adapters.presentation.isShown
import com.mkz.tessavar.shared.adapters.presentation.nextFrame
import korlibs.event.MouseEvent
import korlibs.korge.tests.ViewsForTesting

fun SidebarView.titleText(): String = title.text

fun SidebarView.groupNames(): List<String> = groupTexts.map { text -> text.text }

fun SidebarView.componentTitles(): List<String> = componentTexts.map { text -> text.text }

fun SidebarView.storyNames(): List<String> = storyRows.map { row -> row.label.text }

fun SidebarView.selectedStoryNames(): List<String> = storyRows.filter { row -> row.background.color == SidebarView.SELECTED_COLOR }.map { row -> row.label.text }

fun SidebarView.isListedTopDown(): Boolean {
    val lists = listOf(groupTexts, componentTexts, storyRows)
    return title.globalPos.y < searchInput.globalPos.y &&
        lists.all { views -> views.zipWithNext().all { (above, below) -> above.globalPos.y < below.globalPos.y } } &&
        lists.flatten().all { view -> view.isShown() && view.globalPos.y > searchInput.globalPos.y + searchInput.height }
}

fun SidebarView.searchText(): String = searchInput.text

fun SidebarView.searchPlaceholderText(): String? = searchPlaceholder.takeIf { text -> text.isShown() }?.text

fun SidebarView.typeSearch(text: String) {
    searchInput.text = text
}

fun SidebarView.isClearSearchShown(): Boolean = clearSearchButton.isShown()

suspend fun ViewsForTesting.clickClearSearch(sidebarView: SidebarView) = clickOn(sidebarView.clearSearchButton)

fun SidebarView.noStoriesMessage(): String? = noStoriesText?.takeIf { text -> text.isShown() }?.text

suspend fun ViewsForTesting.clickStory(
    sidebarView: SidebarView,
    storyName: String,
) = clickOn(sidebarView.storyRows.first { row -> row.label.text == storyName })

suspend fun ViewsForTesting.clickStoryId(
    sidebarView: SidebarView,
    storyId: String,
) = clickOn(sidebarView.storyRows.first { row -> row.storyId == storyId })

fun SidebarView.scrollOffset(): Double = catalogue.y

fun SidebarView.visibleStoryNames(): List<String> {
    val bounds = list.getGlobalBounds()
    return storyRows.filter { row -> row.getGlobalBounds().let { rowBounds -> rowBounds.top >= bounds.top && rowBounds.bottom <= bounds.bottom } }.map { row -> row.label.text }
}

@Suppress("DEPRECATION")
suspend fun ViewsForTesting.scrollSidebar(
    sidebarView: SidebarView,
    pixels: Double,
    ctrl: Boolean = false,
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
            isCtrlDown = ctrl,
        ),
    )
    nextFrame()
}

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
