package effect.domain

interface EffectPublisher {
    fun publish(events: Set<EffectEvent>)
}
