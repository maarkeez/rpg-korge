package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookEvent.ArgsReset
import com.mkz.rpg.storybook.domain.StorybookMother.location
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ResetArgsTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val storyLocation = FakeStoryLocation()
    private val eventBus = FakeEventBus()
    private val resetArgs = ResetArgs(storybookRepository, storyLocation, eventBus)

    @Test
    fun `should give the story on show its own args back, also in the location, when args were changed`() {
        // Given
        storybookRepository.save(storybook(requestedStoryId = "screen-health-bar--full", requestedArgs = mapOf("remaining" to "10")))
        // When
        resetArgs()
        // Then
        storybookRepository.search()?.toDto()?.args shouldBe mapOf("remaining" to ArgDto.int(30))
        storyLocation.keptLocations.shouldContainExactly(location(storyId = "screen-health-bar--full"))
        eventBus.shouldHavePublishedEvents(ArgsReset(storyId = "screen-health-bar--full"))
    }

    @Test
    fun `should do nothing when no arg was changed`() {
        // Given
        storybookRepository.save(storybook())
        // When
        resetArgs()
        // Then
        storyLocation.keptLocations.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when the catalogue is not initialised`() {
        // When
        resetArgs()
        // Then
        storyLocation.keptLocations.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }
}
