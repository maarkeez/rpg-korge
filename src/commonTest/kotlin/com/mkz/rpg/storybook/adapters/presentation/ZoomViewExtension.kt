package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.STORYBOOK_WINDOW_SIZE
import com.mkz.rpg.shared.adapters.presentation.clickOn
import com.mkz.rpg.shared.adapters.presentation.descendants
import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.tests.ViewsForTesting
import korlibs.korge.view.Stage
import korlibs.math.geom.Point
import korlibs.math.geom.Rectangle

// Page object of [ZoomView] and its [ZoomBarView].

/** The zoom shown in the bar, e.g.: `125%`. */
fun ZoomBarView.zoomText(): String = zoomText.text

suspend fun ViewsForTesting.clickZoomOut(zoomBarView: ZoomBarView) = clickOn(zoomBarView.zoomOutButton)

suspend fun ViewsForTesting.clickZoomIn(zoomBarView: ZoomBarView) = clickOn(zoomBarView.zoomInButton)

suspend fun ViewsForTesting.clickFit(zoomBarView: ZoomBarView) = clickOn(zoomBarView.fitButton)

suspend fun ViewsForTesting.clickPhone(zoomBarView: ZoomBarView) = clickOn(zoomBarView.phoneButton)

/** The bar of the zoom. */
fun ZoomView.zoomBar(): ZoomBarView = zoomBarView

/** How many times larger than at Fit the Storybook is drawn. */
fun ZoomView.zoom(): Double = storybook.scaleX

/** Where the whole 1280x900 Storybook is, on the stage. */
fun ZoomView.storybookArea(): Rectangle = Rectangle.fromBounds(storybook.localToGlobal(Point.ZERO), storybook.localToGlobal(Point(STORYBOOK_WINDOW_SIZE.width, STORYBOOK_WINDOW_SIZE.height)))

/** Where an area of the Storybook, in its own units, is on the stage, e.g.: the phone. */
fun ZoomView.areaOnStage(area: Rectangle): Rectangle = Rectangle.fromBounds(storybook.localToGlobal(area.topLeft), storybook.localToGlobal(area.bottomRight))

/** Whether the bar is shown entirely on the [screen], at its bottom right corner. */
fun ZoomView.isBarAtBottomRightOf(screen: Rectangle): Boolean {
    val bar = zoomBarView.getGlobalBounds()
    return zoomBarView.isShown() &&
        screen.contains(bar.topLeft) &&
        screen.contains(bar.bottomRight) &&
        bar.right > screen.right - 0.05 * screen.width &&
        bar.bottom > screen.bottom - 0.05 * screen.height
}

/** The zoom on the screen, or `null` while the Storybook is not on the stage. */
fun Stage.zoomView(): ZoomView? = descendants().filterIsInstance<ZoomView>().firstOrNull()
