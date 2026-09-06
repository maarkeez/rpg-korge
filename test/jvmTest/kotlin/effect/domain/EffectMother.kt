package effect.domain

import effect.domain.Effect.Dto.ApplicationDto
import effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import korlibs.crypto.SecureRandom.nextInt

object EffectMother {
    fun effect(
        id: String = effectId(),
        power: Int = 3,
        applicationType: String = "ON_TURN_STARTED",
    ) = Effect.create(Effect.Dto(
        id = id,
        type = "DECREASE_HEALTH",
        power = power,
        probability = 100,
        modifiers = emptyList(),
        application = if (applicationType == "IMMEDIATELY") {
            ApplicationDto(
                type = "IMMEDIATELY",
                onTurnStarted = null,
                beforeApplyingEffect = null
            )
        } else {
            ApplicationDto(
                type = "ON_TURN_STARTED",
                onTurnStarted = OnTurnStartedDto(
                    duration = 5
                ),
                beforeApplyingEffect = null
            )
        }
    ))

    fun effectId(): String = "effect-${nextInt(from = 1, until = 100)}"
}
