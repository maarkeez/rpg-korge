package com.mkz.rpg.storybook.adapters.presentation

import korlibs.korge.view.View

// Records the views the presenter asks the phone to show, instead of drawing them.
class FakePhoneFrameView : PhoneFrameView() {
    val displayedViews = mutableListOf<View>()

    override fun display(storyView: View) {
        displayedViews += storyView
    }
}
