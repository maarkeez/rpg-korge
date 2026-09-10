package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
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
        assertThat(storedBattleUnit).isNotNull
        assertThat(storedBattleUnit!!.playerId).isEqualTo(player.id)
        assertThat(storedBattleUnit.unitId).isEqualTo(unit.id)
        assertThat(storedBattleUnit.remainingHealthPoints).isEqualTo(unit.healthPoints)
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitEvent.BattleUnitDeployed("battle-unit-1", 0, 0),
        )
    }

    @Test
    fun `should throw unit not found when the unit does not exist`() {
        // Given
        whenever(searchUnitById("unknown-unit")).thenReturn(null)
        // When
        val error =
            catchThrowable {
                deployBattleUnit("battle-unit-1", "unknown-unit", "player-1", 0, 0)
            }
        // Then
        assertThat(error).isInstanceOf(BattleUnitError.UnitNotFound::class.java)
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
            catchThrowable {
                deployBattleUnit("battle-unit-1", unit.id, "unknown-player", 0, 0)
            }
        // Then
        assertThat(error).isInstanceOf(BattleUnitError.PlayerNotFound::class.java)
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
            catchThrowable {
                deployBattleUnit("battle-unit-1", unit.id, player.id, 0, 0)
            }
        // Then
        assertThat(error)
            .isInstanceOf(BattleUnitError.BattlefieldTileCanNotBeOccupied::class.java)
    }
}
