package com.mkz.tessavar.storybook.usecases.commands

import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.tessavar.storybook.domain.StorybookEvent.StorySelected
import com.mkz.tessavar.storybook.domain.StorybookMother.component
import com.mkz.tessavar.storybook.domain.StorybookMother.location
import com.mkz.tessavar.storybook.domain.StorybookMother.story
import com.mkz.tessavar.storybook.domain.StorybookMother.storybook
import com.mkz.tessavar.storybook.usecases.services.FakeStoryLocation
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SelectStoryTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val storyLocation = FakeStoryLocation()
    private val eventBus = FakeEventBus()
    private val selectStory = SelectStory(storybookRepository, storyLocation, eventBus)
    private val healthBar =
        component(
            id = "screen-health-bar",
            stories = listOf(story(id = "screen-health-bar--full"), story(id = "screen-health-bar--empty")),
        )

    @Test
    fun `should show the story and keep it in the location when it is another story of the catalogue`() {
        // Given
        storybookRepository.save(storybook(components = listOf(healthBar)))
        // When
        selectStory(storyId = "screen-health-bar--empty")
        // Then
        storybookRepository.search()?.toDto()?.selectedStoryId shouldBe "screen-health-bar--empty"
        storyLocation.keptLocations.shouldContainExactly(location(storyId = "screen-health-bar--empty"))
        eventBus.shouldHavePublishedEvents(StorySelected(storyId = "screen-health-bar--empty"))
    }

    @Test
    fun `should do nothing when the story is already selected`() {
        // Given
        storybookRepository.save(storybook(components = listOf(healthBar)))
        // When
        selectStory(storyId = "screen-health-bar--full")
        // Then
        storyLocation.keptStoryIds.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when the catalogue is not open`() {
        // When
        selectStory(storyId = "screen-health-bar--empty")
        // Then
        storyLocation.keptStoryIds.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }
}
