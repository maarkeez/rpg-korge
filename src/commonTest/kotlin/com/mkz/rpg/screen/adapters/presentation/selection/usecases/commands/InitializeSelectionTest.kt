package com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class InitializeSelectionTest {
    private val selectionRepository = InMemorySelectionRepository()
    private val initializeSelection =
        InitializeSelection(
            selectionRepository = selectionRepository,
        )

    @Test
    fun `should create a selection of nothing when the selection is initialized`() {
        // Given
        // When
        initializeSelection()
        // Then
        selectionRepository.search().shouldBeInstanceOf<Selection.NothingSelected>()
    }
}
