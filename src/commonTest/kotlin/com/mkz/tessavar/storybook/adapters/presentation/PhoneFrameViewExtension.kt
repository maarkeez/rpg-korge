package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.shared.adapters.presentation.isShown
import korlibs.korge.view.View

fun PhoneFrameView.displayedView(): View? = storyView

fun PhoneFrameView.displayedViewCount(): Int = screen.children.count { child -> child != screenBackground }

fun PhoneFrameView.isViewCenteredOnScreen(): Boolean {
    val view = storyView ?: return false
    val screenBounds = screenBackground.getGlobalBounds()
    val viewBounds = view.getGlobalBounds()
    return view.isShown() &&
        screenBounds.contains(viewBounds.topLeft) &&
        screenBounds.contains(viewBounds.bottomRight) &&
        kotlin.math.abs(screenBounds.center.x - viewBounds.center.x) < 1 &&
        kotlin.math.abs(screenBounds.center.y - viewBounds.center.y) < 1
}

fun PhoneFrameView.screenSize(): Pair<Double, Double> = screenBackground.width to screenBackground.height
