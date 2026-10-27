package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto

// Records what the presenter asks the sidebar to display, instead of drawing it.
class FakeSidebarView : SidebarView() {
    val displayedCatalogues = mutableListOf<List<ComponentDto>>()
    val selectedStoryIds = mutableListOf<String>()
    var delegate: Delegate? = null
        private set

    override fun setDelegate(delegate: Delegate) {
        this.delegate = delegate
    }

    override fun display(components: List<ComponentDto>) {
        displayedCatalogues += components
    }

    override fun selectStory(storyId: String) {
        selectedStoryIds += storyId
    }
}
