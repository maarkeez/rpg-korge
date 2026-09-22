package com.mkz.rpg.battlesetup.usecases.commands

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectSpecDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.CASTER
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TARGET
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto.Type.SELECTED_TILE
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ADJACENT_ENEMY
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.ALL_ADJACENT_ENEMIES
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.SELF
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT
import com.mkz.rpg.ability.domain.Ability.Dto.TargetingDto.VACANT_TILE_ADJACENT_TO_SELF
import com.mkz.rpg.ability.domain.AbilityEvent.RequestAbilityCreation
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.ApplyEffectOnNearbyAlliesDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.DecreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.DeployBattleUnitDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.IncreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DEPLOY_BATTLE_UNIT
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.TELEPORT
import com.mkz.rpg.effect.domain.EffectEvent.RequestEffectCreation
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.unit.domain.Unit
import com.mkz.rpg.unit.domain.UnitEvent.RequestUnitCreation

class SetupBattle(
    private val eventBus: EventBus,
) {
    operator fun invoke() {
        val playerOneId = "player-one"
        val playerTwoId = "player-two"
        eventBus.publish(PlayerEvent.RequestPlayerCreation(playerOneId, "Human", Player.Dto.PlayerTypeDto.HUMAN))
        eventBus.publish(PlayerEvent.RequestPlayerCreation(playerTwoId, "CPU", Player.Dto.PlayerTypeDto.CPU))

        val sandTerrain = Terrain.Dto(id = "sand", canBeOccupied = true)
        val voidTerrain = Terrain.Dto(id = "void", canBeOccupied = false)
        listOf(sandTerrain).forEach { terrain ->
            eventBus.publish(TerrainEvent.RequestTerrainCreation(terrain))
        }
        val tiles =
            listOf(
                listOf(sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id),
                listOf(sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id),
                listOf(sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id),
                listOf(sandTerrain.id, sandTerrain.id, voidTerrain.id, voidTerrain.id, voidTerrain.id, voidTerrain.id, sandTerrain.id, sandTerrain.id),
                listOf(sandTerrain.id, sandTerrain.id, voidTerrain.id, voidTerrain.id, voidTerrain.id, voidTerrain.id, sandTerrain.id, sandTerrain.id),
                listOf(sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id),
                listOf(sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id),
                listOf(sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id, sandTerrain.id),
            )
        eventBus.publish(BattlefieldEvent.RequestInitializeBattlefield(8, 8, tiles))

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
                        ApplicationTypeDto.ON_TURN_STARTED,
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
                        ApplicationTypeDto.IMMEDIATELY,
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
                        ApplicationTypeDto.IMMEDIATELY,
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
                        ApplicationTypeDto.IMMEDIATELY,
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
                        ApplicationTypeDto.ON_DEFEATED,
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            )
        val deployBeeEffect =
            Effect.Dto(
                id = "deploy-bee",
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = DEPLOY_BATTLE_UNIT,
                        decreaseHealth = null,
                        increaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                        deployBattleUnit = DeployBattleUnitDto(unitId = "bee"),
                    ),
                application =
                    ApplicationDto(
                        ApplicationTypeDto.IMMEDIATELY,
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            )
        eventBus.publish(RequestEffectCreation(venomDamage))
        eventBus.publish(RequestEffectCreation(lowPhysicalDamage))
        eventBus.publish(RequestEffectCreation(lowDamageHeal))
        eventBus.publish(RequestEffectCreation(teleportEffect))
        eventBus.publish(RequestEffectCreation(venomOnDeath))
        eventBus.publish(RequestEffectCreation(deployBeeEffect))

        val poisonedSword =
            Ability.Dto(
                id = "poisoned-sword",
                name = "Poisoned Sword",
                cost = 5,
                cooldown = 1,
                targeting = ADJACENT_ENEMY,
                effectSpecs = listOf(EffectSpecDto(effectId = venomDamage.id, target = TargetExpressionDto(type = SELECTED_TARGET))),
            )
        val sword =
            Ability.Dto(
                id = "sword",
                name = "Sword",
                cost = 0,
                cooldown = 0,
                targeting = ADJACENT_ENEMY,
                effectSpecs = listOf(EffectSpecDto(effectId = lowPhysicalDamage.id, target = TargetExpressionDto(type = SELECTED_TARGET))),
            )
        val heal =
            Ability.Dto(
                id = "heal",
                name = "Heal",
                cost = 10,
                cooldown = 2,
                targeting = SELF,
                effectSpecs = listOf(EffectSpecDto(effectId = lowDamageHeal.id, target = TargetExpressionDto(type = CASTER))),
            )
        val mushroom =
            Ability.Dto(
                id = "mushroom",
                name = "Mushroom",
                cost = 10,
                cooldown = 0,
                targeting = ALL_ADJACENT_ENEMIES,
                effectSpecs = listOf(EffectSpecDto(effectId = venomDamage.id, target = TargetExpressionDto(type = SELECTED_TARGET))),
            )
        val skull =
            Ability.Dto(
                id = "skull",
                name = "Skull",
                cost = 10,
                cooldown = 0,
                targeting = ADJACENT_ENEMY,
                effectSpecs =
                    listOf(
                        EffectSpecDto(effectId = lowPhysicalDamage.id, target = TargetExpressionDto(type = SELECTED_TARGET)),
                        EffectSpecDto(effectId = venomOnDeath.id, target = TargetExpressionDto(type = SELECTED_TARGET)),
                    ),
            )
        val teleport =
            Ability.Dto(
                id = "teleport",
                name = "Teleport",
                cost = 5,
                cooldown = 1,
                targeting = VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
                effectSpecs = listOf(EffectSpecDto(effectId = teleportEffect.id, target = TargetExpressionDto(type = CASTER))),
            )
        val beeAbility =
            Ability.Dto(
                id = "bee",
                name = "Bee",
                cost = 10,
                cooldown = 0,
                targeting = VACANT_TILE_ADJACENT_TO_SELF,
                effectSpecs = listOf(EffectSpecDto(effectId = deployBeeEffect.id, target = TargetExpressionDto(type = SELECTED_TILE))),
            )
        eventBus.publish(RequestAbilityCreation(poisonedSword))
        eventBus.publish(RequestAbilityCreation(sword))
        eventBus.publish(RequestAbilityCreation(heal))
        eventBus.publish(RequestAbilityCreation(mushroom))
        eventBus.publish(RequestAbilityCreation(skull))
        eventBus.publish(RequestAbilityCreation(teleport))
        eventBus.publish(RequestAbilityCreation(beeAbility))

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
                        beeAbility.id,
                        heal.id,
                    ),
                movementRange = 3,
            )
        val beeUnit =
            Unit.Dto(
                id = "bee",
                name = "Bee",
                healthPoints = 10,
                manaPoints = 0,
                abilities =
                    listOf(sword.id),
                movementRange = 5,
            )
        eventBus.publish(RequestUnitCreation(ratUnit))
        eventBus.publish(RequestUnitCreation(knight))
        eventBus.publish(RequestUnitCreation(beeUnit))

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
