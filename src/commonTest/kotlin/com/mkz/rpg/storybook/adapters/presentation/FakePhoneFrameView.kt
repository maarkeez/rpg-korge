package com.mkz.rpg.storybook.adapters.presentation

import korlibs.korge.view.View

class FakePhoneFrameView : PhoneFrameView() {
    val displayedViews = mutableListOf<View>()

    override fun display(storyView: View) {
        displayedViews += storyView
    }
}
