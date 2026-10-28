package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookError
import com.mkz.rpg.storybook.domain.StorybookEvent.ArgChanged
import com.mkz.rpg.storybook.domain.StorybookMother.location
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class ChangeArgTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val storyLocation = FakeStoryLocation()
    private val eventBus = FakeEventBus()
    private val changeArg = ChangeArg(storybookRepository, storyLocation, eventBus)

    @Test
    fun `should change the arg of the story on show and keep it in the location when its control accepts it`() {
        // Given
        storybookRepository.save(storybook())
        // When
        changeArg(name = "remaining", value = ArgDto.int(10))
        // Then
        storybookRepository.search()?.toDto()?.args shouldBe mapOf("remaining" to ArgDto.int(10))
        storyLocation.keptLocations.shouldContainExactly(location(storyId = "screen-health-bar--full", args = mapOf("remaining" to "10")))
        eventBus.shouldHavePublishedEvents(ArgChanged(storyId = "screen-health-bar--full", argName = "remaining"))
    }

    @Test
    fun `should do nothing when the arg has that value`() {
        // Given
        storybookRepository.save(storybook())
        // When
        changeArg(name = "remaining", value = ArgDto.int(30))
        // Then
        storyLocation.keptLocations.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail, keeping nothing, when its control does not accept the value`() {
        // Given
        storybookRepository.save(storybook())
        // When
        val result = runCatching { changeArg(name = "remaining", value = ArgDto.int(31)) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
        storyLocation.keptLocations.shouldBeEmpty()
    }

    @Test
    fun `should do nothing when the catalogue is not initialised`() {
        // When
        changeArg(name = "remaining", value = ArgDto.int(10))
        // Then
        storyLocation.keptLocations.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }
}
