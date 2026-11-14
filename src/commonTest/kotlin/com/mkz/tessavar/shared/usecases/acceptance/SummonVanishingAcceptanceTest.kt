package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.tessavar.ability.domain.Ability.Dto.TargetingDto
import com.mkz.tessavar.ability.domain.AbilityMother
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.adapters.presentation.startBattleWithEveryBattleUnit
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
import com.mkz.tessavar.effect.domain.EffectMother
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import com.mkz.tessavar.unit.domain.UnitMother
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SummonVanishingAcceptanceTest {
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

    private val summonerPlayerId = "summoner"
    private val opponentId = "enemy"
    private val summonerId = "summoner-1"
    private val enemyId = "enemy-1"

    private suspend fun setUp() {
        playerApi.requestPlayerCreation(summonerPlayerId, HUMAN)
        playerApi.requestPlayerCreation(opponentId, HUMAN)
        terrainApi.init()
        terrainApi.initialiseTerrains()
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })

        effectApi.requestEffectCreation(EffectMother.summonEffect(id = "deploy-summon", unitId = "summon").toDto())
        effectApi.requestEffectCreation(EffectMother.damageEffect(id = "deadly-damage", damage = 100, timingType = "IMMEDIATELY").toDto())
        abilityApi.requestAbilityCreation(
            AbilityMother
                .ability(
                    id = "summon",
                    manaCost = 0,
                    cooldown = 0,
                    abilityEffects = listOf(AbilityMother.abilityEffect(effectId = "deploy-summon", target = EffectTargetDto.Type.TARGETED_TILE)),
                    targeting = TargetingDto.VACANT_TILE_ADJACENT_TO_CASTER,
                    summonDuration = 2,
                ).toDto(),
        )
        abilityApi.requestAbilityCreation(
            AbilityMother
                .ability(
                    id = "deadly-strike",
                    manaCost = 0,
                    cooldown = 0,
                    abilityEffects = listOf(AbilityMother.abilityEffect(effectId = "deadly-damage", target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT)),
                    targeting = TargetingDto.ADJACENT_ENEMY,
                ).toDto(),
        )
        unitApi.requestUnitCreation(UnitMother.unit(id = "summoner", baseMaximumHealth = 20, baseMaximumMana = 10, startingAbilities = listOf("summon"), maximumSteps = 3).toDto())
        unitApi.requestUnitCreation(UnitMother.unit(id = "summon", baseMaximumHealth = 20, baseMaximumMana = 10, startingAbilities = emptyList(), maximumSteps = 3).toDto())
        unitApi.requestUnitCreation(UnitMother.unit(id = "enemy", baseMaximumHealth = 20, baseMaximumMana = 10, startingAbilities = listOf("deadly-strike"), maximumSteps = 3).toDto())

        battleUnitApi.deployBattleUnit(battleUnitId = summonerId, unitId = "summoner", playerId = summonerPlayerId, deployAtRow = 2, deployAtColumn = 2)
        battleUnitApi.deployBattleUnit(battleUnitId = enemyId, unitId = "enemy", playerId = opponentId, deployAtRow = 2, deployAtColumn = 3)
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, summonerPlayerId, opponentId)
        eventBus.dispatch()
    }

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
    fun `should summon a battle unit lasting the summon duration of the ability`() =
        scenarioTest(::setUp) {
            // When
            val tile = summon()
            // Then
            battleUnitApi.searchBattleUnitById(summonedBattleUnitId(tile))!!.remainingSummonDuration shouldBe 2
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
            battleUnitApi.searchBattleUnitById(summonedBattleUnitId)!!.remainingSummonDuration shouldBe 1
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
            battle.currentPlayerId shouldBe opponentId
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
            require(battleUnitApi.searchBattleUnitById(summonerId)!!.remainingHealth == 0)
            finishPlayerTurn()
            require(!battleApi.searchBattle()!!.isFinished)
            // When
            finishPlayerTurn()
            // Then
            battleApi.searchBattle()!!.isFinished shouldBe true
        }
}
