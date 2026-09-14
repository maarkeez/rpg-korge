package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.DecreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.IncreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto.DECREASE_HEALTH
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
                    ),
                application =
                    if (applicationType == "IMMEDIATELY") {
                        ApplicationDto(
                            type = "IMMEDIATELY",
                            onTurnStarted = null,
                            beforeApplyingEffect = null,
                        )
                    } else {
                        ApplicationDto(
                            type = "ON_TURN_STARTED",
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
                    ),
                application =
                    ApplicationDto(
                        type = "IMMEDIATELY",
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
                        ),
                    application =
                        ApplicationDto(
                            type = "IMMEDIATELY",
                            onTurnStarted = null,
                            beforeApplyingEffect = null,
                        ),
                ),
            ).pullEvents()
            .second

    fun effectId(): String = "effect-${UUID.randomUUID()}"
}
