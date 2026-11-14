package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.commands

import com.mkz.tessavar.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.abilityInfoDisplayed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.abilitySelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.battleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.castPreviewed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.nothingSelected
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DeselectBattleUnitTest {
    private val selectionRepository = InMemorySelectionRepository()
    private val eventBus = FakeEventBus()
    private val deselectBattleUnit =
        DeselectBattleUnit(
            selectionRepository = selectionRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should deselect when the battle unit is deselected and nothing is selected`() {
        // Given
        selectionRepository.create(nothingSelected())
        // When
        deselectBattleUnit()
        // Then
        selectionRepository.search() shouldBe nothingSelected()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect when the battle unit is deselected and a battle unit is selected`() {
        // Given
        selectionRepository.create(battleUnitSelected())
        // When
        deselectBattleUnit()
        // Then
        selectionRepository.search() shouldBe nothingSelected()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect when the battle unit is deselected and the selection is displaying an ability cast range`() {
        // Given
        selectionRepository.create(abilitySelected())
        // When
        deselectBattleUnit()
        // Then
        selectionRepository.search() shouldBe nothingSelected()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect when the battle unit is deselected and the selection is displaying the ability info`() {
        // Given
        selectionRepository.create(abilityInfoDisplayed())
        // When
        deselectBattleUnit()
        // Then
        selectionRepository.search() shouldBe nothingSelected()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should deselect when the battle unit is deselected and the selection is previewing an ability cast`() {
        // Given
        selectionRepository.create(castPreviewed())
        // When
        deselectBattleUnit()
        // Then
        selectionRepository.search() shouldBe nothingSelected()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should throw a selection not found error when no selection exists`() {
        // Given
        // When
        // Then
        shouldThrow<SelectionError.SelectionNotFound> { deselectBattleUnit() }
    }
}
