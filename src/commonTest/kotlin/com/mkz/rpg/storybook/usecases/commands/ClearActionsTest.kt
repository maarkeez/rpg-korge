package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.StorybookEvent.ActionsCleared
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class ClearActionsTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val eventBus = FakeEventBus()
    private val clearActions = ClearActions(storybookRepository, eventBus)

    @Test
    fun `should empty the log of actions when actions were logged`() {
        // Given
        storybookRepository.save(storybook().recordAction("shopSlotSelected", listOf("3")).pullEvents().second)
        // When
        clearActions()
        // Then
        storybookRepository
            .search()
            ?.toDto()
            ?.actions
            ?.shouldBeEmpty()
        eventBus.shouldHavePublishedEvents(ActionsCleared(storyId = "screen-health-bar--full"))
    }

    @Test
    fun `should do nothing when no action was logged`() {
        // Given
        storybookRepository.save(storybook())
        // When
        clearActions()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when the catalogue is not initialised`() {
        // When
        clearActions()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
