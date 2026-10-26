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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

// Uses the units defined in the resources: three human knights exploring a corridor, one tile high, whose ends are dead ends.
class PartyMemberSwapAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi)
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)

    // The corridor "M L K . .", K leading, then L and M.
    private suspend fun setUp() {
        terrainApi.init()
        effectApi.init()
        abilityApi.init()
        unitApi.init()
        terrainApi.initialiseTerrains()
        effectApi.initialiseEffects()
        abilityApi.initialiseAbilities()
        unitApi.initialiseUnits()
        playerApi.requestPlayerCreation("human", HUMAN)
        playerApi.requestPlayerCreation("cpu", CPU)
        battlefieldApi.requestBattlefieldCreation(1, 5, listOf(List(5) { "sand" }))
        battleUnitApi.deployBattleUnit("knight-1", "knight", "human", deployAtRow = 0, deployAtColumn = 2)
        battleUnitApi.deployBattleUnit("knight-2", "knight", "human", deployAtRow = 0, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("knight-3", "knight", "human", deployAtRow = 0, deployAtColumn = 0)
        partyApi.startExploration()
        eventBus.dispatch()
    }

    private fun walk(direction: DirectionDto) {
        partyApi.walkParty(direction)
        eventBus.dispatch()
    }

    private fun columns() = listOf("knight-1", "knight-2", "knight-3").map { battleUnitId -> battlefieldApi.searchPosition(battleUnitId)!!.column }

    @Test
    fun `should swap the leader with the knight behind it when the leader walks back`() =
        scenarioTest(::setUp) {
            // When
            walk(DirectionDto.WEST)
            // Then
            battlefieldApi.searchPosition("knight-1") shouldBe PositionDto(row = 0, column = 1)
            battlefieldApi.searchPosition("knight-2") shouldBe PositionDto(row = 0, column = 2)
            battlefieldApi.searchPosition("knight-3") shouldBe PositionDto(row = 0, column = 0)
            partyApi.searchParty()!!.isWalking.shouldBeFalse()
            partyApi.searchParty()!!.isExploring.shouldBeTrue()
        }

    @Test
    fun `should never leave the party stuck when it turns around at the end of a dead-end corridor`() =
        scenarioTest(::setUp) {
            // Given the leader at the east end: ". . M L K"
            repeat(2) { walk(DirectionDto.EAST) }
            columns() shouldBe listOf(4, 3, 2)
            // When it turns around and walks back to the west end
            repeat(6) { walk(DirectionDto.WEST) }
            // Then the leader leads the way back, the others following it in the line rebuilt by the swaps: "K M L . ."
            columns() shouldBe listOf(0, 2, 1)
            partyApi.searchParty()!!.lineBattleUnitIds.shouldContainExactly("knight-1", "knight-3", "knight-2")
            partyApi.searchParty()!!.battleUnitIds.shouldContainExactly("knight-1", "knight-2", "knight-3")
        }
}
