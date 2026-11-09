package com.mkz.rpg.shared.usecases.acceptance

import com.mkz.rpg.ability.adapters.presentation.AbilityApi
import com.mkz.rpg.ability.adapters.resources.ResourcesAbilityLoader
import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectTargetDto
import com.mkz.rpg.ability.domain.AbilityMother
import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battle.adapters.presentation.startBattleWithEveryBattleUnit
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battleUnit.adapters.resources.ResourcesBattleUnitLoader
import com.mkz.rpg.battleUnit.domain.BattleUnitMother
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.battlefield.adapters.resources.ResourcesBattlefieldLoader
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldDecoder
import com.mkz.rpg.battlefield.adapters.resources.TomlBattlefieldEncoder
import com.mkz.rpg.cpuBattleBrain.adapters.presentation.CpuBattleBrainApi
import com.mkz.rpg.cpuWanderBrain.adapters.presentation.CpuWanderBrainApi
import com.mkz.rpg.effect.adapters.presentation.EffectApi
import com.mkz.rpg.effect.adapters.resources.ResourcesEffectLoader
import com.mkz.rpg.effect.domain.EffectMother
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.player.adapters.resources.ResourcesPlayerLoader
import com.mkz.rpg.player.domain.PlayerMother
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.CPU
import com.mkz.rpg.player.usecases.commands.RequestPlayerCreation.PlayerType.HUMAN
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.FixedDoublesRandom
import com.mkz.rpg.terrain.adapters.presentation.TerrainApi
import com.mkz.rpg.terrain.adapters.resources.ResourcesTerrainLoader
import com.mkz.rpg.unit.adapters.presentation.UnitApi
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import com.mkz.rpg.unit.domain.UnitMother
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SkullAbilityAcceptanceTest {
    private val eventBus = InMemoryEventBus()
    private val terrainApi = TerrainApi(eventBus, ResourcesTerrainLoader())
    private val unitApi = UnitApi(eventBus, ResourcesUnitLoader())
    private val playerApi = PlayerApi(eventBus, ResourcesPlayerLoader())
    private val battlefieldApi = BattlefieldApi(terrainApi, eventBus, ResourcesBattlefieldLoader(), TomlBattlefieldEncoder(), TomlBattlefieldDecoder())
    private val effectApi = EffectApi(eventBus, ResourcesEffectLoader())
    private val abilityApi = AbilityApi(effectApi, eventBus, ResourcesAbilityLoader())
    private val battleUnitApi = BattleUnitApi(effectApi, abilityApi, unitApi, playerApi, battlefieldApi, eventBus, ResourcesBattleUnitLoader())
    private val battleApi = BattleApi(eventBus, playerApi)

    // The CPU battle units out of the battle rest when a round finishes, rather than wandering.
    private val cpuBattleBrainApi = CpuBattleBrainApi(unitApi, playerApi, battleUnitApi, battlefieldApi, terrainApi, effectApi, abilityApi, eventBus, FixedDoublesRandom(0.0))
    private val partyApi = PartyApi(playerApi, battleUnitApi, battlefieldApi, eventBus)
    private val cpuWanderBrainApi = CpuWanderBrainApi(playerApi, battleUnitApi, battlefieldApi, cpuBattleBrainApi, partyApi, eventBus)

    private val humanPlayerId = PlayerMother.id()
    private val cpuPlayerId = PlayerMother.id()
    private val venomEffectId = EffectMother.effectId()
    private val skullEffectId = EffectMother.effectId()
    private val swordDamageEffectId = EffectMother.effectId()
    private val healEffectId = EffectMother.effectId()
    private val skullAbilityId = AbilityMother.id()
    private val swordAbilityId = AbilityMother.id()
    private val healAbilityId = AbilityMother.id()
    private val skullUnitId = UnitMother.id()
    private val swordUnitId = UnitMother.id()
    private val enemyUnitId = UnitMother.id()
    private val allyUnitId = UnitMother.id()
    private val skullBattleUnitId = BattleUnitMother.id()
    private val swordBattleUnitId = BattleUnitMother.id()
    private val enemyBattleUnitId = BattleUnitMother.id()
    private val allyBattleUnitId = BattleUnitMother.id()

    private suspend fun setUp() {
        playerApi.requestPlayerCreation(humanPlayerId, HUMAN)
        playerApi.requestPlayerCreation(cpuPlayerId, CPU)
        terrainApi.init()
        terrainApi.initialiseTerrains()
        battlefieldApi.requestBattlefieldCreation(8, 8, List(8) { List(8) { "sand" } })

        val venomEffect = EffectMother.damageEffect(id = venomEffectId, damage = 3, timingType = "ON_PLAYER_TURN_STARTED")
        val skullEffect = EffectMother.applyEffectToAdjacentAlliesEffect(id = skullEffectId, effectId = venomEffectId)
        val swordDamageEffect = EffectMother.damageEffect(id = swordDamageEffectId, damage = 10, timingType = "IMMEDIATELY")
        val healEffect = EffectMother.healEffect(id = healEffectId, healing = 20)
        effectApi.requestEffectCreation(venomEffect.toDto())
        effectApi.requestEffectCreation(skullEffect.toDto())
        effectApi.requestEffectCreation(swordDamageEffect.toDto())
        effectApi.requestEffectCreation(healEffect.toDto())

        val skullAbility =
            AbilityMother.ability(
                id = skullAbilityId,
                manaCost = 0,
                cooldown = 0,
                abilityEffects = listOf(AbilityMother.abilityEffect(effectId = skullEffectId, target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT)),
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
            )
        val swordAbility =
            AbilityMother.ability(
                id = swordAbilityId,
                manaCost = 0,
                cooldown = 0,
                abilityEffects = listOf(AbilityMother.abilityEffect(effectId = swordDamageEffectId, target = EffectTargetDto.Type.TARGETED_BATTLE_UNIT)),
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
            )
        val healAbility =
            AbilityMother.ability(
                id = healAbilityId,
                manaCost = 10,
                cooldown = 2,
                abilityEffects = listOf(AbilityMother.abilityEffect(effectId = healEffectId, target = EffectTargetDto.Type.CASTER)),
                targeting = Ability.Dto.TargetingDto.CASTER,
            )
        abilityApi.requestAbilityCreation(skullAbility.toDto())
        abilityApi.requestAbilityCreation(swordAbility.toDto())
        abilityApi.requestAbilityCreation(healAbility.toDto())

        val skullUnit =
            UnitMother.unit(
                id = skullUnitId,
                baseMaximumHealth = 100,
                baseMaximumMana = 30,
                startingAbilities = listOf(skullAbilityId),
                maximumSteps = 3,
            )
        val swordUnit =
            UnitMother.unit(
                id = swordUnitId,
                baseMaximumHealth = 100,
                baseMaximumMana = 30,
                startingAbilities = listOf(swordAbilityId),
                maximumSteps = 3,
            )
        val enemyUnit =
            UnitMother.unit(
                id = enemyUnitId,
                baseMaximumHealth = 5,
                baseMaximumMana = 1,
                startingAbilities = emptyList(),
                maximumSteps = 3,
            )
        val allyUnit =
            UnitMother.unit(
                id = allyUnitId,
                baseMaximumHealth = 100,
                baseMaximumMana = 30,
                startingAbilities = listOf(healAbilityId),
                maximumSteps = 3,
            )
        unitApi.requestUnitCreation(skullUnit.toDto())
        unitApi.requestUnitCreation(swordUnit.toDto())
        unitApi.requestUnitCreation(enemyUnit.toDto())
        unitApi.requestUnitCreation(allyUnit.toDto())

        battleUnitApi.deployBattleUnit(
            battleUnitId = skullBattleUnitId,
            unitId = skullUnitId,
            playerId = humanPlayerId,
            deployAtRow = 1,
            deployAtColumn = 1,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = swordBattleUnitId,
            unitId = swordUnitId,
            playerId = humanPlayerId,
            deployAtRow = 2,
            deployAtColumn = 2,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = enemyBattleUnitId,
            unitId = enemyUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 1,
            deployAtColumn = 2,
        )
        battleUnitApi.deployBattleUnit(
            battleUnitId = allyBattleUnitId,
            unitId = allyUnitId,
            playerId = cpuPlayerId,
            deployAtRow = 0,
            deployAtColumn = 2,
        )
        battleApi.startBattleWithEveryBattleUnit(battleUnitApi, humanPlayerId, cpuPlayerId)
        eventBus.dispatch()
    }

    @Test
    fun `should apply the effect to the adjacent allies when the battle unit with the on defeated effect is defeated`() =
        scenarioTest(::setUp) {
            // Given
            require(battlefieldApi.searchOccupant(row = 1, column = 1) == skullBattleUnitId)
            require(battlefieldApi.searchOccupant(row = 2, column = 2) == swordBattleUnitId)
            require(battlefieldApi.searchOccupant(row = 1, column = 2) == enemyBattleUnitId)
            require(battlefieldApi.searchOccupant(row = 0, column = 2) == allyBattleUnitId)
            require(battleApi.searchBattle()!!.currentPlayerId == humanPlayerId)
            val skullCastGroup = battleUnitApi.whereCanCast(skullBattleUnitId, skullAbilityId).single()
            val swordCastGroup = battleUnitApi.whereCanCast(swordBattleUnitId, swordAbilityId).single()
            battleUnitApi.castAbility(
                battleUnitId = skullBattleUnitId,
                abilityId = skullAbilityId,
                castGroup = skullCastGroup,
            )
            battleUnitApi.castAbility(
                battleUnitId = swordBattleUnitId,
                abilityId = swordAbilityId,
                castGroup = swordCastGroup,
            )
            // When
            eventBus.dispatch()
            // Then
            val enemy = battleUnitApi.searchBattleUnitById(enemyBattleUnitId)!!
            enemy.remainingHealth shouldBe 0
            val ally = battleUnitApi.searchBattleUnitById(allyBattleUnitId)!!
            ally.ongoingEffects.onPlayerTurnStarted.shouldContain(venomEffectId)
        }
}
