package com.mkz.rpg.effect.domain

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
import korlibs.io.util.UUID

object EffectMother {
    fun decreaseHealthEffect(
        id: String = effectId(),
        damage: Int = 3,
        applicationType: String = "ON_TURN_STARTED",
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = DECREASE_HEALTH,
                        decreaseHealth = DecreaseHealthDto(damage = damage),
                        increaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                    ),
                application =
                    if (applicationType == "IMMEDIATELY") {
                        ApplicationDto(
                            type = ApplicationTypeDto.IMMEDIATELY,
                            onTurnStarted = null,
                            beforeApplyingEffect = null,
                        )
                    } else {
                        ApplicationDto(
                            type = ApplicationTypeDto.ON_TURN_STARTED,
                            onTurnStarted =
                                OnTurnStartedDto(
                                    duration = 5,
                                ),
                            beforeApplyingEffect = null,
                        )
                    },
            ),
        ).pullEvents()
        .second

    fun increaseHealthEffect(
        id: String = effectId(),
        healing: Int = 3,
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = INCREASE_HEALTH,
                        decreaseHealth = null,
                        increaseHealth = IncreaseHealthDto(healing = healing),
                        applyEffectOnNearbyAllies = null,
                    ),
                application =
                    ApplicationDto(
                        type = ApplicationTypeDto.IMMEDIATELY,
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun teleportEffect(id: String = effectId()) =
        Effect
            .create(
                Effect.Dto(
                    id = id,
                    outcome =
                        Effect.Dto.EffectOutcomeDto(
                            type = TELEPORT,
                            decreaseHealth = null,
                            increaseHealth = null,
                            applyEffectOnNearbyAllies = null,
                        ),
                    application =
                        ApplicationDto(
                            type = ApplicationTypeDto.IMMEDIATELY,
                            onTurnStarted = null,
                            beforeApplyingEffect = null,
                        ),
                ),
            ).pullEvents()
            .second

    fun applyEffectOnNearbyAlliesEffect(
        id: String = effectId(),
        effectId: String = "effect-nearby",
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = APPLY_EFFECT_ON_NEARBY_ALLIES,
                        decreaseHealth = null,
                        increaseHealth = null,
                        applyEffectOnNearbyAllies = ApplyEffectOnNearbyAlliesDto(effectId = effectId),
                    ),
                application =
                    ApplicationDto(
                        type = ApplicationTypeDto.ON_DEFEATED,
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun deployBattleUnitEffect(
        id: String = effectId(),
        unitId: String = "unit-${UUID.randomUUID()}",
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome =
                    Effect.Dto.EffectOutcomeDto(
                        type = DEPLOY_BATTLE_UNIT,
                        decreaseHealth = null,
                        increaseHealth = null,
                        applyEffectOnNearbyAllies = null,
                        deployBattleUnit = DeployBattleUnitDto(unitId = unitId),
                    ),
                application =
                    ApplicationDto(
                        type = ApplicationTypeDto.IMMEDIATELY,
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun effectId(): String = "effect-${UUID.randomUUID()}"
}
