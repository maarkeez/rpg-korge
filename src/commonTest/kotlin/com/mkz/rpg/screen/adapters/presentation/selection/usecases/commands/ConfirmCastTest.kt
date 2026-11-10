package com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionError
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.battleUnitSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.castGroup
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.castPreviewed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.tile
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ConfirmCastTest {
    private val selectionRepository = InMemorySelectionRepository()
    private val eventBus = FakeEventBus()
    private val confirmCast =
        ConfirmCast(
            selectionRepository = selectionRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should cast the ability and keep the caster selected when the selection is previewing the ability cast`() {
        // Given
        val castGroup = castGroup(tile(1, 1))
        selectionRepository.create(
            castPreviewed(
                casterTile = tile(0, 0),
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                walkingRange = setOf(tile(0, 1)),
                castGroup = castGroup,
            ),
        )
        // When
        confirmCast()
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = setOf(tile(0, 1)))
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestCastAbility(
                battleUnitId = "battle-unit-1",
                abilityId = "ability-1",
                castGroup =
                    castGroup.tiles
                        .map { tile -> Battlefield.Dto.PositionDto(row = tile.row, column = tile.column) },
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
        shouldThrow<SelectionError.InvalidSelectionState> { confirmCast() }
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
        shouldThrow<SelectionError.InvalidSelectionState> { confirmCast() }
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
        shouldThrow<SelectionError.InvalidSelectionState> { confirmCast() }
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
        shouldThrow<SelectionError.InvalidSelectionState> { confirmCast() }
    }

    @Test
    fun `should throw a selection not found error when no selection exists`() {
        // Given
        // When
        // Then
        shouldThrow<SelectionError.SelectionNotFound> { confirmCast() }
    }
}
