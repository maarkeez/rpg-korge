package effect.domain

sealed class EffectError(message: String) : Throwable(message=message) {
    class InvalidEffectType : EffectError("Invalid effect type")
    class InvalidEffectModifier : EffectError("Invalid effect modifier")
}
