package effect.domain

interface EffectEvent {
    data class EffectCreated(val effectId: String): EffectEvent
}
