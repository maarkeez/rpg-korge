package com.mkz.tessavar.effect.domain

sealed class EffectError(
    message: String,
) : Throwable(message = message) {
    class InvalidEffectTiming : EffectError("Invalid effect timing")

    class MissingEffectTimingDetails : EffectError("Missing effect timing details")

    class EmptyEffectId : EffectError("Effect id cannot be empty")

    class NegativeDuration : EffectError("Duration cannot be negative")

    class DurationAboveLimit : EffectError("Duration cannot be higher than 99")

    class NegativeAmount : EffectError("Amount cannot be negative")

    class AmountAboveLimit : EffectError("Amount cannot be higher than 999")
}
