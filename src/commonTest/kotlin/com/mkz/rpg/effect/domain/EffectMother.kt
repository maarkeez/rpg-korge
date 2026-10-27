package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
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
                outcome = Effect.Dto.EffectOutcomeDto.decreaseHealth(damage = damage),
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
                outcome = Effect.Dto.EffectOutcomeDto.increaseHealth(healing = healing),
                application =
                    ApplicationDto(
                        type = ApplicationTypeDto.IMMEDIATELY,
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun increaseManaEffect(
        id: String = effectId(),
        mana: Int = 3,
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.increaseMana(mana = mana),
                application =
                    ApplicationDto(
                        type = ApplicationTypeDto.IMMEDIATELY,
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun increaseMaximumHealthEffect(
        id: String = effectId(),
        healthPoints: Int = 10,
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.increaseMaximumHealth(healthPoints = healthPoints),
                application = whileEquippedApplication(),
            ),
        ).pullEvents()
        .second

    fun increaseMaximumManaEffect(
        id: String = effectId(),
        manaPoints: Int = 10,
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.increaseMaximumMana(manaPoints = manaPoints),
                application = whileEquippedApplication(),
            ),
        ).pullEvents()
        .second

    fun whileEquippedApplication() =
        ApplicationDto(
            type = ApplicationTypeDto.WHILE_EQUIPPED,
            onTurnStarted = null,
            beforeApplyingEffect = null,
        )

    fun teleportEffect(id: String = effectId()) =
        Effect
            .create(
                Effect.Dto(
                    id = id,
                    outcome = Effect.Dto.EffectOutcomeDto.teleport(),
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
                outcome = Effect.Dto.EffectOutcomeDto.applyEffectOnNearbyAllies(effectId = effectId),
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
                outcome = Effect.Dto.EffectOutcomeDto.deployBattleUnit(unitId = unitId),
                application =
                    ApplicationDto(
                        type = ApplicationTypeDto.IMMEDIATELY,
                        onTurnStarted = null,
                        beforeApplyingEffect = null,
                    ),
            ),
        ).pullEvents()
        .second

    fun learnAbilityEffect(
        id: String = effectId(),
        abilityId: String = "ability-${UUID.randomUUID()}",
    ) = Effect
        .create(
            Effect.Dto(
                id = id,
                outcome = Effect.Dto.EffectOutcomeDto.learnAbility(abilityId = abilityId),
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
