package com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.adapters.storage.InMemoryBattlefieldHudRepository
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHud
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudError
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastPreview
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayAbilityCastRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.displayMovementRange
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.idle
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudMother.tile
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.usecases.services.MovementService
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class RefreshMovementRangeTest {
    private val queries = InMemoryQueries()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val refreshMovementRange =
        RefreshMovementRange(
            searchPosition = queries.searchPosition,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = MovementService(queries.searchBattleUnitById, queries.searchTilesThatCanBeOccupied, queries.canMoveTo),
        )

    @Test
    fun `should display the current tile and movement range of the selected battle unit when the hud is displaying its movement range`() {
        // Given
        // The battle unit has moved from where the hud displays it.
        battlefieldHudRepository.create(displayMovementRange(tile = tile(0, 0), battleUnitId = "battle-unit-1"))
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(movementRange = 1).toDto()))
        queries.battlefieldRepository.create(battlefield(". K .", occupants = mapOf('K' to "battle-unit-1")))
        val newTiles = setOf(tile(0, 0), tile(0, 2))
        // When
        refreshMovementRange()
        // Then
        battlefieldHudRepository.search() shouldBe displayMovementRange(tile = tile(0, 1), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = newTiles)
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnit(tile = tile(0, 1), battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = newTiles),
        )
    }

    @Test
    fun `should go idle when the selected battle unit is no longer on the battlefield`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(battleUnitId = "battle-unit-1"))
        queries.battlefieldRepository.create(battlefield(". ."))
        // When
        refreshMovementRange()
        // Then
        battlefieldHudRepository.search().shouldBeInstanceOf<BattlefieldHud.Idle>()
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should not refresh when the hud is idle`() {
        // Given
        val hud = idle()
        battlefieldHudRepository.create(hud)
        // When
        refreshMovementRange()
        // Then
        battlefieldHudRepository.search() shouldBe hud
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not refresh when the hud is displaying an ability cast range`() {
        // Given
        val hud = displayAbilityCastRange()
        battlefieldHudRepository.create(hud)
        // When
        refreshMovementRange()
        // Then
        battlefieldHudRepository.search() shouldBe hud
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should not refresh when the hud is previewing an ability cast`() {
        // Given
        val hud = displayAbilityCastPreview()
        battlefieldHudRepository.create(hud)
        // When
        refreshMovementRange()
        // Then
        battlefieldHudRepository.search() shouldBe hud
        eventBus.publishedEvents.shouldBeEmpty()
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        shouldThrow<BattlefieldHudError.BattlefieldHudNotFound> { refreshMovementRange() }
    }
}
