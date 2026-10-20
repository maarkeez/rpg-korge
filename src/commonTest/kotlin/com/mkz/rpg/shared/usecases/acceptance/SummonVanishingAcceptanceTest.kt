package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto
import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.domain.UnitMother
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

/** Both players are human, so the test plays every turn. */
class SummonVanishingAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus)
    private val unitApi = UnitApi(eventBus)
    private val playerApi = PlayerApi(eventBus)
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus)
    private val effectApi = EffectApi(eventBus)
    private val abilityApi = AbilityApi(effectApi, eventBus)
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus)
    private val battleApi = BattleApi(eventBus, playerApi, battleUnitApi)
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, eventBus)

    private val summonerPlayerId = "summoner"
    private val enemyPlayerId = "enemy"
    private val summonerId = "summoner-1"
    private val enemyId = "enemy-1"

    // The summoner summons a battle unit lasting 2 turns, next to it. The enemy stands next to the summoner, and can
    // defeat it at once.
    private suspend fun setUp() {
        playerApi.requestPlayerCreation(summonerPlayerId, "Summoner", HUMAN)
        playerApi.requestPlayerCreation(enemyPlayerId, "Enemy", HUMAN)
        terrainApi.init()
        terrainApi.initialiseTerrains()
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })

        effectApi.requestEffectCreation(EffectMother.deployBattleUnitEffect(id = "deploy-summon", unitId = "summon").toDto())
        effectApi.requestEffectCreation(EffectMother.decreaseHealthEffect(id = "deadly-damage", damage = 100, applicationType = "IMMEDIATELY").toDto())
        abilityApi.requestAbilityCreation(
            AbilityMother
                .ability(
                    id = "summon",
                    cost = 0,
                    cooldown = 0,
                    effectSpecs = listOf(AbilityMother.effectSpec(effectId = "deploy-summon", target = TargetExpressionDto.Type.SELECTED_TILE)),
                    targeting = TargetingDto.VACANT_TILE_ADJACENT_TO_SELF,
                    turnsDuration = 2,
                ).toDto(),
        )
        abilityApi.requestAbilityCreation(
            AbilityMother
                .ability(
                    id = "deadly-strike",
                    cost = 0,
                    cooldown = 0,
                    effectSpecs = listOf(AbilityMother.effectSpec(effectId = "deadly-damage", target = TargetExpressionDto.Type.SELECTED_TARGET)),
                    targeting = TargetingDto.ADJACENT_ENEMY,
                ).toDto(),
        )
        unitApi.requestUnitCreation(UnitMother.unit(id = "summoner", healthPoints = 20, manaPoints = 10, abilities = listOf("summon"), movementRange = 3).toDto())
        unitApi.requestUnitCreation(UnitMother.unit(id = "summon", healthPoints = 20, manaPoints = 10, abilities = emptyList(), movementRange = 3).toDto())
        unitApi.requestUnitCreation(UnitMother.unit(id = "enemy", healthPoints = 20, manaPoints = 10, abilities = listOf("deadly-strike"), movementRange = 3).toDto())

        battleUnitApi.deployBattleUnit(battleUnitId = summonerId, unitId = "summoner", playerId = summonerPlayerId, deployAtRow = 2, deployAtColumn = 2)
        battleUnitApi.deployBattleUnit(battleUnitId = enemyId, unitId = "enemy", playerId = enemyPlayerId, deployAtRow = 2, deployAtColumn = 3)
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, summonerPlayerId, enemyPlayerId)
        eventBus.dispatch()
    }

    // Summons a battle unit next to the summoner, in the summoner player turn, and returns the tile it stands on.
    private fun summon(): PositionDto {
        val castGroup = battleUnitApi.whereCanCast(summonerId, "summon").first()
        battleUnitApi.castAbility(battleUnitId = summonerId, abilityId = "summon", castGroup = castGroup)
        eventBus.dispatch()
        val tile = castGroup.positions.first()
        return PositionDto(row = tile.row, column = tile.column)
    }

    private fun summonedBattleUnitId(tile: PositionDto): String = battlefieldApi.searchOccupant(row = tile.row, column = tile.column).shouldNotBeNull()

    private fun finishPlayerTurn() {
        battleApi.finishPlayerTurn()
        eventBus.dispatch()
    }

    @Test
    fun `should summon a battle unit lasting the turns duration of the ability`() =
        scenarioTest(::setUp) {
            // When
            val tile = summon()
            // Then
            battleUnitApi.searchBattleUnitById(summonedBattleUnitId(tile))!!.remainingTurns shouldBe 2
        }

    @Test
    fun `should count the turns of its player, the one it is summoned in included`() =
        scenarioTest(::setUp) {
            // Given
            val summonedBattleUnitId = summonedBattleUnitId(summon())
            // When
            finishPlayerTurn()
            finishPlayerTurn()
            // Then
            battleApi.searchBattle()!!.currentRound shouldBe 2
            battleUnitApi.searchBattleUnitById(summonedBattleUnitId)!!.remainingTurns shouldBe 1
        }

    @Test
    fun `should vanish once its player finishes its last turn`() =
        scenarioTest(::setUp) {
            // Given
            val tile = summon()
            val summonedBattleUnitId = summonedBattleUnitId(tile)
            finishPlayerTurn()
            finishPlayerTurn()
            // When
            finishPlayerTurn()
            // Then
            battleUnitApi.searchBattleUnitById(summonedBattleUnitId).shouldBeNull()
            battlefieldApi.searchOccupant(row = tile.row, column = tile.column).shouldBeNull()
            val battle = battleApi.searchBattle()!!
            battle.battleUnitIds shouldNotContain summonedBattleUnitId
            battle.isFinished shouldBe false
            battle.currentPlayerTurn shouldBe enemyPlayerId
        }

    @Test
    fun `should defeat its player when it vanishes as its last battle unit`() =
        scenarioTest(::setUp) {
            // Given
            summon()
            finishPlayerTurn()
            val summonerTile = battlefieldApi.searchPosition(summonerId)!!
            val strike =
                battleUnitApi.whereCanCast(enemyId, "deadly-strike").first { castGroup ->
                    castGroup.positions.any { tile -> tile.row == summonerTile.row && tile.column == summonerTile.column }
                }
            battleUnitApi.castAbility(battleUnitId = enemyId, abilityId = "deadly-strike", castGroup = strike)
            eventBus.dispatch()
            require(battleUnitApi.searchBattleUnitById(summonerId)!!.remainingHealthPoints == 0)
            finishPlayerTurn()
            require(!battleApi.searchBattle()!!.isFinished)
            // When
            finishPlayerTurn()
            // Then
            battleApi.searchBattle()!!.isFinished shouldBe true
        }
}
