package com.mkz.rpg.effect.domain

sealed class EffectError(
    message: String,
) : Throwable(message = message) {
    class InvalidEffectApplication : EffectError("Invalid effect application")

    class MissingEffectApplicationDetails : EffectError("Missing effect application details")

    class EmptyEffectId : EffectError("Effect id cannot be empty")

    class NegativeApplicationDuration : EffectError("Application duration cannot be negative")

    class ApplicationDurationAboveLimit : EffectError("Application duration cannot be higher than 99")

    class NegativePower : EffectError("Power cannot be negative")

    class PowerAboveLimit : EffectError("Power cannot be higher than 999")
}
