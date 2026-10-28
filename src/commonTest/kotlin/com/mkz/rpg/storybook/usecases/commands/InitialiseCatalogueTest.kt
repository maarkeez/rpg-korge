package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookEvent.CatalogueInitialised
import com.mkz.rpg.storybook.domain.StorybookEvent.StorySelected
import com.mkz.rpg.storybook.domain.StorybookMother.LANGUAGE_CONTROL
import com.mkz.rpg.storybook.domain.StorybookMother.component
import com.mkz.rpg.storybook.domain.StorybookMother.story
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InitialiseCatalogueTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val eventBus = FakeEventBus()
    private val healthBar =
        component(
            id = "screen-health-bar",
            stories = listOf(story(id = "screen-health-bar--full"), story(id = "screen-health-bar--empty")),
        )

    @Test
    fun `should open the catalogue on the story of the location when it is in the catalogue`() {
        // Given
        val storyLocation = FakeStoryLocation(requestedStoryId = "screen-health-bar--empty")
        val initialiseCatalogue = InitialiseCatalogue(storybookRepository, storyLocation, eventBus)
        // When
        initialiseCatalogue(listOf(healthBar), globalControls = listOf(LANGUAGE_CONTROL), defaultGlobals = mapOf("language" to ArgDto.text("en")))
        // Then
        val storybook = storybookRepository.search()?.toDto()
        storybook?.components shouldBe listOf(healthBar)
        storybook?.selectedStoryId shouldBe "screen-health-bar--empty"
        eventBus.shouldHavePublishedEvents(CatalogueInitialised, StorySelected(storyId = "screen-health-bar--empty"))
    }

    @Test
    fun `should open the catalogue on its first story, keeping the location, when the location asks for an unknown story`() {
        // Given
        val storyLocation = FakeStoryLocation(requestedStoryId = "screen-health-bar--renamed")
        val initialiseCatalogue = InitialiseCatalogue(storybookRepository, storyLocation, eventBus)
        // When
        initialiseCatalogue(listOf(healthBar), globalControls = listOf(LANGUAGE_CONTROL), defaultGlobals = mapOf("language" to ArgDto.text("en")))
        // Then
        storybookRepository.search()?.toDto()?.selectedStoryId shouldBe "screen-health-bar--full"
        storyLocation.keptStoryIds.shouldBeEmpty()
    }

    @Test
    fun `should open the story with the args and globals of the location when their controls accept them`() {
        // Given
        val storyLocation = FakeStoryLocation(requestedStoryId = "screen-health-bar--empty", requestedArgs = mapOf("remaining" to "10"), requestedGlobals = mapOf("language" to "es"))
        val initialiseCatalogue = InitialiseCatalogue(storybookRepository, storyLocation, eventBus)
        // When
        initialiseCatalogue(listOf(healthBar), globalControls = listOf(LANGUAGE_CONTROL), defaultGlobals = mapOf("language" to ArgDto.text("en")))
        // Then
        val storybook = storybookRepository.search()?.toDto()
        storybook?.args shouldBe mapOf("remaining" to ArgDto.int(10))
        storybook?.globals shouldBe mapOf("language" to ArgDto.text("es"))
    }
}
