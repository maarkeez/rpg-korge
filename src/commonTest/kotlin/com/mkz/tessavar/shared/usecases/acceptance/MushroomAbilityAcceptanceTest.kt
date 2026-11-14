package com.mkz.tessavar.shared.usecases.acceptance

import com.mkz.tessavar.ability.adapters.presentation.AbilityApi
import com.mkz.tessavar.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.tessavar.ability.domain.Ability
import com.mkz.tessavar.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.tessavar.ability.domain.AbilityMother
import com.mkz.tessavar.battle.adapters.presentation.BattleApi
import com.mkz.tessavar.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.tessavar.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.tessavar.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.tessavar.battleUnit.domain.BattleUnitMother
import com.mkz.tessavar.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.tessavar.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.tessavar.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.tessavar.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.tessavar.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.tessavar.effect.adapters.presentation.EffectApi
import com.mkz.tessavar.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.tessavar.effect.domain.EffectMother
import com.mkz.tessavar.party.adapters.presentation.PartyApi
import com.mkz.tessavar.player.adapters.presentation.PlayerApi
import com.mkz.tessavar.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.tessavar.player.domain.PlayerMother
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.tessavar.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.tessavar.shared.adapters.events.InMemoryEventBus
import com.mkz.tessavar.shared.domain.FixedDoublesRandom
import com.mkz.tessavar.terrain.adapters.presentation.TerrainApi
import com.mkz.tessavar.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.tessavar.unit.adapters.presentation.UnitApi
import com.mkz.tessavar.unit.adapters.resources.ResourcesUnitLoader
import com.mkz.tessavar.unit.domain.UnitMother
import io.kotest.matchers.collections.shouldContain
import kotlin.test.Test

class MushroomAbilityAcceptanceTest {
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

    private val humanPlayerId = PlayerMother.id()
    private val cpuPlayerId = PlayerMother.id()
    private val venomEffectId = EffectMother.effectId()
    private val mushroomAbilityId = AbilityMother.id()
    private val knightUnitId = UnitMother.id()
    private val ratUnitId = UnitMother.id()
    private val humanBattleUnitId = BattleUnitMother.id()
    private val firstRatBattleUnitId = BattleUnitMother.id()
    private val secondRatBattleUnitId = BattleUnitMother.id()

    private suspend fun setUp() {
        playerApi.requestPlayerCreation(humanPlayerId, HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, CPU)
        terrainApi.init()
        terrainApi.initialiseTerrains()
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })

        val venomEffect = EffectMother.damageEffect(id = venomEffectId)
        effectApi.requestEffectCreation(venomEffect.toDto())

        val mushroomAbility =
            AbilityMother.ability(
                id = mushroomAbilityId,
                manaCost = 10,
                cooldown = 0,
                abilityEffects = listOf(AbilityMother.abilityEffect(effectId = venomEffectId, target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT)),
                targeting = Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES,
            )
        abilityApi.requestAbilityCreation(mushroomAbility.toDto())

        val knight =
            UnitMother.unit(
                id = knightUnitId,
                baseMaximumHealth = 100,
                baseMaximumMana = 30,
                startingAbilities = listOf(mushroomAbilityId),
                maximumSteps = 3,
            )
        val rat =
            UnitMother.unit(
                id = ratUnitId,
                baseMaximumHealth = 20,
                baseMaximumMana = 10,
                startingAbilities = emptyList(),
                maximumSteps = 3,
            )
        unitApi.requestUnitCreation(knight.toDto())
        unitApi.requestUnitCreation(rat.toDto())

        battleUnitApi.deployBattleUnit(
            battleUnitId = firstRatBattleUnitId,
            unitId = ratUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 1,
            deployAtColumn = 0,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = secondRatBattleUnitId,
            unitId = ratUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 0,
            deployAtColumn = 1,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = humanBattleUnitId,
            unitId = knightUnitId,
            playerId = humanPlayerId,
            deployAtRow = 1,
            deployAtColumn = 1,
        )
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, humanPlayerId, cpuPlayerId)
        eventBus.dispatch()
    }

    @Test
    fun `should apply the venom effect to all adjacent enemy battle units when the mushroom ability is cast`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchOccupant(row = 1, column = 1) == humanBattleUnitId)
            require(battlefieldApi.searchOccupant(row = 1, column = 0) == firstRatBattleUnitId)
            require(battlefieldApi.searchOccupant(row = 0, column = 1) == secondRatBattleUnitId)
            require(battleApi.searchBattle()!!.currentPlayerId == humanPlayerId)
            val castGroup = battleUnitApi.whereCanCast(humanBattleUnitId, mushroomAbilityId).single()
            battleUnitApi.castAbility(
                battleUnitId = humanBattleUnitId,
                abilityId = mushroomAbilityId,
                castGroup = castGroup,
            )
            // When
            eventBus.dispatch()
            // Then
            val firstRat = battleUnitApi.searchBattleUnitById(firstRatBattleUnitId)!!
            firstRat.ongoingEffects.onPlayerTurnStarted.shouldContain(venomEffectId)
            val secondRat = battleUnitApi.searchBattleUnitById(secondRatBattleUnitId)!!
            secondRat.ongoingEffects.onPlayerTurnStarted.shouldContain(venomEffectId)
        }
}
