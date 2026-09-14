package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
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
                type = Effect.Dto.TypeDto.DECREASE_HEALTH,
                increaseHealth = null,
                decreaseHealth = Effect.Dto.DecreaseHealthDto(damage),
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
                type = Effect.Dto.TypeDto.INCREASE_HEALTH,
                increaseHealth = Effect.Dto.IncreaseHealthDto(healing),
                decreaseHealth = null,
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
