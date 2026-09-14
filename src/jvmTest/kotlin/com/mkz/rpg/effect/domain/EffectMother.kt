package com.mkz.rpg.effect.domain

import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.DecreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.IncreaseHealthDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.TypeDto.DECREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.TypeDto.INCREASE_HEALTH
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTypeDto.TypeDto.TELEPORT
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
                type =
                    Effect.Dto.EffectTypeDto(
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
                type =
                    Effect.Dto.EffectTypeDto(
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
                    type =
                        Effect.Dto.EffectTypeDto(
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
