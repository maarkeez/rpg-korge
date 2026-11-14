package com.mkz.tessavar.storybook.usecases.commands

import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.tessavar.storybook.domain.StorybookEvent.ActionsCleared
import com.mkz.tessavar.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.collections.shouldBeEmpty
import kotlin.test.Test

class ClearActionsTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val eventBus = FakeEventBus()
    private val clearActions = ClearActions(storybookRepository, eventBus)

    @Test
    fun `should empty the actions recorded when actions were recorded`() {
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
    fun `should do nothing when no action was recorded`() {
        // Given
        storybookRepository.save(storybook())
        // When
        clearActions()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }

    @Test
    fun `should do nothing when the catalogue is not open`() {
        // When
        clearActions()
        // Then
        eventBus.shouldNotHavePublishedEvents()
    }
}
