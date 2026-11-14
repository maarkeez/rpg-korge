package com.mkz.tessavar.screen.adapters.presentation.selection.usecases.commands

import com.mkz.tessavar.battle.domain.BattleMother.battle
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.tessavar.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.tessavar.screen.adapters.presentation.selection.adapters.storage.InMemorySelectionRepository
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.Selection
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionError
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.abilityInfoDisplayed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.abilitySelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.battleUnitSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.castPreviewed
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.nothingSelected
import com.mkz.tessavar.screen.adapters.presentation.selection.domain.SelectionMother.tile
import com.mkz.tessavar.screen.adapters.presentation.selection.usecases.services.WalkingRangeService
import com.mkz.tessavar.shared.domain.FakeEventBus
import com.mkz.tessavar.shared.domain.shouldHavePublishedEvents
import com.mkz.tessavar.shared.usecases.InMemoryQueries
import com.mkz.tessavar.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class SelectBattleUnitTest {
    private val queries = InMemoryQueries()
    private val selectionRepository = InMemorySelectionRepository()
    private val eventBus = FakeEventBus()
    private val selectBattleUnit =
        SelectBattleUnit(
            searchPosition = queries.searchPosition,
            searchBattle = queries.searchBattle,
            selectionRepository = selectionRepository,
            eventBus = eventBus,
            walkingRangeService = WalkingRangeService(queries.searchBattleUnitById, queries.searchVacantTiles, queries.canWalkTo),
        )
    private val battleUnitTile = tile(0, 1)
    private val walkingRange = setOf(tile(0, 0), tile(0, 2))

    init {
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(maximumSteps = 1).toDto()))
        queries.battlefieldRepository.create(battlefield(". K .", occupants = mapOf('K' to "battle-unit-1")))
    }

    private fun startBattle() = queries.battleRepository.create(battle(battleUnitIds = setOf("battle-unit-1")))

    @Test
    fun `should select the battle unit with its walking range when nothing is selected`() {
        // Given
        startBattle()
        selectionRepository.create(nothingSelected())
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange)
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.BattleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange),
        )
    }

    @Test
    fun `should select the battle unit with no walking range out of the battles`() {
        // Given
        selectionRepository.create(battleUnitSelected(battleUnitId = "battle-unit-2", walkingRange = emptySet()))
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = emptySet())
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.Deselected,
            SelectionEvent.BattleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = emptySet()),
        )
    }

    @Test
    fun `should replace the selection when another battle unit is selected`() {
        // Given
        startBattle()
        selectionRepository.create(battleUnitSelected(battleUnitId = "battle-unit-2"))
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange)
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.Deselected,
            SelectionEvent.BattleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange),
        )
    }

    @Test
    fun `should replace the selection when the selection is displaying an ability cast range`() {
        // Given
        startBattle()
        selectionRepository.create(abilitySelected())
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange)
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.Deselected,
            SelectionEvent.BattleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange),
        )
    }

    @Test
    fun `should replace the selection when the selection is displaying the ability info`() {
        // Given
        startBattle()
        selectionRepository.create(abilityInfoDisplayed())
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange)
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.Deselected,
            SelectionEvent.BattleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange),
        )
    }

    @Test
    fun `should replace the selection when the selection is previewing an ability cast`() {
        // Given
        startBattle()
        selectionRepository.create(castPreviewed())
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        selectionRepository.search() shouldBe battleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange)
        eventBus.shouldHavePublishedEvents(
            SelectionEvent.Deselected,
            SelectionEvent.BattleUnitSelected(tile = battleUnitTile, battleUnitId = "battle-unit-1", walkingRange = walkingRange),
        )
    }

    @Test
    fun `should only deselect when the battle unit is not on the battlefield`() {
        // Given
        selectionRepository.create(battleUnitSelected(battleUnitId = "battle-unit-1"))
        // When
        selectBattleUnit("battle-unit-2")
        // Then
        selectionRepository.search().shouldBeInstanceOf<Selection.NothingSelected>()
        eventBus.shouldHavePublishedEvents(SelectionEvent.Deselected)
    }

    @Test
    fun `should throw a selection not found error when no selection exists`() {
        // Given
        // When
        // Then
        shouldThrow<SelectionError.SelectionNotFound> { selectBattleUnit("battle-unit-1") }
    }
}
