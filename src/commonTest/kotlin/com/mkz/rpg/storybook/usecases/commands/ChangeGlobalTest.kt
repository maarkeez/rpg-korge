package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.StorybookError
import com.mkz.rpg.storybook.domain.StorybookEvent.GlobalChanged
import com.mkz.rpg.storybook.domain.StorybookMother.location
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import com.mkz.rpg.storybook.usecases.services.FakeStoryLocation
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.test.Test

class ChangeGlobalTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val storyLocation = FakeStoryLocation()
    private val eventBus = FakeEventBus()
    private val changeGlobal = ChangeGlobal(storybookRepository, storyLocation, eventBus)

    @Test
    fun `should change the global and keep it in the location when its control accepts it`() {
        // Given
        storybookRepository.save(storybook())
        // When
        changeGlobal(name = "language", value = ArgDto.text("es"))
        // Then
        storybookRepository.search()?.toDto()?.globals shouldBe mapOf("language" to ArgDto.text("es"))
        storyLocation.keptLocations.shouldContainExactly(location(storyId = "screen-health-bar--full", globals = mapOf("language" to "es")))
        eventBus.shouldHavePublishedEvents(GlobalChanged(globalName = "language"))
    }

    @Test
    fun `should do nothing when the global has that value`() {
        // Given
        storybookRepository.save(storybook())
        // When
        changeGlobal(name = "language", value = ArgDto.text("en"))
        // Then
        storyLocation.keptLocations.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should fail when its control does not accept the value`() {
        // Given
        storybookRepository.save(storybook())
        // When
        val result = runCatching { changeGlobal(name = "language", value = ArgDto.text("fr")) }
        // Then
        result.exceptionOrNull().shouldBeTypeOf<StorybookError.InvalidValue>()
    }

    @Test
    fun `should do nothing when the catalogue is not initialised`() {
        // When
        changeGlobal(name = "language", value = ArgDto.text("es"))
        // Then
        storyLocation.keptLocations.shouldBeEmpty()
        eventBus.shouldNotHavePublishedEvents()
    }
}
