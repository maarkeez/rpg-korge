package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.storybook.adapters.location.platformStoryLocation
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.StorybookRepository
import com.mkz.rpg.storybook.usecases.commands.InitialiseCatalogue
import com.mkz.rpg.storybook.usecases.commands.SelectStory
import com.mkz.rpg.storybook.usecases.queries.SearchCatalogue
import com.mkz.rpg.storybook.usecases.queries.SearchSelectedStory
import com.mkz.rpg.storybook.usecases.services.StoryLocation

/** The Storybook: the views of the game shown in isolation, in each of their states. Location: where the story on show is kept. */
class StorybookApi(
    eventBus: EventBus,
    storyLocation: StoryLocation = platformStoryLocation(),
) {
    companion object {
        /** Every story binding, in the order the sidebar lists them. They are listed by hand, as JS has no reflection. */
        val STORY_BINDINGS: List<StoryBinding> =
            listOf(
                HealthBarViewStories,
            )
    }

    // Storage
    private val storybookRepository: StorybookRepository = InMemoryStorybookRepository()

    // Commands
    val initialiseCatalogue = InitialiseCatalogue(storybookRepository, storyLocation, eventBus)
    val selectStory = SelectStory(storybookRepository, storyLocation, eventBus)

    // Queries
    val searchCatalogue = SearchCatalogue(storybookRepository)
    val searchSelectedStory = SearchSelectedStory(storybookRepository)

    /** Opens the catalogue of every story binding, on the story the location asks for. */
    fun init() {
        initialiseCatalogue(STORY_BINDINGS.map(StoryBinding::component))
    }

    /** The binding of the component the story belongs to. */
    fun bindingOf(storyId: String): StoryBinding = STORY_BINDINGS.first { binding -> binding.component.stories.any { story -> story.id == storyId } }
}
