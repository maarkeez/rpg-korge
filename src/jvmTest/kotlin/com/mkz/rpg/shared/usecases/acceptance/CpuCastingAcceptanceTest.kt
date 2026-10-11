package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBrain.adapters.presentation.CpuBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// Uses the effects, abilities and units defined in the resources: a CPU rat (20 HP, sword and heal) next to a human knight.
class CpuCastingAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, battleUnitApi)
    private val cpuBrainApi = CpuBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val knightBattleUnitId = "human-knight"
    private val ratBattleUnitId = "cpu-rat"

    @BeforeEach
    fun setup() {
        runBlocking {
            terrainApi.init()
            effectApi.init()
            abilityApi.init()
            unitApi.init()
        }
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })
        battleUnitApi.deployBattleUnit(knightBattleUnitId, "knight", humanPlayerId, deployAtRow = 1, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit(ratBattleUnitId, "rat", cpuPlayerId, deployAtRow = 1, deployAtColumn = 2)
        battleApi.startFirstRound(listOf(humanPlayerId, cpuPlayerId))
        eventBus.dispatch()
    }

    @Test
    fun `should attack the adjacent knight when the cpu rat is healthy`() {
        // Given
        require(battleUnitApi.searchBattleUnitById(ratBattleUnitId)!!.remainingHealthPoints == 20)
        battleApi.finishPlayerTurn()
        // When
        eventBus.dispatch()
        // Then
        assertThat(battleUnitApi.searchBattleUnitById(knightBattleUnitId)!!.remainingHealthPoints).isEqualTo(90)
    }

    @Nested
    inner class BadlyHurtRat {
        // The knight's skull leaves the rat at 10 of 20 HP. Its damage is applied through several events,
        // so it is applied here, before the CPU turn starts.
        @BeforeEach
        fun hurtRat() {
            battleUnitApi.castAbility(
                battleUnitId = knightBattleUnitId,
                abilityId = "skull",
                castGroup = WhereCanCast.CastGroup(listOf(WhereCanCast.PositionDto(row = 1, column = 2))),
            )
            eventBus.dispatch()
        }

        @Test
        fun `should heal instead of attacking when the cpu rat is badly hurt`() {
            // Given
            require(battleUnitApi.searchBattleUnitById(ratBattleUnitId)!!.remainingHealthPoints == 10)
            battleApi.finishPlayerTurn()
            // When
            eventBus.dispatch()
            // Then
            assertThat(battleUnitApi.searchBattleUnitById(ratBattleUnitId)!!.remainingHealthPoints).isEqualTo(20)
            assertThat(battleUnitApi.searchBattleUnitById(knightBattleUnitId)!!.remainingHealthPoints).isEqualTo(100)
        }
    }
}
