package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.exploration.adapters.presentation.ExplorationApi
import com.mkz.rpg.exploration.domain.Exploration.Dto.DirectionDto
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Uses the units and terrains defined in the resources: two human knights with 100 health points, a CPU rat with 20, and
// the lava, which damages 5 health points.
class ExploringTerrainEffectAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus)
    private val explorationApi = ExplorationApi(playerApi, battleUnitApi, battlefieldApi, NpcApi(battlefieldApi, unitApi, eventBus), eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val victories = mutableListOf<String>().also { winners -> eventBus.subscribe<BattleEvent.PlayerVictory> { winners += it.playerId } }

    // The knight-1 leads from the lava at (6,1), and the knight-2 follows it from the sand at (7,1). The rat stands on the
    // lava at (0,6), 10 tiles away from the leader, so it does not engage.
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation(humanPlayerId, "Human", HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, "CPU", CPU)
        val lavaTiles = setOf(6 to 1, 0 to 6)
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { row -> List(8) { column -> if (row to column in lavaTiles) "lava" else "sand" } })
        battleUnitApi.deployBattleUnit("knight-1", "knight", humanPlayerId, deployAtRow = 6, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("knight-2", "knight", humanPlayerId, deployAtRow = 7, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("rat-1", "rat", cpuPlayerId, deployAtRow = 0, deployAtColumn = 6)
        explorationApi.startExploration()
        eventBus.dispatch()
    }

    // A second passes while exploring.
    private fun waitASecond() {
        explorationApi.applyStandingTerrainEffects()
        eventBus.dispatch()
    }

    private fun healthOf(battleUnitId: String) = battleUnitApi.searchBattleUnitById(battleUnitId)!!.remainingHealthPoints

    @Test
    fun `should hurt the battle units standing on lava, the cpu ones included, every second while exploring`() =
        scenarioTest(::setUp) {
            // When
            waitASecond()
            waitASecond()
            // Then
            healthOf("knight-1") shouldBe 90
            healthOf("knight-2") shouldBe 100
            healthOf("rat-1") shouldBe 10
        }

    @Test
    fun `should not hurt the battle units that walked during the second, until they stand on the lava for a second`() =
        scenarioTest(::setUp) {
            // Given
            // The leader walks off the lava to (5,1), and the knight-2 onto it.
            explorationApi.walkParty(DirectionDto.NORTH)
            eventBus.dispatch()
            // When
            waitASecond()
            // Then
            healthOf("knight-1") shouldBe 100
            healthOf("knight-2") shouldBe 100
            // When
            waitASecond()
            // Then
            healthOf("knight-1") shouldBe 100
            healthOf("knight-2") shouldBe 95
        }

    @Test
    fun `should have the human player win when the last cpu battle unit is defeated by the lava while exploring`() =
        scenarioTest(::setUp) {
            // When
            repeat(4) { waitASecond() }
            // Then
            battlefieldApi.searchPosition("rat-1").shouldBeNull()
            victories.shouldContainExactly(humanPlayerId)
            battleApi.searchBattle()!!.isFinished.shouldBeTrue()
        }
}
