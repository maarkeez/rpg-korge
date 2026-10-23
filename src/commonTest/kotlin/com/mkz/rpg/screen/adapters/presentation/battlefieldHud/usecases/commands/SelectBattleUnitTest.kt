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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class SelectBattleUnitTest {
    // The battle unit-1 stands at (0,1) and can walk 1 tile: (0,0) and (0,2).
    private val queries = InMemoryQueries()
    private val battlefieldHudRepository = InMemoryBattlefieldHudRepository()
    private val eventBus = FakeEventBus()
    private val selectBattleUnit =
        SelectBattleUnit(
            searchPosition = queries.searchPosition,
            battlefieldHudRepository = battlefieldHudRepository,
            eventBus = eventBus,
            movementService = MovementService(queries.searchBattleUnitById, queries.searchTilesThatCanBeOccupied, queries.canMoveTo),
        )
    private val battleUnitTile = tile(0, 1)
    private val tilesWhereCanBeMoved = setOf(tile(0, 0), tile(0, 2))

    init {
        queries.battleUnitRepository.create(battleUnit(id = "battle-unit-1", unit = unit(movementRange = 1).toDto()))
        queries.battlefieldRepository.create(battlefield(". K .", occupants = mapOf('K' to "battle-unit-1")))
    }

    @Test
    fun `should select the battle unit with its movement range when the hud is idle`() {
        // Given
        battlefieldHudRepository.create(idle())
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        battlefieldHudRepository.search() shouldBe displayMovementRange(tile = battleUnitTile, battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved)
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.SelectedBattleUnit(tile = battleUnitTile, battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved),
        )
    }

    @Test
    fun `should replace the selection when the hud is displaying the movement range of another battle unit`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(battleUnitId = "battle-unit-2"))
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        battlefieldHudRepository.search() shouldBe displayMovementRange(tile = battleUnitTile, battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved)
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.Idle,
            BattlefieldHudEvent.SelectedBattleUnit(tile = battleUnitTile, battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved),
        )
    }

    @Test
    fun `should replace the selection when the hud is displaying an ability cast range`() {
        // Given
        battlefieldHudRepository.create(displayAbilityCastRange())
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        battlefieldHudRepository.search() shouldBe displayMovementRange(tile = battleUnitTile, battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved)
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.Idle,
            BattlefieldHudEvent.SelectedBattleUnit(tile = battleUnitTile, battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved),
        )
    }

    @Test
    fun `should replace the selection when the hud is previewing an ability cast`() {
        // Given
        battlefieldHudRepository.create(displayAbilityCastPreview())
        // When
        selectBattleUnit("battle-unit-1")
        // Then
        battlefieldHudRepository.search() shouldBe displayMovementRange(tile = battleUnitTile, battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved)
        eventBus.shouldHavePublishedEvents(
            BattlefieldHudEvent.Idle,
            BattlefieldHudEvent.SelectedBattleUnit(tile = battleUnitTile, battleUnitId = "battle-unit-1", tilesWhereCanBeMoved = tilesWhereCanBeMoved),
        )
    }

    @Test
    fun `should only go idle when the battle unit is not on the battlefield`() {
        // Given
        battlefieldHudRepository.create(displayMovementRange(battleUnitId = "battle-unit-1"))
        // When
        selectBattleUnit("battle-unit-2")
        // Then
        battlefieldHudRepository.search().shouldBeInstanceOf<BattlefieldHud.Idle>()
        eventBus.shouldHavePublishedEvents(BattlefieldHudEvent.Idle)
    }

    @Test
    fun `should throw a battlefield hud not found error when no battlefield hud exists`() {
        // Given
        // When
        // Then
        shouldThrow<BattlefieldHudError.BattlefieldHudNotFound> { selectBattleUnit("battle-unit-1") }
    }
}
