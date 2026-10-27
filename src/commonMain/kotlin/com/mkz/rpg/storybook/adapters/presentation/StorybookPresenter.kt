package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.storybook.domain.StorybookEvent.CatalogueInitialised
import com.mkz.rpg.storybook.domain.StorybookEvent.StorySelected

/** Lists the catalogue in the sidebar, and shows the story selected, rebuilding its view from scratch. */
class StorybookPresenter(
    private val sidebarView: SidebarView,
    private val storyInfoView: StoryInfoView,
    private val phoneFrameView: PhoneFrameView,
    private val storybookApi: StorybookApi,
    private val storyContext: StoryContext,
    eventBus: EventBus,
) {
    private val subscriptions =
        listOf(
            eventBus.subscribe<CatalogueInitialised> {
                sidebarView.display(storybookApi.searchCatalogue())
            },
            eventBus.subscribe<StorySelected> { event ->
                displayStory(event.storyId)
            },
        )

    init {
        sidebarView.setDelegate(
            object : SidebarView.Delegate {
                override fun storySelected(storyId: String) = storybookApi.selectStory(storyId)
            },
        )
    }

    private fun displayStory(storyId: String) {
        val binding = storybookApi.bindingOf(storyId)
        val story = binding.component.stories.first { story -> story.id == storyId }
        sidebarView.selectStory(storyId)
        storyInfoView.display(binding.component, story)
        phoneFrameView.display(binding.render(storyId, storyContext))
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }
}
