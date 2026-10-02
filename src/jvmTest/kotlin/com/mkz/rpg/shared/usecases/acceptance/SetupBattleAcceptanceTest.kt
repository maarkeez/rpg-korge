package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlesetup.adapters.presentation.BattleSetupApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SetupBattleAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val battleSetupApi = BattleSetupApi(eventBus)

    private val playerOneId = "player-one"
    private val playerOneKnightBattleUnitId = "player-1-unit-1"

    @BeforeEach
    fun setup() {
        runBlocking { terrainApi.init() }
        battleSetupApi.setupBattle()
        eventBus.dispatch()
    }

    @Test
    fun `should occupy adjacent tile when battle unit was moved`() {
        // Given
        val currentPosition = battlefieldApi.searchPosition(playerOneKnightBattleUnitId)!!
        require(currentPosition.row == 6 && currentPosition.column == 6)
        require(battleApi.searchBattle()!!.currentPlayerTurn == playerOneId)
        battleUnitApi.moveBattleUnit(
            battleUnitId = playerOneKnightBattleUnitId,
            moveToRow = 5,
            moveToColumn = 6,
        )
        // When
        eventBus.dispatch()
        // Then
        val previousTileOccupantId = battlefieldApi.searchOccupant(row = 6, column = 6)
        assertThat(previousTileOccupantId).isNull()
        val newTileOccupantId = battlefieldApi.searchOccupant(row = 5, column = 6)
        assertThat(newTileOccupantId).isEqualTo(playerOneKnightBattleUnitId)
    }
}
