package effect.domain

import effect.domain.Effect.Dto.ApplicationDto
import effect.domain.Effect.Dto.ApplicationDto.OnTurnStartedDto
import korlibs.crypto.SecureRandom.nextInt

object EffectMother {
    fun effect(
        id: String = effectId(),
    ) = Effect.create(Effect.Dto(
        id = id,
        type = "DECREASE_HEALTH",
        power = 3,
        probability = 100,
        modifiers = emptyList(),
        application = ApplicationDto(
            "ON_TURN_STARTED",
            onTurnStarted = OnTurnStartedDto(
                duration = 5
            ),
            beforeApplyingEffect = null
        )
    ))

    fun effectId(): String = "effect-${nextInt(from = 1, until = 100)}"
}
