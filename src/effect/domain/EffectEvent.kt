package effect.domain

sealed interface EffectEvent {
    data class EffectCreated(val effectId: String): EffectEvent
}
