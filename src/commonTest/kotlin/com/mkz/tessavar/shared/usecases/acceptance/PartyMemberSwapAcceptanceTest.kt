package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.party.domain.Party.Dto.DirectionDto
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class PartyMemberSwapAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val battleApi = BattleApi(eventBus)
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom.cpuBattleUnitsAlwaysResting())
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)

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
        partyApi.stepParty(direction)
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
            partyApi.searchParty()!!.isStepping.shouldBeFalse()
            partyApi.searchParty()!!.isExploring.shouldBeTrue()
        }

    @Test
    fun `should never leave the party stuck when it turns around at the end of a dead-end corridor`() =
        scenarioTest(::setUp) {
            // Given
            repeat(2) { walk(DirectionDto.EAST) }
            columns() shouldBe listOf(4, 3, 2)
            // When
            repeat(6) { walk(DirectionDto.WEST) }
            // Then
            columns() shouldBe listOf(0, 2, 1)
            partyApi.searchParty()!!.walkingOrderBattleUnitIds.shouldContainExactly("knight-1", "knight-3", "knight-2")
            partyApi.searchParty()!!.battleUnitIds.shouldContainExactly("knight-1", "knight-2", "knight-3")
        }
}
