package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class DeployBattleUnitTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val searchUnitById: SearchUnitById = mock()
    private val searchPlayerById: SearchPlayerById = mock()
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied = mock()
    private val deployBattleUnit =
        DeployBattleUnit(
            battleUnitRepository = battleUnitRepository,
            eventBus = eventBus,
            searchUnitById = searchUnitById,
            searchPlayerById = searchPlayerById,
            canBattlefieldTileBeOccupied = canBattlefieldTileBeOccupied,
        )

    @Test
    fun `should deploy battle unit when the battlefield tile can be occupied`() {
        // Given
        val unit = unit().toDto()
        val player = player().toDto()
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPlayerById(player.id)).thenReturn(player)
        whenever(canBattlefieldTileBeOccupied(0, 0)).thenReturn(true)
        // When
        deployBattleUnit(
            battleUnitId = "battle-unit-1",
            unitId = unit.id,
            playerId = player.id,
            deployAtRow = 0,
            deployAtColumn = 0,
        )
        // Then
        val storedBattleUnit = battleUnitRepository.searchById("battle-unit-1")?.toDto()
        storedBattleUnit.shouldNotBeNull()
        storedBattleUnit!!.playerId shouldBe player.id
        storedBattleUnit.unitId shouldBe unit.id
        storedBattleUnit.remainingHealthPoints shouldBe unit.healthPoints
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.BattleUnitDeployed("battle-unit-1", 0, 0),
        )
    }

    @Test
    fun `should throw unit not found when the unit does not exist`() {
        // Given
        whenever(searchUnitById("unknown-unit")).thenReturn(null)
        // When
        val error =
            shouldThrowAny {
                deployBattleUnit("battle-unit-1", "unknown-unit", "player-1", 0, 0)
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.UnitNotFound>()
    }

    @Test
    fun `should throw player not found when the player does not exist`() {
        // Given
        val unit =
            unit()
                .toDto()
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPlayerById("unknown-player")).thenReturn(null)
        // When
        val error =
            shouldThrowAny {
                deployBattleUnit("battle-unit-1", unit.id, "unknown-player", 0, 0)
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.PlayerNotFound>()
    }

    @Test
    fun `should throw tile error when the battlefield tile can not be occupied`() {
        // Given
        val unit =
            unit()
                .toDto()
        val player =
            player()
                .toDto()
        whenever(searchUnitById(unit.id)).thenReturn(unit)
        whenever(searchPlayerById(player.id)).thenReturn(player)
        whenever(canBattlefieldTileBeOccupied(0, 0)).thenReturn(false)
        // When
        val error =
            shouldThrowAny {
                deployBattleUnit("battle-unit-1", unit.id, player.id, 0, 0)
            }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.BattlefieldTileCanNotBeOccupied>()
    }
}
