package com.mkz.tessavar.storybook.usecases.queries

import com.mkz.tessavar.storybook.adapters.storage.InMemoryStorybookRepository
import com.mkz.tessavar.storybook.domain.Storybook.Dto.ActionDto
import com.mkz.tessavar.storybook.domain.StorybookMother.storybook
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.test.Test

class SearchActionsTest {
    private val storybookRepository = InMemoryStorybookRepository()
    private val searchActions = SearchActions(storybookRepository)

    @Test
    fun `should return the actions recorded, the newest first, when the catalogue is open`() {
        // Given
        storybookRepository.save(storybook().recordAction("shopSlotSelected", listOf("3")).recordAction("shopBackSelected", emptyList()))
        // When
        val actions = searchActions()
        // Then
        actions.shouldContainExactly(ActionDto(name = "shopBackSelected", arguments = emptyList()), ActionDto(name = "shopSlotSelected", arguments = listOf("3")))
    }

    @Test
    fun `should return no actions when the catalogue is not open`() {
        // When
        val actions = searchActions()
        // Then
        actions.shouldBeEmpty()
    }
}
