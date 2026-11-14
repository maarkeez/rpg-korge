package com.mkz.tessavar.storybook.usecases.commands

import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.domain.shouldNotHavePublishedEvents
import com.mkz.tessavar.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ActionDto
import com.mkz.tessavar.storybook.domain.StorybookEvent.ActionRecorded
import com.mkz.tessavar.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import kotlin.test.Test

class RecordActionTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val eventBus = FakeEventBus()
    private val recordAction = RecordAction(storybookRepository, eventBus)

    @Test
    fun `should record the action as the newest one when the catalogue is open`() {
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
    fun `should do nothing when the catalogue is not open`() {
        // When
        recordAction(name = "shopSlotSelected", arguments = listOf("3"))
        // Then
        storybookRepository.search().shouldBeNull()
        eventBus.shouldNotHavePublishedEvents()
    }
}
