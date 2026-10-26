package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Uses the units defined in the resources: two human knights exploring, which walk 3 tiles in a battle, and a CPU rat.
class ExplorationAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi)

    // The CPU battle units out of the battle rest when a round finishes, rather than wandering.
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"

    // The knights start in a line at (6,1) and (7,1), and the rat at (0,1), 6 tiles away from the leader.
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation(humanPlayerId, HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, CPU)
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })
        battleUnitApi.deployBattleUnit("knight-1", "knight", humanPlayerId, deployAtRow = 6, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("knight-2", "knight", humanPlayerId, deployAtRow = 7, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("rat-1", "rat", cpuPlayerId, deployAtRow = 0, deployAtColumn = 1)
        partyApi.startExploration()
        eventBus.dispatch()
    }

    private fun walk(direction: DirectionDto) {
        partyApi.walkParty(direction)
        eventBus.dispatch()
    }

    @Test
    fun `should explore without a battle while no cpu battle unit has the party within reach`() =
        scenarioTest(::setUp) {
            // Then
            partyApi.searchParty()!!.isExploring.shouldBeTrue()
            battleApi.searchBattle().shouldBeNull()
        }

    @Test
    fun `should walk the leader and have the other knight follow it without spending steps when the party walks`() =
        scenarioTest(::setUp) {
            // When
            walk(DirectionDto.EAST)
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 6, column = 2)
            battlefieldApi.searchPosition("knight-2") shouldBe PositionDto(row = 6, column = 1)
            battleUnitApi.searchBattleUnitById("knight-1")!!.remainingTurnActions.remainingSteps shouldBe 3
            battleUnitApi.searchBattleUnitById("knight-2")!!.remainingTurnActions.remainingSteps shouldBe 3
        }

    @Test
    fun `should keep the party in a line when it walks several tiles`() =
        scenarioTest(::setUp) {
            // When
            repeat(4) { walk(DirectionDto.EAST) }
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 6, column = 5)
            battlefieldApi.searchPosition("knight-2") shouldBe PositionDto(row = 6, column = 4)
            partyApi.searchParty()!!.isExploring.shouldBeTrue()
        }

    @Test
    fun `should start the battle and stop exploring when the party walks within reach of the cpu rat`() =
        scenarioTest(::setUp) {
            // When
            walk(DirectionDto.NORTH)
            // Then
            battleApi.searchBattle().shouldNotBeNull().currentPlayerTurn shouldBe humanPlayerId
            partyApi.searchParty()!!.isExploring.shouldBeFalse()
            walk(DirectionDto.NORTH)
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 5, column = 1)
        }
}
