package com.mkz.tessavar.screen.adapters.presentation.selection.adapters.storage

import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.battleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.nothingSelected
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class InMemorySelectionRepositoryTest {
    private val selectionRepository = InMemorySelectionRepository()

    @Test
    fun `Create should store the selection when the selection is created`() {
        // Given
        val createdSelection = nothingSelected()
        // When
        selectionRepository.create(createdSelection)
        // Then
        val storedSelection = selectionRepository.search()
        storedSelection shouldBe createdSelection
    }

    @Test
    fun `Update should replace the stored selection when the selection is updated`() {
        // Given
        val storedSelection = nothingSelected()
        selectionRepository.create(storedSelection)
        val updatedSelection = battleUnitSelected()
        // When
        selectionRepository.update(updatedSelection)
        // Then
        val searchResult = selectionRepository.search()
        searchResult shouldBe updatedSelection
    }

    @Test
    fun `Search should return null when no selection is stored`() {
        // When
        val storedSelection = selectionRepository.search()
        // Then
        storedSelection.shouldBeNull()
    }
}
