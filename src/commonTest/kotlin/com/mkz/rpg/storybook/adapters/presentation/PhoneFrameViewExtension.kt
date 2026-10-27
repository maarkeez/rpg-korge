package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.adapters.presentation.isShown
import korlibs.korge.view.View

// Page object of [PhoneFrameView].

/** The view of the story on show, or `null` while no story is shown. */
fun PhoneFrameView.displayedView(): View? = storyView

/** How many views the screen shows, besides its background. */
fun PhoneFrameView.displayedViewCount(): Int = screen.children.count { child -> child != screenBackground }

/** Whether the view on show is entirely on the screen of the phone, in its middle. */
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

/** The size of the screen of the phone, e.g.: the game window, 390x844. */
fun PhoneFrameView.screenSize(): Pair<Double, Double> = screenBackground.width to screenBackground.height
