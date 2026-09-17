package com.mkz.rpg.battlesetup.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.AbilityEvent
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.ApplyEffectOnNearbyAlliesDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.DecreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.IncreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.EffectEvent
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitEvent

class SetupBattle(
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val playerOneId = "player-one"
        val playerTwoId = "player-two"
        eventBus.publish(PlayerEvent.RequestPlayerCreation(playerOneId, "Human", Player.Dto.PlayerTypeDto.HUMAN))
        eventBus.publish(PlayerEvent.RequestPlayerCreation(playerTwoId, "CPU", Player.Dto.PlayerTypeDto.CPU))

        eventBus.publish(BattlefieldEvent.RequestInitializeBattlefield(8, 8, List(8) { List(8) { "tile-id-$it" } }))

        val venomDamage =
            Effect.Dto(
                id = "venom-damage",
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = DECREASE_HEALTH,
                        decreaseHealth = DecreaseHealthDto(damage = 3),
                        increaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                    ),
                application =
                    ApplicationDto(
                        "ON_TURN_STARTED",
                        onTurnStarted =
                            OnTurnStartedDto(
                                duration = 5,
                            ),
                        beforeApplyingEffect = null,
                    ),
            )
        val lowPhysicalDamage =
            Effect.Dto(
                id = "low-physical-damage",
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = DECREASE_HEALTH,
                        decreaseHealth = DecreaseHealthDto(damage = 10),
                        increaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                    ),
                application =
                    ApplicationDto(
                        "IMMEDIATELY",
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            )
        val lowDamageHeal =
            Effect.Dto(
                id = "low-damage-heal",
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = INCREASE_HEALTH,
                        decreaseHealth = null,
                        increaseHealth = IncreaseHealthDto(healing = 20),
                        applyEffectOnNearbyAllies = null,
                    ),
                application =
                    ApplicationDto(
                        "IMMEDIATELY",
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            )
        val teleportEffect =
            Effect.Dto(
                id = "teleport",
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = TELEPORT,
                        decreaseHealth = null,
                        increaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                    ),
                application =
                    ApplicationDto(
                        "IMMEDIATELY",
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            )
        val venomOnDeath =
            Effect.Dto(
                id = "venom-on-death",
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = APPLY_EFFECT_ON_NEARBY_ALLIES,
                        decreaseHealth = null,
                        increaseHealth = null,
                        applyEffectOnNearbyAllies =
                            ApplyEffectOnNearbyAlliesDto(
                                effectId = venomDamage.id,
                            ),
                    ),
                application =
                    ApplicationDto(
                        "ON_DEFEATED",
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            )
        eventBus.publish(EffectEvent.RequestEffectCreation(venomDamage))
        eventBus.publish(EffectEvent.RequestEffectCreation(lowPhysicalDamage))
        eventBus.publish(EffectEvent.RequestEffectCreation(lowDamageHeal))
        eventBus.publish(EffectEvent.RequestEffectCreation(teleportEffect))
        eventBus.publish(EffectEvent.RequestEffectCreation(venomOnDeath))

        val poisonedSword =
            Ability.Dto(
                id = "poisoned-sword",
                name = "Poisoned Sword",
                cost = 5,
                cooldown = 1,
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs = listOf(Ability.Dto.EffectSpecDto(effectId = venomDamage.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.SELECTED_TARGET))),
            )
        val sword =
            Ability.Dto(
                id = "sword",
                name = "Sword",
                cost = 0,
                cooldown = 0,
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs = listOf(Ability.Dto.EffectSpecDto(effectId = lowPhysicalDamage.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.SELECTED_TARGET))),
            )
        val heal =
            Ability.Dto(
                id = "heal",
                name = "Heal",
                cost = 10,
                cooldown = 2,
                targeting = Ability.Dto.TargetingDto.SELF,
                effectSpecs = listOf(Ability.Dto.EffectSpecDto(effectId = lowDamageHeal.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.CASTER))),
            )
        val mushroom =
            Ability.Dto(
                id = "mushroom",
                name = "Mushroom",
                cost = 10,
                cooldown = 0,
                targeting = Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES,
                effectSpecs = listOf(Ability.Dto.EffectSpecDto(effectId = venomDamage.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.SELECTED_TARGET))),
            )
        val skull =
            Ability.Dto(
                id = "skull",
                name = "Skull",
                cost = 10,
                cooldown = 0,
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs =
                    listOf(
                        Ability.Dto.EffectSpecDto(effectId = lowPhysicalDamage.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.SELECTED_TARGET)),
                        Ability.Dto.EffectSpecDto(effectId = venomOnDeath.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.SELECTED_TARGET)),
                    ),
            )
        val teleport =
            Ability.Dto(
                id = "teleport",
                name = "Teleport",
                cost = 5,
                cooldown = 1,
                targeting = Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
                effectSpecs = listOf(Ability.Dto.EffectSpecDto(effectId = teleportEffect.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.CASTER))),
            )
        val bee =
            Ability.Dto(
                id = "bee",
                name = "Bee",
                cost = 10,
                cooldown = 0,
                targeting = Ability.Dto.TargetingDto.ADJACENT_ENEMY,
                effectSpecs = listOf(Ability.Dto.EffectSpecDto(effectId = lowPhysicalDamage.id, target = TargetExpressionDto(type = TargetExpressionDto.Type.SELECTED_TARGET))),
            )
        eventBus.publish(AbilityEvent.RequestAbilityCreation(poisonedSword))
        eventBus.publish(AbilityEvent.RequestAbilityCreation(sword))
        eventBus.publish(AbilityEvent.RequestAbilityCreation(heal))
        eventBus.publish(AbilityEvent.RequestAbilityCreation(mushroom))
        eventBus.publish(AbilityEvent.RequestAbilityCreation(skull))
        eventBus.publish(AbilityEvent.RequestAbilityCreation(teleport))
        eventBus.publish(AbilityEvent.RequestAbilityCreation(bee))

        val ratUnit =
            Unit.Dto(
                id = "rat",
                name = "Rat",
                healthPoints = 20,
                manaPoints = 10,
                abilities = listOf(sword.id, heal.id),
                movementRange = 3,
            )
        val knight =
            Unit.Dto(
                id = "knight",
                name = "Knight",
                healthPoints = 100,
                manaPoints = 30,
                abilities =
                    listOf(
                        poisonedSword.id,
                        mushroom.id,
                        skull.id,
                        teleport.id,
                        bee.id,
                        heal.id,
                    ),
                movementRange = 3,
            )
        eventBus.publish(UnitEvent.RequestUnitCreation(ratUnit))
        eventBus.publish(UnitEvent.RequestUnitCreation(knight))

        eventBus.publish(
            BattleUnitEvent.RequestDeployBattleUnit(
                battleUnitId = "player-2-unit-1",
                unitId = ratUnit.id,
                playerId = playerTwoId,
                deployAtRow = 0,
                deployAtColumn = 0,
            ),
        )
        eventBus.publish(
            BattleUnitEvent.RequestDeployBattleUnit(
                battleUnitId = "player-2-unit-2",
                unitId = ratUnit.id,
                playerId = playerTwoId,
                deployAtRow = 1,
                deployAtColumn = 1,
            ),
        )

        eventBus.publish(
            BattleUnitEvent.RequestDeployBattleUnit(
                battleUnitId = "player-1-unit-1",
                unitId = knight.id,
                playerId = playerOneId,
                deployAtRow = 6,
                deployAtColumn = 6,
            ),
        )
        eventBus.publish(
            BattleUnitEvent.RequestDeployBattleUnit(
                battleUnitId = "player-1-unit-2",
                unitId = knight.id,
                playerId = playerOneId,
                deployAtRow = 7,
                deployAtColumn = 7,
            ),
        )

        eventBus.publish(BattleEvent.RequestStartFirstRound(listOf(playerOneId, playerTwoId)))
    }
}
