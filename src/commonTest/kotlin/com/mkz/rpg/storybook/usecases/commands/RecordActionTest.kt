package com.mkz.rpg.storybook.usecases.commands

import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.domain.shouldNotHavePublishedEvents
import com.mkz.rpg.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.rpg.storybook.domain.Storybook.Dto.ActionDto
import com.mkz.rpg.storybook.domain.StorybookEvent.ActionRecorded
import com.mkz.rpg.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class RecordActionTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val eventBus = FakeEventBus()
    private val recordAction = RecordAction(storybookRepository, eventBus)

    @Test
    fun `should log the action as the newest one when the catalogue is initialised`() {
        // Given
        storybookRepository.save(storybook())
        // When
        recordAction(name = "shopSlotSelected", arguments = listOf("3"))
        // Then
        storybookRepository
            .search()
            ?.toDto()
            ?.actions
            ?.shouldContainExactly(ActionDto(name = "shopSlotSelected", arguments = listOf("3")))
        eventBus.shouldHavePublishedEvents(ActionRecorded(storyId = "screen-health-bar--full", actionName = "shopSlotSelected"))
    }

    @Test
    fun `should do nothing when the catalogue is not initialised`() {
        // When
        recordAction(name = "shopSlotSelected", arguments = listOf("3"))
        // Then
        storybookRepository.search().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
