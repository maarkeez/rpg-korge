package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.RecipientDto
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.PartyEvent
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

class GameOverAcceptanceTest {
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

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val gameOvers = mutableListOf<String>().also { gameOvers -> eventBus.subscribe<PartyEvent.GameOver> { gameOvers += it.playerId } }
    private val resumedExplorations = mutableListOf<PartyEvent.ExplorationResumed>().also { resumed -> eventBus.subscribe<PartyEvent.ExplorationResumed> { resumed += it } }

    private suspend fun setUpExploring() {
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
        battleUnitApi.deployBattleUnit("rat-2", "rat", cpuPlayerId, deployAtRow = 2, deployAtColumn = 6)
        partyApi.startExploration()
        eventBus.dispatch()
    }

    private suspend fun setUpBattle() {
        setUpExploring()
        partyApi.stepParty(DirectionDto.NORTH)
        eventBus.dispatch()
    }

    private fun summonBee(): String {
        val castGroup = battleUnitApi.whereCanCast("knight-1", "bee").first()
        battleUnitApi.castAbility(battleUnitId = "knight-1", abilityId = "bee", castGroup = castGroup)
        eventBus.dispatch()
        val tile = castGroup.positions.first()
        return battlefieldApi.searchOccupant(row = tile.row, column = tile.column).shouldNotBeNull()
    }

    private fun defeat(battleUnitId: String) {
        while (battleUnitApi.searchBattleUnitById(battleUnitId)!!.remainingHealth > 0) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = SourceDto.terrain("lava"),
                    recipient = RecipientDto.battleUnit(battleUnitId),
                    effectId = "low-physical-damage",
                    destination = null,
                ),
            )
        }
        eventBus.dispatch()
    }

    @Test
    fun `should be game over, with the battle finished and no exploring again, when the last party member is defeated in a battle`() =
        scenarioTest(::setUpBattle) {
            // Given
            defeat("knight-1")
            // When
            defeat("knight-2")
            // Then
            gameOvers.shouldContainExactly(humanPlayerId)
            battleApi.searchBattle()!!.isFinished.shouldBeTrue()
            partyApi.searchParty()!!.isOver.shouldBeTrue()
            partyApi.searchParty()!!.isExploring.shouldBeFalse()
            resumedExplorations.shouldBeEmpty()
        }

    @Test
    fun `should be game over at once, with the summon left vanished, when the last party member is defeated in a battle`() =
        scenarioTest(::setUpBattle) {
            // Given
            val beeId = summonBee()
            defeat("knight-1")
            // When
            defeat("knight-2")
            // Then
            gameOvers.shouldContainExactly(humanPlayerId)
            battleApi.searchBattle()!!.isFinished.shouldBeTrue()
            battleUnitApi.searchBattleUnitById(beeId).shouldBeNull()
        }

    @Test
    fun `should be game over when the last party member is defeated by the lava while exploring`() =
        scenarioTest(::setUpExploring) {
            // Given
            defeat("knight-1")
            // When
            defeat("knight-2")
            // Then
            gameOvers.shouldContainExactly(humanPlayerId)
            partyApi.searchParty()!!.isOver.shouldBeTrue()
            partyApi.searchParty()!!.isExploring.shouldBeFalse()
        }

    @Test
    fun `should not be game over when a party member is defeated and another is left`() =
        scenarioTest(::setUpBattle) {
            // When
            defeat("knight-1")
            // Then
            gameOvers.shouldBeEmpty()
            partyApi.searchParty()!!.isOver.shouldBeFalse()
            battleApi.searchBattle()!!.isFinished.shouldBeFalse()
        }
}
