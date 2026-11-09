package com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands

import com.mkz.rpg.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionError
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.abilityInfoDisplayed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.abilitySelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.castGroup
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.castPreviewed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.tile
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class CancelCastTest {
    private val selectionRepository = InMemorySelectionRepository()
    private val eventBus = FakeEventBus()
    private val cancelCast =
        CancelCast(
            selectionRepository = selectionRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should go back to the ability cast range when the selection is previewing the ability cast`() {
        // Given
        val castGroupsWhereCanCast = listOf(castGroup(tile(1, 1)), castGroup(tile(0, 1)))
        selectionRepository.create(
            castPreviewed(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                walkingRange = setOf(tile(1, 0)),
                castGroupsWhereCanCast = castGroupsWhereCanCast,
                castGroup = castGroup(tile(1, 1)),
            ),
        )
        // When
        cancelCast()
        // Then
        selectionRepository.search() shouldBe
            abilitySelected(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                walkingRange = setOf(tile(1, 0)),
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            )
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.AbilitySelected(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroupsWhereCanCast = castGroupsWhereCanCast,
            ),
        )
    }

    @Test
    fun `should throw an invalid selection state error when nothing is selected`() {
        // Given
        selectionRepository.create(
            com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother
                .nothingSelected(),
        )
        // When
        // Then
        shouldThrow<SelectionError.InvalidSelectionState> { cancelCast() }
    }

    @Test
    fun `should throw an invalid selection state error when a battle unit is selected`() {
        // Given
        selectionRepository.create(
            com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother
                .battleUnitSelected(),
        )
        // When
        // Then
        shouldThrow<SelectionError.InvalidSelectionState> { cancelCast() }
    }

    @Test
    fun `should throw an invalid selection state error when the selection is displaying the ability cast range`() {
        // Given
        selectionRepository.create(
            com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother
                .abilitySelected(),
        )
        // When
        // Then
        shouldThrow<SelectionError.InvalidSelectionState> { cancelCast() }
    }

    @Test
    fun `should throw an invalid selection state error when the selection is displaying the ability info`() {
        // Given
        selectionRepository.create(
            com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother
                .abilityInfoDisplayed(),
        )
        // When
        // Then
        shouldThrow<SelectionError.InvalidSelectionState> { cancelCast() }
    }

    @Test
    fun `should throw a selection not found error when no selection exists`() {
        // Given
        // When
        // Then
        shouldThrow<SelectionError.SelectionNotFound> { cancelCast() }
    }
}
