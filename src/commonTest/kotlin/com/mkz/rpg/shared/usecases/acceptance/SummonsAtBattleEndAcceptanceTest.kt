package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.lootableItem.adapters.presentation.LootableItemApi
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.shared.domain.subscribe
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

// Uses the units defined in the resources: two human knights, which summon bees lasting 5 turns, and two CPU rats.
class SummonsAtBattleEndAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)

    // The CPU battle units out of the battle rest when a round finishes, rather than wandering.
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, eventBus)
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, NpcApi(battlefieldApi, unitApi, LootableItemApi(effectApi, eventBus), eventBus), eventBus)

    private val humanPlayerId = "human"
    private val cpuPlayerId = "cpu"
    private val winners = mutableListOf<String>().also { winners -> eventBus.subscribe<BattleEvent.PlayerVictory> { winners += it.playerId } }

    // The knights start in a line at (6,1) and (7,1). The leader walks to (5,1), 5 tiles away from the rat-1, at (0,1),
    // which engages the party and plays first. The rat-2, at (2,6), stays out of the battle.
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
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })
        battleUnitApi.deployBattleUnit("knight-1", "knight", humanPlayerId, deployAtRow = 6, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("knight-2", "knight", humanPlayerId, deployAtRow = 7, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("rat-1", "rat", cpuPlayerId, deployAtRow = 0, deployAtColumn = 1)
        battleUnitApi.deployBattleUnit("rat-2", "rat", cpuPlayerId, deployAtRow = 2, deployAtColumn = 6)
        partyApi.startExploration()
        eventBus.dispatch()
        partyApi.walkParty(DirectionDto.NORTH)
        eventBus.dispatch()
    }

    // The leader summons a bee next to it, in the human turn, and returns it.
    private fun summonBee(): String {
        val castGroup = battleUnitApi.whereCanCast("knight-1", "bee").first()
        battleUnitApi.castAbility(battleUnitId = "knight-1", abilityId = "bee", castGroup = castGroup)
        eventBus.dispatch()
        val tile = castGroup.positions.first()
        return battlefieldApi.searchOccupant(row = tile.row, column = tile.column).shouldNotBeNull()
    }

    // The battle unit receives a low physical damage, of 10, until it is defeated.
    private fun defeat(battleUnitId: String) {
        while (battleUnitApi.searchBattleUnitById(battleUnitId)!!.remainingHealthPoints > 0) {
            battleUnitApi.applyEffect(
                EffectApplicationDto(
                    source = ApplicationSourceDto.terrain("lava"),
                    target = EffectTargetDto.Unit(battleUnitId),
                    effectId = "low-physical-damage",
                ),
            )
        }
        eventBus.dispatch()
    }

    @Test
    fun `should vanish the summons left when the battle ends, before exploring again`() =
        scenarioTest(::setUp) {
            // Given
            val beeId = summonBee()
            val beeTile = battlefieldApi.searchPosition(beeId)!!
            defeat("rat-1")
            // When
            battleApi.finishPlayerTurn()
            eventBus.dispatch()
            // Then
            battleApi.searchBattle()!!.isFinished.shouldBeTrue()
            partyApi.searchParty()!!.isExploring.shouldBeTrue()
            battleUnitApi.searchBattleUnitById(beeId).shouldBeNull()
            battlefieldApi.searchOccupant(row = beeTile.row, column = beeTile.column).shouldBeNull()
            winners.shouldBeEmpty()
        }

    @Test
    fun `should lose the game when only summons are left when the battle ends`() =
        scenarioTest(::setUp) {
            // Given
            summonBee()
            defeat("knight-1")
            defeat("knight-2")
            defeat("rat-1")
            require(winners.isEmpty())
            // When
            battleApi.finishPlayerTurn()
            eventBus.dispatch()
            // Then
            winners.shouldContainExactly(cpuPlayerId)
        }
}
