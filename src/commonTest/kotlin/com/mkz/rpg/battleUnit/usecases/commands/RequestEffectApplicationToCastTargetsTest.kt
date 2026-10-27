package com.mkz.rpg.battleUnit.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.ability.domain.AbilityMother.effectSpec
import com.mkz.rpg.battleUnit.domain.BattleUnitError
import com.mkz.rpg.battleUnit.domain.BattleUnitError.AbilityDoesNotExists
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitMother.battleUnit
import com.mkz.rpg.battlefield.domain.Battlefield
import com.mkz.rpg.battlefield.domain.BattlefieldMother.battlefield
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.effect.domain.EffectMother.decreaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.deployBattleUnitEffect
import com.mkz.rpg.effect.domain.EffectMother.increaseHealthEffect
import com.mkz.rpg.effect.domain.EffectMother.teleportEffect
import com.mkz.rpg.player.domain.PlayerMother.player
import com.mkz.rpg.shared.domain.FakeEventBus
import com.mkz.rpg.shared.domain.shouldHavePublishedEvents
import com.mkz.rpg.shared.usecases.InMemoryQueries
import com.mkz.rpg.unit.domain.UnitMother.unit
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RequestEffectApplicationToCastTargetsTest {
    private val queries = InMemoryQueries()
    private val battleUnitRepository = queries.battleUnitRepository
    private val eventBus = FakeEventBus()
    private val requestEffectApplicationToCastTargets =
        RequestEffectApplicationToCastTargets(
            searchAbilityById = queries.searchAbilityById,
            battleUnitRepository = battleUnitRepository,
            abilityExecution = queries.abilityExecution,
            eventBus = eventBus,
        )

    @Test
    fun `should request the effect application to the caster when the effect targets the caster`() {
        // Given
        val effectId = "effect-1"
        val effect = decreaseHealthEffect(id = effectId, damage = 3, applicationType = "IMMEDIATELY").toDto()
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        queries.abilityRepository.create(
            ability(id = "ability-1", targeting = Ability.Dto.TargetingDto.SELF, effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.CASTER))),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .", occupants = mapOf('C' to battleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(battleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application to the selected tile when the effect outcome is deploy battle unit`() {
        // Given
        val effectId = "effect-1"
        val effect = deployBattleUnitEffect(id = effectId, unitId = "summoned-unit").toDto()
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
                effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.SELECTED_TILE)),
            ),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .\n. .", occupants = mapOf('C' to battleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        target = EffectTargetDto.position(row = 1, column = 1),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application to the enemy occupant when the effect targets the selected target`() {
        // Given
        val effectId = "effect-1"
        val effect = decreaseHealthEffect(id = effectId, damage = 3, applicationType = "IMMEDIATELY").toDto()
        val enemyUnit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val battleUnit = battleUnit(player = player)
        val enemyBattleUnit = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(battleUnit)
        battleUnitRepository.create(enemyBattleUnit)
        val battleUnitId = battleUnit.toDto().id
        val enemyBattleUnitId = enemyBattleUnit.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES,
                effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.SELECTED_TARGET)),
            ),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to battleUnitId, 'E' to enemyBattleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(enemyBattleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application when the effect application type is on defeated`() {
        // Given
        val effectId = "effect-1"
        val effect =
            decreaseHealthEffect(id = effectId, damage = 3)
                .toDto()
                .copy(application = ApplicationDto(type = ApplicationTypeDto.ON_DEFEATED, onTurnStarted = null, beforeApplyingEffect = null))
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        queries.abilityRepository.create(
            ability(id = "ability-1", targeting = Ability.Dto.TargetingDto.SELF, effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.CASTER))),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .", occupants = mapOf('C' to battleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(battleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application with the teleport destination when the effect outcome is teleport`() {
        // Given
        val effectId = "effect-1"
        val effect = teleportEffect(id = effectId).toDto()
        val unit = unit(healthPoints = 10).toDto()
        val player = player(id = "player-1").toDto()
        val battleUnit = battleUnit(unit = unit, player = player)
        battleUnitRepository.create(battleUnit)
        val battleUnitId = battleUnit.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
                effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.CASTER)),
            ),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .\n. .", occupants = mapOf('C' to battleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(battleUnitId),
                        effectId = effectId,
                        destination = EffectTargetDto.position(row = 1, column = 1),
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request the effect application to the defeated target when the ability targets an adjacent enemy`() {
        // Given
        val effectId = "effect-1"
        val effect = decreaseHealthEffect(id = effectId, damage = 10, applicationType = "IMMEDIATELY").toDto()
        val enemyUnit = unit(healthPoints = 3).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val battleUnit = battleUnit(player = player)
        val enemyBattleUnit = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(battleUnit)
        battleUnitRepository.create(enemyBattleUnit)
        val battleUnitId = battleUnit.toDto().id
        val enemyBattleUnitId = enemyBattleUnit.toDto().id
        queries.abilityRepository.create(
            ability(id = "ability-1", targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY, effectSpecs = listOf(effectSpec(effectId = effectId, target = TargetExpressionDto.Type.SELECTED_TARGET))),
        )
        queries.effectRepository.create(Effect.create(effect))
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to battleUnitId, 'E' to enemyBattleUnitId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = battleUnitId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(battleUnitId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(enemyBattleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = battleUnitId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should throw ability does not exist when the ability is unknown`() {
        // Given
        val battleUnit = battleUnit()
        battleUnitRepository.create(battleUnit)
        // When
        val error =
            shouldThrowAny {
                requestEffectApplicationToCastTargets(battleUnitId = battleUnit.toDto().id, abilityId = "unknown-ability", castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 0)))
            }
        // Then
        error.shouldBeInstanceOf<AbilityDoesNotExists>()
    }

    @Test
    fun `should not receive effects when the battle unit does not exist`() {
        // Given
        val ability = ability().also(queries.abilityRepository::create).toDto()
        // When
        val error =
            shouldThrowAny { requestEffectApplicationToCastTargets(battleUnitId = "unknown-battle-unit", abilityId = ability.id, castGroup = listOf(Battlefield.Dto.PositionDto(row = 0, column = 0))) }
        // Then
        error.shouldBeInstanceOf<BattleUnitError.BattleUnitDoesNotExists>()
    }

    @Test
    fun `should request each effect application to its own resolved target when the ability targets an enemy but an effect targets the caster`() {
        // Given
        val enemyDamageEffectId = "enemy-damage-effect"
        val casterDamageEffectId = "caster-damage-effect"
        val enemyDamageEffect = decreaseHealthEffect(id = enemyDamageEffectId, damage = 10, applicationType = "IMMEDIATELY").toDto()
        val casterDamageEffect = decreaseHealthEffect(id = casterDamageEffectId, damage = 5, applicationType = "IMMEDIATELY").toDto()
        val casterUnit = unit(healthPoints = 10).toDto()
        val enemyUnit = unit(healthPoints = 20).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs =
                    listOf(
                        effectSpec(effectId = enemyDamageEffectId, target = TargetExpressionDto.Type.SELECTED_TARGET),
                        effectSpec(effectId = casterDamageEffectId, target = TargetExpressionDto.Type.CASTER),
                    ),
            ),
        )
        queries.effectRepository.create(Effect.create(enemyDamageEffect))
        queries.effectRepository.create(Effect.create(casterDamageEffect))
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to casterId, 'E' to enemyId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(enemyId),
                        effectId = enemyDamageEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(casterId),
                        effectId = casterDamageEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = casterId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request only the caster effect application when the only effect targets the caster`() {
        // Given
        val healEffectId = "heal-effect"
        val healEffect = increaseHealthEffect(id = healEffectId, healing = 10).toDto()
        val casterUnit = unit(healthPoints = 10).toDto()
        val enemyUnit = unit(healthPoints = 20).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs = listOf(effectSpec(effectId = healEffectId, target = TargetExpressionDto.Type.CASTER)),
            ),
        )
        queries.effectRepository.create(Effect.create(healEffect))
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to casterId, 'E' to enemyId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(casterId),
                        effectId = healEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = casterId, abilityId = "ability-1"),
        )
    }

    @Test
    fun `should request each effect application to its own resolved target when the effects target different expressions`() {
        // Given
        val damageEffectId = "damage-effect"
        val healEffectId = "heal-effect"
        val damageEffect = decreaseHealthEffect(id = damageEffectId, damage = 10, applicationType = "IMMEDIATELY").toDto()
        val healEffect = increaseHealthEffect(id = healEffectId, healing = 5).toDto()
        val casterUnit = unit(healthPoints = 10).toDto()
        val enemyUnit = unit(healthPoints = 20).toDto()
        val player = player(id = "player-1").toDto()
        val enemyPlayer = player(id = "player-2").toDto()
        val caster = battleUnit(unit = casterUnit, player = player)
        val enemy = battleUnit(unit = enemyUnit, player = enemyPlayer)
        battleUnitRepository.create(caster)
        battleUnitRepository.create(enemy)
        val casterId = caster.toDto().id
        val enemyId = enemy.toDto().id
        queries.effectRepository.create(Effect.create(damageEffect))
        queries.effectRepository.create(Effect.create(healEffect))
        queries.abilityRepository.create(
            ability(
                id = "ability-1",
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs =
                    listOf(
                        effectSpec(effectId = damageEffectId, target = TargetExpressionDto.Type.SELECTED_TARGET),
                        effectSpec(effectId = healEffectId, target = TargetExpressionDto.Type.CASTER),
                    ),
            ),
        )
        queries.battlefieldRepository.create(battlefield("C .\n. E", occupants = mapOf('C' to casterId, 'E' to enemyId)))
        // When
        requestEffectApplicationToCastTargets(battleUnitId = casterId, abilityId = "ability-1", castGroup = listOf(Battlefield.Dto.PositionDto(row = 1, column = 1)))
        // Then
        eventBus.shouldHavePublishedEvents(
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(enemyId),
                        effectId = damageEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.battleUnit(casterId, abilityId = "ability-1"),
                        target = EffectTargetDto.battleUnit(casterId),
                        effectId = healEffectId,
                        destination = null,
                    ),
            ),
            BattleUnitEvent.AbilityCastResolved(battleUnitId = casterId, abilityId = "ability-1"),
        )
    }
}
