package com.mkz.rpg.screen.adapters.presentation.selection.usecases.commands

import com.mkz.rpg.battle.domain.BattleMother.battle
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.rpg.screen.adapters.presentation.selection.domain.Selection
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionError
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.abilityInfoDisplayed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.abilitySelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.battleUnitSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.castPreviewed
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.nothingSelected
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionMother.tile
import com.mkz.rpg.screen.adapters.presentation.selection.usecases.services.WalkingRangeService
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RefreshWalkingRangeTest {
    private val queries = InMemoryQueries()
    private val selectionRepository = InMemorySelectionRepository()
    private val eventBus = FakeEventBus()
    private val refreshWalkingRange =
        RefreshWalkingRange(
            searchPosition = queries.searchPosition,
            searchBattle = queries.searchBattle,
            selectionRepository = selectionRepository,
            eventBus = eventBus,
            walkingRangeService = WalkingRangeService(queries.searchBattleUnitById, queries.searchVacantTiles, queries.canWalkTo),
        )

    @Test
    fun `should display the current tile and walking range of the selected battle unit when it is selected`() {
        // Given
        queries.battleRepository.create(battle(battleUnitIds = setOf("battle-unit-1")))
        // The battle unit has moved from where the selection displays it.
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(maximumSteps = 1).toDto()))
        queries.battlefieldRepository.create(battlefield(". K .", occupants = mapOf('K' to "battle-unit-1")))
        val newTiles = setOf(tile(0, 0), tile(0, 2))
        // When
        refreshWalkingRange()
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = tile(0, 1), battleUnitId = "battle-unit-1", walkingRange = newTiles)
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.BattleUnitSelected(tile = tile(0, 1), battleUnitId = "battle-unit-1", walkingRange = newTiles),
        )
    }

    @Test
    fun `should display the current tile of the selected battle unit with no walking range when it has moved out of the battles`() {
        // Given
        selectionRepository.create(battleUnitSelected(tile = tile(0, 0), battleUnitId = "battle-unit-1", walkingRange = emptySet()))
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(maximumSteps = 1).toDto()))
        queries.battlefieldRepository.create(battlefield(". K .", occupants = mapOf('K' to "battle-unit-1")))
        // When
        refreshWalkingRange()
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = tile(0, 1), battleUnitId = "battle-unit-1", walkingRange = emptySet())
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.BattleUnitSelected(tile = tile(0, 1), battleUnitId = "battle-unit-1", walkingRange = emptySet()),
        )
    }

    @Test
    fun `should not display the selected battle unit again when it has not moved out of the battles`() {
        // Given
        // E.g.: a CPU battle unit wandering next to it while its inventory is displayed.
        val selection = battleUnitSelected(tile = tile(0, 1), battleUnitId = "battle-unit-1", walkingRange = emptySet())
        selectionRepository.create(selection)
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(maximumSteps = 1).toDto()))
        queries.battlefieldRepository.create(battlefield(". K .", occupants = mapOf('K' to "battle-unit-1")))
        // When
        refreshWalkingRange()
        // Then
        selectionRepository.search() shouldBe selection
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should deselect when the selected battle unit is no longer on the battlefield`() {
        // Given
        selectionRepository.create(battleUnitSelected(battleUnitId = "battle-unit-1"))
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        refreshWalkingRange()
        // Then
        selectionRepository.search().shouldBeInstanceOf<Selection.NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should not refresh when nothing is selected`() {
        // Given
        val selection = nothingSelected()
        selectionRepository.create(selection)
        // When
        refreshWalkingRange()
        // Then
        selectionRepository.search() shouldBe selection
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not refresh when the selection is displaying an ability cast range`() {
        // Given
        val selection = abilitySelected()
        selectionRepository.create(selection)
        // When
        refreshWalkingRange()
        // Then
        selectionRepository.search() shouldBe selection
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not refresh when the selection is displaying the ability info`() {
        // Given
        val selection = abilityInfoDisplayed()
        selectionRepository.create(selection)
        // When
        refreshWalkingRange()
        // Then
        selectionRepository.search() shouldBe selection
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not refresh when the selection is previewing an ability cast`() {
        // Given
        val selection = castPreviewed()
        selectionRepository.create(selection)
        // When
        refreshWalkingRange()
        // Then
        selectionRepository.search() shouldBe selection
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should throw a selection not found error when no selection exists`() {
        // Given
        // When
        // Then
        shouldThrow<SelectionError.SelectionNotFound> { refreshWalkingRange() }
    }
}
