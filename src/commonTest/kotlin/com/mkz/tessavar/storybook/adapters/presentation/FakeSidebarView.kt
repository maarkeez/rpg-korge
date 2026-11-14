package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.storybook.domain.Storybook.Dto.ComponentDto

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
