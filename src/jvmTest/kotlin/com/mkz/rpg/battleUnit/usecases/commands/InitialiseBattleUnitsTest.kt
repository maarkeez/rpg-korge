package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.battleUnit.adapters.storage.InMemoryBattleUnitRepository
import com.mkz.rpg.battleUnit.domain.BattleUnitError.UnitNotFound
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.BattleUnitDeployed
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader
import com.mkz.rpg.battleUnit.usecases.services.BattleUnitLoader.Deployment
import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.player.usecases.queries.SearchPlayerById
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.assertThat
import com.mkz.rpg.unit.domain.UnitMother.unit
import com.mkz.rpg.unit.usecases.queries.SearchUnitById
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InitialiseBattleUnitsTest {
    private val battleUnitRepository = InMemoryBattleUnitRepository()
    private val eventBus = FakeEventBus()
    private val battleUnitLoader: BattleUnitLoader = mock()
    private val searchUnitById: SearchUnitById = mock()
    private val searchPlayerById: SearchPlayerById = mock()
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied = mock()
    private val initialiseBattleUnits =
        InitialiseBattleUnits(battleUnitLoader, battleUnitRepository, searchUnitById, searchPlayerById, canBattlefieldTileBeOccupied, eventBus)

    @Test
    fun `should deploy every loaded battle unit when battle units are initialised`() {
        // Given
        whenever(battleUnitLoader.loadDeployments()).thenReturn(
            listOf(
                Deployment(battleUnitId = "player-1-unit-1", unitId = "knight", playerId = "player-one", row = 6, column = 6),
                Deployment(battleUnitId = "player-1-unit-2", unitId = "knight", playerId = "player-one", row = 7, column = 7),
            ),
        )
        whenever(searchUnitById("knight")).thenReturn(unit(id = "knight").toDto())
        whenever(searchPlayerById("player-one")).thenReturn(player(id = "player-one").toDto())
        whenever(canBattlefieldTileBeOccupied(any(), any())).thenReturn(true)
        // When
        initialiseBattleUnits()
        // Then
        assertThat(battleUnitRepository.searchById("player-1-unit-1")?.toDto()?.playerId).isEqualTo("player-one")
        assertThat(battleUnitRepository.searchById("player-1-unit-2")?.toDto()?.playerId).isEqualTo("player-one")
        assertThat(eventBus).hasPublishedEvents(
            BattleUnitDeployed(battleUnitId = "player-1-unit-1", row = 6, column = 6),
            BattleUnitDeployed(battleUnitId = "player-1-unit-2", row = 7, column = 7),
        )
    }

    @Test
    fun `should fail when the unit of a battle unit does not exist`() {
        // Given
        whenever(battleUnitLoader.loadDeployments()).thenReturn(
            listOf(Deployment(battleUnitId = "player-1-unit-1", unitId = "dragon", playerId = "player-one", row = 6, column = 6)),
        )
        // When
        val initialisation = { initialiseBattleUnits() }
        // Then
        assertThatThrownBy { initialisation() }.isInstanceOf(UnitNotFound::class.java)
    }
}
