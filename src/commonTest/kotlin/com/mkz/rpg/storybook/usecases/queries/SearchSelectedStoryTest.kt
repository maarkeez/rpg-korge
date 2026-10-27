package com.mkz.rpg.storybook.usecases.queries

import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.StorybookMother.component
import com.mkz.rpg.storybook.domain.StorybookMother.story
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchSelectedStoryTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val searchSelectedStory = SearchSelectedStory(storybookRepository)

    @Test
    fun `should return the story on show when the catalogue is initialised`() {
        // Given
        val empty = story(id = "screen-health-bar--empty", name = "Empty")
        val healthBar = component(id = "screen-health-bar", stories = listOf(story(id = "screen-health-bar--full"), empty))
        storybookRepository.save(storybook(components = listOf(healthBar), requestedStoryId = "screen-health-bar--empty"))
        // When
        val selectedStory = searchSelectedStory()
        // Then
        selectedStory shouldBe empty
    }

    @Test
    fun `should return null when the catalogue is not initialised`() {
        // When
        val selectedStory = searchSelectedStory()
        // Then
        selectedStory.shouldBeNull()
    }
}
